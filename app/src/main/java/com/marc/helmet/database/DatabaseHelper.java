package com.marc.helmet.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * SQLite helper for MARC — user profile, contacts, devices, calibration, settings, chat, rides.
 */
public final class DatabaseHelper extends SQLiteOpenHelper {

    public static final String DATABASE_NAME = "marc.db";
    public static final int DATABASE_VERSION = 1;

    private static final String TABLE_USER_PROFILE = "user_profile";
    private static final String TABLE_EMERGENCY_CONTACTS = "emergency_contacts";
    private static final String TABLE_DEVICES = "devices";
    private static final String TABLE_CALIBRATION = "calibration";
    private static final String TABLE_SETTINGS = "settings";
    private static final String TABLE_MARC_CONVERSATIONS = "marc_conversations";
    private static final String TABLE_RIDE_SESSIONS = "ride_sessions";

    private static final String SQL_CREATE_USER_PROFILE =
            "CREATE TABLE IF NOT EXISTS " + TABLE_USER_PROFILE + " ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "name TEXT,"
                    + "age INTEGER,"
                    + "blood_type TEXT,"
                    + "allergies TEXT,"
                    + "medical_conditions TEXT,"
                    + "medications TEXT,"
                    + "emergency_notes TEXT,"
                    + "profile_photo_path TEXT,"
                    + "updated_at INTEGER"
                    + ")";

    private static final String SQL_CREATE_EMERGENCY_CONTACTS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_EMERGENCY_CONTACTS + " ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "priority INTEGER,"
                    + "name TEXT NOT NULL,"
                    + "phone TEXT NOT NULL,"
                    + "relationship TEXT,"
                    + "created_at INTEGER"
                    + ")";

    private static final String SQL_CREATE_DEVICES =
            "CREATE TABLE IF NOT EXISTS " + TABLE_DEVICES + " ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "device_type TEXT,"
                    + "ip_address TEXT NOT NULL,"
                    + "port INTEGER DEFAULT 80,"
                    + "firmware_version TEXT,"
                    + "last_connected INTEGER,"
                    + "is_connected INTEGER DEFAULT 0"
                    + ")";

    private static final String SQL_CREATE_CALIBRATION =
            "CREATE TABLE IF NOT EXISTS " + TABLE_CALIBRATION + " ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "device_id INTEGER,"
                    + "standing_roll REAL,"
                    + "standing_pitch REAL,"
                    + "max_left_roll REAL,"
                    + "max_right_roll REAL,"
                    + "calibrated_at INTEGER"
                    + ")";

    private static final String SQL_CREATE_SETTINGS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_SETTINGS + " ("
                    + "key TEXT PRIMARY KEY,"
                    + "value TEXT"
                    + ")";

    private static final String SQL_CREATE_MARC_CONVERSATIONS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_MARC_CONVERSATIONS + " ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "role TEXT,"
                    + "message TEXT NOT NULL,"
                    + "timestamp INTEGER,"
                    + "session_id TEXT"
                    + ")";

    private static final String SQL_CREATE_RIDE_SESSIONS =
            "CREATE TABLE IF NOT EXISTS " + TABLE_RIDE_SESSIONS + " ("
                    + "id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "start_time INTEGER,"
                    + "end_time INTEGER,"
                    + "max_speed_kmh REAL,"
                    + "crash_detected INTEGER DEFAULT 0,"
                    + "crash_time INTEGER,"
                    + "notes TEXT"
                    + ")";

    private static volatile DatabaseHelper instance;

    public static DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            synchronized (DatabaseHelper.class) {
                if (instance == null) {
                    instance = new DatabaseHelper(context.getApplicationContext());
                }
            }
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_USER_PROFILE);
        db.execSQL(SQL_CREATE_EMERGENCY_CONTACTS);
        db.execSQL(SQL_CREATE_DEVICES);
        db.execSQL(SQL_CREATE_CALIBRATION);
        db.execSQL(SQL_CREATE_SETTINGS);
        db.execSQL(SQL_CREATE_MARC_CONVERSATIONS);
        db.execSQL(SQL_CREATE_RIDE_SESSIONS);

        if (isSettingsTableEmpty(db)) {
            insertDefaultSettings(db);
        }
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USER_PROFILE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_EMERGENCY_CONTACTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_DEVICES);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CALIBRATION);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SETTINGS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MARC_CONVERSATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_RIDE_SESSIONS);
        onCreate(db);
    }

    private static boolean isSettingsTableEmpty(SQLiteDatabase db) {
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_SETTINGS, null)) {
            if (c.moveToFirst()) {
                return c.getInt(0) == 0;
            }
        }
        return true;
    }

    private static void insertDefaultSettings(SQLiteDatabase db) {
        insertSetting(db, "ai_engine", "gemini");
        insertSetting(db, "gemini_api_key", "");
        insertSetting(db, "ollama_ip", "192.168.1.100:11434");
        insertSetting(db, "ollama_model", "llama3.2:3b-instruct-q4_K_M");
        insertSetting(db, "wake_word_engine", "always_on");
        insertSetting(db, "speed_alert_threshold_kmh", "80");
    }

    private static void insertSetting(SQLiteDatabase db, String key, String value) {
        ContentValues cv = new ContentValues();
        cv.put("key", key);
        cv.put("value", value);
        db.insertWithOnConflict(
                TABLE_SETTINGS,
                null,
                cv,
                SQLiteDatabase.CONFLICT_IGNORE);
    }
}
