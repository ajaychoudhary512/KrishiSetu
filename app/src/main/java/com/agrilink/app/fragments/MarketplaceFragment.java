package com.agrilink.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agrilink.app.ChatDealActivity;
import com.agrilink.app.R;
import com.agrilink.app.adapters.WasteAdapter;
import com.agrilink.app.models.WasteItem;

import java.util.ArrayList;
import java.util.List;

public class MarketplaceFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_marketplace, container, false);

        RecyclerView rv = view.findViewById(R.id.rvWaste);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        List<WasteItem> items = new ArrayList<>();
        // Farmer Stubble / Waste Offers (Supply)
        items.add(new WasteItem("🌾 Rice Straw (50 Qtl)", "Farmer Stubble Offer", "₹100/quintal", "Indore, MP • 12 km", "Ramesh Patel (Farmer)", true, R.drawable.rice_straw));
        items.add(new WasteItem("🌾 Wheat Straw Bales (30 Qtl)", "Farmer Stubble Offer", "₹130/quintal", "Dewas, MP • 12 km", "Suresh Kumar (Farmer)", true, R.drawable.wheat_straw));
        // Industry Material Requirements (Demand)
        items.add(new WasteItem("🏭 Paddy Straw Bulk Demand (100 Tons)", "Industry Bio-Fuel Demand", "₹1,800/ton", "Pithampur SEZ • 25 km", "GreenBio Energy Ltd (Industry)", true, R.drawable.rice_straw));
        items.add(new WasteItem("🏭 Sugarcane Bagasse Purchase (50 Tons)", "Industry Paper Mill Demand", "₹2,200/ton", "Ujjain Agro Park • 35 km", "Apex Bio-Pellets Pvt Ltd (Industry)", true, R.drawable.wheat_straw));

        WasteAdapter adapter = new WasteAdapter(items, item -> {
            startActivity(new Intent(getContext(), ChatDealActivity.class));
        });
        rv.setAdapter(adapter);

        View btnPostListing = view.findViewById(R.id.btnPostListing);
        if (btnPostListing != null) {
            btnPostListing.setOnClickListener(v -> {
                startActivity(new Intent(getContext(), com.agrilink.app.CreateListingActivity.class));
            });
        }

        return view;
    }
}
