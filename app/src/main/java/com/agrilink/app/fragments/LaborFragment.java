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
import com.agrilink.app.adapters.LaborAdapter;
import com.agrilink.app.models.LaborItem;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.List;

public class LaborFragment extends Fragment {

    private final List<LaborItem> allList = new ArrayList<>();
    private final List<LaborItem> filteredList = new ArrayList<>();
    private LaborAdapter adapter;
    private RecyclerView rv;
    private String currentCategory = "All";
    private String currentSearch = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_labor, container, false);

        rv = view.findViewById(R.id.rvLabor);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        allList.clear();
        // Farmer Farm Labour Requirements
        allList.add(new LaborItem("🌾 Harvesting Workers (Farm)", "Indore, MP", "₹550/Day", "15 workers", "Immediate", "Harvesting", true));
        allList.add(new LaborItem("🌾 Paddy Planting Team", "Dewas, MP", "₹500/Day", "20 workers", "Tomorrow", "Planting", false));
        // Industry & Factory Labour / Operator Hiring
        allList.add(new LaborItem("🏭 Stubble Pellet Machine Operators", "Pithampur SEZ", "₹750/Day", "8 workers", "Shift A", "Factory", true));
        allList.add(new LaborItem("🏭 Biomass Loading & Unloading Crew", "Ujjain Agro Hub", "₹650/Day", "12 workers", "Regular", "Loading", true));

        filteredList.clear();
        filteredList.addAll(allList);

        adapter = new LaborAdapter(filteredList);
        rv.setAdapter(adapter);

        // Chip Filters
        ChipGroup chipGroup = view.findViewById(R.id.chipGroupLabor);
        if (chipGroup != null) {
            chipGroup.setOnCheckedStateChangeListener((group, checkedIds) -> {
                if (checkedIds.isEmpty() || checkedIds.contains(R.id.chipAllLabor)) {
                    currentCategory = "All";
                } else if (checkedIds.contains(R.id.chipLaborHarvesting)) {
                    currentCategory = "Harvesting";
                } else if (checkedIds.contains(R.id.chipLaborPlanting)) {
                    currentCategory = "Planting";
                } else if (checkedIds.contains(R.id.chipLaborOperators)) {
                    currentCategory = "Factory";
                }
                filterData();
            });
        }

        // Search Input
        EditText etSearch = view.findViewById(R.id.etSearchLabor);
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

        // Header Action Button
        View btnPostLabor = view.findViewById(R.id.btnPostLabor);
        if (btnPostLabor != null) {
            btnPostLabor.setOnClickListener(v -> showAddLaborDialog());
        }

        // Bottom Extended FAB
        View fabAddLabor = view.findViewById(R.id.fabAddLabor);
        if (fabAddLabor != null) {
            fabAddLabor.setOnClickListener(v -> showAddLaborDialog());
        }

        return view;
    }

    private void showAddLaborDialog() {
        if (getContext() == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        View sheet = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_labor, null);
        dialog.setContentView(sheet);

        EditText etTitle = sheet.findViewById(R.id.etAddLaborTitle);
        EditText etCount = sheet.findViewById(R.id.etAddLaborCount);
        EditText etWage = sheet.findViewById(R.id.etAddLaborWage);
        EditText etLocation = sheet.findViewById(R.id.etAddLaborLocation);
        EditText etTiming = sheet.findViewById(R.id.etAddLaborTiming);

        View btnCancel = sheet.findViewById(R.id.btnCancelAddLabor);
        if (btnCancel != null) btnCancel.setOnClickListener(v -> dialog.dismiss());

        View btnSubmit = sheet.findViewById(R.id.btnSubmitAddLabor);
        if (btnSubmit != null) {
            btnSubmit.setOnClickListener(v -> {
                String title = etTitle != null ? etTitle.getText().toString().trim() : "";
                String count = etCount != null ? etCount.getText().toString().trim() : "";
                String wage = etWage != null ? etWage.getText().toString().trim() : "";
                String loc = etLocation != null ? etLocation.getText().toString().trim() : "";
                String timing = etTiming != null ? etTiming.getText().toString().trim() : "";

                if (TextUtils.isEmpty(title)) {
                    Toast.makeText(getContext(), getString(R.string.err_enter_listing_title), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(wage)) {
                    Toast.makeText(getContext(), getString(R.string.err_enter_price), Toast.LENGTH_SHORT).show();
                    return;
                }
                if (TextUtils.isEmpty(count)) count = "10 workers";
                if (TextUtils.isEmpty(loc)) loc = "Indore, MP";
                if (TextUtils.isEmpty(timing)) timing = "Immediate";

                String category = "Harvesting";
                if (title.toLowerCase().contains("plant") || title.toLowerCase().contains("sow")) {
                    category = "Planting";
                } else if (title.toLowerCase().contains("operator") || title.toLowerCase().contains("factory")) {
                    category = "Factory";
                } else if (title.toLowerCase().contains("loading") || title.toLowerCase().contains("transport")) {
                    category = "Loading";
                }

                LaborItem newItem = new LaborItem(
                        title,
                        loc,
                        wage,
                        count,
                        timing,
                        category,
                        true
                );

                allList.add(0, newItem);
                filterData();

                if (rv != null) rv.smoothScrollToPosition(0);

                Toast.makeText(getContext(), getString(R.string.labour_added_success), Toast.LENGTH_LONG).show();
                dialog.dismiss();
            });
        }

        dialog.show();
    }

    private void filterData() {
        filteredList.clear();
        for (LaborItem item : allList) {
            boolean matchesCat = currentCategory.equals("All") ||
                    item.getCategory().toLowerCase().contains(currentCategory.toLowerCase()) ||
                    item.getSkill().toLowerCase().contains(currentCategory.toLowerCase());
            boolean matchesSearch = TextUtils.isEmpty(currentSearch) ||
                    item.getCategory().toLowerCase().contains(currentSearch.toLowerCase()) ||
                    item.getSkill().toLowerCase().contains(currentSearch.toLowerCase()) ||
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
