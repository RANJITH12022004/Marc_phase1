package com.marc.helmet.network.pico;

import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;

import java.io.IOException;
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
 * Async HTTP client for Pico helmet / bike firmware (OkHttp + Gson).
 */
public class PicoApiClient {

    private static final MediaType JSON_MEDIA = MediaType.parse("application/json; charset=utf-8");

    public interface PicoCallback<T> {
        void onSuccess(T result);

        void onError(String error);
    }

    public static class PicoStatus {
        @SerializedName("device_type")
        public String deviceType;

        public double roll;
        public double pitch;

        /** Firmware may expose {@code crash_flag} or {@code crash} in JSON. */
        @SerializedName(value = "crash_flag", alternate = {"crash"})
        public boolean crashFlag;

        @SerializedName("speed_alert_active")
        public boolean speedAlertActive;

        public boolean initialized;

        /** Pico firmware uses {@code version}; keep {@code firmware_version} as alternate. */
        @SerializedName(value = "firmware_version", alternate = {"version"})
        public String firmwareVersion;
    }

    @FunctionalInterface
    private interface BodyParser<T> {
        T parse(String body) throws Exception;
    }

    private final HttpUrl base;
    private final OkHttpClient client;
    private final Gson gson;
    private final Handler mainHandler;

    public PicoApiClient(String baseUrl) {
        String trimmed = baseUrl == null ? "" : baseUrl.trim();
        while (trimmed.endsWith("/")) {
            trimmed = trimmed.substring(0, trimmed.length() - 1);
        }
        this.base = HttpUrl.get(trimmed);
        this.client = new OkHttpClient.Builder()
                .connectTimeout(3, TimeUnit.SECONDS)
                .readTimeout(5, TimeUnit.SECONDS)
                .build();
        this.gson = new Gson();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void identify(PicoCallback<PicoStatus> callback) {
        Request request = new Request.Builder()
                .url(endpoint("identify"))
                .get()
                .build();
        enqueue(request, callback, body -> gson.fromJson(body, PicoStatus.class));
    }

    public void getStatus(PicoCallback<PicoStatus> callback) {
        Request request = new Request.Builder()
                .url(endpoint("status"))
                .get()
                .build();
        enqueue(request, callback, body -> gson.fromJson(body, PicoStatus.class));
    }

    public void calibrate(
            double standingRoll,
            double standingPitch,
            double maxLeft,
            double maxRight,
            PicoCallback<Boolean> callback) {
        JsonObject json = new JsonObject();
        json.addProperty("standing_roll", standingRoll);
        json.addProperty("standing_pitch", standingPitch);
        json.addProperty("max_left_roll", maxLeft);
        json.addProperty("max_right_roll", maxRight);
        RequestBody body = RequestBody.create(gson.toJson(json), JSON_MEDIA);
        Request request = new Request.Builder()
                .url(endpoint("calibrate"))
                .post(body)
                .build();
        enqueue(request, callback, b -> true);
    }

    public void setLedAlert(boolean active, float thresholdKmh, PicoCallback<Boolean> callback) {
        JsonObject json = new JsonObject();
        json.addProperty("threshold_kmh", thresholdKmh);
        json.addProperty("mode", active ? "alert" : "clear");
        RequestBody body = RequestBody.create(gson.toJson(json), JSON_MEDIA);
        Request request = new Request.Builder()
                .url(endpoint("led"))
                .post(body)
                .build();
        enqueue(request, callback, b -> true);
    }

    public void confirmInit(PicoCallback<Boolean> callback) {
        Request request = new Request.Builder()
                .url(endpoint("init_confirm"))
                .get()
                .build();
        enqueue(request, callback, b -> true);
    }

    public void resetCrashFlag(PicoCallback<Boolean> callback) {
        RequestBody empty =
                RequestBody.create(new byte[0], MediaType.parse("application/octet-stream"));
        Request request = new Request.Builder()
                .url(endpoint("reset_crash"))
                .post(empty)
                .build();
        enqueue(request, callback, b -> true);
    }

    public void ping(PicoCallback<Long> callback) {
        final long startNs = System.nanoTime();
        Request request = new Request.Builder()
                .url(endpoint("identify"))
                .get()
                .build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                postError(callback, e.getMessage() != null ? e.getMessage() : "Network error");
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
                    long ms = (System.nanoTime() - startNs) / 1_000_000L;
                    postSuccess(callback, ms);
                } catch (IOException e) {
                    postError(callback, e.getMessage() != null ? e.getMessage() : "Read error");
                } catch (Exception e) {
                    postError(callback, e.getMessage() != null ? e.getMessage() : "Error");
                }
            }
        });
    }

    private HttpUrl endpoint(String path) {
        String seg = path.startsWith("/") ? path.substring(1) : path;
        return base.newBuilder().addPathSegment(seg).build();
    }

    private <T> void enqueue(Request request, PicoCallback<T> callback, BodyParser<T> parser) {
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                postError(callback, e.getMessage() != null ? e.getMessage() : "Network error");
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
                        T result = parser.parse(body);
                        postSuccess(callback, result);
                    } catch (Exception ex) {
                        postError(callback, ex.getMessage() != null ? ex.getMessage() : "Parse error");
                    }
                } catch (IOException e) {
                    postError(callback, e.getMessage() != null ? e.getMessage() : "Read error");
                }
            }
        });
    }

    private static String truncate(String s) {
        if (s == null) {
            return "";
        }
        if (s.length() <= 200) {
            return s;
        }
        return s.substring(0, 200) + "…";
    }

    private <T> void postSuccess(PicoCallback<T> callback, T value) {
        mainHandler.post(() -> callback.onSuccess(value));
    }

    private void postError(PicoCallback<?> callback, String msg) {
        mainHandler.post(() -> callback.onError(msg));
    }
}
