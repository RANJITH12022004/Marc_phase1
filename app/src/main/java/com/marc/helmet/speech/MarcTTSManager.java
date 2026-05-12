package com.marc.helmet.speech;

import android.annotation.SuppressLint;
import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioDeviceInfo;
import android.media.AudioManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import java.util.Locale;
import java.util.Random;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Text-to-speech helper for MARC with optional Bluetooth routing and alarm-stream emergency speech.
 */
public class MarcTTSManager {

    private static final String[] LOADING_PHRASES = {
        "MARC is thinking...",
        "Analyzing, hold on...",
        "Checking database...",
        "Running parallel search...",
        "Cross-referencing medical data...",
        "Scanning bike diagnostics...",
        "Processing request..."
    };

    private static final String UTTERANCE_EMERGENCY = "marc_emergency";
    private static final String UTTERANCE_REPLY = "marc_reply";

    public interface OnReadyListener {
        void onReady();

        void onError(String msg);
    }

    private final Context appContext;
    private final OnReadyListener readyListener;
    private final Random random = new Random();

    private volatile TextToSpeech tts;
    private volatile boolean ready;
    private final AtomicBoolean shuttingDown = new AtomicBoolean(false);

    private int savedAlarmVolume = -1;

    public MarcTTSManager(Context context, OnReadyListener listener) {
        this.appContext = context.getApplicationContext();
        this.readyListener = listener;
        this.tts =
                new TextToSpeech(
                        appContext,
                        status -> {
                            if (shuttingDown.get()) {
                                return;
                            }
                            if (status != TextToSpeech.SUCCESS) {
                                if (readyListener != null) {
                                    readyListener.onError("TTS init failed");
                                }
                                return;
                            }
                            TextToSpeech engine = tts;
                            if (engine == null) {
                                return;
                            }
                            Locale locale = Locale.forLanguageTag("en-IN");
                            int langResult = engine.setLanguage(locale);
                            if (langResult == TextToSpeech.LANG_MISSING_DATA
                                    || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                                langResult = engine.setLanguage(Locale.US);
                            }
                            if (langResult == TextToSpeech.LANG_MISSING_DATA
                                    || langResult == TextToSpeech.LANG_NOT_SUPPORTED) {
                                if (readyListener != null) {
                                    readyListener.onError("Language not supported");
                                }
                                return;
                            }
                            engine.setPitch(0.9f);
                            engine.setSpeechRate(0.9f);
                            applyAudioRouting(engine);
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH_MR1) {
                                engine.setOnUtteranceProgressListener(
                                        new UtteranceProgressListener() {
                                            @Override
                                            public void onStart(String utteranceId) {
                                            }

                                            @Override
                                            public void onDone(String utteranceId) {
                                                if (UTTERANCE_EMERGENCY.equals(utteranceId)) {
                                                    restoreAlarmVolume();
                                                }
                                            }

                                            @Override
                                            public void onError(String utteranceId) {
                                                if (UTTERANCE_EMERGENCY.equals(utteranceId)) {
                                                    restoreAlarmVolume();
                                                }
                                            }
                                        });
                            }
                            ready = true;
                            if (readyListener != null) {
                                readyListener.onReady();
                            }
                        });
    }

    public boolean isReady() {
        return ready && tts != null;
    }

    public void speak(String text) {
        TextToSpeech engine = tts;
        if (engine == null || !ready) {
            return;
        }
        applyAudioRouting(engine);
        String s = text == null ? "" : text;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Bundle params = new Bundle();
            engine.speak(s, TextToSpeech.QUEUE_FLUSH, params, UTTERANCE_REPLY);
        } else {
            engine.speak(s, TextToSpeech.QUEUE_FLUSH, null);
        }
    }

    public void speakEmergency(String text) {
        TextToSpeech engine = tts;
        if (engine == null || !ready) {
            return;
        }
        AudioManager am = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
        if (am != null) {
            savedAlarmVolume = am.getStreamVolume(AudioManager.STREAM_ALARM);
            int max = am.getStreamMaxVolume(AudioManager.STREAM_ALARM);
            am.setStreamVolume(AudioManager.STREAM_ALARM, max, 0);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            engine.setAudioAttributes(
                    new AudioAttributes.Builder()
                            .setUsage(AudioAttributes.USAGE_ALARM)
                            .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                            .build());
            Bundle params = new Bundle();
            engine.speak(
                    text == null ? "" : text,
                    TextToSpeech.QUEUE_FLUSH,
                    params,
                    UTTERANCE_EMERGENCY);
        } else {
            engine.speak(text == null ? "" : text, TextToSpeech.QUEUE_FLUSH, null);
            new Handler(Looper.getMainLooper())
                    .postDelayed(this::restoreAlarmVolume, 5000L);
        }
    }

    public void speakLoadingPhrase() {
        if (LOADING_PHRASES.length == 0) {
            return;
        }
        speak(LOADING_PHRASES[random.nextInt(LOADING_PHRASES.length)]);
    }

    public void speakOllamaLoading() {
        speak("MARC BACK is thinking locally...");
    }

    public void countDown(int seconds) {
        speak(seconds + " seconds...");
    }

    public void stop() {
        TextToSpeech engine = tts;
        if (engine != null) {
            engine.stop();
        }
        restoreAlarmVolume();
    }

    public void shutdown() {
        shuttingDown.set(true);
        ready = false;
        TextToSpeech engine = tts;
        tts = null;
        if (engine != null) {
            engine.stop();
            engine.shutdown();
        }
        restoreAlarmVolume();
    }

    private void restoreAlarmVolume() {
        if (savedAlarmVolume < 0) {
            return;
        }
        AudioManager am = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
        if (am != null) {
            am.setStreamVolume(AudioManager.STREAM_ALARM, savedAlarmVolume, 0);
        }
        savedAlarmVolume = -1;
        TextToSpeech engine = tts;
        if (engine != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            applyAudioRouting(engine);
        }
    }

    @SuppressLint("MissingPermission")
    private void applyAudioRouting(@Nullable TextToSpeech engine) {
        if (engine == null || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            return;
        }
        boolean bluetoothAudio = isBluetoothHeadsetOrA2dpConnected();

        AudioAttributes.Builder b =
                new AudioAttributes.Builder().setContentType(AudioAttributes.CONTENT_TYPE_SPEECH);
        if (bluetoothAudio) {
            b.setUsage(AudioAttributes.USAGE_MEDIA);
        } else {
            b.setUsage(AudioAttributes.USAGE_ASSISTANT);
        }
        engine.setAudioAttributes(b.build());

        AudioManager am = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
        if (am != null) {
            am.setSpeakerphoneOn(!bluetoothAudio);
        }
    }

    @SuppressLint("MissingPermission")
    private boolean isBluetoothHeadsetOrA2dpConnected() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            AudioManager am = (AudioManager) appContext.getSystemService(Context.AUDIO_SERVICE);
            if (am != null) {
                AudioDeviceInfo[] devices = am.getDevices(AudioManager.GET_DEVICES_OUTPUTS);
                for (AudioDeviceInfo d : devices) {
                    int t = d.getType();
                    if (t == AudioDeviceInfo.TYPE_BLUETOOTH_A2DP
                            || t == AudioDeviceInfo.TYPE_BLUETOOTH_SCO) {
                        return true;
                    }
                }
            }
        }

        BluetoothManager bm =
                (BluetoothManager) appContext.getSystemService(Context.BLUETOOTH_SERVICE);
        BluetoothAdapter adapter = bm != null ? bm.getAdapter() : null;
        if (adapter == null || !adapter.isEnabled()) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
                && ContextCompat.checkSelfPermission(appContext, Manifest.permission.BLUETOOTH_CONNECT)
                        != PackageManager.PERMISSION_GRANTED) {
            return false;
        }
        try {
            int headset = adapter.getProfileConnectionState(BluetoothProfile.HEADSET);
            int a2dp = adapter.getProfileConnectionState(BluetoothProfile.A2DP);
            return headset == BluetoothProfile.STATE_CONNECTED
                    || a2dp == BluetoothProfile.STATE_CONNECTED;
        } catch (SecurityException ignored) {
            return false;
        }
    }
}
