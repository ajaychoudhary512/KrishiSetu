package com.agrilink.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.agrilink.app.MainActivity;
import com.agrilink.app.R;


public class HomeFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        View.OnClickListener marketListener = v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new MarketplaceFragment());
            }
        };

        view.findViewById(R.id.cardWasteMarket).setOnClickListener(marketListener);
        view.findViewById(R.id.btnBannerSell).setOnClickListener(marketListener);

        view.findViewById(R.id.cardEquipment).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new EquipmentFragment());
            }
        });

        view.findViewById(R.id.cardLabor).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new LaborFragment());
            }
        });

        view.findViewById(R.id.cardDisease).setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new DiseaseFragment());
            }
        });

        return view;
    }
}
