package com.marc.helmet.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.marc.helmet.models.UserProfile;

public class UserProfileDao {

    private static final String TABLE = "user_profile";
    private static final String COL_ID = "id";
    private static final String COL_NAME = "name";
    private static final String COL_AGE = "age";
    private static final String COL_BLOOD_TYPE = "blood_type";
    private static final String COL_ALLERGIES = "allergies";
    private static final String COL_MEDICAL_CONDITIONS = "medical_conditions";
    private static final String COL_MEDICATIONS = "medications";
    private static final String COL_EMERGENCY_NOTES = "emergency_notes";
    private static final String COL_PROFILE_PHOTO_PATH = "profile_photo_path";
    private static final String COL_UPDATED_AT = "updated_at";

    private final DatabaseHelper dbHelper;

    public UserProfileDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public UserProfile getProfile() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try (Cursor c = db.query(
                TABLE,
                null,
                null,
                null,
                null,
                null,
                COL_ID + " ASC",
                "1")) {
            if (c.moveToFirst()) {
                return parseProfile(c);
            }
        }
        return null;
    }

    public long insertOrUpdateProfile(UserProfile profile) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = toContentValues(profile);

        int id = profile.getId();
        if (id > 0) {
            cv.put(COL_ID, id);
            int updated = db.update(TABLE, cv, COL_ID + " = ?", new String[]{String.valueOf(id)});
            if (updated > 0) {
                return id;
            }
            cv.remove(COL_ID);
            return db.insert(TABLE, null, cv);
        }

        return db.insert(TABLE, null, cv);
    }

    public void clearProfile() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TABLE, null, null);
    }

    private static ContentValues toContentValues(UserProfile p) {
        ContentValues cv = new ContentValues();
        cv.put(COL_NAME, p.getName());
        cv.put(COL_AGE, p.getAge());
        cv.put(COL_BLOOD_TYPE, p.getBloodType());
        cv.put(COL_ALLERGIES, p.getAllergies());
        cv.put(COL_MEDICAL_CONDITIONS, p.getMedicalConditions());
        cv.put(COL_MEDICATIONS, p.getMedications());
        cv.put(COL_EMERGENCY_NOTES, p.getEmergencyNotes());
        cv.put(COL_PROFILE_PHOTO_PATH, p.getProfilePhotoPath());
        cv.put(COL_UPDATED_AT, p.getUpdatedAt());
        return cv;
    }

    private static UserProfile parseProfile(Cursor c) {
        UserProfile p = new UserProfile();
        p.setId(c.getInt(c.getColumnIndexOrThrow(COL_ID)));
        p.setName(getStringOrNull(c, COL_NAME));
        p.setAge(c.getInt(c.getColumnIndexOrThrow(COL_AGE)));
        p.setBloodType(getStringOrNull(c, COL_BLOOD_TYPE));
        p.setAllergies(getStringOrNull(c, COL_ALLERGIES));
        p.setMedicalConditions(getStringOrNull(c, COL_MEDICAL_CONDITIONS));
        p.setMedications(getStringOrNull(c, COL_MEDICATIONS));
        p.setEmergencyNotes(getStringOrNull(c, COL_EMERGENCY_NOTES));
        p.setProfilePhotoPath(getStringOrNull(c, COL_PROFILE_PHOTO_PATH));
        p.setUpdatedAt(c.getLong(c.getColumnIndexOrThrow(COL_UPDATED_AT)));
        return p;
    }

    private static String getStringOrNull(Cursor c, String col) {
        int idx = c.getColumnIndex(col);
        if (idx < 0 || c.isNull(idx)) {
            return null;
        }
        return c.getString(idx);
    }
}
