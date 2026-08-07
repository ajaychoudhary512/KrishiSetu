package com.example.agrilink.fragments;

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

import com.example.agrilink.ChatDealActivity;
import com.example.agrilink.R;
import com.example.agrilink.adapters.WasteAdapter;
import com.example.agrilink.models.WasteItem;

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
        items.add(new WasteItem("Rice Straw", "Crop Residue", "₹100/quintal", "Indore, MP • 12 km", "Ramesh Patel", true, R.drawable.rice_straw));
        items.add(new WasteItem("Wheat Straw", "Crop Residue", "₹130/quintal", "Dewas, MP • 12 km", "Suresh Kumar", true, R.drawable.wheat_straw));

        WasteAdapter adapter = new WasteAdapter(items, item -> {
            startActivity(new Intent(getContext(), ChatDealActivity.class));
        });
        rv.setAdapter(adapter);

        return view;
    }
}
