package com.agrilink.app;

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

        btnAcceptEscrow.setOnClickListener(v -> showDealCommissionSplitDialog());

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                Toast.makeText(ChatDealActivity.this, "Message Sent", Toast.LENGTH_SHORT).show();
                etMessage.setText("");
            }
        });
    }

    private void showDealCommissionSplitDialog() {
        String msg = "💳 DEAL SUMMARY & AUTOMATIC SPLIT PAYOUT\n\n" +
                "📦 Deal Item: 10 Tons Paddy Straw Stubble\n" +
                "💰 Total Deal Value: ₹56,350.00\n\n" +
                "⚡ AUTOMATIC PAYMENT ROUTING:\n" +
                "• 👨‍🌾 Direct Payout to Farmer: ₹54,941.25 (97.5%)\n" +
                "  └ Transferred directly to Farmer's Bank / UPI.\n\n" +
                "• ⚡ Platform Service Commission: ₹1,408.75 (2.5%)\n" +
                "  └ Sent directly to AgriLink Platform Account.\n\n" +
                "🔒 Escrow Safety: Buyer funds remain protected until delivery OTP verification.";

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("⚡ Lock Escrow & Process Payout Split")
                .setMessage(msg)
                .setPositiveButton("Confirm & Lock Escrow", (dialog, which) -> {
                    Toast.makeText(ChatDealActivity.this, "🎉 Deal Locked! ₹54,941.25 queued for Farmer and ₹1,408.75 platform fee secured!", Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                })
                .setNegativeButton("Cancel", (d, w) -> d.dismiss())
                .show();
    }
}
