package com.marc.helmet.models;

public class Device {

    public static final String HELMET = "MARC_HELMET";
    public static final String BIKE = "MARC_BIKE";

    private int id;
    private String deviceType;
    private String ipAddress;
    private int port;
    private String firmwareVersion;
    private long lastConnected;
    private boolean connected;

    public Device() {
    }

    public Device(
            int id,
            String deviceType,
            String ipAddress,
            int port,
            String firmwareVersion,
            long lastConnected,
            boolean connected) {
        this.id = id;
        this.deviceType = deviceType;
        this.ipAddress = ipAddress;
        this.port = port;
        this.firmwareVersion = firmwareVersion;
        this.lastConnected = lastConnected;
        this.connected = connected;
    }

    public String getBaseUrl() {
        return "http://" + ipAddress + ":" + port;
    }

    public boolean isHelmet() {
        return HELMET.equals(deviceType);
    }

    public boolean isConnected() {
        return connected;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getDeviceType() {
        return deviceType;
    }

    public void setDeviceType(String deviceType) {
        this.deviceType = deviceType;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public int getPort() {
        return port;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public String getFirmwareVersion() {
        return firmwareVersion;
    }

    public void setFirmwareVersion(String firmwareVersion) {
        this.firmwareVersion = firmwareVersion;
    }

    public long getLastConnected() {
        return lastConnected;
    }

    public void setLastConnected(long lastConnected) {
        this.lastConnected = lastConnected;
    }

    public void setConnected(boolean connected) {
        this.connected = connected;
    }

    @Override
    public String toString() {
        return "Device{"
                + "id=" + id
                + ", deviceType='" + deviceType + '\''
                + ", ipAddress='" + ipAddress + '\''
                + ", port=" + port
                + ", firmwareVersion='" + firmwareVersion + '\''
                + ", lastConnected=" + lastConnected
                + ", connected=" + connected
                + '}';
    }
}
