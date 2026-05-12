package com.marc.helmet.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;

import com.marc.helmet.R;

/** Branded fullscreen splash with fade-in, then navigates to {@link MainActivity}. */
public class SplashActivity extends AppCompatActivity {

    private static final int MAIN_DELAY_MS = 2500;
    private static final int FADE_MS = 800;

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow()
                .setFlags(
                        WindowManager.LayoutParams.FLAG_FULLSCREEN,
                        WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setContentView(R.layout.activity_splash);

        View centerGroup = findViewById(R.id.splash_center_group);
        centerGroup.setAlpha(0f);
        centerGroup.animate().alpha(1f).setDuration(FADE_MS).start();

        mainHandler.postDelayed(
                () -> {
                    startActivity(new Intent(this, MainActivity.class));
                    finish();
                },
                MAIN_DELAY_MS);
    }
}
