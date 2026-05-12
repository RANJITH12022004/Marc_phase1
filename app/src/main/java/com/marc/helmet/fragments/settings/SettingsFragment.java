package com.marc.helmet.fragments.settings;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.marc.helmet.R;
import com.marc.helmet.activities.MainActivity;
import com.marc.helmet.adapters.DeviceAdapter;
import com.marc.helmet.database.CalibrationDao;
import com.marc.helmet.database.DatabaseHelper;
import com.marc.helmet.database.DeviceDao;
import com.marc.helmet.database.SettingsDao;
import com.marc.helmet.models.Calibration;
import com.marc.helmet.models.Device;
import com.marc.helmet.network.ai.OllamaApiClient;
import com.marc.helmet.network.pico.DeviceScanner;
import com.marc.helmet.network.pico.PicoApiClient;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class SettingsFragment extends Fragment {

    private ToggleButton toggleAiMode;
    private TextView tvAiEngineHeader;
    private View layoutSecretConfig;
    private View layoutGemini;
    private View layoutOllama;
    private EditText etGeminiKey;
    private EditText etOllamaIp;
    private EditText etOllamaModel;
    private TextView tvOllamaStatus;
    private ProgressBar pbScanning;
    private RecyclerView rvDevices;
    private EditText etManualIp;
    private Button btnManualAdd;
    private TextView tvCalibStatus;
    private TextView tvStandingVal;
    private TextView tvRightVal;
    private TextView tvLeftVal;
    private Button btnSaveCalibration;

    private SettingsDao settingsDao;
    private DeviceDao deviceDao;
    private CalibrationDao calibDao;
    private DeviceScanner scanner;
    private DeviceAdapter deviceAdapter;

    private Double capturedStandingRoll;
    private Double capturedStandingPitch;
    private Double capturedRightRoll;
    private Double capturedLeftRoll;

    private int aiTapCount;
    private long lastAiTapTimeMs;

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_settings, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        bindViews(view);

        DatabaseHelper db = DatabaseHelper.getInstance(requireContext());
        settingsDao = new SettingsDao(db);
        deviceDao = new DeviceDao(db);
        calibDao = new CalibrationDao(db);
        scanner = new DeviceScanner(requireContext());

        setupDeviceList();
        loadSettingsIntoUi();
        loadCalibrationStatus();
        wireActions(view);
    }

    private void bindViews(View view) {
        toggleAiMode = view.findViewById(R.id.toggle_ai_mode);
        tvAiEngineHeader = view.findViewById(R.id.tv_ai_engine_header);
        layoutSecretConfig = view.findViewById(R.id.layout_secret_config);
        layoutGemini = view.findViewById(R.id.layout_gemini);
        layoutOllama = view.findViewById(R.id.layout_ollama);
        etGeminiKey = view.findViewById(R.id.et_gemini_key);
        etOllamaIp = view.findViewById(R.id.et_ollama_ip);
        etOllamaModel = view.findViewById(R.id.et_ollama_model);
        tvOllamaStatus = view.findViewById(R.id.tv_ollama_status);
        pbScanning = view.findViewById(R.id.pb_scanning);
        rvDevices = view.findViewById(R.id.rv_devices);
        etManualIp = view.findViewById(R.id.et_manual_ip);
        btnManualAdd = view.findViewById(R.id.btn_manual_add);
        tvCalibStatus = view.findViewById(R.id.tv_calib_status);
        tvStandingVal = view.findViewById(R.id.tv_standing_val);
        tvRightVal = view.findViewById(R.id.tv_right_val);
        tvLeftVal = view.findViewById(R.id.tv_left_val);
        btnSaveCalibration = view.findViewById(R.id.btn_save_calibration);
    }

    private void setupDeviceList() {
        rvDevices.setLayoutManager(new LinearLayoutManager(requireContext()));
        deviceAdapter =
                new DeviceAdapter(
                        deviceDao.getAllDevices(),
                        new DeviceAdapter.DeviceAdapterListener() {
                            @Override
                            public void onConnect(Device device) {
                                connectDevice(device);
                            }

                            @Override
                            public void onPing(Device device) {
                                pingDevice(device);
                            }
                        });
        rvDevices.setAdapter(deviceAdapter);
    }

    private void loadSettingsIntoUi() {
        boolean geminiMode = settingsDao.isGeminiMode();
        toggleAiMode.setChecked(!geminiMode);
        showEngineLayouts(geminiMode);
        etGeminiKey.setText(settingsDao.getGeminiApiKey());
        etOllamaIp.setText(settingsDao.getOllamaIp());
        etOllamaModel.setText(settingsDao.getSetting("ollama_model", "llama3.2:3b"));
        settingsDao.setSetting("wake_word_engine", "always_on");
    }

    private void wireActions(View view) {
        toggleAiMode.setOnCheckedChangeListener(
                (buttonView, isChecked) -> {
                    boolean geminiMode = !isChecked;
                    showEngineLayouts(geminiMode);
                    settingsDao.setSetting("ai_engine", geminiMode ? "gemini" : "ollama");
                    settingsDao.setSetting("gemini_api_key", etGeminiKey.getText().toString().trim());
                    settingsDao.setSetting("ollama_ip", etOllamaIp.getText().toString().trim());
                    settingsDao.setSetting("ollama_model", etOllamaModel.getText().toString().trim());
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity())
                                .updateAiModeBadge(geminiMode ? "MARC ONE" : "MARC BACK", false);
                    }
                });

        tvAiEngineHeader.setOnClickListener(
                v -> {
                    long now = System.currentTimeMillis();
                    if (now - lastAiTapTimeMs > 3000L) {
                        aiTapCount = 0;
                    }
                    lastAiTapTimeMs = now;
                    aiTapCount++;
                    if (aiTapCount >= 7) {
                        Context ctx = requireContext();
                        Vibrator vibrator =
                                (Vibrator) ctx.getSystemService(Context.VIBRATOR_SERVICE);
                        if (vibrator != null && vibrator.hasVibrator()) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                vibrator.vibrate(
                                        VibrationEffect.createOneShot(
                                                50, VibrationEffect.DEFAULT_AMPLITUDE));
                            } else {
                                //noinspection deprecation
                                vibrator.vibrate(50);
                            }
                        }
                        layoutSecretConfig.setVisibility(View.VISIBLE);
                        aiTapCount = 0;
                        Toast.makeText(ctx, "Developer mode", Toast.LENGTH_SHORT).show();
                    }
                });

        view.findViewById(R.id.btn_test_ollama).setOnClickListener(v -> testOllama());

        view.findViewById(R.id.btn_scan_devices).setOnClickListener(v -> startScan());
        btnManualAdd.setOnClickListener(v -> tryManualAddDevice());
        view.findViewById(R.id.btn_capture_standing).setOnClickListener(v -> captureStanding());
        view.findViewById(R.id.btn_capture_right).setOnClickListener(v -> captureRight());
        view.findViewById(R.id.btn_capture_left).setOnClickListener(v -> captureLeft());
        btnSaveCalibration.setOnClickListener(v -> saveCalibration());

        TextView tvGithub = view.findViewById(R.id.tv_github_link);
        tvGithub.setOnClickListener(
                v ->
                        startActivity(
                                new Intent(
                                        Intent.ACTION_VIEW,
                                        Uri.parse("https://github.com/RANJITH12022004"))));

        View.OnFocusChangeListener persistOnBlur =
                (v, hasFocus) -> {
                    if (!hasFocus) {
                        persistAiFields();
                    }
                };
        etGeminiKey.setOnFocusChangeListener(persistOnBlur);
        etOllamaIp.setOnFocusChangeListener(persistOnBlur);
        etOllamaModel.setOnFocusChangeListener(persistOnBlur);
        etGeminiKey.setOnEditorActionListener(
                (v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_DONE) {
                        persistAiFields();
                        return true;
                    }
                    return false;
                });

        etGeminiKey.addTextChangedListener(
                new TextWatcher() {
                    @Override
                    public void beforeTextChanged(
                            CharSequence s, int start, int count, int after) {}

                    @Override
                    public void onTextChanged(
                            CharSequence s, int start, int before, int count) {}

                    @Override
                    public void afterTextChanged(Editable s) {
                        if (!isAdded() || settingsDao == null) {
                            return;
                        }
                        settingsDao.setSetting("gemini_api_key", s.toString().trim());
                    }
                });
    }

    private void persistAiFields() {
        if (!isAdded()) {
            return;
        }
        settingsDao.setSetting("gemini_api_key", etGeminiKey.getText().toString().trim());
        settingsDao.setSetting("ollama_ip", etOllamaIp.getText().toString().trim());
        settingsDao.setSetting("ollama_model", etOllamaModel.getText().toString().trim());
    }

    private void showEngineLayouts(boolean geminiMode) {
        layoutGemini.setVisibility(geminiMode ? View.VISIBLE : View.GONE);
        layoutOllama.setVisibility(geminiMode ? View.GONE : View.VISIBLE);
    }

    private void testOllama() {
        String ip = etOllamaIp.getText().toString().trim();
        settingsDao.setSetting("ollama_ip", ip);
        settingsDao.setSetting("ollama_model", etOllamaModel.getText().toString().trim());
        OllamaApiClient client = new OllamaApiClient("http://" + ip);
        tvOllamaStatus.setText("Testing...");
        client.testConnection(
                new PicoApiClient.PicoCallback<List<String>>() {
                    @Override
                    public void onSuccess(List<String> result) {
                        tvOllamaStatus.setText("? CONNECTED");
                        tvOllamaStatus.setTextColor(0xFF00FF88);
                    }

                    @Override
                    public void onError(String error) {
                        tvOllamaStatus.setText("? FAILED");
                        tvOllamaStatus.setTextColor(0xFFFF2020);
                    }
                });
    }

    private void startScan() {
        pbScanning.setVisibility(View.VISIBLE);
        deviceAdapter.updateDevices(new ArrayList<>());
        scanner.startScan(
                new DeviceScanner.ScanCallback() {
                    private final List<Device> found = new ArrayList<>();

                    @Override
                    public void onDeviceFound(String ip, String deviceType, String firmwareVersion) {
                        Device d = new Device();
                        d.setDeviceType(deviceType);
                        d.setIpAddress(ip);
                        d.setPort(80);
                        d.setFirmwareVersion(firmwareVersion);
                        d.setLastConnected(System.currentTimeMillis());
                        d.setConnected(false);
                        deviceDao.insertOrUpdateDevice(d);
                        found.add(d);
                        deviceAdapter.updateDevices(deviceDao.getAllDevices());
                        updateDeviceIpLabels();
                    }

                    @Override
                    public void onScanComplete(int devicesFound) {
                        pbScanning.setVisibility(View.GONE);
                        toast("Scan complete. Found " + devicesFound + " devices.");
                        deviceAdapter.updateDevices(deviceDao.getAllDevices());
                        updateDeviceIpLabels();
                    }

                    @Override
                    public void onScanProgress(int current, int total) {
                        // optional progress UI
                    }
                });
    }

    private void connectDevice(Device device) {
        boolean next = !device.isConnected();
        device.setConnected(next);
        device.setLastConnected(System.currentTimeMillis());
        deviceDao.insertOrUpdateDevice(device);
        deviceDao.setConnected(device.getDeviceType(), next);

        if (getActivity() instanceof MainActivity) {
            MainActivity activity = (MainActivity) getActivity();
            if (activity.getMarcBinder() != null) {
                PicoApiClient pico = new PicoApiClient(device.getBaseUrl());
                if (device.isHelmet()) {
                    activity.getMarcBinder().setHelmetClient(pico);
                } else if (Device.BIKE.equals(device.getDeviceType())) {
                    activity.getMarcBinder().setBikeClient(pico);
                }
            }
        }

        deviceAdapter.updateDevices(deviceDao.getAllDevices());
        updateDeviceIpLabels();
    }

    private static String normalizeManualIp(String raw) {
        if (raw == null) {
            return "";
        }
        String s = raw.trim();
        if (s.startsWith("http://")) {
            s = s.substring(7);
        }
        if (s.startsWith("https://")) {
            s = s.substring(8);
        }
        int slash = s.indexOf('/');
        if (slash >= 0) {
            s = s.substring(0, slash);
        }
        int colon = s.indexOf(':');
        if (colon > 0) {
            s = s.substring(0, colon);
        }
        return s.trim();
    }

    private void tryManualAddDevice() {
        String ip = normalizeManualIp(etManualIp.getText().toString());
        if (ip.isEmpty()) {
            return;
        }
        PicoApiClient client = new PicoApiClient("http://" + ip);
        client.identify(
                new PicoApiClient.PicoCallback<PicoApiClient.PicoStatus>() {
                    @Override
                    public void onSuccess(PicoApiClient.PicoStatus status) {
                        if (!isAdded()) {
                            return;
                        }
                        String dt = status.deviceType;
                        if (dt == null
                                || (!Device.HELMET.equals(dt) && !Device.BIKE.equals(dt))) {
                            toast("Device did not identify as MARC helmet or bike.");
                            return;
                        }
                        Device d = new Device();
                        d.setDeviceType(dt);
                        d.setIpAddress(ip);
                        d.setPort(80);
                        d.setFirmwareVersion(
                                status.firmwareVersion != null ? status.firmwareVersion : "1.0");
                        d.setLastConnected(System.currentTimeMillis());
                        d.setConnected(true);
                        deviceDao.insertOrUpdateDevice(d);
                        deviceDao.setConnected(dt, true);
                        updateDeviceIpLabels();
                        deviceAdapter.updateDevices(deviceDao.getAllDevices());
                        initPicoClient(d);
                        String label = Device.HELMET.equals(dt) ? "Helmet" : "Bike";
                        toast(label + " connected: " + ip);
                    }

                    @Override
                    public void onError(String error) {
                        if (!isAdded()) {
                            return;
                        }
                        toast("Cannot reach " + ip + " ? " + error);
                    }
                });
    }

    private void initPicoClient(Device device) {
        if (!(getActivity() instanceof MainActivity)) {
            return;
        }
        MainActivity activity = (MainActivity) getActivity();
        if (activity.getMarcBinder() != null) {
            PicoApiClient pico = new PicoApiClient(device.getBaseUrl());
            if (device.isHelmet()) {
                activity.getMarcBinder().setHelmetClient(pico);
            } else if (Device.BIKE.equals(device.getDeviceType())) {
                activity.getMarcBinder().setBikeClient(pico);
            }
        }
    }

    private void updateDeviceIpLabels() {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).refreshDashboardDeviceCards();
        }
    }

    private void pingDevice(Device device) {
        PicoApiClient client = new PicoApiClient(device.getBaseUrl());
        client.ping(
                new PicoApiClient.PicoCallback<Long>() {
                    @Override
                    public void onSuccess(Long result) {
                        deviceAdapter.updatePingResult(device.getDeviceType(), result);
                    }

                    @Override
                    public void onError(String error) {
                        toast("Ping failed: " + error);
                    }
                });
    }

    private void captureStanding() {
        getBikeStatus(
                status -> {
                    capturedStandingRoll = status.roll;
                    capturedStandingPitch = status.pitch;
                    tvStandingVal.setText(String.format(Locale.US, "%.1f deg", status.roll));
                    updateSaveCalibrationEnabled();
                });
    }

    private void captureRight() {
        getBikeStatus(
                status -> {
                    capturedRightRoll = status.roll;
                    tvRightVal.setText(String.format(Locale.US, "%.1f deg", status.roll));
                    updateSaveCalibrationEnabled();
                });
    }

    private void captureLeft() {
        getBikeStatus(
                status -> {
                    capturedLeftRoll = status.roll;
                    tvLeftVal.setText(String.format(Locale.US, "%.1f deg", status.roll));
                    updateSaveCalibrationEnabled();
                });
    }

    private void updateSaveCalibrationEnabled() {
        btnSaveCalibration.setEnabled(
                capturedStandingRoll != null && capturedRightRoll != null && capturedLeftRoll != null);
    }

    private void saveCalibration() {
        Device bike = deviceDao.getBike();
        if (bike == null) {
            toast("Bike device not found. Scan and connect first.");
            return;
        }
        if (capturedStandingRoll == null || capturedRightRoll == null || capturedLeftRoll == null) {
            toast("Capture all three calibration points first.");
            return;
        }

        Calibration calibration = new Calibration();
        Calibration existing = calibDao.getCalibrationForDevice(bike.getId());
        if (existing != null) {
            calibration.setId(existing.getId());
        }
        calibration.setDeviceId(bike.getId());
        calibration.setStandingRoll(capturedStandingRoll);
        calibration.setStandingPitch(capturedStandingPitch != null ? capturedStandingPitch : 0d);
        calibration.setMaxRightRoll(capturedRightRoll);
        calibration.setMaxLeftRoll(capturedLeftRoll);
        calibration.setCalibratedAt(System.currentTimeMillis());
        calibDao.insertOrUpdateCalibration(calibration);

        new PicoApiClient(bike.getBaseUrl())
                .calibrate(
                        capturedStandingRoll,
                        capturedStandingPitch != null ? capturedStandingPitch : 0d,
                        capturedLeftRoll,
                        capturedRightRoll,
                        new PicoApiClient.PicoCallback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean result) {
                                tvCalibStatus.setText("CALIBRATED");
                                tvCalibStatus.setTextColor(0xFF00FF88);
                                toast("Calibration saved. MARC knows your bike now.");
                            }

                            @Override
                            public void onError(String error) {
                                tvCalibStatus.setText("CALIBRATION SAVE FAILED");
                                tvCalibStatus.setTextColor(0xFFFF2020);
                                toast("Calibration save failed: " + error);
                            }
                        });
    }

    private interface StatusConsumer {
        void onStatus(PicoApiClient.PicoStatus status);
    }

    private void getBikeStatus(StatusConsumer consumer) {
        Device bike = deviceDao.getBike();
        if (bike == null) {
            toast("Bike device not found.");
            return;
        }
        new PicoApiClient(bike.getBaseUrl())
                .getStatus(
                        new PicoApiClient.PicoCallback<PicoApiClient.PicoStatus>() {
                            @Override
                            public void onSuccess(PicoApiClient.PicoStatus result) {
                                if (isAdded()) {
                                    consumer.onStatus(result);
                                }
                            }

                            @Override
                            public void onError(String error) {
                                toast("Bike status failed: " + error);
                            }
                        });
    }

    private void loadCalibrationStatus() {
        Device bike = deviceDao.getBike();
        if (bike == null) {
            tvCalibStatus.setText("NOT CALIBRATED");
            tvCalibStatus.setTextColor(0xFFFF2020);
            return;
        }
        Calibration c = calibDao.getCalibrationForDevice(bike.getId());
        if (c != null && c.isCalibrated()) {
            tvCalibStatus.setText("CALIBRATED");
            tvCalibStatus.setTextColor(0xFF00FF88);
            capturedStandingRoll = c.getStandingRoll();
            capturedStandingPitch = c.getStandingPitch();
            capturedRightRoll = c.getMaxRightRoll();
            capturedLeftRoll = c.getMaxLeftRoll();
            tvStandingVal.setText(String.format(Locale.US, "%.1f deg", c.getStandingRoll()));
            tvRightVal.setText(String.format(Locale.US, "%.1f deg", c.getMaxRightRoll()));
            tvLeftVal.setText(String.format(Locale.US, "%.1f deg", c.getMaxLeftRoll()));
            updateSaveCalibrationEnabled();
        } else {
            tvCalibStatus.setText("NOT CALIBRATED");
            tvCalibStatus.setTextColor(0xFFFF2020);
        }
    }

    @Override
    public void onPause() {
        persistAiFields();
        super.onPause();
    }

    @Override
    public void onDestroyView() {
        scanner.stopScan();
        super.onDestroyView();
    }

    private void toast(String msg) {
        if (!isAdded()) {
            return;
        }
        Toast.makeText(requireContext(), msg, Toast.LENGTH_SHORT).show();
    }
}
