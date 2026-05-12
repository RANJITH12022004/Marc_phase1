package com.marc.helmet.activities;

import android.animation.ObjectAnimator;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.marc.helmet.R;
import com.marc.helmet.database.DatabaseHelper;
import com.marc.helmet.database.DeviceDao;
import com.marc.helmet.models.Device;
import com.marc.helmet.network.pico.PicoApiClient;
import com.marc.helmet.services.EmergencyService;
import com.marc.helmet.speech.MarcTTSManager;

/**
 * Lock-screen overlay for crash countdown and emergency dispatch. Cancels via button or voice
 * broadcast {@link #ACTION_VOICE_CANCEL_EMERGENCY}.
 */
public class CrashAlertActivity extends AppCompatActivity {

    public static final String ACTION_VOICE_CANCEL_EMERGENCY =
            "com.marc.helmet.VOICE_CANCEL_EMERGENCY";

    public static final String EXTRA_CRASH_LAT = "crash_lat";
    public static final String EXTRA_CRASH_LNG = "crash_lng";

    private TextView tvCountdown;
    private TextView tvCrashTitle;
    private Button btnCancel;
    private android.view.View vFlash;

    private final Handler countdownHandler = new Handler(Looper.getMainLooper());
    private CountDownTimer countDownTimer;
    private int countdownSeconds = 10;
    private boolean isCancelled;
    private boolean countdownRunning;
    private boolean emergencyDispatched;
    private boolean acknowledgedSafe;

    private ObjectAnimator flashAnimator;
    private EmergencyService emergencyService;
    private MarcTTSManager ttsManager;

    private double crashLat;
    private double crashLng;

    private final BroadcastReceiver voiceCancelReceiver =
            new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {
                    if (ACTION_VOICE_CANCEL_EMERGENCY.equals(intent.getAction())) {
                        cancelEmergency();
                    }
                }
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true);
            setTurnScreenOn(true);
        } else {
            getWindow()
                    .addFlags(
                            WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                                    | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON);
        }
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        getWindow()
                .setFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_crash_alert);

        Intent in = getIntent();
        crashLat = in.getDoubleExtra(EXTRA_CRASH_LAT, 0.0);
        crashLng = in.getDoubleExtra(EXTRA_CRASH_LNG, 0.0);

        tvCountdown = findViewById(R.id.tv_countdown);
        tvCrashTitle = findViewById(R.id.tv_crash_title);
        btnCancel = findViewById(R.id.btn_cancel);
        vFlash = findViewById(R.id.v_flash);

        flashAnimator = ObjectAnimator.ofFloat(vFlash, "alpha", 0f, 0.15f);
        flashAnimator.setDuration(500);
        flashAnimator.setRepeatMode(ObjectAnimator.REVERSE);
        flashAnimator.setRepeatCount(ObjectAnimator.INFINITE);
        flashAnimator.start();

        emergencyService = new EmergencyService(this);

        ttsManager =
                new MarcTTSManager(
                        this,
                        new MarcTTSManager.OnReadyListener() {
                            @Override
                            public void onReady() {
                                if (ttsManager != null && ttsManager.isReady()) {
                                    ttsManager.speak(getString(R.string.crash_detected));
                                }
                            }

                            @Override
                            public void onError(String msg) {
                            }
                        });

        LocalBroadcastManager.getInstance(this)
                .registerReceiver(
                        voiceCancelReceiver, new IntentFilter(ACTION_VOICE_CANCEL_EMERGENCY));

        btnCancel.setOnClickListener(v -> cancelEmergency());

        startCountdown();
    }

    private void startCountdown() {
        countdownRunning = true;
        countdownSeconds = 10;
        tvCountdown.setText("10");
        countDownTimer =
                new CountDownTimer(10_000L, 1_000L) {
                    @Override
                    public void onTick(long millisUntilFinished) {
                        if (isCancelled) {
                            return;
                        }
                        long sec = (millisUntilFinished + 999L) / 1000L;
                        tvCountdown.setText(String.valueOf(sec));
                    }

                    @Override
                    public void onFinish() {
                        if (!isCancelled) {
                            tvCountdown.setText("0");
                            countdownRunning = false;
                            executeEmergency();
                        }
                    }
                };
        countDownTimer.start();
    }

    void cancelEmergency() {
        if (isCancelled || emergencyDispatched) {
            return;
        }
        isCancelled = true;
        countdownRunning = false;
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }
        countdownHandler.removeCallbacksAndMessages(null);

        emergencyService.cancelEmergency();

        if (flashAnimator != null) {
            flashAnimator.cancel();
            vFlash.setAlpha(0f);
        }

        final String spoken = getString(R.string.emergency_cancelled);
        Runnable finishTask = this::finish;
        if (ttsManager != null && ttsManager.isReady()) {
            ttsManager.speak(spoken);
            countdownHandler.postDelayed(finishTask, 2200);
        } else {
            countdownHandler.postDelayed(finishTask, 400);
        }
    }

    void executeEmergency() {
        if (isCancelled || emergencyDispatched) {
            return;
        }
        emergencyDispatched = true;
        countdownRunning = false;
        if (countDownTimer != null) {
            countDownTimer.cancel();
            countDownTimer = null;
        }

        if (flashAnimator != null) {
            flashAnimator.cancel();
            vFlash.setAlpha(0f);
        }

        emergencyService.dispatchEmergencyNow(
                crashLat,
                crashLng,
                new EmergencyService.EmergencyListener() {
                    @Override
                    public void onCountdownTick(int secondsRemaining) {
                    }

                    @Override
                    public void onEmergencyStarted() {
                    }

                    @Override
                    public void onEmergencyCancelled() {
                    }

                    @Override
                    public void onEmergencyCompleted() {
                        runOnUiThread(
                                () -> {
                                    tvCrashTitle.setText("CALL PLACED");
                                    btnCancel.setText("● I AM SAFE");
                                    btnCancel.setOnClickListener(
                                            v -> {
                                                acknowledgedSafe = true;
                                                resetCrashFlag();
                                                finish();
                                            });
                                });
                    }
                });
    }

    void resetCrashFlag() {
        Device bike = new DeviceDao(DatabaseHelper.getInstance(this)).getBike();
        if (bike == null) {
            return;
        }
        String base = bike.getBaseUrl();
        new PicoApiClient(base)
                .resetCrashFlag(
                        new PicoApiClient.PicoCallback<Boolean>() {
                            @Override
                            public void onSuccess(Boolean result) {
                            }

                            @Override
                            public void onError(String error) {
                            }
                        });
    }

    @Override
    public void onBackPressed() {
        if (isCancelled || acknowledgedSafe) {
            super.onBackPressed();
            return;
        }
        if (countdownRunning || emergencyDispatched) {
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void onDestroy() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(voiceCancelReceiver);
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
        countdownHandler.removeCallbacksAndMessages(null);
        if (flashAnimator != null) {
            flashAnimator.cancel();
        }
        if (ttsManager != null) {
            ttsManager.shutdown();
        }
        super.onDestroy();
    }
}
