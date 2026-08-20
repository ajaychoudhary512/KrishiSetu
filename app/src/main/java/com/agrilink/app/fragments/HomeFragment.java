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

        if (getActivity() != null) {
            android.content.SharedPreferences prefs = getActivity().getSharedPreferences("agrilink_prefs", android.content.Context.MODE_PRIVATE);
            String userName = prefs.getString("user_name", null);
            String userPhone = prefs.getString("user_phone", null);

            android.widget.TextView tvHomeGreeting = view.findViewById(R.id.tvHomeGreeting);
            android.widget.TextView tvHomeAvatarInitials = view.findViewById(R.id.tvHomeAvatarInitials);

            String rawName = !android.text.TextUtils.isEmpty(userName) ? userName : userPhone;
            String nameToDisplay = getCleanDisplayName(rawName);

            if (tvHomeGreeting != null) {
                tvHomeGreeting.setText("Hello, " + nameToDisplay + "!");
            }

            if (tvHomeAvatarInitials != null) {
                String initials = "AG";
                if (!android.text.TextUtils.isEmpty(nameToDisplay)) {
                    String[] parts = nameToDisplay.trim().split("\\s+");
                    if (parts.length >= 2 && parts[0].length() > 0 && parts[1].length() > 0) {
                        initials = ("" + parts[0].charAt(0) + parts[1].charAt(0)).toUpperCase();
                    } else if (parts.length >= 1 && parts[0].length() > 0) {
                        initials = ("" + parts[0].charAt(0)).toUpperCase();
                    }
                }
                tvHomeAvatarInitials.setText(initials);
            }
        }

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

    private String getCleanDisplayName(String input) {
        if (android.text.TextUtils.isEmpty(input)) return "Farmer";
        if (input.contains("@")) {
            String namePart = input.split("@")[0];
            namePart = namePart.replaceAll("[._-]", " ");
            String[] words = namePart.trim().split("\\s+");
            StringBuilder sb = new StringBuilder();
            for (String w : words) {
                if (w.length() > 0) {
                    sb.append(Character.toUpperCase(w.charAt(0))).append(w.substring(1).toLowerCase()).append(" ");
                }
            }
            return sb.toString().trim();
        }
        return input;
    }
}
