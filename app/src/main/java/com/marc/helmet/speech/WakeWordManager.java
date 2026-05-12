package com.marc.helmet.speech;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;

import androidx.annotation.Nullable;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.marc.helmet.activities.CrashAlertActivity;

import java.util.ArrayList;
import java.util.Locale;

/**
 * Continuous wake listening using only {@link SpeechRecognizer}. Triggers when the recognized phrase
 * contains "marc" (case-insensitive).
 */
public class WakeWordManager {

    private static final long ERROR_RESTART_DELAY_MS = 500L;

    public interface WakeWordListener {
        void onWakeWordDetected();

        void onError(String error);
    }

    private final Context appContext;
    private final WakeWordListener listener;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    private volatile boolean listeningActive;

    /** True while STT has the mic after wake; blocks automatic restart until {@link #resumeListening()}. */
    private volatile boolean handedOff;

    @Nullable
    private SpeechRecognizer speechRecognizer;

    @Nullable
    private Intent listenIntent;

    @Nullable
    private Runnable restartRunnable;

    public WakeWordManager(Context context, WakeWordListener listener) {
        this.appContext = context.getApplicationContext();
        this.listener = listener;
    }

    /** Start the always-on recognition loop (or re-enter after {@link #resumeListening()}). */
    public void startListening() {
        cancelRestart();
        if (!SpeechRecognizer.isRecognitionAvailable(appContext)) {
            listeningActive = false;
            WakeWordListener l = listener;
            if (l != null) {
                mainHandler.post(() -> l.onError("Speech recognition not available"));
            }
            return;
        }
        listeningActive = true;
        beginListenSession();
    }

    /**
     * Stop for handoff to in-app STT: cancel recognizer, clear listening flag, do not schedule
     * restart.
     */
    public void stopListeningForHandoff() {
        handedOff = true;
        listeningActive = false;
        cancelRestart();
        if (speechRecognizer != null) {
            try {
                speechRecognizer.stopListening();
            } catch (Exception ignored) {
            }
            try {
                speechRecognizer.cancel();
            } catch (Exception ignored) {
            }
            try {
                speechRecognizer.destroy();
            } catch (Exception ignored) {
            }
            speechRecognizer = null;
        }
    }

    /** Resume the continuous loop after Marc STT finishes. */
    public void resumeListening() {
        handedOff = false;
        startListening();
    }

    public void destroy() {
        stopListeningForHandoff();
    }

    public boolean isListening() {
        return listeningActive;
    }

    private void beginListenSession() {
        if (!listeningActive) {
            return;
        }
        ensureListenIntent();
        ensureSpeechRecognizer();
        try {
            if (speechRecognizer != null) {
                speechRecognizer.startListening(listenIntent);
            }
        } catch (Exception e) {
            scheduleRestart(ERROR_RESTART_DELAY_MS);
        }
    }

    private void ensureListenIntent() {
        if (listenIntent != null) {
            return;
        }
        listenIntent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        listenIntent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        listenIntent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, "en-US");
        listenIntent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, false);
    }

    private void ensureSpeechRecognizer() {
        if (speechRecognizer != null) {
            speechRecognizer.setRecognitionListener(buildListener());
            return;
        }
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(appContext);
        speechRecognizer.setRecognitionListener(buildListener());
    }

    private RecognitionListener buildListener() {
        return new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {}

            @Override
            public void onBeginningOfSpeech() {}

            @Override
            public void onRmsChanged(float rmsdB) {}

            @Override
            public void onBufferReceived(byte[] buffer) {}

            @Override
            public void onEndOfSpeech() {}

            @Override
            public void onError(int error) {
                if (!listeningActive || handedOff) {
                    return;
                }
                if (error == SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS) {
                    WakeWordListener l = listener;
                    if (l != null) {
                        mainHandler.post(() -> l.onError("RECORD_AUDIO permission required"));
                    }
                    return;
                }
                if (error == SpeechRecognizer.ERROR_CLIENT) {
                    return;
                }
                scheduleRestart(ERROR_RESTART_DELAY_MS);
            }

            @Override
            public void onResults(Bundle results) {
                if (!listeningActive || handedOff) {
                    return;
                }
                String text = extractBestText(results);
                if (text == null) {
                    scheduleRestart(200);
                    return;
                }
                String lower = text.toLowerCase(Locale.ROOT).trim();
                if (lower.contains("cancel") || lower.contains("marc cancel")) {
                    mainHandler.post(
                            () -> {
                                Intent cancelIntent =
                                        new Intent(CrashAlertActivity.ACTION_VOICE_CANCEL_EMERGENCY);
                                LocalBroadcastManager.getInstance(appContext)
                                        .sendBroadcast(cancelIntent);
                                WakeWordListener l = listener;
                                if (l != null) {
                                    l.onWakeWordDetected();
                                }
                            });
                    scheduleRestart(500);
                    return;
                }
                if (isWakePhrase(lower)) {
                    mainHandler.post(
                            () -> {
                                WakeWordListener l = listener;
                                if (l != null) {
                                    l.onWakeWordDetected();
                                }
                            });
                }
                scheduleRestart(200);
            }

            @Override
            public void onPartialResults(Bundle partialResults) {}

            @Override
            public void onEvent(int eventType, Bundle params) {}
        };
    }

    private static boolean isWakePhrase(String lower) {
        return lower.contains("marc");
    }

    @Nullable
    private static String extractBestText(@Nullable Bundle bundle) {
        if (bundle == null) {
            return null;
        }
        ArrayList<String> list = bundle.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        if (list == null || list.isEmpty()) {
            return null;
        }
        return list.get(0);
    }

    private void scheduleRestart(long delayMs) {
        cancelRestart();
        restartRunnable =
                () -> {
                    restartRunnable = null;
                    if (!listeningActive) {
                        return;
                    }
                    beginListenSession();
                };
        mainHandler.postDelayed(restartRunnable, delayMs);
    }

    private void cancelRestart() {
        if (restartRunnable != null) {
            mainHandler.removeCallbacks(restartRunnable);
            restartRunnable = null;
        }
    }
}
