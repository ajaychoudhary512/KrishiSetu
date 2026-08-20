package com.agrilink.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.agrilink.app.R;
import com.agrilink.app.adapters.LaborAdapter;
import com.agrilink.app.models.LaborItem;

import java.util.ArrayList;
import java.util.List;

public class LaborFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_labor, container, false);

        RecyclerView rv = view.findViewById(R.id.rvLabor);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        List<LaborItem> list = new ArrayList<>();
        // Farmer Farm Labour Requirements
        list.add(new LaborItem("🌾 Harvesting Workers (Farm)", "Indore, MP", "₹550/Day", "15 workers", "Immediate", "Harvesting", true));
        list.add(new LaborItem("🌾 Paddy Planting Team", "Dewas, MP", "₹500/Day", "20 workers", "Tomorrow", "Planting", false));
        // Industry & Factory Labour / Operator Hiring
        list.add(new LaborItem("🏭 Stubble Pellet Machine Operators", "Pithampur SEZ", "₹750/Day", "8 workers", "Shift A", "Factory", true));
        list.add(new LaborItem("🏭 Biomass Loading & Unloading Crew", "Ujjain Agro Hub", "₹650/Day", "12 workers", "Regular", "Loading", true));

        LaborAdapter adapter = new LaborAdapter(list);
        rv.setAdapter(adapter);

        return view;
    }
}
