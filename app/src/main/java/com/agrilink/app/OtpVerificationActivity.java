package com.agrilink.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class OtpVerificationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        View.OnClickListener verifyListener = v -> {
            startActivity(new Intent(OtpVerificationActivity.this, ChooseRoleActivity.class));
            finish();
        };

        if (findViewById(R.id.btnVerify) != null) {
            findViewById(R.id.btnVerify).setOnClickListener(verifyListener);
        }
    }
}
