package com.marc.helmet.fragments.marc;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
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
import com.marc.helmet.adapters.ChatMessageAdapter;
import com.marc.helmet.database.DatabaseHelper;
import com.marc.helmet.database.DeviceDao;
import com.marc.helmet.database.SettingsDao;
import com.marc.helmet.network.ai.GeminiApiClient;
import com.marc.helmet.network.ai.MarcCoreEngine;
import com.marc.helmet.models.Device;
import com.marc.helmet.network.ai.OllamaApiClient;
import com.marc.helmet.speech.GoogleSTTManager;
import com.marc.helmet.speech.MarcTTSManager;
import com.marc.helmet.utils.MarcCoreUiHelper;
import com.marc.helmet.utils.MarcLocalResponder;
import com.marc.helmet.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

/**
 * MARC AI surface: Gemini / Ollama chat, Marc Core mode, voice and text entry.
 */
public class MarcFragment extends Fragment {

    /**
     * Max prior chat messages sent to Gemini/Ollama per request (excluding the current user turn). The UI
     * keeps the full transcript, but forwarding the entire thread on every API call blows up tokens and
     * trips per-minute quotas — a common cause of RESOURCE_EXHAUSTED even with new API keys.
     */
    private static final int MAX_API_HISTORY_MESSAGES = 24;

    // Views
    private RecyclerView rvChat;
    private EditText etMessage;
    private TextView tvMarcState;
    private TextView tvListeningText;
    private TextView tvLoadingPhrase;
    private TextView tvMarcLabel;
    private FrameLayout flOrbContainer;
    private View ivOrb;
    private Button btnTapToSpeak;
    private Button btnSend;
    private LinearLayout layoutVoiceMode;
    private LinearLayout layoutTextMode;
    private ToggleButton toggleMode;

    // AI
    private GeminiApiClient geminiClient;
    private OllamaApiClient ollamaClient;
    private MarcCoreEngine marcCoreEngine;
    private ChatMessageAdapter adapter;

    // Speech
    private MarcTTSManager ttsManager;
    private GoogleSTTManager sttManager;

    // DB
    private SettingsDao settingsDao;

    // State
    private boolean isVoiceMode = true;
    private boolean marcCoreActive = false;
    private boolean awaitingCoreConfirmation = false;
    private boolean suppressToggleCallback;
    /** Last partial STT line; used if final {@code onResults} is empty (common on some devices). */
    private String pendingVoicePartial = "";
    /**
     * Reserved for scripted voice demos / future HUD states.
     */
    @SuppressWarnings("unused")
    private int voiceDemoState = 0;

    static final String[] LOADING_PHRASES =
            new String[] {
                "MARC is thinking...",
                "Analyzing, hold on...",
                "Checking database...",
                "Running parallel search...",
                "Cross-referencing medical data...",
                "Scanning bike diagnostics...",
                "Processing request..."
            };

    static final String[] CORE_LOADING_PHRASES =
            new String[] {
                "Analyzing your stupidity...",
                "Loading maximum destruction...",
                "Calculating damage...",
                "Preparing the roast...",
                "Sharpening vocabulary..."
            };

    private static final String[] WAKE_PREFIXES =
            new String[] {"hey marc", "hi marc", "hello marc", "okay marc", "ok marc"};

    private final Random random = new Random();

    /** Delays {@link MainActivity#restartWakeWord()} after STT so TTS / mic handoff can settle. */
    private final Handler wakeRestartHandler = new Handler(Looper.getMainLooper());

    /** After wake-word handoff, wait briefly so the wake SpeechRecognizer fully tears down. */
    private final Runnable pendingStartVoiceRunnable =
            () -> {
                if (isAdded()) {
                    startVoiceListening();
                }
            };

    @Nullable
    @Override
    public View onCreateView(
            @NonNull LayoutInflater inflater,
            @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_marc, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View root, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(root, savedInstanceState);

        rvChat = root.findViewById(R.id.rv_chat);
        etMessage = root.findViewById(R.id.et_message);
        tvMarcState = root.findViewById(R.id.tv_marc_state);
        tvListeningText = root.findViewById(R.id.tv_listening_text);
        tvLoadingPhrase = root.findViewById(R.id.tv_loading_phrase);
        tvMarcLabel = root.findViewById(R.id.tv_marc_label);
        flOrbContainer = root.findViewById(R.id.fl_orb_container);
        ivOrb = root.findViewById(R.id.iv_orb);
        btnTapToSpeak = root.findViewById(R.id.btn_tap_to_speak);
        btnSend = root.findViewById(R.id.btn_send);
        layoutVoiceMode = root.findViewById(R.id.layout_voice_mode);
        layoutTextMode = root.findViewById(R.id.layout_text_mode);
        toggleMode = root.findViewById(R.id.toggle_mode);

        settingsDao =
                new SettingsDao(DatabaseHelper.getInstance(requireContext().getApplicationContext()));
        initAiClients();

        LinearLayoutManager layoutManager =
                new LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false);
        layoutManager.setStackFromEnd(true);
        adapter = new ChatMessageAdapter();
        rvChat.setLayoutManager(layoutManager);
        rvChat.setAdapter(adapter);
        adapter.setRecyclerView(rvChat);

        suppressToggleCallback = true;
        toggleMode.setChecked(false);
        isVoiceMode = true;
        layoutVoiceMode.setVisibility(View.VISIBLE);
        layoutTextMode.setVisibility(View.GONE);
        layoutVoiceMode.setAlpha(1f);
        layoutTextMode.setAlpha(0f);
        suppressToggleCallback = false;

        toggleMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (suppressToggleCallback) {
                return;
            }
            boolean textMode = isChecked;
            isVoiceMode = !textMode;
            crossFadeMode(layoutVoiceMode, layoutTextMode, textMode);
        });

        MarcCoreUiHelper.setOrbNormal(ivOrb, requireContext(), tvMarcState, tvListeningText);
        tvMarcState.setText("STANDBY // SAY HEY MARC");

        if (getActivity() instanceof MainActivity) {
            ttsManager = ((MainActivity) getActivity()).getTtsManager();
        }

        btnTapToSpeak.setOnClickListener(v -> startVoiceListening());

        btnSend.setOnClickListener(v -> sendTextMessage(etMessage.getText().toString().trim()));

        etMessage.setOnEditorActionListener(
                (v, actionId, event) -> {
                    if (actionId == EditorInfo.IME_ACTION_SEND) {
                        sendTextMessage(etMessage.getText().toString().trim());
                        return true;
                    }
                    return false;
                });

        flOrbContainer.setOnLongClickListener(
                v -> {
                    if (marcCoreActive) {
                        deactivateMarcCore();
                        return true;
                    }
                    triggerCoreWarning();
                    return true;
                });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (settingsDao != null) {
            initAiClients();
        }
    }

    private void crossFadeMode(LinearLayout voiceLayout, LinearLayout textLayout, boolean showText) {
        if (showText) {
            voiceLayout.animate().cancel();
            textLayout.animate().cancel();
            voiceLayout
                    .animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction(
                            () -> {
                                voiceLayout.setVisibility(View.GONE);
                                textLayout.setAlpha(0f);
                                textLayout.setVisibility(View.VISIBLE);
                                textLayout.animate().alpha(1f).setDuration(200).start();
                            })
                    .start();
        } else {
            textLayout.animate().cancel();
            voiceLayout.animate().cancel();
            textLayout
                    .animate()
                    .alpha(0f)
                    .setDuration(200)
                    .withEndAction(
                            () -> {
                                textLayout.setVisibility(View.GONE);
                                voiceLayout.setAlpha(0f);
                                voiceLayout.setVisibility(View.VISIBLE);
                                voiceLayout.animate().alpha(1f).setDuration(200).start();
                            })
                    .start();
        }
    }

    private void initAiClients() {
        geminiClient = new GeminiApiClient();
        String ollamaIp = settingsDao.getOllamaIp();
        ollamaClient = new OllamaApiClient("http://" + ollamaIp);
        marcCoreEngine = new MarcCoreEngine();
    }

    /** Device connectivity + network-aware instructions for Gemini / Ollama / Core. */
    private String buildSystemPrompt() {
        DatabaseHelper db = DatabaseHelper.getInstance(requireContext());
        DeviceDao deviceDao = new DeviceDao(db);
        Device helmet = deviceDao.getHelmet();
        Device bike = deviceDao.getBike();
        boolean helmetConnected = helmet != null && helmet.isConnected();
        boolean bikeConnected = bike != null && bike.isConnected();

        boolean wifiOk = false;
        boolean dataOk = false;
        try {
            wifiOk = NetworkUtils.isWifiConnected(requireContext());
        } catch (SecurityException e) {
            Log.w("MARC_VOICE", "NetworkUtils.isWifiConnected denied", e);
        }
        try {
            dataOk = isDataConnected();
        } catch (SecurityException e) {
            Log.w("MARC_VOICE", "isDataConnected denied", e);
        }
        boolean hasInternet = wifiOk || dataOk;

        if (!helmetConnected && !bikeConnected) {
            if (hasInternet) {
                return "You are MARC, a smart assistant for a motorcycle rider. "
                        + "Both helmet and bike units are currently disconnected. "
                        + "You have no hardware data available. "
                        + "The rider knows who they are talking to. Answer any question directly. "
                        + "No domain restrictions. Be helpful, concise, speak like you are talking to a rider.";
            } else {
                return "You are MARC. Both helmet and bike units are disconnected and "
                        + "there is no internet. Tell the user: MARC network is down. "
                        + "Connect your helmet and bike units, or connect to the internet to proceed. "
                        + "You cannot answer questions right now.";
            }
        }

        if (!helmetConnected && bikeConnected) {
            return "You are MARC (Motorcycle Accident Response Companion). "
                    + "HELMET UNIT IS DISCONNECTED. Do NOT provide first aid guidance. "
                    + "If asked about injuries or first aid, respond: "
                    + "Helmet unit is disconnected. Connect your helmet unit to access first aid data. "
                    + "You CAN help with bike troubleshooting and repairs — bike unit is connected. "
                    + "Keep responses under 20 seconds spoken aloud. Direct language.";
        }

        if (helmetConnected && !bikeConnected) {
            return "You are MARC (Motorcycle Accident Response Companion). "
                    + "BIKE UNIT IS DISCONNECTED. Do NOT provide motorcycle repair or troubleshooting. "
                    + "If asked about bike problems, respond: "
                    + "Bike unit is disconnected. Connect your bike unit to ask for troubleshooting help. "
                    + "You CAN help with first aid and injury guidance — helmet unit is connected. "
                    + "Keep responses under 20 seconds spoken aloud. Direct language.";
        }

        return "You are MARC (Motorcycle Accident Response Companion), an AI embedded in a smart helmet. "
                + "Your domains: (1) First aid for motorcycle injuries — bleeding, fractures, shock, CPR. "
                + "(2) Motorcycle troubleshooting and roadside repairs. "
                + "Keep responses under 20 seconds spoken aloud. Simple direct language. "
                + "Assume user may be injured or stressed. "
                + "If asked outside these domains: I'm MARC — injuries and bike repairs only. What's wrong?";
    }

    private boolean isDataConnected() {
        ConnectivityManager cm =
                (ConnectivityManager)
                        requireContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            android.net.NetworkInfo ni = cm.getActiveNetworkInfo();
            return ni != null && ni.isConnected();
        }
        NetworkCapabilities caps = cm.getNetworkCapabilities(cm.getActiveNetwork());
        return caps != null
                && (caps.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)
                        || caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI));
    }

    void startVoiceListening() {
        Log.d("MARC_WAKE", "STT startListening called");
        if (ttsManager != null) {
            ttsManager.stop();
        }
        if (sttManager == null) {
            sttManager = new GoogleSTTManager(requireContext());
        }
        pendingVoicePartial = "";
        MarcCoreUiHelper.setOrbListening(ivOrb);
        tvMarcState.setText("LISTENING // STT ACTIVE");
        sttManager.startListening(
                new GoogleSTTManager.STTCallback() {
                    @Override
                    public void onListeningStarted() {
                        if (tvMarcState != null) {
                            tvMarcState.setText("LISTENING...");
                        }
                    }

                    @Override
                    public void onPartialResult(String partial) {
                        if (partial != null && !partial.trim().isEmpty()) {
                            pendingVoicePartial = partial.trim();
                        }
                        if (tvListeningText != null) {
                            tvListeningText.setText(partial);
                        }
                    }

                    @Override
                    public void onResult(String text) {
                        Log.d("MARC_WAKE", "STT result: " + text);
                        if (tvListeningText != null) {
                            tvListeningText.setText(text != null ? text : "");
                        }
                        // Do not call stopListening() here — session already ended; extra stop triggers ERROR_CLIENT.
                        String resolved = text != null ? text.trim() : "";
                        if (resolved.isEmpty() && !pendingVoicePartial.isEmpty()) {
                            resolved = pendingVoicePartial;
                        }
                        pendingVoicePartial = "";
                        if (resolved.isEmpty()) {
                            resetOrbToIdle();
                        } else {
                            handleVoiceCommand(resolved);
                        }
                        wakeRestartHandler.postDelayed(
                                () -> {
                                    if (!isAdded()
                                            || !(getActivity() instanceof MainActivity)) {
                                        return;
                                    }
                                    ((MainActivity) getActivity()).restartWakeWord();
                                },
                                1500);
                    }

                    @Override
                    public void onError(String e) {
                        Log.w("MARC_WAKE", "STT error: " + e);
                        pendingVoicePartial = "";
                        resetOrbToIdle();
                        tvMarcState.setText("ERROR // STT");
                        if (tvListeningText != null) {
                            tvListeningText.setText(e != null ? e : "Listening error");
                        }
                        wakeRestartHandler.postDelayed(
                                () -> {
                                    if (!isAdded()
                                            || !(getActivity() instanceof MainActivity)) {
                                        return;
                                    }
                                    ((MainActivity) getActivity()).restartWakeWord();
                                },
                                500);
                    }

                    @Override
                    public void onListeningStopped() {
                        // no-op: wake word restart is handled by onResult/onError
                    }
                });
    }

    void sendTextMessage(String text) {
        if (text == null || text.isEmpty()) {
            return;
        }
        etMessage.setText("");
        handleVoiceCommand(text);
    }

    void sendToMarc(String userMessage) {
        Log.d("MARC_VOICE", "sendToMarc called with: " + userMessage);
        String dbgKey = settingsDao.getGeminiApiKey();
        Log.d(
                "MARC_VOICE",
                "Gemini mode="
                        + settingsDao.isGeminiMode()
                        + " keyConfigured="
                        + GeminiApiClient.isConfigured(dbgKey)
                        + " keyLen="
                        + (dbgKey != null ? dbgKey.length() : 0));
        if (userMessage == null || userMessage.trim().isEmpty()) {
            return;
        }
        if (marcCoreActive) {
            sendToMarcCore(userMessage.trim());
            return;
        }

        String trimmed = userMessage.trim();
        adapter.addMessage(
                new ChatMessageAdapter.ChatMessage("user", trimmed, System.currentTimeMillis()));
        MarcCoreUiHelper.setOrbProcessing(ivOrb);
        tvMarcState.setText("PROCESSING // OFFLINE");
        String phrase = LOADING_PHRASES[random.nextInt(LOADING_PHRASES.length)];
        tvLoadingPhrase.setText(phrase);

        // Fully offline response using curated switch-case style data.
        String response = MarcLocalResponder.respond(trimmed);
        tvLoadingPhrase.setText("");
        adapter.addMessage(
                new ChatMessageAdapter.ChatMessage(
                        "marc", response, System.currentTimeMillis()));
        resetOrbToIdle();
        tvMarcState.setText("STANDBY // SAY HEY MARC");
        if (ttsManager != null && ttsManager.isReady()) {
            ttsManager.speak(response);
        }
    }

    private void sendToMarcCore(String userMessage) {
        adapter.addMessage(
                new ChatMessageAdapter.ChatMessage("user", userMessage, System.currentTimeMillis()));
        tvMarcState.setText("MARC CORE // PROCESSING");
        MarcCoreUiHelper.setOrbProcessing(ivOrb);
        String phrase = CORE_LOADING_PHRASES[random.nextInt(CORE_LOADING_PHRASES.length)];
        tvLoadingPhrase.setText(phrase);
        if (ttsManager != null) {
            ttsManager.speak(phrase);
        }

        marcCoreEngine.processQuery(
                userMessage,
                null,
                null,
                new MarcCoreEngine.MarcCoreCallback() {
                    @Override
                    public void onResponse(String response) {
                        if (!isAdded()) {
                            return;
                        }
                        tvLoadingPhrase.setText("");
                        adapter.addMessage(
                                new ChatMessageAdapter.ChatMessage(
                                        "core", response, System.currentTimeMillis()));
                        MarcCoreUiHelper.setOrbCoreUnleashed(
                                ivOrb, requireContext(), tvMarcState, tvListeningText);
                        tvMarcState.setText("MARC CORE // UNLEASHED");
                        if (ttsManager != null && ttsManager.isReady()) {
                            ttsManager.speak(response);
                        }
                    }
                });
    }

    /**
     * Converts adapter history for Gemini / Ollama / Core: drops a trailing user turn so the client
     * can append the current {@code userMessage} without duplicating it.
     */
    private List<GeminiApiClient.ChatMessage> historyForApi(
            List<ChatMessageAdapter.ChatMessage> full) {
        List<ChatMessageAdapter.ChatMessage> slice = full;
        if (full != null && !full.isEmpty()) {
            ChatMessageAdapter.ChatMessage last = full.get(full.size() - 1);
            if (last != null && "user".equalsIgnoreCase(last.getRole())) {
                slice = full.subList(0, full.size() - 1);
            }
        }
        return toGeminiMessages(slice);
    }

    private static List<GeminiApiClient.ChatMessage> capGeminiHistory(
            List<GeminiApiClient.ChatMessage> history) {
        if (history == null || history.isEmpty()) {
            return history != null ? history : new ArrayList<>();
        }
        if (history.size() <= MAX_API_HISTORY_MESSAGES) {
            return history;
        }
        return new ArrayList<>(
                history.subList(
                        history.size() - MAX_API_HISTORY_MESSAGES, history.size()));
    }

    private List<GeminiApiClient.ChatMessage> toGeminiMessages(
            List<ChatMessageAdapter.ChatMessage> messages) {
        List<GeminiApiClient.ChatMessage> out = new ArrayList<>();
        if (messages == null) {
            return out;
        }
        for (ChatMessageAdapter.ChatMessage m : messages) {
            if (m == null || m.getRole() == null) {
                continue;
            }
            String r = m.getRole().toLowerCase(Locale.US);
            String gemRole = "user";
            if ("marc".equals(r) || "core".equals(r)) {
                gemRole = "model";
            }
            out.add(
                    new GeminiApiClient.ChatMessage(
                            gemRole, m.getContent() != null ? m.getContent() : ""));
        }
        return out;
    }

    void triggerCoreWarning() {
        awaitingCoreConfirmation = true;
        MarcCoreUiHelper.setOrbCoreWarning(ivOrb, requireContext(), tvMarcState);
        tvListeningText.setText("");
        String warning = getString(R.string.marc_core_warning);
        if (ttsManager != null) {
            ttsManager.speak(warning);
        }
    }

    void activateMarcCore() {
        awaitingCoreConfirmation = false;
        marcCoreActive = true;
        MarcCoreUiHelper.setOrbCoreUnleashed(ivOrb, requireContext(), tvMarcState, tvListeningText);
        if (ttsManager != null) {
            ttsManager.speak(getString(R.string.marc_core_activated));
        }
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).updateAiModeBadge("MARC CORE", true);
        }
    }

    /** Turns Marc Core off. Invoked only from explicit user actions (voice cool-down phrase or long-press orb). */
    void deactivateMarcCore() {
        marcCoreActive = false;
        awaitingCoreConfirmation = false;
        MarcCoreUiHelper.setOrbNormal(ivOrb, requireContext(), tvMarcState, tvListeningText);
        if (ttsManager != null) {
            ttsManager.speak(getString(R.string.marc_core_deactivated));
        }
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity())
                    .updateAiModeBadge(
                            settingsDao.isGeminiMode() ? "MARC ONE" : "MARC BACK", false);
        }
        tvMarcState.setText("STANDBY // SAY HEY MARC");
    }

    /** Voice command router; also used for text entry so Core phrases work from the keyboard. */
    public void handleVoiceCommand(String text) {
        Log.d("MARC_VOICE", "handleVoiceCommand received: " + text);
        if (text == null) {
            return;
        }
        String trimmedRaw = text.trim();
        if (trimmedRaw.isEmpty()) {
            return;
        }
        String stripped = stripWakeArtifacts(trimmedRaw);
        String commandText = stripped.isEmpty() ? trimmedRaw : stripped;
        String lower = commandText.toLowerCase(Locale.US).trim();

        if (lower.contains("proceed anyway") && awaitingCoreConfirmation) {
            activateMarcCore();
            return;
        }
        if (lower.contains("cancel") && awaitingCoreConfirmation) {
            awaitingCoreConfirmation = false;
            resetOrbToIdle();
            if (ttsManager != null) {
                ttsManager.speak("Marc Core cancelled. Smart choice.");
            }
            return;
        }
        if (!marcCoreActive && requestsMarcCoreActivation(lower)) {
            triggerCoreWarning();
            return;
        }
        if (lower.contains("cool down") && marcCoreActive) {
            deactivateMarcCore();
            return;
        }
        sendToMarc(commandText);
    }

    /** Remove leading wake prefix so Gemini/Ollama get the actual question (not \"hey marc\" noise). */
    private static String stripWakeArtifacts(String raw) {
        if (raw == null || raw.isEmpty()) {
            return "";
        }
        String t = raw.trim();
        String lower = t.toLowerCase(Locale.US);
        for (String prefix : WAKE_PREFIXES) {
            if (!lower.startsWith(prefix)) {
                continue;
            }
            String rest = t.substring(prefix.length()).trim();
            if (!rest.isEmpty() && rest.charAt(0) == ',') {
                rest = rest.substring(1).trim();
            }
            return rest;
        }
        return "";
    }

    /**
     * Intentionally narrow phrases so casual \"Marc, what's weather\" does not open Core flow.
     */
    private static boolean requestsMarcCoreActivation(String lower) {
        return lower.equals("marc core")
                || lower.startsWith("marc core ")
                || lower.contains("switch to marc core")
                || lower.contains("activate marc core")
                || lower.contains("enable marc core")
                || lower.contains("turn on marc core");
    }

    /** Invoked from {@link MainActivity} when the wake word fires. */
    public void activateVoice() {
        Log.d("MARC_WAKE", "activateVoice() called");
        wakeRestartHandler.removeCallbacks(pendingStartVoiceRunnable);
        /* Longer delay: wake SpeechRecognizer teardown + ERROR_SERVER_DISCONNECTED (11) if STT races. */
        wakeRestartHandler.postDelayed(pendingStartVoiceRunnable, 850L);
    }

    /**
     * Normal / standby orb. Does not exit Marc Core — only {@link #deactivateMarcCore()} (cool down
     * voice or long-press orb) clears Core mode.
     */
    void resetOrbToIdle() {
        tvLoadingPhrase.setText("");
        if (marcCoreActive) {
            MarcCoreUiHelper.setOrbCoreUnleashed(
                    ivOrb, requireContext(), tvMarcState, tvListeningText);
            tvMarcState.setText("MARC CORE // UNLEASHED");
            return;
        }
        if (awaitingCoreConfirmation) {
            MarcCoreUiHelper.setOrbCoreWarning(ivOrb, requireContext(), tvMarcState);
            return;
        }
        MarcCoreUiHelper.setOrbNormal(ivOrb, requireContext(), tvMarcState, tvListeningText);
        tvMarcState.setText("STANDBY // SAY HEY MARC");
    }

    @Override
    public void onDestroyView() {
        wakeRestartHandler.removeCallbacksAndMessages(null);
        if (sttManager != null) {
            sttManager.destroy();
        }
        MarcCoreUiHelper.cancelAll(ivOrb, tvMarcState);
        rvChat = null;
        etMessage = null;
        tvMarcState = null;
        tvListeningText = null;
        tvLoadingPhrase = null;
        tvMarcLabel = null;
        flOrbContainer = null;
        ivOrb = null;
        btnTapToSpeak = null;
        btnSend = null;
        layoutVoiceMode = null;
        layoutTextMode = null;
        toggleMode = null;
        sttManager = null;
        ttsManager = null;
        super.onDestroyView();
    }
}
