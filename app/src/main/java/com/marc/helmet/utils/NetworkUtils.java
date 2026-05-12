package com.marc.helmet.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.Nullable;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * LAN / WiFi helpers and asynchronous TCP reachability probes.
 */
public final class NetworkUtils {

    private static final ExecutorService PING_EXECUTOR = Executors.newCachedThreadPool();

    private NetworkUtils() {
    }

    public interface PingCallback {
        void onResult(boolean reachable, long latencyMs);
    }

    /** IPv4 WiFi address as dotted string, or empty if unavailable. */
    public static String getDeviceIp(Context ctx) {
        Context app = ctx.getApplicationContext();
        try {
            WifiManager wm = (WifiManager) app.getSystemService(Context.WIFI_SERVICE);
            if (wm == null) {
                return "";
            }
            WifiInfo info = wm.getConnectionInfo();
            if (info == null) {
                return "";
            }
            int ip = info.getIpAddress();
            if (ip == 0) {
                return "";
            }
            return formatIpAddress(ip);
        } catch (Exception e) {
            return "";
        }
    }

    private static String formatIpAddress(int ip) {
        return String.format(
                Locale.US,
                "%d.%d.%d.%d",
                (ip & 0xff),
                (ip >> 8) & 0xff,
                (ip >> 16) & 0xff,
                (ip >> 24) & 0xff);
    }

    /**
     * Returns {@code "192.168.1."} style prefix for the /24 containing {@code ip}, or empty on
     * parse failure.
     */
    public static String getSubnet(String ip) {
        if (ip == null || ip.isEmpty()) {
            return "";
        }
        int lastDot = ip.lastIndexOf('.');
        if (lastDot < 0) {
            return "";
        }
        return ip.substring(0, lastDot + 1);
    }

    public static boolean isWifiConnected(Context ctx) {
        Context app = ctx.getApplicationContext();
        ConnectivityManager cm =
                (ConnectivityManager) app.getSystemService(Context.CONNECTIVITY_SERVICE);
        if (cm == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Network net = cm.getActiveNetwork();
            if (net == null) {
                return false;
            }
            NetworkCapabilities caps = cm.getNetworkCapabilities(net);
            return caps != null && caps.hasTransport(NetworkCapabilities.TRANSPORT_WIFI);
        }
        android.net.NetworkInfo ni = cm.getActiveNetworkInfo();
        return ni != null
                && ni.isConnected()
                && ni.getType() == ConnectivityManager.TYPE_WIFI;
    }

    /**
     * Attempts a TCP connect to {@code host:port} on a background thread; {@link PingCallback} is
     * always invoked on the main thread.
     */
    public static void pingHost(
            String host, int port, int timeoutMs, @Nullable PingCallback callback) {
        Handler main = new Handler(Looper.getMainLooper());
        if (host == null || host.isEmpty() || callback == null) {
            if (callback != null) {
                main.post(() -> callback.onResult(false, -1L));
            }
            return;
        }
        PING_EXECUTOR.execute(
                () -> {
                    long t0 = System.nanoTime();
                    boolean ok = false;
                    try (Socket s = new Socket()) {
                        s.connect(new InetSocketAddress(host, port), Math.max(1, timeoutMs));
                        ok = s.isConnected();
                    } catch (IOException ignored) {
                        ok = false;
                    }
                    long ms = (System.nanoTime() - t0) / 1_000_000L;
                    final boolean reachable = ok;
                    final long latency = ok ? ms : -1L;
                    main.post(() -> callback.onResult(reachable, latency));
                });
    }
}
