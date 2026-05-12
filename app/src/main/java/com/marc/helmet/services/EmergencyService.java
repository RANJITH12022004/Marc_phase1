package com.marc.helmet.services;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.telephony.SmsManager;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.marc.helmet.database.DatabaseHelper;
import com.marc.helmet.database.EmergencyContactDao;
import com.marc.helmet.database.UserProfileDao;
import com.marc.helmet.models.EmergencyContact;
import com.marc.helmet.models.UserProfile;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Coordinates emergency countdown, auto-call to primary contact, and SMS to all contacts.
 * Long SMS bodies use multipart delivery. Requires {@code CALL_PHONE}, {@code SEND_SMS}, and runtime
 * permission checks by the caller.
 */
public class EmergencyService {

    private static final String TAG_SMS = "MARC_SMS";

    public static final int COUNTDOWN_SECONDS = 10;

    public static final String ACTION_EMERGENCY_STARTED = "com.marc.helmet.EMERGENCY_STARTED";
    public static final String ACTION_EMERGENCY_CANCELLED = "com.marc.helmet.EMERGENCY_CANCELLED";
    public static final String ACTION_EMERGENCY_COMPLETED = "com.marc.helmet.EMERGENCY_COMPLETED";
    public static final String ACTION_COUNTDOWN_TICK = "com.marc.helmet.COUNTDOWN_TICK";
    public static final String EXTRA_SECONDS_REMAINING = "seconds_remaining";

    public interface EmergencyListener {
        void onCountdownTick(int secondsRemaining);

        void onEmergencyStarted();

        void onEmergencyCancelled();

        void onEmergencyCompleted();
    }

    private final Context appContext;
    private final EmergencyContactDao contactDao;
    private final UserProfileDao profileDao;
    private final Handler handler = new Handler(Looper.getMainLooper());

    private volatile boolean emergencyActive;
    private volatile boolean countingDown;
    private volatile boolean cancelled;

    private EmergencyListener currentListener;
    private double lastLat;
    private double lastLng;

    private int secondsLeft;
    private final Runnable countdownRunnable =
            new Runnable() {
                @Override
                public void run() {
                    if (cancelled || currentListener == null) {
                        return;
                    }
                    currentListener.onCountdownTick(secondsLeft);
                    broadcastCountdown(secondsLeft);

                    if (secondsLeft == 0) {
                        executeEmergency(lastLat, lastLng);
                        if (currentListener != null && !cancelled) {
                            currentListener.onEmergencyCompleted();
                        }
                        broadcastCompleted();
                        finishEmergencyState();
                        return;
                    }

                    secondsLeft--;
                    handler.postDelayed(this, 1000L);
                }
            };

    /** Wires DAOs from {@link DatabaseHelper#getInstance(Context)}. */
    public EmergencyService(Context context) {
        this(
                context,
                new EmergencyContactDao(DatabaseHelper.getInstance(context)),
                new UserProfileDao(DatabaseHelper.getInstance(context)));
    }

    /** Explicit DAO injection (tests / custom instances). */
    public EmergencyService(
            Context context, EmergencyContactDao contactDao, UserProfileDao profileDao) {
        this.appContext = context.getApplicationContext();
        this.contactDao = contactDao;
        this.profileDao = profileDao;
    }

    /**
     * Places call + SMS immediately (no internal countdown). Use when the host UI already counted
     * down (e.g. {@link com.marc.helmet.activities.CrashAlertActivity}).
     */
    public void dispatchEmergencyNow(double lat, double lng, EmergencyListener listener) {
        executeEmergency(lat, lng);
        if (listener != null) {
            listener.onEmergencyCompleted();
        }
        broadcastCompleted();
    }

    public void triggerEmergency(double lat, double lng, EmergencyListener listener) {
        if (listener == null) {
            return;
        }
        if (emergencyActive) {
            return;
        }

        cancelled = false;
        emergencyActive = true;
        countingDown = true;
        lastLat = lat;
        lastLng = lng;
        currentListener = listener;

        currentListener.onEmergencyStarted();
        broadcastStarted();

        secondsLeft = COUNTDOWN_SECONDS;
        handler.removeCallbacks(countdownRunnable);
        handler.post(countdownRunnable);
    }

    public void cancelEmergency() {
        if (!emergencyActive && !countingDown) {
            return;
        }
        cancelled = true;
        handler.removeCallbacks(countdownRunnable);
        EmergencyListener listener = currentListener;
        if (listener != null) {
            listener.onEmergencyCancelled();
        }
        broadcastCancelled();
        finishEmergencyState();
    }

    private void finishEmergencyState() {
        emergencyActive = false;
        countingDown = false;
        currentListener = null;
        cancelled = false;
    }

    public boolean isEmergencyActive() {
        return emergencyActive;
    }

    public boolean isCountingDown() {
        return countingDown;
    }

    private void executeEmergency(double lat, double lng) {
        String smsBody = buildEmergencySms(lat, lng);
        List<EmergencyContact> all = contactDao.getAllContacts();
        Log.d(TAG_SMS, "Sending SMS to " + (all != null ? all.size() : 0) + " contacts");
        if (all == null || all.isEmpty()) {
            Log.e(TAG_SMS, "NO CONTACTS SAVED — SMS not sent");
        } else {
            SmsManager smsManager = obtainSmsManager();
            if (smsManager == null) {
                Log.e(TAG_SMS, "SmsManager unavailable");
            } else {
                for (EmergencyContact c : all) {
                    if (c == null || c.getPhone() == null || c.getPhone().trim().isEmpty()) {
                        Log.w(TAG_SMS, "Skipping contact with empty phone");
                        continue;
                    }
                    if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.SEND_SMS)
                            != PackageManager.PERMISSION_GRANTED) {
                        Log.e(TAG_SMS, "SEND_SMS permission not granted");
                        continue;
                    }
                    sendEmergencySmsParts(smsManager, c.getPhone().trim(), smsBody, c.getPriority());
                }
            }
        }

        EmergencyContact primary = contactDao.getPrimaryContact();
        String primaryPhone =
                primary != null ? normalizePhone(primary.getPhone()) : "";
        if (!primaryPhone.isEmpty()) {
            if (ActivityCompat.checkSelfPermission(appContext, Manifest.permission.CALL_PHONE)
                    == PackageManager.PERMISSION_GRANTED) {
                Intent call =
                        new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + Uri.encode(primaryPhone)));
                call.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                try {
                    appContext.startActivity(call);
                } catch (Exception ignored) {
                }
            }
        }
    }

    /** Digits/plus after stripping spaces/dashes/parentheses; empty if unusable. */
    private static String normalizePhone(String raw) {
        if (raw == null) {
            return "";
        }
        String stripped = raw.replaceAll("[\\s\\-\\u00A0().]", "").trim();
        if (stripped.isEmpty()) {
            return "";
        }
        StringBuilder digits = new StringBuilder(stripped.length());
        for (int i = 0; i < stripped.length(); i++) {
            char ch = stripped.charAt(i);
            if (Character.isDigit(ch) || ch == '+') {
                digits.append(ch);
            }
        }
        return digits.toString();
    }

    /** Uses multipart when {@link SmsManager#divideMessage} splits (long UCS-2 / GSM bodies). */
    private void sendEmergencySmsParts(
            SmsManager smsManager, String phone, String smsBody, int priority) {
        try {
            ArrayList<String> parts = smsManager.divideMessage(smsBody);
            if (parts.size() > 1) {
                smsManager.sendMultipartTextMessage(phone, null, parts, null, null);
            } else {
                smsManager.sendTextMessage(phone, null, smsBody, null, null);
            }
            Log.d(TAG_SMS, "SMS queued to " + phone + " (" + parts.size() + " parts), priority " + priority);
        } catch (Exception e) {
            Log.e(TAG_SMS, "SMS failed to " + phone + ": " + e.getMessage());
        }
    }

    @SuppressWarnings("deprecation")
    private SmsManager obtainSmsManager() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            SmsManager sm = appContext.getSystemService(SmsManager.class);
            if (sm != null) {
                return sm;
            }
        }
        return SmsManager.getDefault();
    }

    private String buildEmergencySms(double lat, double lng) {
        UserProfile p = profileDao.getProfile();
        String name = p != null && p.getName() != null && !p.getName().isEmpty() ? p.getName() : "Rider";
        String blood = safe(p != null ? p.getBloodType() : null);
        String allergies = safe(p != null ? p.getAllergies() : null);
        String conditions = safe(p != null ? p.getMedicalConditions() : null);
        String medications = safe(p != null ? p.getMedications() : null);
        String notes = safe(p != null ? p.getEmergencyNotes() : null);

        String time =
                new SimpleDateFormat("yyyy-MM-dd HH:mm:ss z", Locale.US).format(new Date());
        String loc = formatLocation(lat, lng);
        String mapLine = "";
        if (Math.abs(lat) > 1e-5 || Math.abs(lng) > 1e-5) {
            String ll = String.format(Locale.US, "%.6f,%.6f", lat, lng);
            mapLine = "\nMaps: https://maps.google.com/?q=" + Uri.encode(ll);
        }

        return "[MARC EMERGENCY ALERT]\n\n"
                + name
                + " has been in a motorcycle accident.\n\n"
                + "Location: "
                + loc
                + mapLine
                + "\n"
                + "Time: "
                + time
                + "\n\n"
                + "Medical Info:\nBlood Type: "
                + blood
                + "\nAllergies: "
                + allergies
                + "\nConditions: "
                + conditions
                + "\nMedications: "
                + medications
                + "\nNotes: "
                + notes
                + "\n\nPlease call emergency services immediately. — MARC System";
    }

    private static String safe(String s) {
        return s == null || s.isEmpty() ? "None" : s;
    }

    private static String formatLocation(double lat, double lng) {
        String ns = lat >= 0 ? "N" : "S";
        String ew = lng >= 0 ? "E" : "W";
        return String.format(
                Locale.US,
                "%.6f° %s, %.6f° %s",
                Math.abs(lat),
                ns,
                Math.abs(lng),
                ew);
    }

    private void broadcastStarted() {
        LocalBroadcastManager.getInstance(appContext)
                .sendBroadcast(new Intent(ACTION_EMERGENCY_STARTED));
    }

    private void broadcastCancelled() {
        LocalBroadcastManager.getInstance(appContext)
                .sendBroadcast(new Intent(ACTION_EMERGENCY_CANCELLED));
    }

    private void broadcastCompleted() {
        LocalBroadcastManager.getInstance(appContext)
                .sendBroadcast(new Intent(ACTION_EMERGENCY_COMPLETED));
    }

    private void broadcastCountdown(int secondsRemaining) {
        Intent i = new Intent(ACTION_COUNTDOWN_TICK);
        i.putExtra(EXTRA_SECONDS_REMAINING, secondsRemaining);
        LocalBroadcastManager.getInstance(appContext).sendBroadcast(i);
    }
}
