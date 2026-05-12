package com.marc.helmet.services;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

/**
 * Process-wide crash/emergency UI state ({@linkplain com.marc.helmet.MarcApplication} singleton).
 */
public class CrashViewModel {

    private final MutableLiveData<Boolean> isCrashActive = new MutableLiveData<>(false);
    private final MutableLiveData<Integer> countdownSeconds = new MutableLiveData<>(10);
    private final MutableLiveData<Double> crashLat = new MutableLiveData<>(0.0);
    private final MutableLiveData<Double> crashLng = new MutableLiveData<>(0.0);
    private final MutableLiveData<String> crashStatus = new MutableLiveData<>("MONITORING");

    public void triggerCrash(double lat, double lng) {
        crashLat.postValue(lat);
        crashLng.postValue(lng);
        countdownSeconds.postValue(10);
        crashStatus.postValue("CRASH_DETECTED");
        isCrashActive.postValue(true);
    }

    public void updateCountdown(int s) {
        countdownSeconds.postValue(s);
    }

    public void cancelCrash() {
        isCrashActive.postValue(false);
        crashStatus.postValue("CANCELLED");
    }

    public void completeCrash() {
        isCrashActive.postValue(false);
        crashStatus.postValue("EMERGENCY_COMPLETE");
    }

    public void resetState() {
        isCrashActive.postValue(false);
        countdownSeconds.postValue(10);
        crashLat.postValue(0.0);
        crashLng.postValue(0.0);
        crashStatus.postValue("MONITORING");
    }

    public LiveData<Boolean> getIsCrashActive() {
        return isCrashActive;
    }

    public LiveData<Integer> getCountdownSeconds() {
        return countdownSeconds;
    }

    public LiveData<Double> getCrashLat() {
        return crashLat;
    }

    public LiveData<Double> getCrashLng() {
        return crashLng;
    }

    public LiveData<String> getCrashStatus() {
        return crashStatus;
    }
}
