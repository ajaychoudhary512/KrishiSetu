package com.agrilink.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {

    private EditText etMobile;
    private EditText etPassword;
    private CheckBox cbRememberMe;

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        etMobile = findViewById(R.id.etMobile);
        etPassword = findViewById(R.id.etPassword);
        cbRememberMe = findViewById(R.id.cbRememberMe);

        SharedPreferences prefs = getSharedPreferences(KrishiSetuApplication.PREFS_NAME, Context.MODE_PRIVATE);
        boolean remember = prefs.getBoolean("remember_me", true);
        if (cbRememberMe != null) {
            cbRememberMe.setChecked(remember);
            if (remember) {
                String savedPhone = prefs.getString("user_phone", "");
                if (!TextUtils.isEmpty(savedPhone) && etMobile != null) {
                    etMobile.setText(savedPhone);
                }
            }
        }

        // Login Submit
        findViewById(R.id.btnLogin).setOnClickListener(v -> performLogin());

        // Language Toggle Action
        com.google.android.material.button.MaterialButton btnLang = findViewById(R.id.btnLanguageToggle);
        if (btnLang != null) {
            btnLang.setText(LocaleHelper.getLanguageToggleText(this));
            btnLang.setOnClickListener(v -> LocaleHelper.toggleLanguage(LoginActivity.this));
        }

        // Register Link
        findViewById(R.id.tvRegister).setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, RegisterActivity.class));
        });

        // Forgot Password Action
        View tvForgotPassword = findViewById(R.id.tvForgotPassword);
        if (tvForgotPassword != null) {
            tvForgotPassword.setOnClickListener(v -> showForgotPasswordDialog());
        }

        // Optional Social Logins
        View btnGoogle = findViewById(R.id.btnContinueGoogle);
        if (btnGoogle != null) {
            btnGoogle.setOnClickListener(v -> {
                Toast.makeText(this, getString(R.string.connecting_google), Toast.LENGTH_SHORT).show();
            });
        }

        View btnPhone = findViewById(R.id.btnContinuePhone);
        if (btnPhone != null) {
            btnPhone.setOnClickListener(v -> {
                Intent intent = new Intent(LoginActivity.this, OtpVerificationActivity.class);
                String mobile = etMobile != null ? etMobile.getText().toString().trim() : "";
                if (!TextUtils.isEmpty(mobile)) {
                    intent.putExtra("phone_number", mobile);
                }
                startActivity(intent);
            });
        }
    }

    private void showForgotPasswordDialog() {
        android.widget.EditText input = new android.widget.EditText(this);
        input.setHint(getString(R.string.mobile_or_email));
        input.setInputType(android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS);
        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle(getString(R.string.forgot_password))
                .setMessage(getString(R.string.reset_password_instruction))
                .setView(input)
                .setPositiveButton(getString(R.string.send_reset_link), (d, w) -> {
                    String val = input.getText().toString().trim();
                    if (!android.text.TextUtils.isEmpty(val)) {
                        Toast.makeText(this, getString(R.string.reset_sent_to, val), Toast.LENGTH_LONG).show();
                    }
                })
                .setNegativeButton(getString(R.string.cancel), (d, w) -> d.dismiss())
                .show();
    }

    private void performLogin() {
        String identifier = etMobile != null ? etMobile.getText().toString().trim() : "";
        String password = etPassword != null ? etPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(identifier)) {
            Toast.makeText(this, getString(R.string.invalid_mobile_number), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, getString(R.string.incorrect_password), Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("username", identifier);
            jsonBody.put("password", password);

            Toast.makeText(this, getString(R.string.logging_in), Toast.LENGTH_SHORT).show();

            ApiClient.post("/auth/login", jsonBody.toString(), new ApiClient.ApiCallback() {
                @Override
                public void onSuccess(String response, int statusCode) {
                    if (statusCode >= 200 && statusCode < 300) {
                        try {
                            String token = "";
                            String fullName = "";
                            String rawRole = "";
                            if (!TextUtils.isEmpty(response)) {
                                JSONObject root = new JSONObject(response);
                                JSONObject data = root.optJSONObject("data");
                                if (data != null) {
                                    token = data.optString("access_token");
                                    fullName = data.optString("full_name", "");
                                    rawRole = data.optString("role", "");
                                } else {
                                    token = root.optString("access_token");
                                    fullName = root.optString("full_name", "");
                                    rawRole = root.optString("role", "");
                                }
                            }

                            if (TextUtils.isEmpty(fullName)) {
                                fullName = cleanDisplayNameFromEmail(identifier);
                            }

                            // Save JWT Token, User Info, and Role
                            SharedPreferences prefs = getSharedPreferences(KrishiSetuApplication.PREFS_NAME, Context.MODE_PRIVATE);
                            boolean isRemember = cbRememberMe != null && cbRememberMe.isChecked();
                            SharedPreferences.Editor editor = prefs.edit()
                                .putString("access_token", token)
                                .putString("user_name", fullName)
                                .putString("user_phone", identifier)
                                .putBoolean("remember_me", isRemember);

                            if (!TextUtils.isEmpty(rawRole)) {
                                String userRole = "Farmer";
                                String apiRole = rawRole.toUpperCase();
                                if (apiRole.contains("BUYER")) {
                                    userRole = "Industry Buyer";
                                    apiRole = "BUYER";
                                } else if (apiRole.contains("LABOR") || apiRole.contains("LABOUR")) {
                                    userRole = "Labour";
                                    apiRole = "LABOR";
                                } else if (apiRole.contains("EQUIPMENT")) {
                                    userRole = "Equipment Owner";
                                    apiRole = "EQUIPMENT";
                                } else if (apiRole.contains("CONTRACTOR")) {
                                    userRole = "Contractor";
                                    apiRole = "CONTRACTOR";
                                } else if (apiRole.contains("TRANSPORTER")) {
                                    userRole = "Transporter";
                                    apiRole = "TRANSPORTER";
                                } else {
                                    userRole = "Farmer";
                                    apiRole = "FARMER";
                                }
                                editor.putString("user_role", userRole);
                                editor.putString("api_role", apiRole);
                            }
                            editor.apply();

                            Toast.makeText(LoginActivity.this, getString(R.string.login_successful), Toast.LENGTH_SHORT).show();
                            startActivity(new Intent(LoginActivity.this, MainActivity.class));
                            finish();
                        } catch (Exception e) {
                            Toast.makeText(LoginActivity.this, getString(R.string.login_successful), Toast.LENGTH_SHORT).show();
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
                    Toast.makeText(LoginActivity.this, getString(R.string.check_internet_connection), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, getString(R.string.something_went_wrong), Toast.LENGTH_SHORT).show();
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
