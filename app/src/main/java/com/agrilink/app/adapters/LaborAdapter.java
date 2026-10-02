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

import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

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

            // Contact Thekedar / Call
            if (holder.btnViewDetails != null) {
                holder.btnViewDetails.setOnClickListener(v -> {
                    try {
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(Uri.parse("tel:+919826054321"));
                        v.getContext().startActivity(callIntent);
                    } catch (Exception e) {
                        Toast.makeText(v.getContext(), "Dialer unavailable", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // Hire Crew Button
            if (holder.btnHireNow != null) {
                holder.btnHireNow.setOnClickListener(v -> {
                    String msg = String.format(v.getContext().getString(R.string.hire_labour_msg),
                            item.getCategory(), item.getWage(), item.getLocation());

                    new AlertDialog.Builder(v.getContext())
                            .setTitle(v.getContext().getString(R.string.hire_labour_title))
                            .setMessage(msg)
                            .setPositiveButton(v.getContext().getString(R.string.confirm_hire), (d, w) -> {
                                Toast.makeText(v.getContext(), v.getContext().getString(R.string.hire_confirmed_toast), Toast.LENGTH_LONG).show();
                            })
                            .setNegativeButton(v.getContext().getString(R.string.got_it), null)
                            .show();
                });
            }
        }
    }

    @Override
    public int getItemCount() {
        return list != null ? list.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvCategory, tvUrgentBadge, tvLocation, tvWage, tvWorkerCount, tvDate, tvSkill;
        View btnViewDetails, btnHireNow;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvCategory = itemView.findViewById(R.id.tvLaborCategory);
            tvUrgentBadge = itemView.findViewById(R.id.tvUrgentBadge);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvWage = itemView.findViewById(R.id.tvWage);
            tvWorkerCount = itemView.findViewById(R.id.tvWorkerCount);
            tvDate = itemView.findViewById(R.id.tvDate);
            tvSkill = itemView.findViewById(R.id.tvSkill);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnHireNow = itemView.findViewById(R.id.btnHireNow);
        }
    }
}
