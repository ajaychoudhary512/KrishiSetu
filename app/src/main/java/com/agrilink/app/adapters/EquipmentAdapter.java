package com.agrilink.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agrilink.R;
import com.agrilink.app.models.EquipmentItem;

import java.util.List;

public class EquipmentAdapter extends RecyclerView.Adapter<EquipmentAdapter.ViewHolder> {

    private List<EquipmentItem> list;

    public EquipmentAdapter(List<EquipmentItem> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_equipment_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        EquipmentItem item = list.get(position);
        if (item != null) {
            holder.tvTitle.setText(item.getTitle());
            holder.tvRating.setText("★ " + item.getRating());
            holder.tvLocation.setText("📍 " + item.getLocation());
            holder.tvPrice.setText(item.getPrice());
            holder.tvStatusBadge.setText(item.getStatus());

            if (item.isAvailable()) {
                holder.tvStatusBadge.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.available_bg));
                holder.tvStatusBadge.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.available_text));
            } else {
                holder.tvStatusBadge.setBackgroundColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.booked_bg));
                holder.tvStatusBadge.setTextColor(ContextCompat.getColor(holder.itemView.getContext(), R.color.booked_text));
            }

            if (item.getImageResId() != 0) {
                holder.imgEquip.setImageResource(item.getImageResId());
            }
        }
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvRating, tvLocation, tvPrice, tvStatusBadge;
        ImageView imgEquip;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvEquipTitle);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvPrice = itemView.findViewById(R.id.tvEquipPrice);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            imgEquip = itemView.findViewById(R.id.imgEquip);
        }
    }
}
