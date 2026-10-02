package com.agrilink.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agrilink.app.R;
import com.agrilink.app.models.WasteItem;

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
            holder.tvLocation.setText("📍 " + item.getLocation());
            holder.tvSeller.setText(item.getSellerName());
            holder.tvCategoryBadge.setText(item.getCategory());
            holder.tvVerifiedBadge.setVisibility(item.isVerified() ? View.VISIBLE : View.GONE);
            
            // Real Quantity & Stock Status
            if (holder.tvCardQuantity != null) {
                holder.tvCardQuantity.setText("📦 " + item.getQuantityDisplay());
            }
            if (holder.tvCardStatus != null) {
                if (item.isSoldOut()) {
                    holder.tvCardStatus.setText(holder.itemView.getContext().getString(R.string.sold_out_badge));
                    holder.tvCardStatus.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.status_urgent_bg));
                    holder.tvCardStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.status_urgent_text));
                } else if ("PARTIALLY_SOLD".equalsIgnoreCase(item.getStatus())) {
                    holder.tvCardStatus.setText(holder.itemView.getContext().getString(R.string.partially_sold_badge));
                    holder.tvCardStatus.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.soft_orange_bg));
                    holder.tvCardStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.harvest_orange));
                } else {
                    holder.tvCardStatus.setText(holder.itemView.getContext().getString(R.string.available_badge));
                    holder.tvCardStatus.setBackgroundColor(holder.itemView.getContext().getResources().getColor(R.color.status_available_bg));
                    holder.tvCardStatus.setTextColor(holder.itemView.getContext().getResources().getColor(R.color.status_available_text));
                }
            }

            if (item.getImageResId() != 0) {
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
        TextView tvTitle, tvPrice, tvLocation, tvSeller, tvCategoryBadge, tvVerifiedBadge, tvCardQuantity, tvCardStatus;
        ImageView imgWaste;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvWasteTitle);
            tvPrice = itemView.findViewById(R.id.tvPrice);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvSeller = itemView.findViewById(R.id.tvSeller);
            tvCategoryBadge = itemView.findViewById(R.id.tvCategoryBadge);
            tvVerifiedBadge = itemView.findViewById(R.id.tvVerifiedBadge);
            tvCardQuantity = itemView.findViewById(R.id.tvCardQuantity);
            tvCardStatus = itemView.findViewById(R.id.tvCardStatus);
            imgWaste = itemView.findViewById(R.id.imgWaste);
        }
    }
}
