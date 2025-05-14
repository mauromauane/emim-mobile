package com.example.emim;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.WindowManager;

import androidx.appcompat.app.AppCompatActivity;



public class LandingActivity extends AppCompatActivity {
    private static final long SPLASH_DELAY_MS = 2000; // 2 seconds

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.landing);

        getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        // Optional: hide the action bar for a full‐screen splash
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Delay for SPLASH_DELAY_MS then start LoginActivity
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            Intent intent = new Intent(LandingActivity.this, LoginActivity.class);
            startActivity(intent);
            finish(); // close this activity so the user can't return to it
        }, SPLASH_DELAY_MS);
    }
}
