package com.agrilink.app;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

public class OtpVerificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        findViewById(R.id.btnVerifyOtp).setOnClickListener(v -> {
            startActivity(new Intent(OtpVerificationActivity.this, ChooseRoleActivity.class));
            finish();
        });
    }
}
