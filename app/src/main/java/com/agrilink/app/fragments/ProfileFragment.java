package com.agrilink.app.fragments;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
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
import com.agrilink.app.LocaleHelper;
import com.agrilink.app.LoginActivity;
import com.agrilink.app.MainActivity;
import com.agrilink.app.R;

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
                tvUserRole.setText(userRole + " • " + getString(R.string.verified_badge));
                View parent = (View) tvUserRole.getParent();
                View.OnClickListener roleListener = v -> showRoleSelectionDialog(tvUserRole);
                tvUserRole.setOnClickListener(roleListener);
                if (parent != null) {
                    parent.setOnClickListener(roleListener);
                }
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
        updateLanguageDisplay(tvCurrentLanguage);

        if (rlLanguageToggle != null) {
            rlLanguageToggle.setOnClickListener(v -> showLanguageSelectionDialog(tvCurrentLanguage));
        }

        // Wallet Withdraw
        if (view.findViewById(R.id.btnWithdraw) != null) {
            view.findViewById(R.id.btnWithdraw).setOnClickListener(v -> {
                new AlertDialog.Builder(requireContext())
                        .setTitle(getString(R.string.wallet_withdrawal_title))
                        .setMessage(getString(R.string.wallet_withdrawal_msg))
                        .setPositiveButton(getString(R.string.transfer_now), (dialog, which) -> {
                            Toast.makeText(getContext(), getString(R.string.withdrawal_sent_toast), Toast.LENGTH_LONG).show();
                        })
                        .setNegativeButton(getString(R.string.cancel), null)
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
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).setSelectedTab(R.id.nav_deals);
                } else {
                    Intent intent = new Intent(getActivity(), ChatDealActivity.class);
                    startActivity(intent);
                }
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
                        .setTitle(getString(R.string.logout_title))
                        .setMessage(getString(R.string.logout_confirm_msg))
                        .setPositiveButton(getString(R.string.logout), (dialog, which) -> {
                            if (getActivity() != null) {
                                SharedPreferences prefs = getActivity().getSharedPreferences(KrishiSetuApplication.PREFS_NAME, Context.MODE_PRIVATE);
                                prefs.edit().clear().apply();
                                Toast.makeText(getContext(), getString(R.string.logged_out_toast), Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(getActivity(), LoginActivity.class));
                                getActivity().finish();
                            }
                        })
                        .setNegativeButton(getString(R.string.cancel), null)
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

    private void updateLanguageDisplay(TextView tvCurrentLanguage) {
        if (tvCurrentLanguage == null || getActivity() == null) return;
        String lang = LocaleHelper.getPersistedLanguage(requireContext());
        if ("hi".equalsIgnoreCase(lang)) {
            tvCurrentLanguage.setText("हिंदी 🇮🇳 ›");
        } else {
            tvCurrentLanguage.setText("English 🇬🇧 ›");
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
                .setTitle(getString(R.string.theme_mode))
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

                    Toast.makeText(getContext(), getString(R.string.theme_updated_toast), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss())
                .show();
    }

    private void showLanguageSelectionDialog(TextView tvCurrentLanguage) {
        String[] languages = {"🇬🇧 English (Default)", "🇮🇳 हिंदी (Hindi)"};
        int checkedItem = "hi".equalsIgnoreCase(LocaleHelper.getPersistedLanguage(requireContext())) ? 1 : 0;

        new AlertDialog.Builder(requireContext())
                .setTitle("Select App Language / भाषा चुनें")
                .setSingleChoiceItems(languages, checkedItem, (dialog, which) -> {
                    String selectedLang = which == 1 ? LocaleHelper.LANG_HI : LocaleHelper.LANG_EN;
                    LocaleHelper.setLocale(requireContext(), selectedLang);
                    LocaleHelper.applyAppLanguage(selectedLang);

                    updateLanguageDisplay(tvCurrentLanguage);
                    dialog.dismiss();

                    if (getActivity() != null) {
                        getActivity().recreate();
                    }
                })
                .setNegativeButton("Cancel / रद्द करें", (d, w) -> d.dismiss())
                .show();
    }

    private void showOrdersDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.my_orders_bookings))
                .setMessage(getString(R.string.orders_content))
                .setPositiveButton(getString(R.string.got_it), null)
                .show();
    }

    private void showListingsDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.my_published_listings))
                .setMessage(getString(R.string.listings_content))
                .setPositiveButton(getString(R.string.post_new_crop_listing), (d, w) -> {
                    startActivity(new Intent(getActivity(), com.agrilink.app.CreateListingActivity.class));
                })
                .setNegativeButton(getString(R.string.got_it), null)
                .show();
    }

    private void showHelpSupportDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.help_kisan_support))
                .setMessage(getString(R.string.help_support_content))
                .setPositiveButton("1800-180-1551", (d, w) -> {
                    try {
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(Uri.parse("tel:18001801551"));
                        startActivity(callIntent);
                    } catch (Exception ignored) {
                    }
                })
                .setNegativeButton(getString(R.string.got_it), null)
                .show();
    }

    private void showTermsDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.privacy_terms))
                .setMessage(getString(R.string.terms_content))
                .setPositiveButton(getString(R.string.got_it), null)
                .show();
    }

    private String getCleanDisplayName(String input) {
        if (TextUtils.isEmpty(input)) return "Farmer";
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

    private void showRoleSelectionDialog(TextView tvUserRole) {
        String[] roles = {
                "🌾 " + getString(R.string.role_farmer_name) + " (Farmer)",
                "🏭 " + getString(R.string.role_industry_name) + " (Industry Buyer)",
                "🛠️ " + getString(R.string.role_labour_name) + " (Labour / Worker)",
                "🚜 " + getString(R.string.role_equipment_name) + " (Equipment Owner)",
                "🏗️ " + getString(R.string.role_contractor_name) + " (Contractor)",
                "🚚 " + getString(R.string.role_transport_name) + " (Transporter)"
        };

        if (getActivity() == null) return;
        SharedPreferences prefs = getActivity().getSharedPreferences(KrishiSetuApplication.PREFS_NAME, Context.MODE_PRIVATE);
        String currentRole = prefs.getString("user_role", "Farmer");
        int checkedItem = 0;
        if (currentRole.equalsIgnoreCase("Industry Buyer")) checkedItem = 1;
        else if (currentRole.equalsIgnoreCase("Labour") || currentRole.equalsIgnoreCase("Labor")) checkedItem = 2;
        else if (currentRole.equalsIgnoreCase("Equipment Owner") || currentRole.equalsIgnoreCase("Equipment")) checkedItem = 3;
        else if (currentRole.equalsIgnoreCase("Contractor")) checkedItem = 4;
        else if (currentRole.equalsIgnoreCase("Transporter")) checkedItem = 5;

        new AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.select_user_role))
                .setSingleChoiceItems(roles, checkedItem, (dialog, which) -> {
                    String selectedRole = "Farmer";
                    String apiRole = "FARMER";
                    switch (which) {
                        case 1:
                            selectedRole = "Industry Buyer";
                            apiRole = "BUYER";
                            break;
                        case 2:
                            selectedRole = "Labour";
                            apiRole = "LABOR";
                            break;
                        case 3:
                            selectedRole = "Equipment Owner";
                            apiRole = "EQUIPMENT";
                            break;
                        case 4:
                            selectedRole = "Contractor";
                            apiRole = "CONTRACTOR";
                            break;
                        case 5:
                            selectedRole = "Transporter";
                            apiRole = "TRANSPORTER";
                            break;
                        default:
                            selectedRole = "Farmer";
                            apiRole = "FARMER";
                            break;
                    }
                    prefs.edit()
                            .putString("user_role", selectedRole)
                            .putString("api_role", apiRole)
                            .apply();
                    if (tvUserRole != null) {
                        tvUserRole.setText(selectedRole + " • " + getString(R.string.verified_badge));
                    }
                    dialog.dismiss();
                    Toast.makeText(getContext(), selectedRole + " - " + getString(R.string.role_active_toast), Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(getString(R.string.cancel), (dialog, which) -> dialog.dismiss())
                .show();
    }
}
