package com.marc.helmet.database;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.marc.helmet.models.Device;

import java.util.ArrayList;
import java.util.List;

public class DeviceDao {

    private static final String TABLE = "devices";
    private static final String COL_ID = "id";
    private static final String COL_DEVICE_TYPE = "device_type";
    private static final String COL_IP_ADDRESS = "ip_address";
    private static final String COL_PORT = "port";
    private static final String COL_FIRMWARE_VERSION = "firmware_version";
    private static final String COL_LAST_CONNECTED = "last_connected";
    private static final String COL_IS_CONNECTED = "is_connected";

    private final DatabaseHelper dbHelper;

    public DeviceDao(DatabaseHelper dbHelper) {
        this.dbHelper = dbHelper;
    }

    public List<Device> getAllDevices() {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        List<Device> list = new ArrayList<>();
        try (Cursor c = db.query(
                TABLE,
                null,
                null,
                null,
                null,
                null,
                COL_DEVICE_TYPE + " ASC")) {
            while (c.moveToNext()) {
                list.add(parseDevice(c));
            }
        }
        return list;
    }

    public Device getHelmet() {
        return getDeviceByType(Device.HELMET);
    }

    public Device getBike() {
        return getDeviceByType(Device.BIKE);
    }

    private Device getDeviceByType(String deviceType) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        try (Cursor c = db.query(
                TABLE,
                null,
                COL_DEVICE_TYPE + " = ?",
                new String[]{deviceType},
                null,
                null,
                null,
                "1")) {
            if (c.moveToFirst()) {
                return parseDevice(c);
            }
        }
        return null;
    }

    public long insertOrUpdateDevice(Device device) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        String type = device.getDeviceType();
        long existingId = -1;
        try (Cursor c = db.query(
                TABLE,
                new String[]{COL_ID},
                COL_DEVICE_TYPE + " = ?",
                new String[]{type},
                null,
                null,
                null)) {
            if (c.moveToFirst()) {
                existingId = c.getLong(c.getColumnIndexOrThrow(COL_ID));
            }
        }

        ContentValues cv = toContentValues(device);
        if (existingId >= 0) {
            db.update(
                    TABLE,
                    cv,
                    COL_DEVICE_TYPE + " = ?",
                    new String[]{type});
            return existingId;
        }

        return db.insert(TABLE, null, cv);
    }

    public void setConnected(String deviceType, boolean connected) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_IS_CONNECTED, connected ? 1 : 0);
        db.update(TABLE, cv, COL_DEVICE_TYPE + " = ?", new String[]{deviceType});
    }

    public void updateIp(String deviceType, String ip) {
        SQLiteDatabase db = dbHelper.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put(COL_IP_ADDRESS, ip);
        db.update(TABLE, cv, COL_DEVICE_TYPE + " = ?", new String[]{deviceType});
    }

    private static ContentValues toContentValues(Device d) {
        ContentValues cv = new ContentValues();
        if (d.getId() > 0) {
            cv.put(COL_ID, d.getId());
        }
        cv.put(COL_DEVICE_TYPE, d.getDeviceType());
        cv.put(COL_IP_ADDRESS, d.getIpAddress());
        cv.put(COL_PORT, d.getPort());
        cv.put(COL_FIRMWARE_VERSION, d.getFirmwareVersion());
        cv.put(COL_LAST_CONNECTED, d.getLastConnected());
        cv.put(COL_IS_CONNECTED, d.isConnected() ? 1 : 0);
        return cv;
    }

    private static Device parseDevice(Cursor c) {
        Device d = new Device();
        d.setId(c.getInt(c.getColumnIndexOrThrow(COL_ID)));
        d.setDeviceType(getStringOrNull(c, COL_DEVICE_TYPE));
        d.setIpAddress(getStringOrNull(c, COL_IP_ADDRESS));
        d.setPort(c.getInt(c.getColumnIndexOrThrow(COL_PORT)));
        d.setFirmwareVersion(getStringOrNull(c, COL_FIRMWARE_VERSION));
        d.setLastConnected(c.getLong(c.getColumnIndexOrThrow(COL_LAST_CONNECTED)));
        d.setConnected(c.getInt(c.getColumnIndexOrThrow(COL_IS_CONNECTED)) != 0);
        return d;
    }

    private static String getStringOrNull(Cursor c, String col) {
        int idx = c.getColumnIndex(col);
        if (idx < 0 || c.isNull(idx)) {
            return null;
        }
        return c.getString(idx);
    }
}
