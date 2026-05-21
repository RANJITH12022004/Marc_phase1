package com.marc.helmet.services;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.location.Location;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.marc.helmet.activities.CrashAlertActivity;
import com.marc.helmet.R;
import com.marc.helmet.database.*;
import com.marc.helmet.models.*;
import com.marc.helmet.network.pico.PicoApiClient;

/**
 * Foreground ride monitor: GPS speed/location, bike unit {@code crash_flag} only (no phone/heuristic
 * crash), helmet LED alerts.
 * Requires {@code androidx.localbroadcastmanager:localbroadcastmanager}, Play Services Location,
 * manifest service entry, {@code FOREGROUND_SERVICE}, and runtime location permission.
 */
public class MarcForegroundService extends Service {

    private static final String TAG = "MarcForegroundService";

    public static final String ACTION_CRASH_DETECTED = "com.marc.helmet.CRASH_DETECTED";
    public static final String ACTION_SPEED_UPDATE = "com.marc.helmet.SPEED_UPDATE";
    public static final String ACTION_LOCATION_UPDATE = "com.marc.helmet.LOCATION_UPDATE";
    public static final String ACTION_DEVICE_STATUS = "com.marc.helmet.DEVICE_STATUS";
    public static final String ACTION_LEAN_UPDATE = "com.marc.helmet.LEAN_UPDATE";

    public static final String EXTRA_SPEED_KMH = "speed_kmh";
    public static final String EXTRA_LAT = "lat";
    public static final String EXTRA_LNG = "lng";
    public static final String EXTRA_DEVICE_TYPE = "device_type";
    public static final String EXTRA_CONNECTED = "connected";

    public static final String CHANNEL_ID = "MARC_SERVICE_CHANNEL";

    /** High-importance channel + full-screen intent so crash UI can appear over lock screen. */
    public static final String CHANNEL_CRASH_ID = "MARC_CRASH_CHANNEL";

    /** Shown with {@link #CHANNEL_CRASH_ID}; cancel from {@link com.marc.helmet.activities.CrashAlertActivity} when dismissed. */
    public static final int NOTIFICATION_CRASH_SOS_ID = 1003;

    private static final int NOTIFICATION_ID = 1001;
    private static final long BIKE_POLL_MS = 200L;
    private static final long CRASH_POLL_PAUSE_MS = 3000L;
    /** Avoid re-launching full-screen crash UI on every 200 ms poll while crash_flag stays latched. */
    private static final long CRASH_ALERT_LAUNCH_DEBOUNCE_MS = 25_000L;
    private static final int NOTIFICATION_ACCENT_COLOR = 0xFFFF2020;

    private static volatile boolean running;

    private final IBinder binder = new MarcBinder();
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private PicoApiClient helmetClient;
    private PicoApiClient bikeClient;
    private volatile Calibration calibration;
    private volatile float speedThresholdKmh = 80f;

    private Runnable bikePollRunnable;
    private volatile boolean bikePollPaused;

    /** Every 25 × {@link #BIKE_POLL_MS} (5s) ping helmet to refresh DB + UI if link drops. */
    private int helmetPingTick;

    private volatile double lastKnownLat;
    private volatile double lastKnownLng;
    private long lastCrashAlertLaunchUptimeMs;

    private FusedLocationProviderClient fusedLocationClient;
    private LocationCallback locationCallback;
    private boolean speedLedAlertActive;

    @Nullable
    private PowerManager.WakeLock rideWakeLock;

    public static void startService(Context context) {
        Intent i = new Intent(context, MarcForegroundService.class);
        ContextCompat.startForegroundService(context, i);
    }

    public static void stopService(Context context) {
        context.stopService(new Intent(context, MarcForegroundService.class));
    }

    public static boolean isRunning() {
        return running;
    }

    @SuppressLint("WakelockTimeout")
    @Override
    public void onCreate() {
        super.onCreate();
        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        if (pm != null) {
            rideWakeLock =
                    pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "MARC::RideWakeLock");
            rideWakeLock.acquire();
        }
        createNotificationChannel();
        createCrashNotificationChannel();
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        reloadCalibrationFromDb();

        bikePollRunnable =
                new Runnable() {
                    @Override
                    public void run() {
                        if (bikeClient == null) {
                            mainHandler.postDelayed(this, BIKE_POLL_MS);
                            return;
                        }
                        if (bikePollPaused) {
                            mainHandler.postDelayed(this, BIKE_POLL_MS);
                            return;
                        }

                        helmetPingTick++;
                        if (helmetPingTick >= 25) {
                            helmetPingTick = 0;
                            PicoApiClient hc = helmetClient;
                            if (hc != null) {
                                hc.ping(
                                        new PicoApiClient.PicoCallback<Long>() {
                                            @Override
                                            public void onSuccess(Long ms) {
                                                new DeviceDao(
                                                                DatabaseHelper.getInstance(
                                                                        MarcForegroundService.this))
                                                        .setConnected(Device.HELMET, true);
                                                broadcastDeviceStatus(Device.HELMET, true);
                                            }

                                            @Override
                                            public void onError(String e) {
                                                new DeviceDao(
                                                                DatabaseHelper.getInstance(
                                                                        MarcForegroundService.this))
                                                        .setConnected(Device.HELMET, false);
                                                broadcastDeviceStatus(Device.HELMET, false);
                                            }
                                        });
                            }
                        }

                        bikeClient.getStatus(
                                new PicoApiClient.PicoCallback<PicoApiClient.PicoStatus>() {
                                    @Override
                                    public void onSuccess(PicoApiClient.PicoStatus status) {
                                        String type =
                                                status.deviceType != null
                                                        ? status.deviceType
                                                        : Device.BIKE;
                                        new DeviceDao(DatabaseHelper.getInstance(
                                                        MarcForegroundService.this))
                                                .setConnected(type, true);
                                        broadcastDeviceStatus(type, true);
                                        broadcastLeanAngle((float) status.roll);

                                        Calibration cal = calibration;
                                        boolean heuristicCrash =
                                                cal != null
                                                        && cal.isCalibrated()
                                                        && cal.isCrash(status.roll);
                                        if (status.crashFlag || heuristicCrash) {
                                            broadcastCrashDetected();
                                            launchCrashAlertIfNeeded();
                                            bikePollPaused = true;
                                            mainHandler.postDelayed(
                                                    () -> bikePollPaused = false,
                                                    CRASH_POLL_PAUSE_MS);
                                        }
                                        mainHandler.postDelayed(
                                                bikePollRunnable, BIKE_POLL_MS);
                                    }

                                    @Override
                                    public void onError(String error) {
                                        new DeviceDao(DatabaseHelper.getInstance(
                                                        MarcForegroundService.this))
                                                .setConnected(Device.BIKE, false);
                                        broadcastDeviceStatus(Device.BIKE, false);
                                        mainHandler.postDelayed(
                                                bikePollRunnable, BIKE_POLL_MS);
                                    }
                                });
                    }
                };
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        running = true;
        reloadCalibrationFromDb();
        SettingsDao settingsDao = new SettingsDao(DatabaseHelper.getInstance(this));
        speedThresholdKmh = settingsDao.getSpeedThreshold();
        startForegroundWithNotification();
        mainHandler.removeCallbacks(bikePollRunnable);
        mainHandler.post(bikePollRunnable);
        startLocationUpdates();
        return START_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    @Override
    public void onDestroy() {
        running = false;
        mainHandler.removeCallbacks(bikePollRunnable);
        stopLocationUpdates();
        if (rideWakeLock != null && rideWakeLock.isHeld()) {
            rideWakeLock.release();
            rideWakeLock = null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(STOP_FOREGROUND_REMOVE);
        } else {
            stopForeground(true);
        }
        super.onDestroy();
    }

    private void startForegroundWithNotification() {
        Notification notification = buildNotification();
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
                // Location only — do not claim MICROPHONE here; ride monitoring does not record audio,
                // and FGS mic type blocks Google SpeechRecognizer used by wake word + Marc STT.
                startForeground(
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                startForeground(
                        NOTIFICATION_ID,
                        notification,
                        ServiceInfo.FOREGROUND_SERVICE_TYPE_LOCATION);
            } else {
                startForeground(NOTIFICATION_ID, notification);
            }
        } catch (SecurityException e) {
            Log.e(TAG, "startForeground denied — location/microphone FGS policy", e);
            stopSelf();
        }
    }

    private Notification buildNotification() {
        Intent launchIntent = getPackageManager().getLaunchIntentForPackage(getPackageName());
        if (launchIntent == null) {
            launchIntent = new Intent();
        }
        PendingIntent pendingIntent =
                PendingIntent.getActivity(
                        this,
                        0,
                        launchIntent,
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        return new NotificationCompat.Builder(this, CHANNEL_ID)
                .setContentTitle("MARC is Armed")
                .setContentText("Monitoring your ride. Stay safe.")
                .setSmallIcon(android.R.drawable.stat_sys_warning)
                .setColor(NOTIFICATION_ACCENT_COLOR)
                .setColorized(true)
                .setOngoing(true)
                .setOnlyAlertOnce(true)
                .setContentIntent(pendingIntent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setCategory(NotificationCompat.CATEGORY_SERVICE)
                .build();
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationChannel channel =
                new NotificationChannel(
                        CHANNEL_ID,
                        "MARC System",
                        NotificationManager.IMPORTANCE_LOW);
        channel.setDescription("MARC ride monitoring");
        channel.enableLights(true);
        channel.setLightColor(NOTIFICATION_ACCENT_COLOR);
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm != null) {
            nm.createNotificationChannel(channel);
        }
    }

    private void createCrashNotificationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
            return;
        }
        NotificationChannel ch =
                new NotificationChannel(
                        CHANNEL_CRASH_ID,
                        getString(R.string.crash_notification_channel_name),
                        NotificationManager.IMPORTANCE_HIGH);
        ch.setDescription(getString(R.string.crash_notification_channel_desc));
        ch.enableLights(true);
        ch.enableVibration(true);
        ch.setBypassDnd(true);
        ch.setLightColor(NOTIFICATION_ACCENT_COLOR);
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm != null) {
            nm.createNotificationChannel(ch);
        }
    }

    /**
     * Full-screen SOS notification: brings {@link CrashAlertActivity} above lock screen on many
     * devices when paired with {@code USE_FULL_SCREEN_INTENT}.
     */
    private void postCrashFullscreenNotification(double lat, double lng) {
        NotificationManager nm = getSystemService(NotificationManager.class);
        if (nm == null) {
            return;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
                && ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS)
                        != PackageManager.PERMISSION_GRANTED) {
            Log.w(TAG, "POST_NOTIFICATIONS not granted — crash full-screen banner may not show");
            return;
        }
        Intent full =
                new Intent(this, CrashAlertActivity.class)
                        .addFlags(
                                Intent.FLAG_ACTIVITY_NEW_TASK
                                        | Intent.FLAG_ACTIVITY_CLEAR_TOP
                                        | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        full.putExtra(CrashAlertActivity.EXTRA_CRASH_LAT, lat);
        full.putExtra(CrashAlertActivity.EXTRA_CRASH_LNG, lng);

        PendingIntent fullScreenPi =
                PendingIntent.getActivity(
                        this,
                        2001,
                        full,
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        PendingIntent tapPi =
                PendingIntent.getActivity(
                        this,
                        2002,
                        full,
                        PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);

        Notification n =
                new NotificationCompat.Builder(this, CHANNEL_CRASH_ID)
                        .setSmallIcon(android.R.drawable.ic_dialog_alert)
                        .setContentTitle(getString(R.string.crash_notification_title))
                        .setContentText(getString(R.string.crash_notification_text))
                        .setPriority(NotificationCompat.PRIORITY_MAX)
                        .setCategory(NotificationCompat.CATEGORY_ALARM)
                        .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
                        .setColor(NOTIFICATION_ACCENT_COLOR)
                        .setColorized(true)
                        .setAutoCancel(false)
                        .setContentIntent(tapPi)
                        .setFullScreenIntent(fullScreenPi, true)
                        .build();
        nm.notify(NOTIFICATION_CRASH_SOS_ID, n);
    }

    private void broadcastCrashDetected() {
        LocalBroadcastManager.getInstance(this)
                .sendBroadcast(new Intent(ACTION_CRASH_DETECTED));
    }

    /**
     * Opens {@link CrashAlertActivity} (countdown + {@link EmergencyService}). Previously only a
     * dashboard flash ran — the emergency UI was never shown so no call/SMS could start.
     */
    private void launchCrashAlertIfNeeded() {
        long now = SystemClock.uptimeMillis();
        if (now - lastCrashAlertLaunchUptimeMs < CRASH_ALERT_LAUNCH_DEBOUNCE_MS) {
            return;
        }
        lastCrashAlertLaunchUptimeMs = now;
        mainHandler.post(
                () -> {
                    createCrashNotificationChannel();
                    postCrashFullscreenNotification(lastKnownLat, lastKnownLng);
                    Intent i = new Intent(MarcForegroundService.this, CrashAlertActivity.class);
                    i.addFlags(
                            Intent.FLAG_ACTIVITY_NEW_TASK
                                    | Intent.FLAG_ACTIVITY_CLEAR_TOP
                                    | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    i.putExtra(CrashAlertActivity.EXTRA_CRASH_LAT, lastKnownLat);
                    i.putExtra(CrashAlertActivity.EXTRA_CRASH_LNG, lastKnownLng);
                    try {
                        startActivity(i);
                    } catch (Exception e) {
                        Log.e(
                                TAG,
                                "Could not start CrashAlertActivity — grant permissions and ensure "
                                        + "ride is armed from the app so monitoring runs on the phone.",
                                e);
                    }
                });
    }

    private void broadcastSpeedUpdate(float speedKmh) {
        Intent i = new Intent(ACTION_SPEED_UPDATE);
        i.putExtra(EXTRA_SPEED_KMH, speedKmh);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }

    private void broadcastLocationUpdate(double lat, double lng) {
        Intent i = new Intent(ACTION_LOCATION_UPDATE);
        i.putExtra(EXTRA_LAT, lat);
        i.putExtra(EXTRA_LNG, lng);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }

    private void broadcastDeviceStatus(String deviceType, boolean connected) {
        Intent i = new Intent(ACTION_DEVICE_STATUS);
        i.putExtra(EXTRA_DEVICE_TYPE, deviceType);
        i.putExtra(EXTRA_CONNECTED, connected);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }

    private void broadcastLeanAngle(float roll) {
        Log.d("LEAN", "LEAN_UPDATE broadcast roll=" + roll);
        Intent i = new Intent(ACTION_LEAN_UPDATE);
        i.putExtra("roll", roll);
        LocalBroadcastManager.getInstance(this).sendBroadcast(i);
    }

    private void reloadCalibrationFromDb() {
        DatabaseHelper db = DatabaseHelper.getInstance(this);
        DeviceDao deviceDao = new DeviceDao(db);
        Device bike = deviceDao.getBike();
        if (bike == null) {
            calibration = null;
            return;
        }
        CalibrationDao calibrationDao = new CalibrationDao(db);
        calibration = calibrationDao.getCalibrationForDevice(bike.getId());
    }

    @SuppressLint("MissingPermission")
    private void startLocationUpdates() {
        if (!hasLocationPermission()) {
            return;
        }
        if (locationCallback != null) {
            return;
        }
        @SuppressWarnings("deprecation")
        LocationRequest request = LocationRequest.create();
        request.setInterval(1000L);
        request.setFastestInterval(500L);
        request.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        locationCallback =
                new LocationCallback() {
                    @Override
                    public void onLocationResult(LocationResult result) {
                        Location loc = result.getLastLocation();
                        if (loc == null) {
                            return;
                        }
                        lastKnownLat = loc.getLatitude();
                        lastKnownLng = loc.getLongitude();
                        broadcastLocationUpdate(lastKnownLat, lastKnownLng);

                        float speedKmh = loc.getSpeed() * 3.6f;
                        broadcastSpeedUpdate(speedKmh);

                        maybeSendSpeedLedAlert(speedKmh);
                    }
                };

        fusedLocationClient.requestLocationUpdates(request, locationCallback, Looper.getMainLooper());
    }

    private void stopLocationUpdates() {
        if (locationCallback != null && fusedLocationClient != null) {
            fusedLocationClient.removeLocationUpdates(locationCallback);
            locationCallback = null;
        }
    }

    private void maybeSendSpeedLedAlert(float speedKmh) {
        PicoApiClient helmet = helmetClient;
        if (helmet == null) {
            return;
        }
        float threshold = speedThresholdKmh;
        if (speedKmh > threshold) {
            if (!speedLedAlertActive) {
                speedLedAlertActive = true;
                helmet.setLedAlert(
                        true,
                        threshold,
                        new PicoApiClient.PicoCallback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean result) {
                            }

                            @Override
                            public void onError(String error) {
                            }
                        });
            }
        } else if (speedLedAlertActive) {
            speedLedAlertActive = false;
            helmet.setLedAlert(
                    false,
                    threshold,
                    new PicoApiClient.PicoCallback<Boolean>() {
                        @Override
                        public void onSuccess(Boolean result) {
                        }

                        @Override
                        public void onError(String error) {
                        }
                    });
        }
    }

    private boolean hasLocationPermission() {
        return ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(
                                this, Manifest.permission.ACCESS_COARSE_LOCATION)
                        == PackageManager.PERMISSION_GRANTED;
    }

    public void setHelmetClient(PicoApiClient client) {
        this.helmetClient = client;
    }

    public void setBikeClient(PicoApiClient client) {
        this.bikeClient = client;
    }

    public void setCalibration(Calibration calibration) {
        this.calibration = calibration;
    }

    public void setSpeedThreshold(float speedThresholdKmh) {
        this.speedThresholdKmh = speedThresholdKmh;
    }

    public class MarcBinder extends Binder {
        public MarcForegroundService getService() {
            return MarcForegroundService.this;
        }

        public void setHelmetClient(PicoApiClient client) {
            MarcForegroundService.this.setHelmetClient(client);
        }

        public void setBikeClient(PicoApiClient client) {
            MarcForegroundService.this.setBikeClient(client);
        }

        public void setCalibration(Calibration calibration) {
            MarcForegroundService.this.setCalibration(calibration);
        }

        public void setSpeedThreshold(float speedThresholdKmh) {
            MarcForegroundService.this.setSpeedThreshold(speedThresholdKmh);
        }
    }
}
