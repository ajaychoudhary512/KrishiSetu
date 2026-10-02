package com.agrilink.app;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ChatDealActivity extends AppCompatActivity {

    public static class ChatMessage {
        public String message;
        public String time;
        public boolean isSent;

        public ChatMessage(String message, String time, boolean isSent) {
            this.message = message;
            this.time = time;
            this.isSent = isSent;
        }
    }

    private final List<ChatMessage> messageList = new ArrayList<>();
    private ChatAdapter chatAdapter;
    private RecyclerView rvChat;

    @Override
    protected void attachBaseContext(android.content.Context newBase) {
        super.attachBaseContext(LocaleHelper.onAttach(newBase));
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_deal);

        ImageView btnBack = findViewById(R.id.btnBackChat);
        Button btnAcceptEscrow = findViewById(R.id.btnAcceptEscrow);
        ImageButton btnSend = findViewById(R.id.btnSendMessage);
        EditText etMessage = findViewById(R.id.etChatMessage);
        rvChat = findViewById(R.id.rvChatMessages);

        TextView tvContactName = findViewById(R.id.tvChatContactName);
        TextView tvAvatarInitials = findViewById(R.id.tvChatAvatarInitials);

        String contact = getIntent().getStringExtra("contact_name");
        if (!TextUtils.isEmpty(contact) && tvContactName != null) {
            tvContactName.setText(contact);
            if (tvAvatarInitials != null) {
                String[] parts = contact.trim().split("\\s+");
                String ini = "" + parts[0].charAt(0);
                if (parts.length > 1 && parts[1].length() > 0) ini += parts[1].charAt(0);
                tvAvatarInitials.setText(ini.toUpperCase());
            }
        }

        // Setup Chat RecyclerView
        rvChat.setLayoutManager(new LinearLayoutManager(this));
        messageList.add(new ChatMessage(
                "Namaste! We are interested in your 50 Quintals Rice Straw listing in Indore.",
                "10:30 AM", false));
        messageList.add(new ChatMessage(
                "Namaste Ji. The straw has been sun-dried, moisture is below 12%, and baling is ready.",
                "10:35 AM", true));
        messageList.add(new ChatMessage(
                "Excellent. We have created an official KRISHISETU Escrow Proposal for ₹56,350 with 100% funds locked.",
                "10:40 AM", false));

        chatAdapter = new ChatAdapter(messageList);
        rvChat.setAdapter(chatAdapter);
        rvChat.scrollToPosition(messageList.size() - 1);

        btnBack.setOnClickListener(v -> finish());

        btnAcceptEscrow.setOnClickListener(v -> showDealCommissionSplitDialog());

        btnSend.setOnClickListener(v -> {
            String text = etMessage.getText().toString().trim();
            if (!text.isEmpty()) {
                messageList.add(new ChatMessage(text, "Just now", true));
                chatAdapter.notifyItemInserted(messageList.size() - 1);
                rvChat.smoothScrollToPosition(messageList.size() - 1);
                etMessage.setText("");
                Toast.makeText(ChatDealActivity.this, getString(R.string.message_sent_toast), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showDealCommissionSplitDialog() {
        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.escrow_deal_summary_title))
                .setMessage(getString(R.string.escrow_deal_summary_msg))
                .setPositiveButton(getString(R.string.confirm_lock_escrow), (dialog, which) -> {
                    Toast.makeText(ChatDealActivity.this, getString(R.string.escrow_locked_toast), Toast.LENGTH_LONG).show();
                    dialog.dismiss();
                })
                .setNegativeButton(getString(R.string.got_it), (d, w) -> d.dismiss())
                .show();
    }

    private static class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ViewHolder> {
        private final List<ChatMessage> items;

        public ChatAdapter(List<ChatMessage> items) {
            this.items = items;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_message, parent, false);
            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ChatMessage msg = items.get(position);
            if (msg.isSent) {
                holder.layoutSent.setVisibility(View.VISIBLE);
                holder.layoutReceived.setVisibility(View.GONE);
                holder.tvMessageSent.setText(msg.message);
                holder.tvTimeSent.setText(msg.time);
            } else {
                holder.layoutSent.setVisibility(View.GONE);
                holder.layoutReceived.setVisibility(View.VISIBLE);
                holder.tvMessageReceived.setText(msg.message);
                holder.tvTimeReceived.setText(msg.time);
            }
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            View layoutSent, layoutReceived;
            TextView tvMessageSent, tvTimeSent, tvMessageReceived, tvTimeReceived;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                layoutSent = itemView.findViewById(R.id.layoutSent);
                layoutReceived = itemView.findViewById(R.id.layoutReceived);
                tvMessageSent = itemView.findViewById(R.id.tvMessageSent);
                tvTimeSent = itemView.findViewById(R.id.tvTimeSent);
                tvMessageReceived = itemView.findViewById(R.id.tvMessageReceived);
                tvTimeReceived = itemView.findViewById(R.id.tvTimeReceived);
            }
        }
    }
}
