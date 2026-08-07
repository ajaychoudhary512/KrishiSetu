package com.agrilink.app;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class OnboardingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_onboarding);

        findViewById(R.id.btnNextOnboarding).setOnClickListener(v -> {
            startActivity(new Intent(OnboardingActivity.this, LoginActivity.class));
            finish();
        });

        findViewById(R.id.btnSkipOnboarding).setOnClickListener(v -> {
            startActivity(new Intent(OnboardingActivity.this, LoginActivity.class));
            finish();
        });
    }
}
