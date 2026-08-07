package com.agrilink.app;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONObject;

public class RegisterActivity extends AppCompatActivity {

    private EditText etFullName;
    private EditText etMobile;
    private EditText etEmail;
    private EditText etPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        etFullName = findViewById(R.id.etFullName);
        etMobile = findViewById(R.id.etMobile);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);

        // Register Submit
        findViewById(R.id.btnRegister).setOnClickListener(v -> performRegistration());

        // Login Link
        findViewById(R.id.tvLogin).setOnClickListener(v -> finish());
    }

    private void performRegistration() {
        String fullName = etFullName != null ? etFullName.getText().toString().trim() : "";
        String mobile = etMobile != null ? etMobile.getText().toString().trim() : "";
        String email = etEmail != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword != null ? etPassword.getText().toString().trim() : "";

        if (TextUtils.isEmpty(fullName) || fullName.length() < 2) {
            Toast.makeText(this, "Please enter a valid full name (at least 2 characters)", Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(mobile)) {
            Toast.makeText(this, "Please enter your mobile number", Toast.LENGTH_SHORT).show();
            return;
        }

        // Standardize phone number with +91 prefix if missing
        String formattedPhone = mobile;
        if (!formattedPhone.startsWith("+")) {
            formattedPhone = "+91" + formattedPhone;
        }

        if (TextUtils.isEmpty(password) || password.length() < 8) {
            Toast.makeText(this, "Password must be at least 8 characters long", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            JSONObject jsonBody = new JSONObject();
            jsonBody.put("full_name", fullName);
            jsonBody.put("phone", formattedPhone);
            if (!TextUtils.isEmpty(email)) {
                jsonBody.put("email", email);
            }
            jsonBody.put("password", password);
            jsonBody.put("role", "farmer");

            Toast.makeText(this, "Creating account...", Toast.LENGTH_SHORT).show();

            ApiClient.post("/auth/register", jsonBody.toString(), new ApiClient.ApiCallback() {
                @Override
                public void onSuccess(String response, int statusCode) {
                    try {
                        JSONObject root = new JSONObject(response);
                        if (statusCode == 201 || statusCode == 200) {
                            Toast.makeText(RegisterActivity.this, "Account Created Successfully! Please verify OTP.", Toast.LENGTH_LONG).show();
                            startActivity(new Intent(RegisterActivity.this, OtpVerificationActivity.class));
                            finish();
                        } else {
                            String errorMsg = "Registration failed";
                            if (root.has("detail")) {
                                Object detail = root.get("detail");
                                if (detail instanceof JSONArray) {
                                    JSONArray arr = (JSONArray) detail;
                                    if (arr.length() > 0) {
                                        JSONObject errObj = arr.optJSONObject(0);
                                        errorMsg = errObj != null ? errObj.optString("msg", errorMsg) : arr.getString(0);
                                    }
                                } else {
                                    errorMsg = detail.toString();
                                }
                            } else if (root.has("message")) {
                                errorMsg = root.optString("message");
                            }
                            Toast.makeText(RegisterActivity.this, "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(RegisterActivity.this, "Response parsing error", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onError(Exception e) {
                    Toast.makeText(RegisterActivity.this, "Connection failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
                }
            });
        } catch (Exception e) {
            Toast.makeText(this, "Failed to prepare request", Toast.LENGTH_SHORT).show();
        }
    }
}
