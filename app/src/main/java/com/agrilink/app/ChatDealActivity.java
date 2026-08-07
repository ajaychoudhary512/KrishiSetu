package com.example.agrilink;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class ChatDealActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_deal);

        ImageView btnBack = findViewById(R.id.btnBackChat);
        Button btnAcceptEscrow = findViewById(R.id.btnAcceptEscrow);
        ImageButton btnSend = findViewById(R.id.btnSendMessage);
        EditText etMessage = findViewById(R.id.etChatMessage);

        btnBack.setOnClickListener(v -> finish());

        btnAcceptEscrow.setOnClickListener(v -> {
            Toast.makeText(ChatDealActivity.this, "🎉 Deal Approved & Escrow Deposit Locked!", Toast.LENGTH_LONG).show();
        });

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                Toast.makeText(ChatDealActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
                etMessage.setText("");
            }
        });
    }
}
