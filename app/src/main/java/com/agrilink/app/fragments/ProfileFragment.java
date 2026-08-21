package com.agrilink.app.fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.agrilink.app.R;


public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        if (getActivity() != null) {
            android.content.SharedPreferences prefs = getActivity().getSharedPreferences("agrilink_prefs", android.content.Context.MODE_PRIVATE);
            String userName = prefs.getString("user_name", null);
            String userPhone = prefs.getString("user_phone", null);

            android.widget.TextView tvUserName = view.findViewById(R.id.tvUserName);
            if (tvUserName != null) {
                String rawName = !android.text.TextUtils.isEmpty(userName) ? userName : userPhone;
                tvUserName.setText(getCleanDisplayName(rawName));
            }

            String userRole = prefs.getString("user_role", "Farmer");
            android.widget.TextView tvUserRole = view.findViewById(R.id.tvUserRole);
            if (tvUserRole != null) {
                tvUserRole.setText(userRole + " • Verified ✓");
            }
        }

        // Theme Toggle Click Listener
        View rlThemeToggle = view.findViewById(R.id.rlThemeToggle);
        android.widget.TextView tvCurrentTheme = view.findViewById(R.id.tvCurrentTheme);
        if (rlThemeToggle != null) {
            rlThemeToggle.setOnClickListener(v -> {
                if (getActivity() == null) return;
                android.content.SharedPreferences prefs = getActivity().getSharedPreferences("agrilink_prefs", android.content.Context.MODE_PRIVATE);
                boolean isDark = prefs.getBoolean("is_dark_mode", false);
                boolean newDark = !isDark;
                prefs.edit().putBoolean("is_dark_mode", newDark).apply();

                if (newDark) {
                    androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_YES);
                    if (tvCurrentTheme != null) tvCurrentTheme.setText("Dark 🌙 ›");
                    Toast.makeText(getContext(), "🌙 Switched to High-Contrast Dark Mode", Toast.LENGTH_SHORT).show();
                } else {
                    androidx.appcompat.app.AppCompatDelegate.setDefaultNightMode(androidx.appcompat.app.AppCompatDelegate.MODE_NIGHT_NO);
                    if (tvCurrentTheme != null) tvCurrentTheme.setText("Light ☀️ ›");
                    Toast.makeText(getContext(), "☀️ Switched to Outdoor Light Mode", Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Language Switcher Click Listener
        View rlLanguageToggle = view.findViewById(R.id.rlLanguageToggle);
        android.widget.TextView tvCurrentLanguage = view.findViewById(R.id.tvCurrentLanguage);
        if (rlLanguageToggle != null) {
            rlLanguageToggle.setOnClickListener(v -> showLanguageSelectionDialog(tvCurrentLanguage));
        }

        if (view.findViewById(R.id.btnWithdraw) != null) {
            view.findViewById(R.id.btnWithdraw).setOnClickListener(v -> {
                Toast.makeText(getContext(), "Withdrawal request of ₹4,250 sent to Bank!", Toast.LENGTH_SHORT).show();
            });
        }

        if (view.findViewById(R.id.rlLogout) != null) {
            view.findViewById(R.id.rlLogout).setOnClickListener(v -> {
                if (getActivity() != null) {
                    android.content.SharedPreferences prefs = getActivity().getSharedPreferences("agrilink_prefs", android.content.Context.MODE_PRIVATE);
                    prefs.edit().clear().apply();
                    Toast.makeText(getContext(), "Logged Out", Toast.LENGTH_SHORT).show();
                    startActivity(new android.content.Intent(getActivity(), com.agrilink.app.LoginActivity.class));
                    getActivity().finish();
                }
            });
        }

        return view;
    }

    private String getCleanDisplayName(String input) {
        if (android.text.TextUtils.isEmpty(input)) return "KrishiSetu Member";
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

    private void showLanguageSelectionDialog(android.widget.TextView tvCurrentLanguage) {
        String[] languages = {"🇬🇧 English (Default)", "🇮🇳 हिंदी (Hindi)"};
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("🌐 Select App Language / भाषा चुनें")
                .setItems(languages, (dialog, which) -> {
                    String selectedLang = which == 1 ? "hi" : "en";
                    if (getActivity() != null) {
                        android.content.SharedPreferences prefs = getActivity().getSharedPreferences("agrilink_prefs", android.content.Context.MODE_PRIVATE);
                        prefs.edit().putString("app_lang", selectedLang).apply();

                        java.util.Locale locale = new java.util.Locale(selectedLang);
                        java.util.Locale.setDefault(locale);
                        android.content.res.Configuration config = new android.content.res.Configuration();
                        config.setLocale(locale);
                        getResources().updateConfiguration(config, getResources().getDisplayMetrics());

                        if (tvCurrentLanguage != null) {
                            tvCurrentLanguage.setText(which == 1 ? "हिंदी 🇮🇳 ›" : "English 🇬🇧 ›");
                        }
                        Toast.makeText(getContext(), which == 1 ? "🇮🇳 भाषा बदलकर 'हिंदी' कर दी गई है!" : "🇬🇧 Language set to English!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel / रद्द करें", (d, w) -> d.dismiss())
                .show();
    }
}
