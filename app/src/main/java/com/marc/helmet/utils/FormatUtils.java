package com.marc.helmet.utils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Text formatting for speed, angles, coordinates, SMS templates.
 */
public final class FormatUtils {

    private FormatUtils() {
    }

    public static String formatSpeed(float kmh) {
        return String.format(Locale.US, "%.0f km/h", kmh);
    }

    public static String formatCoordinate(double coord, boolean isLat) {
        double v = Math.abs(coord);
        String suffix;
        if (isLat) {
            suffix = coord >= 0 ? "N" : "S";
        } else {
            suffix = coord >= 0 ? "E" : "W";
        }
        return String.format(Locale.US, "%.6f° %s", v, suffix);
    }

    public static String formatAngle(double angle) {
        String sign = angle > 0 ? "+" : (angle < 0 ? "-" : "");
        return String.format(Locale.US, "%s%.1f°", sign, Math.abs(angle));
    }

    public static String formatDuration(long seconds) {
        long s = Math.max(0L, seconds);
        long h = s / 3600L;
        long m = (s % 3600L) / 60L;
        long sec = s % 60L;
        return String.format(Locale.US, "%02d:%02d:%02d", h, m, sec);
    }

    public static String formatTimestamp(long unixMs) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.US);
        return sdf.format(new Date(unixMs));
    }

    public static String formatIp(String ip, int port) {
        if (ip == null) {
            ip = "";
        }
        return ip + ":" + port;
    }

    public static String buildEmergencySms(
            String name,
            String bloodType,
            String allergies,
            String conditions,
            String medications,
            String notes,
            double lat,
            double lng) {
        String nm = nz(name);
        String blood = nz(bloodType);
        String all = nz(allergies);
        String cond = nz(conditions);
        String meds = nz(medications);
        String n = nz(notes);
        String latStr = formatCoordinate(lat, true);
        String lngStr = formatCoordinate(lng, false);
        String time = formatTimestamp(System.currentTimeMillis());
        return "[MARC EMERGENCY ALERT]\n\n"
                + nm
                + " has been in a motorcycle accident.\n\n"
                + "Location: "
                + latStr
                + ", "
                + lngStr
                + "\nTime: "
                + time
                + "\n\n"
                + "Medical Info:\nBlood Type: "
                + blood
                + "\nAllergies: "
                + all
                + "\n"
                + "Conditions: "
                + cond
                + "\nMedications: "
                + meds
                + "\nNotes: "
                + n
                + "\n\n"
                + "Please call emergency services immediately. — MARC System";
    }

    private static String nz(String s) {
        return s == null ? "" : s;
    }
}
