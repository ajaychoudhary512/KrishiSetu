package com.agrilink.app.fragments;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agrilink.app.R;
import com.agrilink.app.adapters.EquipmentAdapter;
import com.agrilink.app.models.EquipmentItem;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class EquipmentFragment extends Fragment {

    private final List<EquipmentItem> allList = new ArrayList<>();
    private final List<EquipmentItem> filteredList = new ArrayList<>();
    private EquipmentAdapter adapter;
    private RecyclerView rv;
    private String currentCategory = "All";
    private String currentSearch = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_equipment, container, false);

        rv = view.findViewById(R.id.rvEquipment);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        allList.clear();
        allList.add(new EquipmentItem("🚜 Tractor (45 HP) - Farmer Rental", "4.8", "Indore, MP", "₹1200/Day", "Available", true, R.drawable.tractor_45hp));
        allList.add(new EquipmentItem("🌾 Rotavator & Seed Drill - Farmer", "4.6", "Dewas, MP", "₹800/Day", "Available", true, R.drawable.rotavator));
        allList.add(new EquipmentItem("🌾 Combined Paddy Harvester - Farmer", "4.7", "Indore, MP", "₹2500/Day", "Available", true, R.drawable.harvester));
        allList.add(new EquipmentItem("🏭 Biomass Baler & Hydraulic Loader", "4.9", "Pithampur, MP", "₹3500/Day", "Available", true, R.drawable.seeder_machine));
        allList.add(new EquipmentItem("🚚 Heavy Duty 10-Ton Biomass Transport Truck", "4.8", "Ujjain Agro Park", "₹4500/Trip", "Available", true, R.drawable.sprayer_power));

        filteredList.clear();
        filteredList.addAll(allList);

        adapter = new EquipmentAdapter(filteredList);
        rv.setAdapter(adapter);

        // Category Filter Chips
        ChipGroup chipGroup = view.findViewById(R.id.chipGroupEquip);
        if (chipGroup != null) {
            chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.isEmpty() || checkedIds.contains(R.id.chipAllEquip)) {
                    currentCategory = "All";
                } else if (checkedIds.contains(R.id.chipTractors)) {
                    currentCategory = "Tractor";
                } else if (checkedIds.contains(R.id.chipImplements)) {
                    currentCategory = "Rotavator";
                } else if (checkedIds.contains(R.id.chipHarvesters)) {
                    currentCategory = "Harvester";
                }
                filterData();
            });
        }

        // Search Input
        EditText etSearch = view.findViewById(R.id.etSearchEquip);
        if (etSearch != null) {
            etSearch.addTextChangedListener(new TextWatcher() {
                @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                    currentSearch = s.toString().trim();
                    filterData();
                }
                @Override public void afterTextChanged(Editable s) {}
            });
        }

        // Post Equipment Action Button (Header)
        View btnPostEquipment = view.findViewById(R.id.btnPostEquipment);
        if (btnPostEquipment != null) {
            btnPostEquipment.setOnClickListener(v -> showAddEquipmentDialog());
        }

        // Extended Floating Action Button (Bottom)
        View fabAddEquipment = view.findViewById(R.id.fabAddEquipment);
        if (fabAddEquipment != null) {
            fabAddEquipment.setOnClickListener(v -> showAddEquipmentDialog());
        }

        return view;
    }

    private void showAddEquipmentDialog() {
        if (getContext() == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        View sheet = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_equipment, null);
        dialog.setContentView(sheet);

        ChipGroup chipGroup = sheet.findViewById(R.id.chipGroupAddEquip);
        EditText etName = sheet.findViewById(R.id.etAddEquipName);
        EditText etPrice = sheet.findViewById(R.id.etAddEquipPrice);
        EditText etLocation = sheet.findViewById(R.id.etAddEquipLocation);
        EditText etOwner = sheet.findViewById(R.id.etAddEquipOwner);

        View btnCancel = sheet.findViewById(R.id.btnCancelAddEquip);
        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());

        View btnSubmit = sheet.findViewById(R.id.btnSubmitAddEquip);
        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                String name = etName != null ? etName.getText().toString().trim() : "";
                String price = etPrice != null ? etPrice.getText().toString().trim() : "";
                String loc = etLocation != null ? etLocation.getText().toString().trim() : "";
                String owner = etOwner != null ? etOwner.getText().toString().trim() : "";

                if (TextUtils.isEmpty(name)) {
                    Toast.makeText(getContext(), getString(R.string.err_enter_listing_title), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(price)) {
                    Toast.makeText(getContext(), getString(R.string.err_enter_price), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(loc)) loc = "Indore, MP";

                int imageRes = R.drawable.tractor_45hp;
                if (name.toLowerCase().contains("rotavator") || name.toLowerCase().contains("drill")) {
                    imageRes = R.drawable.rotavator;
                } else if (name.toLowerCase().contains("harvest")) {
                    imageRes = R.drawable.harvester;
                } else if (name.toLowerCase().contains("truck") || name.toLowerCase().contains("loader")) {
                    imageRes = R.drawable.sprayer_power;
                }

                EquipmentItem newItem = new EquipmentItem(
                        name,
                        "5.0 (New)",
                        loc,
                        price,
                        "Available",
                        true,
                        imageRes
                );

                allList.add(0, newItem);
                filterData();

                if (rv != null) rv.smoothScrollToPosition(0);

                Toast.makeText(getContext(), getString(R.string.equipment_added_success), Toast.LENGTH_LONG).show();
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    private void filterData() {
        filteredList.clear();
        for (EquipmentItem item : allList) {
            boolean matchesCat = currentCategory.equals("All") ||
                    item.getTitle().toLowerCase().contains(currentCategory.toLowerCase());
            boolean matchesSearch = TextUtils.isEmpty(currentSearch) ||
                    item.getTitle().toLowerCase().contains(currentSearch.toLowerCase()) ||
                    item.getLocation().toLowerCase().contains(currentSearch.toLowerCase());

            if (matchesCat && matchesSearch) {
                filteredList.add(item);
            }
        }
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }
}
