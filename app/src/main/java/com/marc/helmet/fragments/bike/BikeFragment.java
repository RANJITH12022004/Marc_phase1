package com.marc.helmet.fragments.bike;

import android.content.BroadcastReceiver;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.marc.helmet.R;
import com.marc.helmet.activities.MainActivity;
import com.marc.helmet.database.CalibrationDao;
import com.marc.helmet.database.DatabaseHelper;
import com.marc.helmet.database.DeviceDao;
import com.marc.helmet.database.SettingsDao;
import com.marc.helmet.models.Calibration;
import com.marc.helmet.models.Device;
import com.marc.helmet.services.MarcForegroundService;
import com.marc.helmet.utils.FormatUtils;
import com.marc.helmet.views.LeanAngleView;
import com.marc.helmet.views.SpeedHistoryView;

import java.util.Locale;

public class BikeFragment extends Fragment {

    private TabLayout tabLayout;
    private ViewPager2 viewPager;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_bike, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tabLayout = view.findViewById(R.id.tab_bike);
        viewPager = view.findViewById(R.id.vp_bike);
        viewPager.setAdapter(new BikeTabsAdapter(this));
        new TabLayoutMediator(
                        tabLayout,
                        viewPager,
                        (tab, position) -> {
                            if (position == 0) {
                                tab.setText("LEAN ANGLE");
                            } else if (position == 1) {
                                tab.setText("COORDINATES");
                            } else {
                                tab.setText("SPEED");
                            }
                        })
                .attach();
    }

    private static class BikeTabsAdapter extends FragmentStateAdapter {
        BikeTabsAdapter(@NonNull Fragment fragment) {
            super(fragment);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            if (position == 0) {
                return new LeanAngleTabFragment();
            } else if (position == 1) {
                return new CoordinatesTabFragment();
            }
            return new SpeedTabFragment();
        }

        @Override
        public int getItemCount() {
            return 3;
        }
    }

    public static class LeanAngleTabFragment extends Fragment {
        private LeanAngleView leanAngleView;
        private TextView tvCurrentAngle;
        private TextView tvMaxLeft;
        private TextView tvMaxRight;
        private TextView chipCrashStatus;

        private final BroadcastReceiver leanReceiver =
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (!MarcForegroundService.ACTION_LEAN_UPDATE.equals(intent.getAction())) {
                            return;
                        }
                        if (!isBikeConnectedForLean()) {
                            return;
                        }
                        float roll = intent.getFloatExtra("roll", 0f);
                        if (leanAngleView != null) {
                            leanAngleView.setLeanAngle(roll);
                        }
                        if (tvCurrentAngle != null) {
                            tvCurrentAngle.setText(
                                    (roll >= 0 ? "+" : "")
                                            + String.format(Locale.US, "%.1f?", roll));
                        }
                        boolean danger =
                                leanAngleView != null && leanAngleView.isInDangerZone();
                        if (chipCrashStatus != null) {
                            chipCrashStatus.setText(danger ? "\u26a0 DANGER" : "\u25cf SAFE");
                            chipCrashStatus.setTextColor(
                                    Color.parseColor(danger ? "#FF2020" : "#00FF88"));
                        }
                    }
                };

        @Nullable
        @Override
        public View onCreateView(
                @NonNull LayoutInflater inflater,
                @Nullable ViewGroup container,
                @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.layout_bike_lean, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            FrameLayout host = view.findViewById(R.id.sv_lean_3d);
            tvCurrentAngle = view.findViewById(R.id.tv_current_angle);
            tvMaxLeft = view.findViewById(R.id.tv_max_left);
            tvMaxRight = view.findViewById(R.id.tv_max_right);
            chipCrashStatus = view.findViewById(R.id.chip_crash_status);
            Button btnRecalibrate = view.findViewById(R.id.btn_recalibrate);

            leanAngleView = new LeanAngleView(requireContext());
            host.addView(
                    leanAngleView,
                    new FrameLayout.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
            btnRecalibrate.setOnClickListener(
                    v -> Toast.makeText(requireContext(), "Open Settings > Calibration", Toast.LENGTH_SHORT)
                            .show());
            refreshBikeDisconnectedOrCalibrationUi();
        }

        @Override
        public void onResume() {
            super.onResume();
            refreshBikeDisconnectedOrCalibrationUi();
        }

        @Override
        public void onStart() {
            super.onStart();
            IntentFilter filter = new IntentFilter(MarcForegroundService.ACTION_LEAN_UPDATE);
            LocalBroadcastManager.getInstance(requireContext()).registerReceiver(leanReceiver, filter);
        }

        @Override
        public void onStop() {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(leanReceiver);
            super.onStop();
        }

        private boolean isBikeConnectedForLean() {
            Device bike =
                    new DeviceDao(DatabaseHelper.getInstance(requireContext().getApplicationContext()))
                            .getBike();
            return bike != null && bike.isConnected();
        }

        private void refreshBikeDisconnectedOrCalibrationUi() {
            Context ctx = requireContext();
            Device bike = new DeviceDao(DatabaseHelper.getInstance(ctx.getApplicationContext())).getBike();
            if (bike == null || !bike.isConnected()) {
                if (tvCurrentAngle != null) {
                    tvCurrentAngle.setText("--");
                }
                if (chipCrashStatus != null) {
                    chipCrashStatus.setText("BIKE DISCONNECTED");
                    chipCrashStatus.setTextColor(Color.parseColor("#FF2020"));
                }
                return;
            }
            loadCalibrationIntoLeanView();
            if (chipCrashStatus != null && leanAngleView != null) {
                boolean danger = leanAngleView.isInDangerZone();
                chipCrashStatus.setText(danger ? "\u26a0 DANGER" : "\u25cf SAFE");
                chipCrashStatus.setTextColor(
                        Color.parseColor(danger ? "#FF2020" : "#00FF88"));
            }
        }

        private void loadCalibrationIntoLeanView() {
            if (leanAngleView == null) {
                return;
            }
            Context ctx = requireContext();
            DatabaseHelper db = DatabaseHelper.getInstance(ctx.getApplicationContext());
            Device bike = new DeviceDao(db).getBike();
            if (bike == null) {
                return;
            }
            Calibration c = new CalibrationDao(db).getCalibrationForDevice(bike.getId());
            if (c != null && c.isCalibrated()) {
                leanAngleView.setCalibration(
                        (float) c.getStandingRoll(),
                        (float) c.getMaxLeftRoll(),
                        (float) c.getMaxRightRoll());
                if (tvMaxLeft != null) {
                    tvMaxLeft.setText(FormatUtils.formatAngle(c.getMaxLeftRoll()));
                }
                if (tvMaxRight != null) {
                    tvMaxRight.setText(FormatUtils.formatAngle(c.getMaxRightRoll()));
                }
            } else {
                leanAngleView.setCalibration(0f, -42f, 45f);
                if (tvMaxLeft != null) {
                    tvMaxLeft.setText(FormatUtils.formatAngle(-45));
                }
                if (tvMaxRight != null) {
                    tvMaxRight.setText(FormatUtils.formatAngle(45));
                }
            }
        }
    }

    public static class CoordinatesTabFragment extends Fragment {
        private TextView tvLatitude;
        private TextView tvLongitude;
        private TextView tvAltitude;
        private TextView tvAccuracy;
        private TextView tvGpsStatus;
        private FusedLocationProviderClient fusedLocationClient;
        private double lat;
        private double lng;

        private final BroadcastReceiver locationReceiver =
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (!MarcForegroundService.ACTION_LOCATION_UPDATE.equals(intent.getAction())) {
                            return;
                        }
                        lat = intent.getDoubleExtra(MarcForegroundService.EXTRA_LAT, 0d);
                        lng = intent.getDoubleExtra(MarcForegroundService.EXTRA_LNG, 0d);
                        tvLatitude.setText(String.format(Locale.US, "%.6f", lat));
                        tvLongitude.setText(String.format(Locale.US, "%.6f", lng));
                        tvAltitude.setText("--m");
                        tvAccuracy.setText("--m");
                        tvGpsStatus.setText("LOCKED");
                        tvGpsStatus.setTextColor(0xFF00FF88);
                    }
                };

        @Nullable
        @Override
        public View onCreateView(
                @NonNull LayoutInflater inflater,
                @Nullable ViewGroup container,
                @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.layout_bike_coordinates, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            tvLatitude = view.findViewById(R.id.tv_latitude);
            tvLongitude = view.findViewById(R.id.tv_longitude);
            tvAltitude = view.findViewById(R.id.tv_altitude);
            tvAccuracy = view.findViewById(R.id.tv_accuracy);
            tvGpsStatus = view.findViewById(R.id.tv_gps_status);
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());
            Button btnCopyCoords = view.findViewById(R.id.btn_copy_coords);
            btnCopyCoords.setOnClickListener(
                    v -> {
                        String text = String.format(Locale.US, "LAT: %.6f, LNG: %.6f", lat, lng);
                        ClipboardManager cm =
                                (ClipboardManager)
                                        requireContext().getSystemService(Context.CLIPBOARD_SERVICE);
                        if (cm != null) {
                            cm.setPrimaryClip(ClipData.newPlainText("coords", text));
                        }
                        Toast.makeText(requireContext(), "Coordinates copied", Toast.LENGTH_SHORT).show();
                    });
            loadLastKnownLocation();
        }

        @Override
        public void onStart() {
            super.onStart();
            LocalBroadcastManager.getInstance(requireContext())
                    .registerReceiver(
                            locationReceiver,
                            new IntentFilter(MarcForegroundService.ACTION_LOCATION_UPDATE));
        }

        @Override
        public void onStop() {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(locationReceiver);
            super.onStop();
        }

        private void loadLastKnownLocation() {
            if (ContextCompat.checkSelfPermission(
                            requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                tvGpsStatus.setText("PERMISSION REQUIRED");
                tvGpsStatus.setTextColor(0xFFFF2020);
                return;
            }
            fusedLocationClient
                    .getLastLocation()
                    .addOnSuccessListener(
                            location -> {
                                if (!isAdded() || location == null) {
                                    return;
                                }
                                lat = location.getLatitude();
                                lng = location.getLongitude();
                                tvLatitude.setText(String.format(Locale.US, "%.6f", lat));
                                tvLongitude.setText(String.format(Locale.US, "%.6f", lng));
                                tvAltitude.setText(
                                        String.format(Locale.US, "%.1f m", location.getAltitude()));
                                tvAccuracy.setText(
                                        String.format(Locale.US, "%.1f m", location.getAccuracy()));
                                tvGpsStatus.setText("LOCKED");
                                tvGpsStatus.setTextColor(0xFF00FF88);
                            });
        }
    }

    public static class SpeedTabFragment extends Fragment {
        private TextView tvSpeedLarge;
        private TextView tvThresholdVal;
        private TextView tvSpeedAlertStatus;
        private SeekBar sbSpeedThreshold;
        private SettingsDao settingsDao;
        private FusedLocationProviderClient fusedLocationClient;
        private SpeedHistoryView speedHistoryView;

        private final BroadcastReceiver speedReceiver =
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        if (!MarcForegroundService.ACTION_SPEED_UPDATE.equals(intent.getAction())) {
                            return;
                        }
                        float speed = intent.getFloatExtra(MarcForegroundService.EXTRA_SPEED_KMH, 0f);
                        tvSpeedLarge.setText(String.valueOf(Math.round(speed)));
                        if (speedHistoryView != null) {
                            speedHistoryView.addSample(speed);
                        }
                        int threshold = sbSpeedThreshold.getProgress();
                        if (speed > threshold) {
                            tvSpeedAlertStatus.setText("LED ALERT: ON");
                            tvSpeedAlertStatus.setTextColor(0xFFFF2020);
                        } else {
                            tvSpeedAlertStatus.setText("LED ALERT: OFF");
                            tvSpeedAlertStatus.setTextColor(0xFF888888);
                        }
                    }
                };

        @Nullable
        @Override
        public View onCreateView(
                @NonNull LayoutInflater inflater,
                @Nullable ViewGroup container,
                @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.layout_bike_speed, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);
            tvSpeedLarge = view.findViewById(R.id.tv_speed_large);
            tvThresholdVal = view.findViewById(R.id.tv_threshold_val);
            tvSpeedAlertStatus = view.findViewById(R.id.tv_speed_alert_status);
            sbSpeedThreshold = view.findViewById(R.id.sb_speed_threshold);
            speedHistoryView = view.findViewById(R.id.speed_history_view);
            settingsDao = new SettingsDao(DatabaseHelper.getInstance(requireContext()));
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext());

            int saved = Math.round(settingsDao.getSpeedThreshold());
            sbSpeedThreshold.setProgress(saved);
            tvThresholdVal.setText(saved + " km/h");
            if (speedHistoryView != null) {
                speedHistoryView.setThresholdKmh(saved);
            }

            sbSpeedThreshold.setOnSeekBarChangeListener(
                    new SeekBar.OnSeekBarChangeListener() {
                        @Override
                        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                            tvThresholdVal.setText(progress + " km/h");
                            if (speedHistoryView != null) {
                                speedHistoryView.setThresholdKmh(progress);
                            }
                            settingsDao.setSetting("speed_alert_threshold_kmh", String.valueOf(progress));
                            if (getActivity() instanceof MainActivity) {
                                MainActivity activity = (MainActivity) getActivity();
                                if (activity.getMarcBinder() != null) {
                                    activity.getMarcBinder().setSpeedThreshold(progress);
                                }
                            }
                        }

                        @Override
                        public void onStartTrackingTouch(SeekBar seekBar) {}

                        @Override
                        public void onStopTrackingTouch(SeekBar seekBar) {}
                    });
            loadLastKnownSpeed();
        }

        @Override
        public void onStart() {
            super.onStart();
            LocalBroadcastManager.getInstance(requireContext())
                    .registerReceiver(
                            speedReceiver, new IntentFilter(MarcForegroundService.ACTION_SPEED_UPDATE));
        }

        @Override
        public void onStop() {
            LocalBroadcastManager.getInstance(requireContext()).unregisterReceiver(speedReceiver);
            super.onStop();
        }

        private void loadLastKnownSpeed() {
            if (ContextCompat.checkSelfPermission(
                            requireContext(), android.Manifest.permission.ACCESS_FINE_LOCATION)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                tvSpeedLarge.setText("0");
                return;
            }
            fusedLocationClient
                    .getLastLocation()
                    .addOnSuccessListener(
                            location -> {
                                if (!isAdded() || location == null) {
                                    return;
                                }
                                float speedKmh = Math.max(0f, location.getSpeed() * 3.6f);
                                tvSpeedLarge.setText(String.valueOf(Math.round(speedKmh)));
                                if (speedHistoryView != null) {
                                    speedHistoryView.addSample(speedKmh);
                                }
                            });
        }
    }
}
