package com.example.agrilink.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.agrilink.R;
import com.example.agrilink.adapters.LaborAdapter;
import com.example.agrilink.models.LaborItem;

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
        list.add(new LaborItem("Harvesting Workers", "Indore, MP", "₹500", "15 workers", "18 Aug", "Harvesting", true));
        list.add(new LaborItem("Planting Workers", "Dewas, MP", "₹450", "20 workers", "20 Aug", "Planting", false));
        list.add(new LaborItem("Spraying Workers", "Ujjain, MP", "₹500", "10 workers", "21 Aug", "Spraying", true));
        list.add(new LaborItem("Irrigation Workers", "Bhopal, MP", "₹400", "8 workers", "22 Aug", "Irrigation", false));

        LaborAdapter adapter = new LaborAdapter(list);
        rv.setAdapter(adapter);

        return view;
    }
}
