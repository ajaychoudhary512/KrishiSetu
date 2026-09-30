package com.agrilink.app.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;

import com.agrilink.app.ChatDealActivity;
import com.agrilink.app.KrishiSetuApplication;
import com.agrilink.app.LoginActivity;
import com.agrilink.app.R;

import java.util.Locale;

public class ProfileFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        if (getActivity() != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences(KrishiSetuApplication.PREFS_NAME, Context.MODE_PRIVATE);
            String userName = prefs.getString("user_name", null);
            String userPhone = prefs.getString("user_phone", null);
            String userEmail = prefs.getString("user_email", null);
            String userRole = prefs.getString("user_role", "Farmer");

            // User Name
            TextView tvUserName = view.findViewById(R.id.tvUserName);
            if (tvUserName != null) {
                String rawName = !TextUtils.isEmpty(userName) ? userName : (!TextUtils.isEmpty(userEmail) ? userEmail : userPhone);
                tvUserName.setText(getCleanDisplayName(rawName));
            }

            // User Email / Phone
            TextView tvUserEmail = view.findViewById(R.id.tvUserEmail);
            if (tvUserEmail != null) {
                if (!TextUtils.isEmpty(userEmail)) {
                    tvUserEmail.setText(userEmail);
                } else if (!TextUtils.isEmpty(userPhone)) {
                    tvUserEmail.setText("+91 " + userPhone);
                } else {
                    tvUserEmail.setText("farmer@krishisetu.in");
                }
            }

            // User Role
            TextView tvUserRole = view.findViewById(R.id.tvUserRole);
            if (tvUserRole != null) {
                tvUserRole.setText(userRole + " • Verified ✓");
            }
        }

        // Theme Toggle Setup
        View rlThemeToggle = view.findViewById(R.id.rlThemeToggle);
        TextView tvCurrentTheme = view.findViewById(R.id.tvCurrentTheme);
        updateThemeDisplay(tvCurrentTheme);

        if (rlThemeToggle != null) {
            rlThemeToggle.setOnClickListener(v -> showThemeSelectionDialog(tvCurrentTheme));
        }

        // Language Switcher Setup
        View rlLanguageToggle = view.findViewById(R.id.rlLanguageToggle);
        TextView tvCurrentLanguage = view.findViewById(R.id.tvCurrentLanguage);
        if (rlLanguageToggle != null) {
            rlLanguageToggle.setOnClickListener(v -> showLanguageSelectionDialog(tvCurrentLanguage));
        }

        // Wallet Withdraw
        if (view.findViewById(R.id.btnWithdraw) != null) {
            view.findViewById(R.id.btnWithdraw).setOnClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle("💰 KrishiSetu Wallet Withdrawal")
                        .setMessage("Your available balance of ₹4,250.00 will be deposited to your verified UPI/Bank Account via KrishiSetu Instant Payout.\n\nProceed with transfer?")
                        .setPositiveButton("Transfer Now", (dialog, which) -> {
                            Toast.makeText(getContext(), "✅ Withdrawal request of ₹4,250 sent to Bank!", Toast.LENGTH_LONG).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        // My Orders
        View rlMyOrders = view.findViewById(R.id.rlMyOrders);
        if (rlMyOrders != null) {
            rlMyOrders.setOnClickListener(v -> showOrdersDialog());
        }

        // My Listings
        View rlMyListings = view.findViewById(R.id.rlMyListings);
        if (rlMyListings != null) {
            rlMyListings.setOnClickListener(v -> showListingsDialog());
        }

        // Messages
        View rlMessages = view.findViewById(R.id.rlMessages);
        if (rlMessages != null) {
            rlMessages.setOnClickListener(v -> {
                Intent intent = new Intent(getActivity(), ChatDealActivity.class);
                startActivity(intent);
            });
        }

        // Help & Support
        View rlHelpSupport = view.findViewById(R.id.rlHelpSupport);
        if (rlHelpSupport != null) {
            rlHelpSupport.setOnClickListener(v -> showHelpSupportDialog());
        }

        // Terms & Privacy
        View rlTermsPrivacy = view.findViewById(R.id.rlTermsPrivacy);
        if (rlTermsPrivacy != null) {
            rlTermsPrivacy.setOnClickListener(v -> showTermsDialog());
        }

        // Logout
        View rlLogout = view.findViewById(R.id.rlLogout);
        if (rlLogout != null) {
            rlLogout.setOnClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle("🚪 Logout of KrishiSetu")
                        .setMessage("Are you sure you want to end your current session?")
                        .setPositiveButton("Logout", (dialog, which) -> {
                            if (getActivity() != null) {
                                SharedPreferences prefs = getActivity().getSharedPreferences(KrishiSetuApplication.PREFS_NAME, Context.MODE_PRIVATE);
                                prefs.edit().clear().apply();
                                Toast.makeText(getContext(), "Logged Out Successfully", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getActivity(), LoginActivity.class));
                                getActivity().finish();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            });
        }

        return view;
    }

    private void updateThemeDisplay(TextView tvCurrentTheme) {
        if (tvCurrentTheme == null || getActivity() == null) return;
        SharedPreferences prefs = getActivity().getSharedPreferences(KrishiSetuApplication.PREFS_NAME, Context.MODE_PRIVATE);
        String mode = prefs.getString(KrishiSetuApplication.KEY_THEME_MODE, "system");
        switch (mode) {
            case "light":
                tvCurrentTheme.setText("Light ☀️ ›");
                break;
            case "dark":
                tvCurrentTheme.setText("Dark 🌙 ›");
                break;
            case "system":
            default:
                tvCurrentTheme.setText("System Default ⚙️ ›");
                break;
        }
    }

    private void showThemeSelectionDialog(TextView tvCurrentTheme) {
        if (getActivity() == null) return;
        SharedPreferences prefs = getActivity().getSharedPreferences(KrishiSetuApplication.PREFS_NAME, Context.MODE_PRIVATE);
        String currentMode = prefs.getString(KrishiSetuApplication.KEY_THEME_MODE, "system");

        int checkedItem = 2; // Default system
        if ("light".equalsIgnoreCase(currentMode)) checkedItem = 0;
        else if ("dark".equalsIgnoreCase(currentMode)) checkedItem = 1;

        String[] themeOptions = {
                "☀️ Light Mode (Outdoor Sun Visibility)",
                "🌙 Dark Mode (Night & Battery Saver)",
                "⚙️ System Default (Follow Android OS)"
        };

        new AlertDialog.Builder(requireContext())
                .setTitle("🎨 Choose App Theme")
                .setSingleChoiceItems(themeOptions, checkedItem, (dialog, which) -> {
                    String selectedMode = "system";
                    int nightMode = AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM;

                    if (which == 0) {
                        selectedMode = "light";
                        nightMode = AppCompatDelegate.MODE_NIGHT_NO;
                    } else if (which == 1) {
                        selectedMode = "dark";
                        nightMode = AppCompatDelegate.MODE_NIGHT_YES;
                    }

                    prefs.edit().putString(KrishiSetuApplication.KEY_THEME_MODE, selectedMode).apply();
                    AppCompatDelegate.setDefaultNightMode(nightMode);
                    updateThemeDisplay(tvCurrentTheme);
                    dialog.dismiss();

                    Toast.makeText(getContext(), "Theme updated to " + themeOptions[which].split("\\(")[0].trim(), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showOrdersDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("📦 My Orders & Bookings")
                .setMessage("• Order #KS-9842: Wheat Straw (50 Quintals) — In Transit 🚚\n• Booking #KS-2311: Mahindra 575 DI Tractor — Confirmed for Tomorrow 🚜\n• Booking #KS-1102: Harvester Operator — Completed ✅")
                .setPositiveButton("Done", null)
                .show();
    }

    private void showListingsDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("📋 My Published Listings")
                .setMessage("• Organic Paddy Straw (20 Tonnes) — Active (3 Enquiries)\n• Mustard Stubble (15 Tonnes) — Active (1 Enquiry)")
                .setPositiveButton("Done", null)
                .show();
    }

    private void showHelpSupportDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("❓ KrishiSetu Kisan Support")
                .setMessage("We are here to support our farmers 24x7.\n\n📞 Toll-Free Helpline: 1800-889-SETU\n📧 Email: support@krishisetu.in\n🕒 Working Hours: Monday - Sunday (6 AM - 10 PM)\n📍 New Delhi, India")
                .setPositiveButton("Call Helpline", (d, w) -> {
                    try {
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(android.net.Uri.parse("tel:18008897388"));
                        startActivity(callIntent);
                    } catch (Exception ignored) {
                    }
                })
                .setNegativeButton("Close", null)
                .show();
    }

    private void showTermsDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("🔒 KrishiSetu Privacy & Escrow Guarantee")
                .setMessage("KrishiSetu safeguards Indian agricultural trade:\n\n1. 100% Escrow Protection: Funds are only released when both buyer and farmer confirm grain/waste delivery.\n2. Fair Pricing: Guaranteed MSP and verified mandi price tracking.\n3. Data Confidentiality: Farmer land and transaction details are encrypted and never sold to third parties.")
                .setPositiveButton("Understood", null)
                .show();
    }

    private String getCleanDisplayName(String input) {
        if (TextUtils.isEmpty(input)) return "KrishiSetu Farmer";
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

    private void showLanguageSelectionDialog(TextView tvCurrentLanguage) {
        String[] languages = {"🇬🇧 English (Default)", "🇮🇳 हिंदी (Hindi)"};
        new AlertDialog.Builder(requireContext())
                .setTitle("🌐 Select App Language / भाषा चुनें")
                .setItems(languages, (dialog, which) -> {
                    String selectedLang = which == 1 ? "hi" : "en";
                    if (getActivity() != null) {
                        SharedPreferences prefs = getActivity().getSharedPreferences(KrishiSetuApplication.PREFS_NAME, Context.MODE_PRIVATE);
                        prefs.edit().putString("app_lang", selectedLang).apply();

                        Locale locale = new Locale(selectedLang);
                        Locale.setDefault(locale);
                        Configuration config = new Configuration();
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
