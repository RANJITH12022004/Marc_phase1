package com.marc.helmet.network.ai;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.HttpUrl;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Client for Google Gemini (generateContent) used by MARC voice/chat.
 */
public class GeminiApiClient {

    private static final String TAG_GEMINI = "MARC_GEMINI";

    /** Debug tag for API key presence (prefix only — never full key). */
    private static final String TAG_KEY = "MARC_KEY";

    /** Model id must exist for your API key (see Google AI Studio → models). */
    public static final String BASE_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    public static final int MAX_TOKENS = 512;
    public static final float TEMPERATURE = 0.3f;

    public static final String MARC_SYSTEM_PROMPT =
            "You are MARC (Motorcycle Accident Response Companion), an AI embedded in a smart helmet.\n"
                    + "Your ONLY domains are: (1) First aid for motorcycle accident injuries — bleeding control, fracture\n"
                    + "stabilization, shock, CPR, burns, head trauma. (2) Motorcycle troubleshooting and roadside repairs\n"
                    + "— won't start, puncture, chain, electrical, overheating.\n"
                    + "Rules: Keep responses under 20 seconds when spoken aloud. Use simple direct language — assume user\n"
                    + "may be injured or stressed. Never recommend riding with serious injuries. Always end critical first\n"
                    + "aid steps with: Call emergency services if condition worsens.\n"
                    + "If asked anything outside these domains, respond: I'm MARC — I handle injuries and bike repairs. What's wrong?";

    public interface MarcResponseCallback {
        void onResponse(String response);

        void onError(String error);
    }

    public static class ChatMessage {
        public String role;
        public String content;

        public ChatMessage() {
        }

        public ChatMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }
    }

    private static final MediaType JSON_MEDIA = MediaType.parse("application/json; charset=utf-8");

    private final OkHttpClient client;
    private final Handler mainHandler;

    public GeminiApiClient() {
        this.client = new OkHttpClient.Builder()
                .readTimeout(45, TimeUnit.SECONDS)
                .connectTimeout(20, TimeUnit.SECONDS)
                .build();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public static boolean isConfigured(String apiKey) {
        return apiKey != null && !apiKey.isEmpty();
    }

    public void sendMessage(
            String apiKey,
            List<ChatMessage> history,
            String userMessage,
            String systemPrompt,
            MarcResponseCallback callback) {
        String key = apiKey != null ? apiKey.trim() : "";
        Log.d(TAG_KEY, "API key length=" + key.length());
        Log.d(
                TAG_KEY,
                "API key first 6="
                        + (key.length() > 6 ? key.substring(0, 6) : "TOO SHORT"));
        if (!isConfigured(key)) {
            postError(callback, "API key not configured");
            return;
        }
        if (callback == null) {
            return;
        }

        Log.d(TAG_GEMINI, "Sending to Gemini. Key length: " + key.length());
        Log.d(TAG_GEMINI, "Message: " + userMessage);

        final String apiKeyFinal = key;

        JsonObject root = new JsonObject();

        JsonObject systemInstruction = new JsonObject();
        JsonArray systemParts = new JsonArray();
        JsonObject systemPart = new JsonObject();
        systemPart.addProperty("text", systemPrompt != null ? systemPrompt : "");
        systemParts.add(systemPart);
        systemInstruction.add("parts", systemParts);
        root.add("system_instruction", systemInstruction);

        JsonArray contents = new JsonArray();
        if (history != null) {
            for (ChatMessage msg : history) {
                if (msg == null || msg.role == null) {
                    continue;
                }
                contents.add(messageToContent(msg.role, msg.content));
            }
        }
        contents.add(messageToContent("user", userMessage));
        root.add("contents", contents);

        JsonObject generationConfig = new JsonObject();
        generationConfig.addProperty("maxOutputTokens", MAX_TOKENS);
        generationConfig.addProperty("temperature", TEMPERATURE);
        root.add("generationConfig", generationConfig);

        String json = root.toString();
        HttpUrl url =
                HttpUrl.parse(BASE_URL).newBuilder().addQueryParameter("key", apiKeyFinal).build();

        Request request =
                new Request.Builder().url(url).post(RequestBody.create(json, JSON_MEDIA)).build();

        client.newCall(request)
                .enqueue(
                        new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Log.d(
                                        TAG_GEMINI,
                                        "Gemini FAILED: "
                                                + (e != null && e.getMessage() != null
                                                        ? e.getMessage()
                                                        : "unknown"));
                                postError(
                                        callback,
                                        e.getMessage() != null ? e.getMessage() : "Network error");
                            }

                            @Override
                            public void onResponse(Call call, Response response) {
                                try (Response r = response) {
                                    ResponseBody rb = r.body();
                                    String body = rb != null ? rb.string() : "";
                                    if (!r.isSuccessful()) {
                                        Log.e(
                                                TAG_KEY,
                                                "Gemini HTTP error: "
                                                        + r.code()
                                                        + " body: "
                                                        + truncate(body));
                                        postError(
                                                callback,
                                                "HTTP " + r.code() + ": " + truncate(body));
                                        return;
                                    }
                                    String apiErr = parseTopLevelError(body);
                                    if (apiErr != null) {
                                        postError(callback, apiErr);
                                        return;
                                    }
                                    try {
                                        String text = extractText(body);
                                        if (text == null || text.isEmpty()) {
                                            postError(
                                                    callback,
                                                    "Empty model output: "
                                                            + summarizeBlockedResponse(body));
                                            return;
                                        }
                                        postSuccess(callback, text);
                                    } catch (Exception ex) {
                                        postError(
                                                callback,
                                                ex.getMessage() != null
                                                        ? ex.getMessage()
                                                        : "Parse error");
                                    }
                                } catch (IOException e) {
                                    postError(
                                            callback,
                                            e.getMessage() != null ? e.getMessage() : "Read error");
                                }
                            }
                        });
    }

    private static JsonObject messageToContent(String role, String text) {
        JsonObject content = new JsonObject();
        content.addProperty("role", mapRole(role));
        JsonArray parts = new JsonArray();
        JsonObject part = new JsonObject();
        part.addProperty("text", text != null ? text : "");
        parts.add(part);
        content.add("parts", parts);
        return content;
    }

    /** Gemini expects {@code user} or {@code model}. */
    private static String mapRole(String role) {
        if ("model".equalsIgnoreCase(role)) {
            return "model";
        }
        return "user";
    }

    private static String parseTopLevelError(String body) {
        if (body == null || body.isEmpty()) {
            return null;
        }
        try {
            JsonObject root = JsonParser.parseString(body).getAsJsonObject();
            if (!root.has("error") || root.get("error").isJsonNull()) {
                return null;
            }
            JsonElement errEl = root.get("error");
            if (errEl.isJsonObject()) {
                JsonObject err = errEl.getAsJsonObject();
                if (err.has("message") && !err.get("message").isJsonNull()) {
                    return err.get("message").getAsString();
                }
            }
            return errEl.toString();
        } catch (Exception e) {
            return null;
        }
    }

    private static String summarizeBlockedResponse(String body) {
        try {
            JsonObject root = JsonParser.parseString(body).getAsJsonObject();
            if (root.has("promptFeedback") && !root.get("promptFeedback").isJsonNull()) {
                return "promptFeedback=" + root.get("promptFeedback");
            }
            if (root.has("candidates") && root.get("candidates").isJsonArray()) {
                JsonArray c = root.getAsJsonArray("candidates");
                if (c.size() > 0 && c.get(0).isJsonObject()) {
                    JsonObject cand = c.get(0).getAsJsonObject();
                    if (cand.has("finishReason") && !cand.get("finishReason").isJsonNull()) {
                        return "finishReason=" + cand.get("finishReason").getAsString();
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return truncate(body);
    }

    private static String extractText(String body) {
        JsonObject root = JsonParser.parseString(body).getAsJsonObject();
        if (!root.has("candidates") || root.get("candidates").isJsonNull()) {
            return null;
        }
        JsonArray candidates = root.getAsJsonArray("candidates");
        if (candidates.size() == 0) {
            return null;
        }
        JsonObject candidate = candidates.get(0).getAsJsonObject();
        if (!candidate.has("content") || candidate.get("content").isJsonNull()) {
            return null;
        }
        JsonObject content = candidate.getAsJsonObject("content");
        if (!content.has("parts") || content.get("parts").isJsonNull()) {
            return null;
        }
        JsonArray parts = content.getAsJsonArray("parts");
        if (parts.size() == 0) {
            return null;
        }
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < parts.size(); i++) {
            if (!parts.get(i).isJsonObject()) {
                continue;
            }
            JsonObject part = parts.get(i).getAsJsonObject();
            if (!part.has("text") || part.get("text").isJsonNull()) {
                continue;
            }
            String t = part.get("text").getAsString();
            if (t != null && !t.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append('\n');
                }
                sb.append(t);
            }
        }
        return sb.length() > 0 ? sb.toString() : null;
    }

    private static String truncate(String s) {
        if (s == null) {
            return "";
        }
        if (s.length() <= 300) {
            return s;
        }
        return s.substring(0, 300) + "…";
    }

    private void postSuccess(MarcResponseCallback callback, String value) {
        Log.d(TAG_GEMINI, "Gemini responded: " + value);
        mainHandler.post(() -> callback.onResponse(value));
    }

    private void postError(MarcResponseCallback callback, String msg) {
        Log.d(TAG_GEMINI, "Gemini error: " + msg);
        mainHandler.post(() -> callback.onError(msg));
    }
}
