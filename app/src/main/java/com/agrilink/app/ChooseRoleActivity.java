package com.agrilink.app;

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

        if (findViewById(R.id.roleFarmer) != null) findViewById(R.id.roleFarmer).setOnClickListener(roleListener);
        if (findViewById(R.id.roleIndustry) != null) findViewById(R.id.roleIndustry).setOnClickListener(roleListener);
        if (findViewById(R.id.roleEquipment) != null) findViewById(R.id.roleEquipment).setOnClickListener(roleListener);
        if (findViewById(R.id.roleContractor) != null) findViewById(R.id.roleContractor).setOnClickListener(roleListener);
        if (findViewById(R.id.roleLaborer) != null) findViewById(R.id.roleLaborer).setOnClickListener(roleListener);
        if (findViewById(R.id.roleTransporter) != null) findViewById(R.id.roleTransporter).setOnClickListener(roleListener);
    }
}
