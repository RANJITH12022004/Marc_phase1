package com.marc.helmet.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.HashMap;
import java.util.Map;

public class SettingsDao {

    private static final String TABLE = "settings";
    private static final String COL_KEY = "key";
    private static final String COL_VALUE = "value";

    private static final String KEY_AI_ENGINE = "ai_engine";
    private static final String KEY_OLLAMA_IP = "ollama_ip";
    private static final String KEY_GEMINI_API_KEY = "gemini_api_key";
    private static final String KEY_SPEED_ALERT_THRESHOLD_KMH = "speed_alert_threshold_kmh";

    private final DatabaseHelper dbHelper;

    public SettingsDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public String getSetting(String key) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try (Cursor c = db.query(
                TABLE,
                new String[]{COL_VALUE},
                COL_KEY + " = ?",
                new String[]{key},
                null,
                null,
                null)) {
            if (c.moveToFirst()) {
                int idx = c.getColumnIndexOrThrow(COL_VALUE);
                if (c.isNull(idx)) {
                    return null;
                }
                return c.getString(idx);
            }
        }
        return null;
    }

    public String getSetting(String key, String defaultValue) {
        String v = getSetting(key);
        return v != null ? v : defaultValue;
    }

    public void setSetting(String key, String value) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_KEY, key);
        cv.put(COL_VALUE, value);
        db.insertWithOnConflict(TABLE, null, cv, SQLiteDatabase.CONFLICT_REPLACE);
    }

    public Map<String, String> getAllSettings() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        Map<String, String> map = new HashMap<>();
        try (Cursor c = db.query(TABLE, new String[]{COL_KEY, COL_VALUE}, null, null, null, null, null)) {
            int iKey = c.getColumnIndexOrThrow(COL_KEY);
            int iVal = c.getColumnIndexOrThrow(COL_VALUE);
            while (c.moveToNext()) {
                String k = c.getString(iKey);
                String v = c.isNull(iVal) ? null : c.getString(iVal);
                map.put(k, v);
            }
        }
        return map;
    }

    public boolean isGeminiMode() {
        String v = getSetting(KEY_AI_ENGINE);
        if (v == null || v.trim().isEmpty()) {
            return true;
        }
        return "gemini".equalsIgnoreCase(v.trim());
    }

    public String getOllamaIp() {
        return getSetting(KEY_OLLAMA_IP, "");
    }

    public String getGeminiApiKey() {
        String v = getSetting(KEY_GEMINI_API_KEY, "");
        return v != null ? v.trim() : "";
    }

    public float getSpeedThreshold() {
        try {
            return Float.parseFloat(getSetting(KEY_SPEED_ALERT_THRESHOLD_KMH, "80"));
        } catch (NumberFormatException e) {
            return 80f;
        }
    }
}
