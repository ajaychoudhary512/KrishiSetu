package com.agrilink.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agrilink.app.ChatDealActivity;
import com.agrilink.app.R;

import java.util.ArrayList;
import java.util.List;

public class ChatFragment extends Fragment {

    public static class ConversationItem {
        public String name;
        public String initials;
        public String role;
        public String dealContext;
        public String lastMessage;
        public String time;
        public int unreadCount;

        public ConversationItem(String name, String initials, String role, String dealContext, String lastMessage, String time, int unreadCount) {
            this.name = name;
            this.initials = initials;
            this.role = role;
            this.dealContext = dealContext;
            this.lastMessage = lastMessage;
            this.time = time;
            this.unreadCount = unreadCount;
        }
    }

    private final List<ConversationItem> allConversations = new ArrayList<>();
    private final List<ConversationItem> displayedConversations = new ArrayList<>();
    private ConversationAdapter adapter;
    private View layoutEmpty;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        RecyclerView rv = view.findViewById(R.id.rvConversations);
        layoutEmpty = view.findViewById(R.id.layoutEmptyChat);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        allConversations.clear();
        allConversations.add(new ConversationItem(
                "GreenPower Bio-Industries", "GP",
                "Industry Buyer • Verified ✓", "🌾 50 Qtl Rice Straw",
                "Escrow Proposal: 50 Qtl @ ₹1,150/Qtl ready for review", "10:45 AM", 1));
        allConversations.add(new ConversationItem(
                "Suresh Kumar", "SK",
                "Equipment Owner • Verified ✓", "🚜 Mahindra Tractor 45HP",
                "Machine confirmed for rental dispatch tomorrow 7 AM", "Yesterday", 0));
        allConversations.add(new ConversationItem(
                "Pithampur Bio-Pellets Ltd", "PB",
                "Industry Buyer • Verified ✓", "🏭 Paddy Straw Bulk",
                "Escrow payment of ₹54,941 secured with KrishiSetu", "Oct 1", 0));

        displayedConversations.clear();
        displayedConversations.addAll(allConversations);

        adapter = new ConversationAdapter(displayedConversations, item -> {
            Intent intent = new Intent(getContext(), ChatDealActivity.class);
            intent.putExtra("contact_name", item.name);
            intent.putExtra("deal_context", item.dealContext);
            startActivity(intent);
        });
        rv.setAdapter(adapter);

        EditText etSearch = view.findViewById(R.id.etSearchChat);
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    filter(s.toString().trim());
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        return view;
    }

    private void filter(String query) {
        displayedConversations.clear();
        if (TextUtils.isEmpty(query)) {
            displayedConversations.addAll(allConversations);
        } else {
            String lower = query.toLowerCase();
            for (ConversationItem item : allConversations) {
                if (item.name.toLowerCase().contains(lower) ||
                    item.dealContext.toLowerCase().contains(lower) ||
                    item.lastMessage.toLowerCase().contains(lower)) {
                    displayedConversations.add(item);
                }
            }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(displayedConversations.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }

    private static class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ViewHolder> {
        private final List<ConversationItem> items;
        private final OnItemClickListener listener;

        interface OnItemClickListener {
            void onItemClick(ConversationItem item);
        }

        public ConversationAdapter(List<ConversationItem> items, OnItemClickListener listener) {
            this.items = items;
            this.listener = listener;
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_conversation, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            ConversationItem item = items.get(position);
            holder.tvInitials.setText(item.initials);
            holder.tvName.setText(item.name);
            holder.tvRole.setText(item.role);
            holder.tvDealContext.setText(item.dealContext);
            holder.tvLastMessage.setText(item.lastMessage);
            holder.tvTime.setText(item.time);

            if (item.unreadCount > 0) {
                holder.tvUnreadBadge.setVisibility(View.VISIBLE);
                holder.tvUnreadBadge.setText(String.valueOf(item.unreadCount));
            } else {
                holder.tvUnreadBadge.setVisibility(View.GONE);
            }

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onItemClick(item);
            });
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        static class ViewHolder extends RecyclerView.ViewHolder {
            TextView tvInitials, tvName, tvRole, tvDealContext, tvLastMessage, tvTime, tvUnreadBadge;

            public ViewHolder(@NonNull View itemView) {
                super(itemView);
                tvInitials = itemView.findViewById(R.id.tvConvAvatarInitials);
                tvName = itemView.findViewById(R.id.tvConvName);
                tvRole = itemView.findViewById(R.id.tvConvRole);
                tvDealContext = itemView.findViewById(R.id.tvConvDealContext);
                tvLastMessage = itemView.findViewById(R.id.tvConvLastMessage);
                tvTime = itemView.findViewById(R.id.tvConvTime);
                tvUnreadBadge = itemView.findViewById(R.id.tvConvUnreadBadge);
            }
        }
    }
}
