package com.marc.helmet.network.pico;

import android.content.Context;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Handler;
import android.os.Looper;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

/**
 * Scans the local Wi‑Fi subnet for MARC Pico W devices via HTTP {@code /identify}.
 */
public class DeviceScanner {

    public interface ScanCallback {
        void onDeviceFound(String ip, String deviceType, String firmwareVersion);

        void onScanComplete(int devicesFound);

        void onScanProgress(int current, int total);
    }

    private static final int THREADS = 32;
    private static final String HELMET = "MARC_HELMET";
    private static final String BIKE = "MARC_BIKE";

    private final Context appContext;
    private final OkHttpClient client;
    private final Gson gson;
    private final Handler mainHandler;

    private volatile boolean stopped;
    private ExecutorService executor;

    public DeviceScanner(Context context) {
        this.appContext = context.getApplicationContext();
        this.client = new OkHttpClient.Builder()
                .connectTimeout(800, TimeUnit.MILLISECONDS)
                .readTimeout(1500, TimeUnit.MILLISECONDS)
                .build();
        this.gson = new Gson();
        this.mainHandler = new Handler(Looper.getMainLooper());
    }

    public void startScan(ScanCallback callback) {
        stopScan();
        stopped = false;

        String deviceIp = getWifiIpv4String();
        if (deviceIp == null) {
            mainHandler.post(() -> {
                callback.onScanProgress(0, 0);
                callback.onScanComplete(0);
            });
            return;
        }

        String[] octets = deviceIp.split("\\.");
        if (octets.length != 4) {
            mainHandler.post(() -> {
                callback.onScanProgress(0, 0);
                callback.onScanComplete(0);
            });
            return;
        }

        String prefix = octets[0] + "." + octets[1] + "." + octets[2] + ".";

        int totalTasks = 0;
        for (int i = 1; i <= 254; i++) {
            String candidate = prefix + i;
            if (candidate.equals(deviceIp)) {
                continue;
            }
            totalTasks++;
        }

        if (totalTasks == 0) {
            mainHandler.post(() -> {
                callback.onScanProgress(0, 0);
                callback.onScanComplete(0);
            });
            return;
        }

        final int finalTotal = totalTasks;
        AtomicInteger completed = new AtomicInteger(0);
        AtomicInteger found = new AtomicInteger(0);

        executor = Executors.newFixedThreadPool(THREADS);

        for (int i = 1; i <= 254; i++) {
            final String candidate = prefix + i;
            if (candidate.equals(deviceIp)) {
                continue;
            }

            executor.submit(() -> {
                try {
                    if (!stopped) {
                        try {
                            String url = "http://" + candidate + "/identify";
                            Request request = new Request.Builder().url(url).get().build();
                            try (Response response = client.newCall(request).execute()) {
                                if (stopped) {
                                    return;
                                }
                                if (!response.isSuccessful()) {
                                    return;
                                }
                                ResponseBody rb = response.body();
                                String body = rb != null ? rb.string() : "";
                                if (!body.contains(HELMET) && !body.contains(BIKE)) {
                                    return;
                                }
                                String deviceType = parseDeviceType(body);
                                if (deviceType == null) {
                                    return;
                                }
                                String firmware = parseFirmwareVersion(body);
                                found.incrementAndGet();
                                final String fw = firmware;
                                final String dt = deviceType;
                                final String ip = candidate;
                                mainHandler.post(() -> {
                                    if (!stopped) {
                                        callback.onDeviceFound(ip, dt, fw);
                                    }
                                });
                            }
                        } catch (Exception ignored) {
                        }
                    }
                } finally {
                    int c = completed.incrementAndGet();
                    if (!stopped) {
                        if (c % 10 == 0 || c == finalTotal) {
                            mainHandler.post(() -> {
                                if (!stopped) {
                                    callback.onScanProgress(c, finalTotal);
                                }
                            });
                        }
                        if (c == finalTotal) {
                            final int totalFound = found.get();
                            mainHandler.post(() -> {
                                if (!stopped) {
                                    callback.onScanComplete(totalFound);
                                }
                            });
                        }
                    }
                }
            });
        }
    }

    private String parseDeviceType(String body) {
        try {
            JsonObject obj = gson.fromJson(body, JsonObject.class);
            if (obj != null && obj.has("device_type") && !obj.get("device_type").isJsonNull()) {
                String t = obj.get("device_type").getAsString();
                if (HELMET.equals(t) || BIKE.equals(t)) {
                    return t;
                }
            }
        } catch (Exception ignored) {
        }
        if (body.contains(HELMET)) {
            return HELMET;
        }
        if (body.contains(BIKE)) {
            return BIKE;
        }
        return null;
    }

    private String parseFirmwareVersion(String body) {
        try {
            JsonObject obj = gson.fromJson(body, JsonObject.class);
            if (obj != null && obj.has("firmware_version")
                    && !obj.get("firmware_version").isJsonNull()) {
                return obj.get("firmware_version").getAsString();
            }
        } catch (Exception ignored) {
        }
        return "";
    }

    private String getWifiIpv4String() {
        try {
            WifiManager wm = (WifiManager) appContext.getSystemService(Context.WIFI_SERVICE);
            if (wm != null) {
                WifiInfo info = wm.getConnectionInfo();
                if (info != null) {
                    int ip = info.getIpAddress();
                    if (ip != 0) {
                        return intToIpv4(ip);
                    }
                }
            }
        } catch (Exception ignored) {
        }

        try {
            java.util.Enumeration<java.net.NetworkInterface> interfaces =
                    java.net.NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                java.net.NetworkInterface ni = interfaces.nextElement();
                if (ni.isLoopback() || !ni.isUp()) {
                    continue;
                }
                String name = ni.getName().toLowerCase(java.util.Locale.US);
                if (!name.startsWith("wlan") && !name.startsWith("eth")) {
                    continue;
                }
                java.util.Enumeration<java.net.InetAddress> addresses = ni.getInetAddresses();
                while (addresses.hasMoreElements()) {
                    java.net.InetAddress addr = addresses.nextElement();
                    if (addr.isLoopbackAddress()) {
                        continue;
                    }
                    String hostAddr = addr.getHostAddress();
                    if (hostAddr != null && hostAddr.contains(".") && !hostAddr.contains(":")) {
                        return hostAddr;
                    }
                }
            }
        } catch (Exception ignored) {
        }

        return null;
    }

    private static String intToIpv4(int ip) {
        return (ip & 0xff)
                + "."
                + ((ip >> 8) & 0xff)
                + "."
                + ((ip >> 16) & 0xff)
                + "."
                + ((ip >> 24) & 0xff);
    }

    public void stopScan() {
        stopped = true;
        ExecutorService ex = executor;
        executor = null;
        if (ex != null) {
            ex.shutdownNow();
        }
    }
}
