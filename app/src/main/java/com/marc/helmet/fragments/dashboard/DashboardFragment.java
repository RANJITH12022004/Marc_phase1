package com.marc.helmet.fragments.dashboard;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.marc.helmet.R;
import com.marc.helmet.activities.MainActivity;
import com.marc.helmet.database.DatabaseHelper;
import com.marc.helmet.database.DeviceDao;
import com.marc.helmet.database.SettingsDao;
import com.marc.helmet.models.Device;
import com.marc.helmet.services.MarcForegroundService;
import com.marc.helmet.speech.WakeWordManager;

import java.util.Locale;

public class DashboardFragment extends Fragment {

    private TextView tvRideTimer;
    private Button btnArmRide;
    private Button btnEndRide;
    private TextView tvHelmetStatus;
    private TextView tvBikeStatus;
    private TextView tvAiEngineStatus;
    private TextView tvSpeed;
    private TextView tvDashCoordinates;
    private View dotHelmet;
    private View dotBike;
    private TextView tvHelmetIp;
    private TextView tvBikeIp;
    private TextView tvWakeWordStatus;

    private final Handler timerHandler = new Handler(Looper.getMainLooper());
    private long rideStartTime;
    private boolean isRiding;

    private final BroadcastReceiver marcReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (intent.getAction() == null) {
                        return;
                    }
                    switch (intent.getAction()) {
                        case MarcForegroundService.ACTION_SPEED_UPDATE:
                            float kmh =
                                    intent.getFloatExtra(
                                            MarcForegroundService.EXTRA_SPEED_KMH, 0f);
                            if (tvSpeed != null) {
                                tvSpeed.setText(
                                        String.format(Locale.US, "%.0f km/h", kmh));
                            }
                            break;
                        case MarcForegroundService.ACTION_LOCATION_UPDATE:
                            double lat =
                                    intent.getDoubleExtra(
                                            MarcForegroundService.EXTRA_LAT, 0d);
                            double lng =
                                    intent.getDoubleExtra(
                                            MarcForegroundService.EXTRA_LNG, 0d);
                            if (tvDashCoordinates != null) {
                                tvDashCoordinates.setText(
                                        String.format(Locale.US, "%.6f, %.6f", lat, lng));
                            }
                            break;
                        case MarcForegroundService.ACTION_DEVICE_STATUS:
                            String type =
                                    intent.getStringExtra(
                                            MarcForegroundService.EXTRA_DEVICE_TYPE);
                            boolean connected =
                                    intent.getBooleanExtra(
                                            MarcForegroundService.EXTRA_CONNECTED, false);
                            if (deviceDao != null && type != null) {
                                deviceDao.setConnected(type, connected);
                                refreshDeviceCards();
                            }
                            break;
                        case MarcForegroundService.ACTION_CRASH_DETECTED:
                            flashCrashBackground();
                            break;
                        default:
                            break;
                    }
                }
            };

    private SettingsDao settingsDao;
    private DeviceDao deviceDao;
    private FusedLocationProviderClient fusedLocationClient;

    private final Runnable timerTick =
            new Runnable() {
                @Override
                public void run() {
                    if (!isRiding || tvRideTimer == null) {
                        return;
                    }
                    long elapsedMs = System.currentTimeMillis() - rideStartTime;
                    tvRideTimer.setText(formatRideDuration(elapsedMs));
                    timerHandler.postDelayed(this, 1000L);
                }
            };

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_dashboard, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        tvRideTimer = view.findViewById(R.id.tv_ride_timer);
        btnArmRide = view.findViewById(R.id.btn_arm_ride);
        btnEndRide = view.findViewById(R.id.btn_end_ride);
        tvHelmetStatus = view.findViewById(R.id.tv_helmet_status);
        tvBikeStatus = view.findViewById(R.id.tv_bike_status);
        tvAiEngineStatus = view.findViewById(R.id.tv_ai_engine_status);
        tvSpeed = view.findViewById(R.id.tv_speed);
        tvDashCoordinates = view.findViewById(R.id.tv_dash_coordinates);
        dotHelmet = view.findViewById(R.id.dot_helmet);
        dotBike = view.findViewById(R.id.dot_bike);
        tvHelmetIp = view.findViewById(R.id.tv_helmet_ip);
        tvBikeIp = view.findViewById(R.id.tv_bike_ip);
        tvWakeWordStatus = view.findViewById(R.id.tv_wake_word_status);

        DatabaseHelper db = DatabaseHelper.getInstance(requireContext());
        settingsDao = new SettingsDao(db);
        deviceDao = new DeviceDao(db);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

        btnArmRide.setOnClickListener(
                v -> {
                    if (requireActivity() instanceof MainActivity) {
                        ((MainActivity) requireActivity()).armRide();
                    }
                    startRideTimer();
                });
        btnEndRide.setOnClickListener(
                v -> {
                    if (requireActivity() instanceof MainActivity) {
                        ((MainActivity) requireActivity()).endRide();
                    }
                    stopRideTimer();
                });

        IntentFilter filter = new IntentFilter();
        filter.addAction(MarcForegroundService.ACTION_SPEED_UPDATE);
        filter.addAction(MarcForegroundService.ACTION_LOCATION_UPDATE);
        filter.addAction(MarcForegroundService.ACTION_DEVICE_STATUS);
        filter.addAction(MarcForegroundService.ACTION_CRASH_DETECTED);
        LocalBroadcastManager.getInstance(requireContext())
                .registerReceiver(marcReceiver, filter);

        refreshAiEngineStatus();
        refreshWakeWordStatus();
        refreshDeviceCards();
        loadLastKnownTelemetry();
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshDeviceCards();
        refreshAiEngineStatus();
        refreshWakeWordStatus();
        loadLastKnownTelemetry();
    }

    @Override
    public void onDestroyView() {
        timerHandler.removeCallbacks(timerTick);
        LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(marcReceiver);
        tvRideTimer = null;
        btnArmRide = null;
        btnEndRide = null;
        tvHelmetStatus = null;
        tvBikeStatus = null;
        tvAiEngineStatus = null;
        tvSpeed = null;
        tvDashCoordinates = null;
        dotHelmet = null;
        dotBike = null;
        tvHelmetIp = null;
        tvBikeIp = null;
        tvWakeWordStatus = null;
        settingsDao = null;
        deviceDao = null;
        super.onDestroyView();
    }

    /** Refresh helmet/bike IP and dots when devices DB changes from Settings. */
    public void notifyDevicesChanged() {
        refreshDeviceCards();
    }

    private void startRideTimer() {
        rideStartTime = System.currentTimeMillis();
        isRiding = true;
        timerHandler.removeCallbacks(timerTick);
        if (tvRideTimer != null) {
            tvRideTimer.setText(formatRideDuration(0L));
        }
        timerHandler.post(timerTick);
    }

    private void stopRideTimer() {
        isRiding = false;
        timerHandler.removeCallbacks(timerTick);
        if (tvRideTimer != null) {
            tvRideTimer.setText("00:00:00");
        }
    }

    private static String formatRideDuration(long elapsedMs) {
        long totalSec = Math.max(0L, elapsedMs / 1000L);
        long h = totalSec / 3600L;
        long m = (totalSec % 3600L) / 60L;
        long s = totalSec % 60L;
        return String.format(Locale.US, "%02d:%02d:%02d", h, m, s);
    }

    private void refreshDeviceCards() {
        if (deviceDao == null) {
            return;
        }
        Device helmet = deviceDao.getHelmet();
        Device bike = deviceDao.getBike();
        updateDeviceCard(helmet, tvHelmetStatus, dotHelmet, tvHelmetIp);
        updateDeviceCard(bike, tvBikeStatus, dotBike, tvBikeIp);
    }

    private void updateDeviceCard(
            @Nullable Device device,
            @Nullable TextView statusTv,
            @Nullable View dot,
            @Nullable TextView ipTv) {
        if (statusTv == null || dot == null || ipTv == null) {
            return;
        }
        boolean connected = device != null && device.isConnected();
        if (connected) {
            statusTv.setText("CONNECTED");
            dot.setBackgroundResource(R.drawable.bg_dot_green);
            ipTv.setText(device != null && device.getIpAddress() != null ? device.getIpAddress() : "");
        } else {
            statusTv.setText("DISCONNECTED");
            dot.setBackgroundResource(R.drawable.bg_dot_gray);
            String ip = (device != null && device.getIpAddress() != null) ? device.getIpAddress() : "";
            ipTv.setText(ip);
        }
    }

    private void refreshAiEngineStatus() {
        if (tvAiEngineStatus == null || settingsDao == null) {
            return;
        }
        int green = ContextCompat.getColor(requireContext(), R.color.colorSuccess);
        if (settingsDao.isGeminiMode()) {
            tvAiEngineStatus.setText("MARC ONE — ONLINE");
        } else {
            tvAiEngineStatus.setText("MARC BACK — ONLINE");
        }
        tvAiEngineStatus.setTextColor(green);
    }

    private void refreshWakeWordStatus() {
        if (tvWakeWordStatus == null) {
            return;
        }
        if (!(requireActivity() instanceof MainActivity)) {
            return;
        }
        WakeWordManager wake = ((MainActivity) requireActivity()).getWakeWordManager();
        if (wake != null && wake.isListening()) {
            tvWakeWordStatus.setText("● LISTENING");
            tvWakeWordStatus.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.colorPrimary));
        } else {
            tvWakeWordStatus.setText("● STANDBY");
            tvWakeWordStatus.setTextColor(
                    ContextCompat.getColor(requireContext(), R.color.textSecondary));
        }
    }

    private void flashCrashBackground() {
        View root = getView();
        if (root == null) {
            return;
        }
        int normal = ContextCompat.getColor(requireContext(), R.color.colorBackground);
        int flash = ContextCompat.getColor(requireContext(), R.color.colorCoreDark);
        ValueAnimator animator = ValueAnimator.ofArgb(normal, flash);
        animator.setDuration(280L);
        animator.setRepeatCount(5);
        animator.setRepeatMode(ValueAnimator.REVERSE);
        animator.addUpdateListener(a -> root.setBackgroundColor((Integer) a.getAnimatedValue()));
        animator.addListener(
                new AnimatorListenerAdapter() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        root.setBackgroundColor(normal);
                    }
                });
        animator.start();
    }

    @SuppressLint("MissingPermission")
    private void loadLastKnownTelemetry() {
        if (fusedLocationClient == null) {
            return;
        }
        if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            if (tvDashCoordinates != null) {
                tvDashCoordinates.setText("Location permission required");
            }
            if (tvSpeed != null) {
                tvSpeed.setText("0 km/h");
            }
            return;
        }
        fusedLocationClient
                .getLastLocation()
                .addOnSuccessListener(
                        location -> {
                            if (!isAdded()) {
                                return;
                            }
                            if (location == null) {
                                if (tvDashCoordinates != null) {
                                    tvDashCoordinates.setText("Acquiring GPS...");
                                }
                                if (tvSpeed != null) {
                                    tvSpeed.setText("0 km/h");
                                }
                                return;
                            }
                            if (tvDashCoordinates != null) {
                                tvDashCoordinates.setText(
                                        String.format(
                                                Locale.US,
                                                "%.6f, %.6f",
                                                location.getLatitude(),
                                                location.getLongitude()));
                            }
                            if (tvSpeed != null) {
                                float speedKmh = Math.max(0f, location.getSpeed() * 3.6f);
                                tvSpeed.setText(String.format(Locale.US, "%.0f km/h", speedKmh));
                            }
                        })
                .addOnFailureListener(
                        e -> {
                            if (!isAdded()) {
                                return;
                            }
                            if (tvDashCoordinates != null) {
                                tvDashCoordinates.setText("GPS unavailable");
                            }
                            if (tvSpeed != null) {
                                tvSpeed.setText("0 km/h");
                            }
                        });
    }
}
