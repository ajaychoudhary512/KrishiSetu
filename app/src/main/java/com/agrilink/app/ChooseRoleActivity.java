package com.example.agrilink;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;

public class ChooseRoleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_role);

        View.OnClickListener roleListener = v -> {
            startActivity(new Intent(ChooseRoleActivity.this, MainActivity.class));
            finish();
        };

        findViewById(R.id.cardFarmerRole).setOnClickListener(roleListener);
        findViewById(R.id.cardBuyerRole).setOnClickListener(roleListener);
        findViewById(R.id.cardEquipRole).setOnClickListener(roleListener);
        findViewById(R.id.cardLaborRole).setOnClickListener(roleListener);
    }
}
