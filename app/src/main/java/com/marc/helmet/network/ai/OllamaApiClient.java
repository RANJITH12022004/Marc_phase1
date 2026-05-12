package com.marc.helmet.network.ai;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.marc.helmet.network.pico.PicoApiClient;

import java.io.IOException;
import java.util.ArrayList;
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
 * Client for a local Ollama server ({@code /api/chat}, {@code /api/tags}).
 */
public class OllamaApiClient {

    private static final MediaType JSON_MEDIA = MediaType.parse("application/json; charset=utf-8");

    public interface MarcResponseCallback {
        void onResponse(String response);

        void onError(String error);
    }

    private final HttpUrl base;
    private final OkHttpClient client;
    private final Handler mainHandler;

    public OllamaApiClient(String ollamaBaseUrl) {
        String trimmed = ollamaBaseUrl == null ? "" : ollamaBaseUrl.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        this.base = HttpUrl.get(trimmed);
        this.client = new OkHttpClient.Builder()
                .readTimeout(30, TimeUnit.SECONDS)
                .connectTimeout(30, TimeUnit.SECONDS)
                .build();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public static boolean isConfigured(String baseUrl) {
        return baseUrl != null && !baseUrl.isEmpty() && baseUrl.contains(":");
    }

    public void sendMessage(
            String model,
            List<GeminiApiClient.ChatMessage> history,
            String userMessage,
            String systemPrompt,
            MarcResponseCallback callback) {
        if (callback == null) {
            return;
        }
        if (model == null || model.isEmpty()) {
            postError(callback, "Model not set");
            return;
        }

        JsonObject root = new JsonObject();
        root.addProperty("model", model);
        root.addProperty("stream", false);

        JsonArray messages = new JsonArray();
        JsonObject systemMsg = new JsonObject();
        systemMsg.addProperty("role", "system");
        systemMsg.addProperty("content", systemPrompt != null ? systemPrompt : "");
        messages.add(systemMsg);

        if (history != null) {
            for (GeminiApiClient.ChatMessage msg : history) {
                if (msg == null || msg.role == null) {
                    continue;
                }
                JsonObject m = new JsonObject();
                m.addProperty("role", toOllamaRole(msg.role));
                m.addProperty("content", msg.content != null ? msg.content : "");
                messages.add(m);
            }
        }

        JsonObject userMsg = new JsonObject();
        userMsg.addProperty("role", "user");
        userMsg.addProperty("content", userMessage != null ? userMessage : "");
        messages.add(userMsg);

        root.add("messages", messages);

        String json = root.toString();
        HttpUrl url = base.newBuilder().addPathSegment("api").addPathSegment("chat").build();
        Request request =
                new Request.Builder().url(url).post(RequestBody.create(json, JSON_MEDIA)).build();

        client.newCall(request)
                .enqueue(
                        new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
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
                                        postError(callback, "HTTP " + r.code() + ": " + truncate(body));
                                        return;
                                    }
                                    try {
                                        String text = extractMessageContent(body);
                                        if (text == null) {
                                            postError(callback, "Empty or invalid response");
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

    /** Maps Gemini-style {@code model} role to Ollama {@code assistant}. */
    private static String toOllamaRole(String role) {
        if ("model".equalsIgnoreCase(role)) {
            return "assistant";
        }
        if ("system".equalsIgnoreCase(role)) {
            return "system";
        }
        return "user";
    }

    private static String extractMessageContent(String body) {
        JsonObject root = JsonParser.parseString(body).getAsJsonObject();
        if (!root.has("message") || root.get("message").isJsonNull()) {
            return null;
        }
        JsonObject message = root.getAsJsonObject("message");
        if (!message.has("content") || message.get("content").isJsonNull()) {
            return null;
        }
        return message.get("content").getAsString();
    }

    public void testConnection(PicoApiClient.PicoCallback<List<String>> callback) {
        if (callback == null) {
            return;
        }
        HttpUrl url = base.newBuilder().addPathSegment("api").addPathSegment("tags").build();
        Request request = new Request.Builder().url(url).get().build();

        client.newCall(request)
                .enqueue(
                        new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                postPicoError(
                                        callback,
                                        e.getMessage() != null ? e.getMessage() : "Network error");
                            }

                            @Override
                            public void onResponse(Call call, Response response) {
                                try (Response r = response) {
                                    ResponseBody rb = r.body();
                                    String body = rb != null ? rb.string() : "";
                                    if (!r.isSuccessful()) {
                                        postPicoError(
                                                callback, "HTTP " + r.code() + ": " + truncate(body));
                                        return;
                                    }
                                    try {
                                        List<String> names = parseModelNames(body);
                                        postPicoSuccess(callback, names);
                                    } catch (Exception ex) {
                                        postPicoError(
                                                callback,
                                                ex.getMessage() != null
                                                        ? ex.getMessage()
                                                        : "Parse error");
                                    }
                                } catch (IOException e) {
                                    postPicoError(
                                            callback,
                                            e.getMessage() != null ? e.getMessage() : "Read error");
                                }
                            }
                        });
    }

    private static List<String> parseModelNames(String body) {
        List<String> out = new ArrayList<>();
        JsonObject root = JsonParser.parseString(body).getAsJsonObject();
        if (!root.has("models") || root.get("models").isJsonNull()) {
            return out;
        }
        JsonArray models = root.getAsJsonArray("models");
        for (int i = 0; i < models.size(); i++) {
            JsonObject m = models.get(i).getAsJsonObject();
            if (m.has("name") && !m.get("name").isJsonNull()) {
                out.add(m.get("name").getAsString());
            }
        }
        return out;
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
        mainHandler.post(() -> callback.onResponse(value));
    }

    private void postError(MarcResponseCallback callback, String msg) {
        mainHandler.post(() -> callback.onError(msg));
    }

    private void postPicoSuccess(PicoApiClient.PicoCallback<List<String>> callback, List<String> v) {
        mainHandler.post(() -> callback.onSuccess(v));
    }

    private void postPicoError(PicoApiClient.PicoCallback<List<String>> callback, String msg) {
        mainHandler.post(() -> callback.onError(msg));
    }
}
