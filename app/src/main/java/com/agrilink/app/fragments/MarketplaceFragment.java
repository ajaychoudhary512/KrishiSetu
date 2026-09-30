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
import com.agrilink.app.CreateListingActivity;
import com.agrilink.app.R;
import com.agrilink.app.adapters.WasteAdapter;
import com.agrilink.app.models.WasteItem;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class MarketplaceFragment extends Fragment {

    private final List<WasteItem> allItems = new ArrayList<>();
    private final List<WasteItem> displayedItems = new ArrayList<>();
    private WasteAdapter adapter;
    private View layoutEmpty;
    private String currentCategory = "All";
    private String currentQuery = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_marketplace, container, false);

        RecyclerView rv = view.findViewById(R.id.rvWaste);
        layoutEmpty = view.findViewById(R.id.layoutEmptyWaste);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        // Populate initial demo listings
        allItems.clear();
        allItems.add(new WasteItem("🌾 Rice Straw (50 Qtl)", "Crop Residue", "₹100/quintal", "Indore, MP • 12 km", "Ramesh Patel (Farmer)", true, R.drawable.rice_straw));
        allItems.add(new WasteItem("🌾 Wheat Straw Bales (30 Qtl)", "Crop Residue", "₹130/quintal", "Dewas, MP • 18 km", "Suresh Kumar (Farmer)", true, R.drawable.wheat_straw));
        allItems.add(new WasteItem("🏭 Paddy Straw Bulk Demand (100 Tons)", "Industry Demand", "₹1,800/ton", "Pithampur SEZ • 25 km", "GreenBio Energy Ltd (Industry)", true, R.drawable.rice_straw));
        allItems.add(new WasteItem("🏭 Sugarcane Bagasse Purchase (50 Tons)", "Bagasse", "₹2,200/ton", "Ujjain Agro Park • 35 km", "Apex Bio-Pellets Pvt Ltd (Industry)", true, R.drawable.wheat_straw));

        displayedItems.clear();
        displayedItems.addAll(allItems);

        adapter = new WasteAdapter(displayedItems, item -> {
            startActivity(new Intent(getContext(), ChatDealActivity.class));
        });
        rv.setAdapter(adapter);

        // Search Input & Clear Button
        EditText etSearch = view.findViewById(R.id.etSearchWaste);
        TextView icClear = view.findViewById(R.id.icClearSearch);

        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentQuery = s.toString().trim();
                    if (icClear != null) {
                        icClear.setVisibility(TextUtils.isEmpty(currentQuery) ? View.GONE : View.VISIBLE);
                    }
                    filterListings();
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        if (icClear != null && etSearch != null) {
            icClear.setOnClickListener(v -> {
                etSearch.setText("");
            });
        }

        // Category Chips
        ChipGroup chipGroup = view.findViewById(R.id.chipGroupWaste);
        if (chipGroup != null) {
            chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.isEmpty() || checkedIds.contains(R.id.chipAll)) {
                    currentCategory = "All";
                } else if (checkedIds.contains(R.id.chipResidue)) {
                    currentCategory = "Crop Residue";
                } else if (checkedIds.contains(R.id.chipBiomass)) {
                    currentCategory = "Industry Demand";
                } else if (checkedIds.contains(R.id.chipBagasse)) {
                    currentCategory = "Bagasse";
                }
                filterListings();
            });
        }

        // Post Listing Button
        View btnPostListing = view.findViewById(R.id.btnPostListing);
        if (btnPostListing != null) {
            btnPostListing.setOnClickListener(v -> {
                startActivity(new Intent(getContext(), CreateListingActivity.class));
            });
        }

        return view;
    }

    private void filterListings() {
        displayedItems.clear();
        for (WasteItem item : allItems) {
            boolean matchesCat = currentCategory.equals("All") ||
                    item.getCategory().toLowerCase().contains(currentCategory.toLowerCase());
            boolean matchesQuery = TextUtils.isEmpty(currentQuery) ||
                    item.getTitle().toLowerCase().contains(currentQuery.toLowerCase()) ||
                    item.getLocation().toLowerCase().contains(currentQuery.toLowerCase()) ||
                    item.getSellerName().toLowerCase().contains(currentQuery.toLowerCase());

            if (matchesCat && matchesQuery) {
                displayedItems.add(item);
            }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
        if (layoutEmpty != null) {
            layoutEmpty.setVisibility(displayedItems.isEmpty() ? View.VISIBLE : View.GONE);
        }
    }
}
