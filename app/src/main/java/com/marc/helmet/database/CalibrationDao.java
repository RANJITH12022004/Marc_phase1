package com.marc.helmet.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.marc.helmet.models.Calibration;

public class CalibrationDao {

    private static final String TABLE = "calibration";
    private static final String COL_ID = "id";
    private static final String COL_DEVICE_ID = "device_id";
    private static final String COL_STANDING_ROLL = "standing_roll";
    private static final String COL_STANDING_PITCH = "standing_pitch";
    private static final String COL_MAX_LEFT_ROLL = "max_left_roll";
    private static final String COL_MAX_RIGHT_ROLL = "max_right_roll";
    private static final String COL_CALIBRATED_AT = "calibrated_at";

    private final DatabaseHelper dbHelper;

    public CalibrationDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public Calibration getCalibrationForDevice(int deviceId) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try (Cursor c = db.query(
                TABLE,
                null,
                COL_DEVICE_ID + " = ?",
                new String[]{String.valueOf(deviceId)},
                null,
                null,
                null,
                "1")) {
            if (c.moveToFirst()) {
                return parseCalibration(c);
            }
        }
        return null;
    }

    public long insertOrUpdateCalibration(Calibration calibration) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        int deviceId = calibration.getDeviceId();
        long existingRowId = -1;
        try (Cursor c = db.query(
                TABLE,
                new String[]{COL_ID},
                COL_DEVICE_ID + " = ?",
                new String[]{String.valueOf(deviceId)},
                null,
                null,
                null)) {
            if (c.moveToFirst()) {
                existingRowId = c.getLong(c.getColumnIndexOrThrow(COL_ID));
            }
        }

        ContentValues cv = toContentValues(calibration);
        if (existingRowId >= 0) {
            db.update(
                    TABLE,
                    cv,
                    COL_DEVICE_ID + " = ?",
                    new String[]{String.valueOf(deviceId)});
            return existingRowId;
        }

        return db.insert(TABLE, null, cv);
    }

    public boolean isCalibrated(int deviceId) {
        Calibration c = getCalibrationForDevice(deviceId);
        return c != null && c.isCalibrated();
    }

    private static ContentValues toContentValues(Calibration cal) {
        ContentValues cv = new ContentValues();
        if (cal.getId() > 0) {
            cv.put(COL_ID, cal.getId());
        }
        cv.put(COL_DEVICE_ID, cal.getDeviceId());
        cv.put(COL_STANDING_ROLL, cal.getStandingRoll());
        cv.put(COL_STANDING_PITCH, cal.getStandingPitch());
        cv.put(COL_MAX_LEFT_ROLL, cal.getMaxLeftRoll());
        cv.put(COL_MAX_RIGHT_ROLL, cal.getMaxRightRoll());
        cv.put(COL_CALIBRATED_AT, cal.getCalibratedAt());
        return cv;
    }

    private static Calibration parseCalibration(Cursor c) {
        Calibration cal = new Calibration();
        cal.setId(c.getInt(c.getColumnIndexOrThrow(COL_ID)));
        cal.setDeviceId(c.getInt(c.getColumnIndexOrThrow(COL_DEVICE_ID)));
        cal.setStandingRoll(c.getDouble(c.getColumnIndexOrThrow(COL_STANDING_ROLL)));
        cal.setStandingPitch(c.getDouble(c.getColumnIndexOrThrow(COL_STANDING_PITCH)));
        cal.setMaxLeftRoll(c.getDouble(c.getColumnIndexOrThrow(COL_MAX_LEFT_ROLL)));
        cal.setMaxRightRoll(c.getDouble(c.getColumnIndexOrThrow(COL_MAX_RIGHT_ROLL)));
        cal.setCalibratedAt(c.getLong(c.getColumnIndexOrThrow(COL_CALIBRATED_AT)));
        return cal;
    }
}
