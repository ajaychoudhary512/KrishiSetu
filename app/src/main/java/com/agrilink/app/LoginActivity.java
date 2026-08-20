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
                    if (statusCode >= 200 && statusCode < 300) {
                        try {
                            String token = "";
                            String fullName = "";
                            if (!TextUtils.isEmpty(response)) {
                                JSONObject root = new JSONObject(response);
                                JSONObject data = root.optJSONObject("data");
                                if (data != null) {
                                    token = data.optString("access_token");
                                    fullName = data.optString("full_name", "");
                                } else {
                                    token = root.optString("access_token");
                                    fullName = root.optString("full_name", "");
                                }
                            }

                            if (TextUtils.isEmpty(fullName)) {
                                fullName = cleanDisplayNameFromEmail(identifier);
                            }

                            // Save JWT Token and User Info
                            SharedPreferences prefs = getSharedPreferences("agrilink_prefs", Context.MODE_PRIVATE);
                            prefs.edit()
                                .putString("access_token", token)
                                .putString("user_name", fullName)
                                .putString("user_phone", identifier)
                                .apply();

                            Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } catch (Exception e) {
                            Toast.makeText(LoginActivity.this, "Login Successful!", Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        }
                        return;
                    }

                    // Parse error message safely
                    String errorMsg = "Login failed (Code " + statusCode + ")";
                    try {
                        if (!TextUtils.isEmpty(response)) {
                            JSONObject root = new JSONObject(response);
                            errorMsg = root.optString("detail", root.optString("message", errorMsg));
                        }
                    } catch (Exception e) {
                        if (!TextUtils.isEmpty(response)) {
                            errorMsg = response;
                        }
                    }
                    Toast.makeText(LoginActivity.this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
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

    private String cleanDisplayNameFromEmail(String input) {
        if (TextUtils.isEmpty(input)) return "Farmer";
        if (input.contains("@")) {
            String namePart = input.split("@")[0];
            namePart = namePart.replaceAll("[._-]", " ");
            String[] words = namePart.trim().split("\\s+");
            StringBuilder sb = new StringBuilder();
            for (String w : words) {
                if (w.length() > 0) {
                    sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1).toLowerCase()).append(" ");
                }
            }
            return sb.toString().trim();
        }
        return input;
    }
}
