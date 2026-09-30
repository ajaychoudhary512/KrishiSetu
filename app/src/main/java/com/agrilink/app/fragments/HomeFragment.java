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
                String initials = "KS";
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

        View.OnClickListener walletListener = v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).loadFragment(new ProfileFragment());
            }
        };
        if (view.findViewById(R.id.cardHomeWallet) != null) {
            view.findViewById(R.id.cardHomeWallet).setOnClickListener(walletListener);
        }

        if (view.findViewById(R.id.cardTransport) != null) {
            view.findViewById(R.id.cardTransport).setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).loadFragment(new EquipmentFragment());
                }
            });
        }

        if (view.findViewById(R.id.cardSchemes) != null) {
            view.findViewById(R.id.cardSchemes).setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).loadFragment(new DiseaseFragment());
                }
            });
        }

        if (view.findViewById(R.id.cardHomeAgribot) != null) {
            view.findViewById(R.id.cardHomeAgribot).setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).loadFragment(new DiseaseFragment());
                }
            });
        }

        if (view.findViewById(R.id.cardWeather) != null) {
            view.findViewById(R.id.cardWeather).setOnClickListener(v -> showWeatherAdvisoryDialog());
        }

        if (view.findViewById(R.id.btnNotifications) != null) {
            view.findViewById(R.id.btnNotifications).setOnClickListener(v -> showNotificationsDialog());
        }

        return view;
    }

    private void showWeatherAdvisoryDialog() {
        if (getContext() == null) return;
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("🌤️ KRISHISETU Weather Advisory")
                .setMessage("📍 Current Location: Indore, MP\n\n" +
                        "🌡️ Temperature: 28°C • Mostly Sunny\n" +
                        "💧 Humidity: 62% • Wind: 11 km/h NE\n\n" +
                        "🌾 Farming Recommendation:\n" +
                        "Optimal conditions for wheat stubble baling and storage. Ensure dried straw is covered before weekend moisture.")
                .setPositiveButton("Got It", (d, w) -> d.dismiss())
                .show();
    }

    private void showNotificationsDialog() {
        if (getContext() == null) return;
        String[] notifications = {
            "⚡ Escrow Payment: ₹54,941.25 queued for delivery verification.",
            "🚜 Equipment: Rental request for Kubota Harvester approved.",
            "🌾 Market: High demand for Paddy Straw in Pithampur SEZ."
        };
        new androidx.appcompat.app.AlertDialog.Builder(getContext())
                .setTitle("🔔 KRISHISETU Notifications")
                .setItems(notifications, (d, w) -> d.dismiss())
                .setPositiveButton("Close", (d, w) -> d.dismiss())
                .show();
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
