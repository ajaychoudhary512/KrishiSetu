package com.agrilink.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class OtpVerificationActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp_verification);

        // Language Switcher
        com.google.android.material.button.MaterialButton btnLang = findViewById(R.id.btnLanguageToggle);
        if (btnLang != null) {
            btnLang.setText(LocaleHelper.getLanguageToggleText(this));
            btnLang.setOnClickListener(v -> LocaleHelper.toggleLanguage(OtpVerificationActivity.this));
        }

        // Phone Input Handling
        android.widget.EditText etOtpPhone = findViewById(R.id.etOtpPhone);
        String passedPhone = getIntent().getStringExtra("phone_number");
        if (etOtpPhone != null && passedPhone != null) {
            etOtpPhone.setText(passedPhone.replace("+91", "").trim());
        }

        View.OnClickListener verifyListener = v -> {
            if (etOtpPhone != null && android.text.TextUtils.isEmpty(etOtpPhone.getText().toString().trim())) {
                android.widget.Toast.makeText(this, getString(R.string.err_enter_mobile), android.widget.Toast.LENGTH_SHORT).show();
                return;
            }
            startActivity(new Intent(OtpVerificationActivity.this, ChooseRoleActivity.class));
            finish();
        };

        if (findViewById(R.id.btnVerify) != null) {
            findViewById(R.id.btnVerify).setOnClickListener(verifyListener);
        }
    }
}
