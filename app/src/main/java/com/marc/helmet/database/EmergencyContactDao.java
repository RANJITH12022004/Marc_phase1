package com.marc.helmet.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.marc.helmet.models.EmergencyContact;

import java.util.ArrayList;
import java.util.List;

public class EmergencyContactDao {

    private static final String TABLE = "emergency_contacts";
    private static final String COL_ID = "id";
    private static final String COL_PRIORITY = "priority";
    private static final String COL_NAME = "name";
    private static final String COL_PHONE = "phone";
    private static final String COL_RELATIONSHIP = "relationship";
    private static final String COL_CREATED_AT = "created_at";

    private final DatabaseHelper dbHelper;

    public EmergencyContactDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public List<EmergencyContact> getAllContacts() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        List<EmergencyContact> list = new ArrayList<>();
        try (Cursor c = db.query(
                TABLE,
                null,
                null,
                null,
                null,
                null,
                COL_PRIORITY + " ASC")) {
            while (c.moveToNext()) {
                list.add(parseContact(c));
            }
        }
        return list;
    }

    public EmergencyContact getPrimaryContact() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try (Cursor c = db.query(
                TABLE,
                null,
                COL_PRIORITY + " = ?",
                new String[]{"1"},
                null,
                null,
                null,
                "1")) {
            if (c.moveToFirst()) {
                return parseContact(c);
            }
        }
        return null;
    }

    public long insertContact(EmergencyContact contact) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        return db.insert(TABLE, null, toContentValues(contact, false));
    }

    public void updateContact(EmergencyContact contact) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.update(
                TABLE,
                toContentValues(contact, true),
                COL_ID + " = ?",
                new String[]{String.valueOf(contact.getId())});
    }

    public void deleteContact(int id) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.delete(TABLE, COL_ID + " = ?", new String[]{String.valueOf(id)});
    }

    public void reorderContacts(List<Integer> orderedIds) {
        if (orderedIds == null || orderedIds.isEmpty()) {
            return;
        }
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        db.beginTransaction();
        try {
            for (int i = 0; i < orderedIds.size(); i++) {
                ContentValues cv = new ContentValues();
                cv.put(COL_PRIORITY, i + 1);
                db.update(
                        TABLE,
                        cv,
                        COL_ID + " = ?",
                        new String[]{String.valueOf(orderedIds.get(i))});
            }
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
        }
    }

    public int getContactCount() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try (Cursor c = db.rawQuery("SELECT COUNT(*) FROM " + TABLE, null)) {
            if (c.moveToFirst()) {
                return c.getInt(0);
            }
        }
        return 0;
    }

    private static ContentValues toContentValues(EmergencyContact e, boolean forUpdate) {
        ContentValues cv = new ContentValues();
        if (forUpdate) {
            cv.put(COL_ID, e.getId());
        }
        cv.put(COL_PRIORITY, e.getPriority());
        cv.put(COL_NAME, e.getName());
        cv.put(COL_PHONE, e.getPhone());
        cv.put(COL_RELATIONSHIP, e.getRelationship());
        cv.put(COL_CREATED_AT, e.getCreatedAt());
        return cv;
    }

    private static EmergencyContact parseContact(Cursor c) {
        EmergencyContact e = new EmergencyContact();
        e.setId(c.getInt(c.getColumnIndexOrThrow(COL_ID)));
        e.setPriority(c.getInt(c.getColumnIndexOrThrow(COL_PRIORITY)));
        e.setName(getStringOrNull(c, COL_NAME));
        e.setPhone(getStringOrNull(c, COL_PHONE));
        e.setRelationship(getStringOrNull(c, COL_RELATIONSHIP));
        e.setCreatedAt(c.getLong(c.getColumnIndexOrThrow(COL_CREATED_AT)));
        return e;
    }

    private static String getStringOrNull(Cursor c, String col) {
        int idx = c.getColumnIndex(col);
        if (idx < 0 || c.isNull(idx)) {
            return null;
        }
        return c.getString(idx);
    }
}
