package com.marc.helmet.models;

public class Calibration {

    private int id;
    private int deviceId;
    private double standingRoll;
    private double standingPitch;
    private double maxLeftRoll;
    private double maxRightRoll;
    private long calibratedAt;

    public Calibration() {
    }

    public Calibration(
            int id,
            int deviceId,
            double standingRoll,
            double standingPitch,
            double maxLeftRoll,
            double maxRightRoll,
            long calibratedAt) {
        this.id = id;
        this.deviceId = deviceId;
        this.standingRoll = standingRoll;
        this.standingPitch = standingPitch;
        this.maxLeftRoll = maxLeftRoll;
        this.maxRightRoll = maxRightRoll;
        this.calibratedAt = calibratedAt;
    }

    public boolean isCrash(double currentRoll) {
        double minSafe = Math.min(maxLeftRoll, maxRightRoll);
        double maxSafe = Math.max(maxLeftRoll, maxRightRoll);
        return currentRoll < minSafe || currentRoll > maxSafe;
    }

    public double getCrashThreshold() {
        return Math.abs(maxRightRoll - maxLeftRoll) / 2.0d;
    }

    public boolean isCalibrated() {
        return calibratedAt > 0;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(int deviceId) {
        this.deviceId = deviceId;
    }

    public double getStandingRoll() {
        return standingRoll;
    }

    public void setStandingRoll(double standingRoll) {
        this.standingRoll = standingRoll;
    }

    public double getStandingPitch() {
        return standingPitch;
    }

    public void setStandingPitch(double standingPitch) {
        this.standingPitch = standingPitch;
    }

    public double getMaxLeftRoll() {
        return maxLeftRoll;
    }

    public void setMaxLeftRoll(double maxLeftRoll) {
        this.maxLeftRoll = maxLeftRoll;
    }

    public double getMaxRightRoll() {
        return maxRightRoll;
    }

    public void setMaxRightRoll(double maxRightRoll) {
        this.maxRightRoll = maxRightRoll;
    }

    public long getCalibratedAt() {
        return calibratedAt;
    }

    public void setCalibratedAt(long calibratedAt) {
        this.calibratedAt = calibratedAt;
    }

    @Override
    public String toString() {
        return "Calibration{"
                + "id=" + id
                + ", deviceId=" + deviceId
                + ", standingRoll=" + standingRoll
                + ", standingPitch=" + standingPitch
                + ", maxLeftRoll=" + maxLeftRoll
                + ", maxRightRoll=" + maxRightRoll
                + ", calibratedAt=" + calibratedAt
                + '}';
    }
}
