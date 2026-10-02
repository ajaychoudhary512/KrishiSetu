package com.agrilink.app;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ChooseRoleActivity extends AppCompatActivity {

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_role);

        // Language Toggle Action
        com.google.android.material.button.MaterialButton btnLang = findViewById(R.id.btnLanguageToggle);
        if (btnLang != null) {
            btnLang.setText(LocaleHelper.getLanguageToggleText(this));
            btnLang.setOnClickListener(v -> LocaleHelper.toggleLanguage(ChooseRoleActivity.this));
        }

        setupRoleCard(R.id.roleFarmer, "Farmer", "farmer");
        setupRoleCard(R.id.roleIndustry, "Industry Buyer", "industry");
        setupRoleCard(R.id.roleEquipment, "Equipment Owner", "equipment");
        setupRoleCard(R.id.roleContractor, "Contractor", "contractor");
        setupRoleCard(R.id.roleLaborer, "Labour", "labor");
        setupRoleCard(R.id.roleTransporter, "Transporter", "transport");
    }

    private void setupRoleCard(int cardId, String displayRole, String apiRole) {
        View card = findViewById(cardId);
        if (card != null) {
            card.setOnClickListener(v -> {
                SharedPreferences prefs = getSharedPreferences(KrishiSetuApplication.PREFS_NAME, Context.MODE_PRIVATE);
                prefs.edit()
                        .putString("user_role", displayRole)
                        .putString("api_role", apiRole)
                        .apply();

                Toast.makeText(this, displayRole, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(ChooseRoleActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish();
            });
        }
    }
}
