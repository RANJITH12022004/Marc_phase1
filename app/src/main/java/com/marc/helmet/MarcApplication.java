package com.marc.helmet;

import android.app.Application;

import androidx.appcompat.app.AppCompatDelegate;

import com.marc.helmet.services.CrashViewModel;

/**
 * Application singleton: theme pin + shared {@link CrashViewModel} for UI and foreground service.
 */
public class MarcApplication extends Application {

    private CrashViewModel crashViewModel;
    private static MarcApplication instance;

    @Override
    public void onCreate() {
        super.onCreate();
        instance = this;
        crashViewModel = new CrashViewModel();
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    }

    public static MarcApplication getInstance() {
        return instance;
    }

    public static CrashViewModel getCrashViewModel() {
        if (instance == null) {
            throw new IllegalStateException("MarcApplication not initialized");
        }
        return instance.crashViewModel;
    }
}
