package com.example.agrilink.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agrilink.R;
import com.example.agrilink.models.WasteItem;

import java.util.List;

public class WasteAdapter extends RecyclerView.Adapter<WasteAdapter.ViewHolder> {

    private List<WasteItem> wasteList;
    private OnDealClickListener listener;

    public interface OnDealClickListener {
        void onDealClick(WasteItem item);
    }

    public WasteAdapter(List<WasteItem> wasteList, OnDealClickListener listener) {
        this.wasteList = wasteList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_waste_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        WasteItem item = wasteList.get(position);
        if (item != null) {
            holder.tvTitle.setText(item.getTitle());
            holder.tvPrice.setText(item.getPrice());

            if (holder.tvLocation != null) {
                holder.tvLocation.setText("📍 " + item.getLocation());
            }
            if (holder.tvSeller != null) {
                holder.tvSeller.setText(item.getSellerName());
            }
            if (holder.tvCategoryBadge != null) {
                holder.tvCategoryBadge.setText(item.getCategory());
            }
            if (holder.tvVerifiedBadge != null) {
                holder.tvVerifiedBadge.setVisibility(item.isVerified() ? View.VISIBLE : View.GONE);
            }
            if (holder.imgWaste != null && item.getImageResId() != 0) {
                holder.imgWaste.setImageResource(item.getImageResId());
            }

            holder.itemView.setOnClickListener(v -> {
                if (listener != null) listener.onDealClick(item);
            });
        }
    }

    @Override
    public int getItemCount() {
        return wasteList != null ? wasteList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle, tvPrice, tvLocation, tvSeller, tvCategoryBadge, tvVerifiedBadge;
        ImageView imgWaste;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvWasteTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvSeller = itemView.findViewById(R.id.tvSeller);
            tvCategoryBadge = itemView.findViewById(R.id.tvCategoryBadge);
            tvVerifiedBadge = itemView.findViewById(R.id.tvVerifiedBadge);
            imgWaste = itemView.findViewById(R.id.imgWaste);
        }
    }
}
