package com.marc.helmet.activities;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.media.AudioManager;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.NavDestination;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;
import com.marc.helmet.R;
import com.marc.helmet.database.CalibrationDao;
import com.marc.helmet.database.DatabaseHelper;
import com.marc.helmet.database.DeviceDao;
import com.marc.helmet.database.SettingsDao;
import com.marc.helmet.fragments.dashboard.DashboardFragment;
import com.marc.helmet.fragments.marc.MarcFragment;
import com.marc.helmet.models.Calibration;
import com.marc.helmet.models.Device;
import com.marc.helmet.network.pico.PicoApiClient;
import com.marc.helmet.services.MarcForegroundService;
import com.marc.helmet.speech.MarcTTSManager;
import com.marc.helmet.speech.WakeWordManager;
import com.marc.helmet.utils.PermissionUtils;

import java.util.List;

/**
 * Root activity: navigation, TTS, wake word, ride foreground service binding, status HUD.
 */
public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MARC";
    private static final int REQUEST_MARC_PERMISSIONS = 1001;

    private NavController navController;
    private BottomNavigationView bottomNav;
    private TextView tvMarcLogo;
    private TextView tvSystemStatus;
    private TextView tvAiMode;

    private MarcTTSManager ttsManager;
    private WakeWordManager wakeWordManager;
    private DatabaseHelper db;
    private SettingsDao settingsDao;
    private AudioManager audioManager;

    private MarcForegroundService.MarcBinder marcBinder;
    private boolean serviceConnected = false;
    private boolean rideArmed = false;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private final ServiceConnection serviceConnection =
            new ServiceConnection() {
                @Override
                public void onServiceConnected(ComponentName name, IBinder service) {
                    marcBinder = (MarcForegroundService.MarcBinder) service;
                    serviceConnected = true;
                    applyCalibrationAndSpeedToService();
                    wireStoredDevicesFromDbToBinder();
                }

                @Override
                public void onServiceDisconnected(ComponentName name) {
                    marcBinder = null;
                    serviceConnected = false;
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel =
                    new NotificationChannel(
                            MarcForegroundService.CHANNEL_ID,
                            "MARC System",
                            NotificationManager.IMPORTANCE_LOW);
            channel.setLightColor(Color.parseColor("#FF2020"));
            NotificationManager nm =
                    (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            if (nm != null) {
                nm.createNotificationChannel(channel);
            }
        }

        db = DatabaseHelper.getInstance(this);
        settingsDao = new SettingsDao(db);
        autoReconnectKnownDevices();
        audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        if (audioManager != null) {
            audioManager.adjustStreamVolume(
                    AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_MUTE, 0);
        }

        tvMarcLogo = findViewById(R.id.tv_marc_logo);
        tvSystemStatus = findViewById(R.id.tv_system_status);
        tvAiMode = findViewById(R.id.tv_ai_mode);

        NavHostFragment navHostFragment =
                (NavHostFragment)
                        getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (navHostFragment == null) {
            throw new IllegalStateException("NavHostFragment not found");
        }
        navController = navHostFragment.getNavController();

        bottomNav = findViewById(R.id.bottom_nav);
        NavigationUI.setupWithNavController(bottomNav, navController);
        bottomNav.setItemIconTintList(ContextCompat.getColorStateList(this, R.color.nav_selector));
        bottomNav.setItemTextColor(ContextCompat.getColorStateList(this, R.color.nav_selector));
        bottomNav.setLabelVisibilityMode(NavigationBarView.LABEL_VISIBILITY_SELECTED);

        ttsManager =
                new MarcTTSManager(
                        this,
                        new MarcTTSManager.OnReadyListener() {
                            @Override
                            public void onReady() {
                                initWakeWord();
                            }

                            @Override
                            public void onError(String msg) {
                                Log.e(TAG, "TTS init failed: " + msg);
                                initWakeWord();
                            }
                        });

        PermissionUtils.requestAll(this, REQUEST_MARC_PERMISSIONS);

        if (tvAiMode != null) {
            tvAiMode.setText(settingsDao.isGeminiMode() ? "MARC ONE" : "MARC BACK");
        }
        updateSystemStatus("● STANDBY", Color.parseColor("#555555"));
    }

    void initWakeWord() {
        if (wakeWordManager != null) {
            wakeWordManager.destroy();
        }

        wakeWordManager =
                new WakeWordManager(
                        this,
                        new WakeWordManager.WakeWordListener() {
                            @Override
                            public void onWakeWordDetected() {
                                activateMarcFromWakeWord();
                            }

                            @Override
                            public void onError(String e) {
                                Log.e(TAG, "WakeWord error: " + e);
                            }
                        });

        wakeWordManager.startListening();
    }

    void activateMarcFromWakeWord() {
        Log.d("MARC_WAKE", "Wake word fired, navigating to MARC");
        if (wakeWordManager != null) {
            wakeWordManager.stopListeningForHandoff();
        }
        runOnUiThread(
                () -> {
                    NavDestination current = navController.getCurrentDestination();
                    if (current != null && current.getId() == R.id.marcFragment) {
                        MarcFragment mf = getCurrentMarcFragment();
                        if (mf != null) {
                            mf.activateVoice();
                        }
                    } else {
                        navController.navigate(R.id.marcFragment);
                        Handler ui = new Handler(Looper.getMainLooper());
                        ui.post(
                                () -> {
                                    getSupportFragmentManager().executePendingTransactions();
                                    MarcFragment mf = getCurrentMarcFragment();
                                    if (mf != null) {
                                        mf.activateVoice();
                                        return;
                                    }
                                    ui.postDelayed(
                                            () -> {
                                                getSupportFragmentManager()
                                                        .executePendingTransactions();
                                                MarcFragment mfDelayed =
                                                        getCurrentMarcFragment();
                                                if (mfDelayed != null) {
                                                    mfDelayed.activateVoice();
                                                }
                                            },
                                            350);
                                });
                    }
                });
    }

    /**
     * Legacy entry used by older docs; delegates to {@link #activateMarcFromWakeWord()}.
     */
    public void activateMarcListening() {
        activateMarcFromWakeWord();
    }

    public void restartWakeWord() {
        if (wakeWordManager != null) {
            wakeWordManager.resumeListening();
        }
    }

    @Nullable
    MarcFragment getCurrentMarcFragment() {
        NavHostFragment nhf =
                (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (nhf == null) {
            return null;
        }
        Fragment primary = nhf.getChildFragmentManager().getPrimaryNavigationFragment();
        if (primary instanceof MarcFragment) {
            return (MarcFragment) primary;
        }
        for (Fragment f : nhf.getChildFragmentManager().getFragments()) {
            if (f instanceof MarcFragment) {
                return (MarcFragment) f;
            }
        }
        return null;
    }

    private void autoReconnectKnownDevices() {
        new Thread(
                        () -> {
                            DeviceDao deviceDao = new DeviceDao(db);
                            List<Device> known = deviceDao.getAllDevices();
                            for (Device d : known) {
                                if (d.getIpAddress() == null || d.getIpAddress().isEmpty()) {
                                    continue;
                                }
                                final Device dev = d;
                                PicoApiClient client =
                                        new PicoApiClient("http://" + dev.getIpAddress());
                                client.ping(
                                        new PicoApiClient.PicoCallback<Long>() {
                                            @Override
                                            public void onSuccess(Long ms) {
                                                deviceDao.setConnected(dev.getDeviceType(), true);
                                                runOnUiThread(
                                                        () -> {
                                                            wirePicoClientIfBound(dev);
                                                            refreshDashboardDeviceCards();
                                                            if (rideArmed) {
                                                                updateSystemStatus(
                                                                        "● ARMED",
                                                                        Color.parseColor(
                                                                                "#00FF88"));
                                                            }
                                                        });
                                            }

                                            @Override
                                            public void onError(String error) {
                                                deviceDao.setConnected(dev.getDeviceType(), false);
                                                runOnUiThread(MainActivity.this::refreshDashboardDeviceCards);
                                            }
                                        });
                            }
                        })
                .start();
    }

    private void wirePicoClientIfBound(Device dev) {
        MarcForegroundService.MarcBinder binder = marcBinder;
        if (binder == null) {
            return;
        }
        PicoApiClient pico = new PicoApiClient(dev.getBaseUrl());
        if (dev.isHelmet()) {
            binder.setHelmetClient(pico);
        } else if (Device.BIKE.equals(dev.getDeviceType())) {
            binder.setBikeClient(pico);
        }
    }

    private void wireStoredDevicesFromDbToBinder() {
        if (marcBinder == null) {
            return;
        }
        DeviceDao deviceDao = new DeviceDao(db);
        Device helmet = deviceDao.getHelmet();
        Device bike = deviceDao.getBike();
        if (helmet != null
                && helmet.getIpAddress() != null
                && !helmet.getIpAddress().isEmpty()
                && helmet.isConnected()) {
            marcBinder.setHelmetClient(new PicoApiClient(helmet.getBaseUrl()));
        }
        if (bike != null
                && bike.getIpAddress() != null
                && !bike.getIpAddress().isEmpty()
                && bike.isConnected()) {
            marcBinder.setBikeClient(new PicoApiClient(bike.getBaseUrl()));
        }
    }

    public void refreshDashboardDeviceCards() {
        NavHostFragment nhf =
                (NavHostFragment)
                        getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment);
        if (nhf == null) {
            return;
        }
        Fragment primary = nhf.getChildFragmentManager().getPrimaryNavigationFragment();
        if (primary instanceof DashboardFragment) {
            ((DashboardFragment) primary).notifyDevicesChanged();
        }
    }

    private void applyCalibrationAndSpeedToService() {
        if (marcBinder == null) {
            return;
        }
        DeviceDao deviceDao = new DeviceDao(db);
        CalibrationDao calibrationDao = new CalibrationDao(db);
        Device bike = deviceDao.getBike();
        Calibration cal =
                bike != null ? calibrationDao.getCalibrationForDevice(bike.getId()) : null;
        marcBinder.setCalibration(cal);
        marcBinder.setSpeedThreshold(settingsDao.getSpeedThreshold());
    }

    public void armRide() {
        if (!PermissionUtils.hasLocation(this) || !PermissionUtils.hasAudio(this)) {
            Toast.makeText(
                            this,
                            "Grant location and microphone permissions before arming ride.",
                            Toast.LENGTH_LONG)
                    .show();
            return;
        }
        rideArmed = true;
        MarcForegroundService.startService(this);
        bindService(
                new Intent(this, MarcForegroundService.class),
                serviceConnection,
                Context.BIND_AUTO_CREATE);
        updateSystemStatus("● ARMED", Color.parseColor("#00FF88"));
    }

    public void endRide() {
        rideArmed = false;
        MarcForegroundService.stopService(this);
        if (serviceConnected) {
            try {
                unbindService(serviceConnection);
            } catch (IllegalArgumentException ignored) {
            }
            serviceConnected = false;
            marcBinder = null;
        }
        updateSystemStatus("● STANDBY", Color.parseColor("#555555"));
    }

    public void updateSystemStatus(String status, int color) {
        runOnUiThread(
                () -> {
                    if (tvSystemStatus != null) {
                        tvSystemStatus.setText(status);
                        tvSystemStatus.setTextColor(color);
                    }
                });
    }

    public void updateAiModeBadge(String mode, boolean isCoreMode) {
        runOnUiThread(
                () -> {
                    if (tvAiMode == null) {
                        return;
                    }
                    tvAiMode.setText(mode);
                    tvAiMode.setTextColor(Color.parseColor("#FF2020"));
                });
    }

    public NavController getNavController() {
        return navController;
    }

    public MarcForegroundService.MarcBinder getMarcBinder() {
        return marcBinder;
    }

    public MarcTTSManager getTtsManager() {
        return ttsManager;
    }

    public WakeWordManager getWakeWordManager() {
        return wakeWordManager;
    }

    public DatabaseHelper getDb() {
        return db;
    }

    public SettingsDao getSettingsDao() {
        return settingsDao;
    }

    public boolean isRideArmed() {
        return rideArmed;
    }

    @Override
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode != REQUEST_MARC_PERMISSIONS) {
            return;
        }
        if (PermissionUtils.hasAll(this)) {
            if (ttsManager != null && ttsManager.isReady()) {
                initWakeWord();
            }
        } else {
            Toast.makeText(this, "MARC needs all permissions to protect you.", Toast.LENGTH_LONG)
                    .show();
        }
    }

    @Override
    protected void onDestroy() {
        if (audioManager != null) {
            audioManager.adjustStreamVolume(
                    AudioManager.STREAM_NOTIFICATION, AudioManager.ADJUST_UNMUTE, 0);
        }
        if (serviceConnected) {
            try {
                unbindService(serviceConnection);
            } catch (IllegalArgumentException ignored) {
            }
            serviceConnected = false;
            marcBinder = null;
        }
        if (wakeWordManager != null) {
            wakeWordManager.destroy();
        }
        if (ttsManager != null) {
            ttsManager.shutdown();
        }
        super.onDestroy();
    }
}
