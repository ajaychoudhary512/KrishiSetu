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
import com.agrilink.app.adapters.EquipmentAdapter;
import com.agrilink.app.models.EquipmentItem;

import java.util.ArrayList;
import java.util.List;

public class EquipmentFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_equipment, container, false);

        RecyclerView rv = view.findViewById(R.id.rvEquipment);
        rv.setLayoutManager(new LinearLayoutManager(getContext()));

        List<EquipmentItem> list = new ArrayList<>();
        list.add(new EquipmentItem("Tractor (45 HP)", "4.8", "Indore, MP", "₹1200/Day", "Available", true, R.drawable.tractor_45hp));
        list.add(new EquipmentItem("Rotavator", "4.6", "Dewas, MP", "₹800/Day", "Available", true, R.drawable.rotavator));
        list.add(new EquipmentItem("Seeder Machine", "4.7", "Indore, MP", "₹700/Day", "Available", true, R.drawable.seeder_machine));
        list.add(new EquipmentItem("Harvester", "4.3", "Ujjain, MP", "₹2500/Day", "Booked", false, R.drawable.harvester));
        list.add(new EquipmentItem("Sprayer (Power)", "4.5", "Bhopal, MP", "₹600/Day", "Available", true, R.drawable.sprayer_power));

        EquipmentAdapter adapter = new EquipmentAdapter(list);
        rv.setAdapter(adapter);

        return view;
    }
}
