package com.example.agrilink.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agrilink.R;
import com.example.agrilink.models.LaborItem;

import java.util.List;

public class LaborAdapter extends RecyclerView.Adapter<LaborAdapter.ViewHolder> {

    private List<LaborItem> list;

    public LaborAdapter(List<LaborItem> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_labor_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        LaborItem item = list.get(position);
        if (item != null) {
            holder.tvCategory.setText(item.getCategory());
            holder.tvWage.setText(item.getWage());

            if (holder.tvLocation != null) {
                holder.tvLocation.setText("📍 " + item.getLocation());
            }
            if (holder.tvWorkerCount != null) {
                holder.tvWorkerCount.setText("🧑 " + item.getWorkerCount());
            }
            if (holder.tvDate != null) {
                holder.tvDate.setText("📅 " + item.getDate());
            }
            if (holder.tvSkill != null) {
                holder.tvSkill.setText("🔧 " + item.getSkill());
            }
            if (holder.tvUrgentBadge != null) {
                holder.tvUrgentBadge.setVisibility(item.isUrgent() ? View.VISIBLE : View.GONE);
            }
        }
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvUrgentBadge, tvLocation, tvWage, tvWorkerCount, tvDate, tvSkill;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvLaborCategory);
            tvUrgentBadge = itemView.findViewById(R.id.tvUrgentBadge);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvWage = itemView.findViewById(R.id.tvWage);
            tvWorkerCount = itemView.findViewById(R.id.tvWorkerCount);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvSkill = itemView.findViewById(R.id.tvSkill);
        }
    }
}
