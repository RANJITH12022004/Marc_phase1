package com.marc.helmet.speech;

import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.RecognitionListener;
import android.speech.RecognizerIntent;
import android.speech.SpeechRecognizer;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;

/**
 * On-device speech-to-text using Android {@link SpeechRecognizer} (no cloud STT).
 *
 * <p>Error 11 (API 31+ {@link SpeechRecognizer#ERROR_SERVER_DISCONNECTED}), {@link
 * SpeechRecognizer#ERROR_RECOGNIZER_BUSY}, and {@link SpeechRecognizer#ERROR_TOO_MANY_REQUESTS}
 * often happen when Marc STT starts immediately after {@link WakeWordManager} tears down —
 * Google's recognizer is still releasing. We delay the first start and retry a few times with
 * backoff.
 */
public class GoogleSTTManager {

    private static final String TAG_STT = "MARC_STT";

    private static final String LANGUAGE_TAG = "en-US";
    private static final int COMPLETE_SILENCE_MS = 1500;
    private static final int MAX_LISTEN_MS = 8000;

    /** Let the system finish releasing the wake-word {@link SpeechRecognizer} before we open STT. */
    private static final long DELAY_BEFORE_FIRST_START_MS = 320L;

    private static final int MAX_RECOVERABLE_RETRIES = 3;

    public interface STTCallback {
        void onResult(String text);

        void onPartialResult(String partial);

        void onError(String error);

        void onListeningStarted();

        void onListeningStopped();
    }

    private final Context appContext;
    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Nullable private SpeechRecognizer recognizer;

    @Nullable private STTCallback activeCallback;

    @Nullable private Runnable maxListenTimeout;

    /** Cancels superseded deferred / retry chains when a new {@link #startListening} runs. */
    private int listenSessionGeneration;

    @Nullable private Runnable deferredStartRunnable;

    public GoogleSTTManager(Context context) {
        this.appContext = context.getApplicationContext();
    }

    public static boolean isAvailable(Context context) {
        return SpeechRecognizer.isRecognitionAvailable(context.getApplicationContext());
    }

    public void startListening(STTCallback callback) {
        if (callback == null) {
            return;
        }
        if (!isAvailable(appContext)) {
            callback.onError("Speech recognition not available");
            return;
        }

        if (deferredStartRunnable != null) {
            mainHandler.removeCallbacks(deferredStartRunnable);
            deferredStartRunnable = null;
        }
        listenSessionGeneration++;
        final int sessionId = listenSessionGeneration;

        deferredStartRunnable =
                () -> {
                    deferredStartRunnable = null;
                    if (sessionId != listenSessionGeneration) {
                        return;
                    }
                    openListeningSession(callback, 0, sessionId);
                };
        mainHandler.postDelayed(deferredStartRunnable, DELAY_BEFORE_FIRST_START_MS);
    }

    private void openListeningSession(STTCallback callback, int attempt, int sessionId) {
        if (callback == null || sessionId != listenSessionGeneration) {
            return;
        }

        cancelMaxListenTimeout();
        teardownRecognizerQuietly();

        activeCallback = callback;
        Intent intent = buildListenIntent();

        SpeechRecognizer sr = SpeechRecognizer.createSpeechRecognizer(appContext);
        recognizer = sr;
        sr.setRecognitionListener(makeListener(callback, attempt, sessionId));

        try {
            sr.startListening(intent);
            Log.d(TAG_STT, "startListening attempt=" + attempt + " session=" + sessionId);
        } catch (Exception ex) {
            Log.w(TAG_STT, "startListening threw", ex);
            if (attempt < MAX_RECOVERABLE_RETRIES - 1
                    && sessionId == listenSessionGeneration) {
                scheduleRetry(callback, attempt, sessionId);
                return;
            }
            teardownRecognizerQuietly();
            callback.onError(
                    ex.getMessage() != null ? ex.getMessage() : "Could not start speech recognition");
            callback.onListeningStopped();
            return;
        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            maxListenTimeout =
                    () -> {
                        if (recognizer != null) {
                            try {
                                recognizer.stopListening();
                            } catch (Exception ignored) {
                            }
                        }
                    };
            mainHandler.postDelayed(maxListenTimeout, MAX_LISTEN_MS);
        }
    }

    private Intent buildListenIntent() {
        Intent intent = new Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH);
        intent.putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM);
        intent.putExtra(RecognizerIntent.EXTRA_LANGUAGE, LANGUAGE_TAG);
        intent.putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true);
        intent.putExtra(
                RecognizerIntent.EXTRA_SPEECH_INPUT_COMPLETE_SILENCE_LENGTH_MILLIS,
                COMPLETE_SILENCE_MS);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            intent.putExtra("android.speech.extra.SPEECH_INPUT_MAXIMUM_LENGTH_MILLIS", MAX_LISTEN_MS);
        }
        return intent;
    }

    private RecognitionListener makeListener(
            final STTCallback callback, final int attempt, final int sessionId) {
        return new RecognitionListener() {
            @Override
            public void onReadyForSpeech(Bundle params) {
                STTCallback cb = activeCallback;
                if (cb != null && sessionId == listenSessionGeneration) {
                    cb.onListeningStarted();
                }
            }

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
                cancelMaxListenTimeout();
                Log.w(TAG_STT, "onError code=" + error + " attempt=" + attempt + " msg=" + errorCodeToMessage(error));
                if (sessionId != listenSessionGeneration) {
                    return;
                }
                if (shouldRetrySpeechError(error) && attempt < MAX_RECOVERABLE_RETRIES - 1) {
                    scheduleRetry(callback, attempt, sessionId);
                    return;
                }
                STTCallback cb = activeCallback;
                if (cb != null) {
                    cb.onError(errorCodeToMessage(error));
                    cb.onListeningStopped();
                }
            }

            @Override
            public void onResults(Bundle results) {
                cancelMaxListenTimeout();
                if (sessionId != listenSessionGeneration) {
                    return;
                }
                STTCallback cb = activeCallback;
                if (cb == null) {
                    return;
                }
                String text = extractBestText(results);
                cb.onResult(text != null ? text : "");
                cb.onListeningStopped();
            }

            @Override
            public void onPartialResults(Bundle partialResults) {
                if (sessionId != listenSessionGeneration) {
                    return;
                }
                STTCallback cb = activeCallback;
                if (cb == null) {
                    return;
                }
                String partial = extractBestText(partialResults);
                if (partial != null && !partial.isEmpty()) {
                    cb.onPartialResult(partial);
                }
            }

            @Override
            public void onEvent(int eventType, Bundle params) {}
        };
    }

    private void scheduleRetry(STTCallback callback, int attempt, int sessionId) {
        teardownRecognizerQuietly();
        long backoffMs = 280L + (attempt + 1L) * 220L;
        Log.d(TAG_STT, "retry STT after " + backoffMs + "ms (was attempt " + attempt + ")");
        mainHandler.postDelayed(
                () -> openListeningSession(callback, attempt + 1, sessionId), backoffMs);
    }

    private static boolean shouldRetrySpeechError(int error) {
        if (error == SpeechRecognizer.ERROR_RECOGNIZER_BUSY) {
            return true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                && error == SpeechRecognizer.ERROR_TOO_MANY_REQUESTS) {
            return true;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && error == SpeechRecognizer.ERROR_SERVER_DISCONNECTED) {
            return true;
        }
        return false;
    }

    private void teardownRecognizerQuietly() {
        if (recognizer != null) {
            try {
                recognizer.stopListening();
            } catch (Exception ignored) {
            }
            try {
                recognizer.cancel();
            } catch (Exception ignored) {
            }
            try {
                recognizer.destroy();
            } catch (Exception ignored) {
            }
            recognizer = null;
        }
    }

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

    private static String errorCodeToMessage(int error) {
        switch (error) {
            case SpeechRecognizer.ERROR_AUDIO:
                return "Audio recording error";
            case SpeechRecognizer.ERROR_CLIENT:
                return "Client side error";
            case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                return "Insufficient permissions";
            case SpeechRecognizer.ERROR_NETWORK:
                return "Network error";
            case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                return "Network timeout";
            case SpeechRecognizer.ERROR_NO_MATCH:
                return "No recognition match";
            case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                return "Recognition service busy";
            case SpeechRecognizer.ERROR_SERVER:
                return "Server error";
            case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                return "No speech input";
            default:
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
                        && error == SpeechRecognizer.ERROR_TOO_MANY_REQUESTS) {
                    return "Too many recognition requests — try again in a moment";
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                        && error == SpeechRecognizer.ERROR_SERVER_DISCONNECTED) {
                    return "Speech service disconnected (wait a moment and try again)";
                }
                return "Recognition error (" + error + ")";
        }
    }

    public void stopListening() {
        stopListeningInternal(true);
    }

    private void stopListeningInternal(boolean notifyStopped) {
        cancelMaxListenTimeout();
        if (deferredStartRunnable != null) {
            mainHandler.removeCallbacks(deferredStartRunnable);
            deferredStartRunnable = null;
        }
        listenSessionGeneration++;
        if (recognizer != null) {
            try {
                recognizer.stopListening();
            } catch (Exception ignored) {
            }
        }
        if (notifyStopped && activeCallback != null) {
            activeCallback.onListeningStopped();
        }
    }

    private void cancelMaxListenTimeout() {
        if (maxListenTimeout != null) {
            mainHandler.removeCallbacks(maxListenTimeout);
            maxListenTimeout = null;
        }
    }

    public void destroy() {
        cancelMaxListenTimeout();
        if (deferredStartRunnable != null) {
            mainHandler.removeCallbacks(deferredStartRunnable);
            deferredStartRunnable = null;
        }
        listenSessionGeneration++;
        activeCallback = null;
        teardownRecognizerQuietly();
    }
}
