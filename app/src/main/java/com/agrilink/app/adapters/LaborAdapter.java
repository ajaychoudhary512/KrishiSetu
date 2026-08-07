package com.agrilink.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.agrilink.app.R;
import com.agrilink.app.models.LaborItem;

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
            holder.tvLocation.setText("📍 " + item.getLocation());
            holder.tvWage.setText(item.getWage());
            holder.tvWorkerCount.setText("🧑 " + item.getWorkerCount());
            holder.tvDate.setText("📅 " + item.getDate());
            holder.tvSkill.setText("🔧 " + item.getSkill());

            holder.tvUrgentBadge.setVisibility(item.isUrgent() ? View.VISIBLE : View.GONE);
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
