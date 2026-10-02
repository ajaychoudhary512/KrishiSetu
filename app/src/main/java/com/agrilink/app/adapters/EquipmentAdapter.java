package com.agrilink.app.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.agrilink.app.R;
import com.agrilink.app.models.EquipmentItem;

import java.util.List;

import android.content.Intent;
import android.net.Uri;
import android.widget.Toast;
import androidx.appcompat.app.AlertDialog;

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

            // Call Owner / View Details
            if (holder.btnViewDetails != null) {
                holder.btnViewDetails.setOnClickListener(v -> {
                    try {
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(Uri.parse("tel:+919826012345"));
                        v.getContext().startActivity(callIntent);
                    } catch (Exception e) {
                        Toast.makeText(v.getContext(), "Dialer unavailable", Toast.LENGTH_SHORT).show();
                    }
                });
            }

            // Book / Rent button
            if (holder.btnBookRent != null) {
                holder.btnBookRent.setOnClickListener(v -> {
                    String msg = String.format(v.getContext().getString(R.string.book_equipment_msg),
                            item.getTitle(), item.getPrice(), item.getLocation());

                    new AlertDialog.Builder(v.getContext())
                            .setTitle(v.getContext().getString(R.string.book_equipment_title))
                            .setMessage(msg)
                            .setPositiveButton(v.getContext().getString(R.string.confirm_booking), (d, w) -> {
                                Toast.makeText(v.getContext(), v.getContext().getString(R.string.booking_confirmed_toast), Toast.LENGTH_LONG).show();
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
        TextView tvTitle, tvRating, tvLocation, tvPrice, tvStatusBadge;
        ImageView imgEquip;
        View btnViewDetails, btnBookRent;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvEquipTitle);
            tvRating = itemView.findViewById(R.id.tvRating);
            tvLocation = itemView.findViewById(R.id.tvLocation);
            tvPrice = itemView.findViewById(R.id.tvEquipPrice);
            tvStatusBadge = itemView.findViewById(R.id.tvStatusBadge);
            imgEquip = itemView.findViewById(R.id.imgEquip);
            btnViewDetails = itemView.findViewById(R.id.btnViewDetails);
            btnBookRent = itemView.findViewById(R.id.btnBookRent);
        }
    }
}
