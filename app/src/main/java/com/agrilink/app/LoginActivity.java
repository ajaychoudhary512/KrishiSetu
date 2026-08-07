package com.agrilink.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private EditText etMobile;
    private EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etMobile = findViewById(R.id.etMobile);
        etPassword = findViewById(R.id.etPassword);

        // Login Submit
        findViewById(R.id.btnLogin).setOnClickListener(v -> performLogin());

        // Register Link
        findViewById(R.id.tvRegister).setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });
    }

    private void performLogin() {
        String identifier = etMobile != null ? etMobile.getText().toString().trim() : "";
        String password = etPassword != null ? etPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(identifier)) {
            Toast.makeText(this, "Please enter your mobile number or email", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Please enter your password", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("username", identifier);
            jsonBody.put("password", password);

            Toast.makeText(this, "Logging in...", Toast.LENGTH_SHORT).show();

            ApiClient.post("/auth/login", jsonBody.toString(), new ApiClient.ApiCallback() {
                @Override
                public void onSuccess(String response, int statusCode) {
                    try {
                        JSONObject root = new JSONObject(response);
                        if (statusCode >= 200 && statusCode < 300) {
                            JSONObject data = root.optJSONObject("data");
                            String token = data != null ? data.optString("access_token") : root.optString("access_token");

                            // Save JWT Token
                            SharedPreferences prefs = getSharedPreferences("agrilink_prefs", Context.MODE_PRIVATE);
                            prefs.edit().putString("access_token", token).apply();

                            Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } else {
                            String errorMsg = root.optString("detail", root.optString("message", "Login failed"));
                            Toast.makeText(LoginActivity.this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(LoginActivity.this, "Response parsing error", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(LoginActivity.this, "Connection failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Failed to prepare request", Toast.LENGTH_SHORT).show();
        }
    }
}
