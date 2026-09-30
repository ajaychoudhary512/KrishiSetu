package com.agrilink.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import androidx.appcompat.app.AppCompatActivity;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        // Subtle Logo Entrance Animation (Fade & Gentle Scale)
        View cardLogo = findViewById(R.id.cardSplashLogo);
        View contentLayout = findViewById(R.id.layoutSplashContent);

        if (cardLogo != null) {
            cardLogo.setAlpha(0f);
            cardLogo.setScaleX(0.85f);
            cardLogo.setScaleY(0.85f);
            cardLogo.animate()
                    .alpha(1f)
                    .scaleX(1f)
                    .scaleY(1f)
                    .setDuration(900)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }

        if (contentLayout != null) {
            contentLayout.setAlpha(0f);
            contentLayout.animate()
                    .alpha(1f)
                    .setDuration(1000)
                    .start();
        }

        findViewById(R.id.btnGetStarted).setOnClickListener(v -> proceedToNextScreen());

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (!isFinishing()) {
                proceedToNextScreen();
            }
        }, 2200);
    }

    private void proceedToNextScreen() {
        SharedPreferences prefs = getSharedPreferences("agrilink_prefs", MODE_PRIVATE);
        String token = prefs.getString("access_token", null);

        Intent intent;
        if (!TextUtils.isEmpty(token)) {
            // Returning authenticated user: navigate to Home Dashboard
            intent = new Intent(SplashActivity.this, MainActivity.class);
        } else {
            intent = new Intent(SplashActivity.this, LoginActivity.class);
        }
        startActivity(intent);
        finish();
    }
}
