package com.agrilink.app.fragments;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.agrilink.app.KrishiSetuApplication;
import com.agrilink.app.LocaleHelper;
import com.agrilink.app.MainActivity;
import com.agrilink.app.R;
import com.agrilink.app.services.WeatherService;
import com.google.android.material.bottomsheet.BottomSheetDialog;

public class HomeFragment extends Fragment {

    private static final int REQUEST_LOCATION_PERMISSION = 201;
    private WeatherService.WeatherData currentWeatherData = null;
    private TextView tvWeatherCardTemp;
    private TextView tvHomeLocation;
    private BottomSheetDialog activeWeatherDialog = null;
    private Runnable activeWeatherPopulate = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        TextView tvHomeAvatarInitials = view.findViewById(R.id.tvHomeAvatarInitials);
        tvHomeLocation = view.findViewById(R.id.tvHomeLocation);
        tvWeatherCardTemp = view.findViewById(R.id.tvWeatherCardTemp);

        String userRole = "Farmer";
        String nameToDisplay = getString(R.string.default_farmer);
        if (getActivity() != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences(KrishiSetuApplication.PREFS_NAME, Context.MODE_PRIVATE);
            String userName = prefs.getString("user_name", null);
            String userPhone = prefs.getString("user_phone", null);
            userRole = prefs.getString("user_role", "Farmer");

            String rawName = !TextUtils.isEmpty(userName) ? userName : (!TextUtils.isEmpty(userPhone) ? userPhone : getString(R.string.default_farmer));
            nameToDisplay = getCleanDisplayName(rawName);

            if (tvHomeAvatarInitials != null) {
                String initials = "KS";
                if (!TextUtils.isEmpty(nameToDisplay)) {
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

        applyRoleDashboard(view, userRole, nameToDisplay);

        if (tvHomeAvatarInitials != null) {
            tvHomeAvatarInitials.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).setSelectedTab(R.id.nav_profile);
                }
            });
        }

        // 1. Waste Market card -> Switches to Market tab
        View cardWasteMarket = view.findViewById(R.id.cardWasteMarket);
        if (cardWasteMarket != null) {
            cardWasteMarket.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).setSelectedTab(R.id.nav_market);
                }
            });
        }

        // 3. Equipment Rental card -> Opens EquipmentFragment
        View cardEquipment = view.findViewById(R.id.cardEquipment);
        if (cardEquipment != null) {
            cardEquipment.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).loadFragment(new EquipmentFragment());
                }
            });
        }

        // 4. Labour Hiring card -> Opens LaborFragment
        View cardLabor = view.findViewById(R.id.cardLabor);
        if (cardLabor != null) {
            cardLabor.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).loadFragment(new LaborFragment());
                }
            });
        }

        // 5. Crop Health AI card -> Opens DiseaseFragment
        View cardDisease = view.findViewById(R.id.cardDisease);
        if (cardDisease != null) {
            cardDisease.setOnClickListener(v -> {
                if (getActivity() instanceof MainActivity) {
                    ((MainActivity) getActivity()).loadFragment(new DiseaseFragment());
                }
            });
        }

        // 6. Escrow Wallet Card & Button -> Opens Escrow Wallet Details Dialog
        View.OnClickListener walletListener = v -> showEscrowWalletDialog();
        View cardHomeWallet = view.findViewById(R.id.cardHomeWallet);
        if (cardHomeWallet != null) {
            cardHomeWallet.setOnClickListener(walletListener);
        }
        View btnHomeWallet = view.findViewById(R.id.btnHomeWallet);
        if (btnHomeWallet != null) {
            btnHomeWallet.setOnClickListener(walletListener);
        }

        // 7. Transport Card -> Opens Transport Fleet Dialog with Call
        View cardTransport = view.findViewById(R.id.cardTransport);
        if (cardTransport != null) {
            cardTransport.setOnClickListener(v -> showTransportDialog());
        }

        // 8. Schemes Card -> Opens Government Schemes Dialog with Helpline
        View cardSchemes = view.findViewById(R.id.cardSchemes);
        if (cardSchemes != null) {
            cardSchemes.setOnClickListener(v -> showSchemesDialog());
        }

        // 9. Weather Card -> Opens Weather Advisory Dialog
        View cardWeather = view.findViewById(R.id.cardWeather);
        if (cardWeather != null) {
            cardWeather.setOnClickListener(v -> showWeatherAdvisoryDialog());
        }

        // 10. AgriBot Card -> Opens Interactive AgriBot AI Assistant Dialog
        View cardHomeAgribot = view.findViewById(R.id.cardHomeAgribot);
        if (cardHomeAgribot != null) {
            cardHomeAgribot.setOnClickListener(v -> showAgribotDialog());
        }

        // 11. Notifications Bell -> Opens Notifications Dialog
        View btnNotifications = view.findViewById(R.id.btnNotifications);
        if (btnNotifications != null) {
            btnNotifications.setOnClickListener(v -> showNotificationsDialog());
        }

        // 12. Location Selector -> Opens Mandi picker
        View llLocationSelector = view.findViewById(R.id.llLocationSelector);
        if (llLocationSelector != null) {
            llLocationSelector.setOnClickListener(v -> showLocationSelectorDialog(tvHomeLocation));
        }

        // Load persisted location if farmer previously selected or detected GPS
        String savedLoc = null;
        if (getActivity() != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences(KrishiSetuApplication.PREFS_NAME, Context.MODE_PRIVATE);
            savedLoc = prefs.getString("saved_location", null);
            if (savedLoc != null && !savedLoc.trim().isEmpty() && tvHomeLocation != null) {
                tvHomeLocation.setText(savedLoc);
            }
        }

        // Fetch initial real-time weather (using saved location or auto GPS/IP)
        if (savedLoc != null && !savedLoc.trim().isEmpty()) {
            loadLiveWeather(savedLoc, null);
        } else {
            fetchGpsWeatherLocation(null);
        }

        return view;
    }

    private void fetchGpsWeatherLocation(@Nullable Runnable onDone) {
        if (getContext() == null) return;
        Toast.makeText(getContext(), getString(R.string.weather_fetching), Toast.LENGTH_SHORT).show();

        WeatherService.fetchWeatherForGps(requireContext(), new WeatherService.WeatherCallback() {
            @Override
            public void onSuccess(WeatherService.WeatherData data) {
                currentWeatherData = data;
                if (tvHomeLocation != null) {
                    tvHomeLocation.setText(data.locationName);
                }
                if (tvWeatherCardTemp != null) {
                    tvWeatherCardTemp.setText(data.getFormattedTemp() + " " + data.emoji);
                }

                // Persist detected location in SharedPreferences
                if (getActivity() != null) {
                    SharedPreferences prefs = getActivity().getSharedPreferences(KrishiSetuApplication.PREFS_NAME, Context.MODE_PRIVATE);
                    prefs.edit().putString("saved_location", data.locationName).apply();
                }

                if (onDone != null) {
                    onDone.run();
                }

                if (getContext() != null) {
                    Toast.makeText(getContext(), getString(R.string.gps_detected) + ": " + data.locationName, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                if (getContext() != null) {
                    Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                }
                if (onDone != null) {
                    onDone.run();
                }
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_LOCATION_PERMISSION) {
            fetchGpsWeatherLocation(() -> {
                if (activeWeatherDialog != null && activeWeatherDialog.isShowing() && activeWeatherPopulate != null) {
                    activeWeatherPopulate.run();
                }
            });
        }
    }

    private void loadLiveWeather(String locationName, @Nullable Runnable onComplete) {
        if (tvWeatherCardTemp != null && currentWeatherData == null) {
            tvWeatherCardTemp.setText(getString(R.string.weather_fetching));
        }

        WeatherService.fetchWeatherForCity(locationName, new WeatherService.WeatherCallback() {
            @Override
            public void onSuccess(WeatherService.WeatherData data) {
                currentWeatherData = data;
                if (tvWeatherCardTemp != null) {
                    tvWeatherCardTemp.setText(data.getFormattedTemp() + " " + data.emoji);
                }
                if (onComplete != null) onComplete.run();
            }

            @Override
            public void onError(String error) {
                if (tvWeatherCardTemp != null && currentWeatherData == null) {
                    tvWeatherCardTemp.setText("32°C ☀️");
                }
                if (onComplete != null) onComplete.run();
            }
        });
    }

    private void showWeatherAdvisoryDialog() {
        if (getContext() == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        View sheet = LayoutInflater.from(getContext()).inflate(R.layout.dialog_live_weather, null);
        dialog.setContentView(sheet);

        TextView tvLocation = sheet.findViewById(R.id.tvWeatherSheetLocation);
        TextView tvEmoji = sheet.findViewById(R.id.tvWeatherEmoji);
        TextView tvTemp = sheet.findViewById(R.id.tvWeatherTemp);
        TextView tvCondition = sheet.findViewById(R.id.tvWeatherCondition);
        TextView tvFeelsLike = sheet.findViewById(R.id.tvWeatherFeelsLike);
        TextView tvHumidity = sheet.findViewById(R.id.tvWeatherHumidity);
        TextView tvWind = sheet.findViewById(R.id.tvWeatherWind);
        TextView tvSpray = sheet.findViewById(R.id.tvWeatherSprayStatus);
        TextView tvRain = sheet.findViewById(R.id.tvWeatherRainStatus);
        TextView tvAdvisory = sheet.findViewById(R.id.tvWeatherAdvisoryText);

        boolean isHindi = getContext() != null && "hi".equalsIgnoreCase(LocaleHelper.getPersistedLanguage(getContext()));

        Runnable populateViews = () -> {
            if (currentWeatherData != null) {
                if (tvLocation != null) tvLocation.setText("📍 " + currentWeatherData.locationName + " • Live Agro-Radar");
                if (tvEmoji != null) tvEmoji.setText(currentWeatherData.emoji);
                if (tvTemp != null) tvTemp.setText(currentWeatherData.getFormattedTemp());
                if (tvCondition != null) tvCondition.setText(currentWeatherData.getCondition(isHindi));
                if (tvFeelsLike != null) tvFeelsLike.setText(isHindi ?
                        "अनुभव: " + Math.round(currentWeatherData.apparentTemperature) + "°C" :
                        "Feels like " + Math.round(currentWeatherData.apparentTemperature) + "°C");
                if (tvHumidity != null) tvHumidity.setText(currentWeatherData.humidity + "%");
                if (tvWind != null) tvWind.setText(Math.round(currentWeatherData.windSpeed) + " km/h");
                if (tvSpray != null) {
                    boolean risky = currentWeatherData.windSpeed > 20.0 || currentWeatherData.weatherCode >= 51;
                    if (risky) {
                        tvSpray.setText(isHindi ? "⚠️ छिड़काव रोकें" : "⚠️ Avoid Spray");
                        tvSpray.setTextColor(getResources().getColor(R.color.error));
                    } else {
                        tvSpray.setText(isHindi ? "✅ अनुकूल मौसम" : "✅ Safe to Spray");
                        tvSpray.setTextColor(getResources().getColor(R.color.primary_green));
                    }
                }
                if (tvRain != null) {
                    if (currentWeatherData.weatherCode >= 51) {
                        tvRain.setText(isHindi ? "🌧️ बारिश संभावना" : "🌧️ Rain Likely");
                        tvRain.setTextColor(getResources().getColor(R.color.harvest_orange));
                    } else {
                        tvRain.setText(isHindi ? "☀️ साफ़ मौसम" : "☀️ Clear / Dry");
                        tvRain.setTextColor(getResources().getColor(R.color.primary_green));
                    }
                }
                if (tvAdvisory != null) tvAdvisory.setText(currentWeatherData.getAdvisory(isHindi));
            } else if (tvHomeLocation != null && tvLocation != null) {
                tvLocation.setText("📍 " + tvHomeLocation.getText().toString() + " • Live Agro-Radar");
            }
        };

        populateViews.run();

        activeWeatherDialog = dialog;
        activeWeatherPopulate = populateViews;
        dialog.setOnDismissListener(d -> {
            if (activeWeatherDialog == dialog) {
                activeWeatherDialog = null;
                activeWeatherPopulate = null;
            }
        });

        // Refresh Button
        View btnRefresh = sheet.findViewById(R.id.btnWeatherRefresh);
        if (btnRefresh != null) {
            btnRefresh.setOnClickListener(v -> {
                String loc = tvHomeLocation != null ? tvHomeLocation.getText().toString() : "Jaipur Mandi, Rajasthan";
                Toast.makeText(getContext(), getString(R.string.weather_fetching), Toast.LENGTH_SHORT).show();
                loadLiveWeather(loc, () -> {
                    populateViews.run();
                    if (getContext() != null) {
                        Toast.makeText(getContext(), getString(R.string.weather_updated), Toast.LENGTH_SHORT).show();
                    }
                });
            });
        }

        // GPS Detection Button
        View btnGps = sheet.findViewById(R.id.btnWeatherGps);
        if (btnGps != null) {
            btnGps.setOnClickListener(v -> {
                boolean hasFine = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED;
                boolean hasCoarse = ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED;

                if (!hasFine && !hasCoarse) {
                    requestPermissions(new String[]{
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                    }, REQUEST_LOCATION_PERMISSION);
                } else {
                    fetchGpsWeatherLocation(populateViews);
                }
            });
        }

        View btnClose = sheet.findViewById(R.id.btnWeatherClose);
        if (btnClose != null) btnClose.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showLocationSelectorDialog(TextView tvLocation) {
        if (getContext() == null) return;
        String[] locations = {
                "Indore Mandi, MP",
                "Dewas Mandi, MP",
                "Ujjain Agro Park, MP",
                "Pithampur Industrial Area, MP",
                "Bhopal Mandi, MP",
                "Karnal Mandi, Haryana",
                "Ludhiana Mandi, Punjab",
                "Jaipur Mandi, Rajasthan",
                "Kota Mandi, Rajasthan",
                "Chandigarh Grain Market"
        };

        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.select_mandi_location))
                .setItems(locations, (d, which) -> {
                    String selected = locations[which];
                    if (tvLocation != null) {
                        tvLocation.setText(selected);
                    }
                    if (tvHomeLocation != null) {
                        tvHomeLocation.setText(selected);
                    }
                    if (getActivity() != null) {
                        SharedPreferences prefs = getActivity().getSharedPreferences(KrishiSetuApplication.PREFS_NAME, Context.MODE_PRIVATE);
                        prefs.edit().putString("saved_location", selected).apply();
                    }
                    Toast.makeText(getContext(), selected, Toast.LENGTH_SHORT).show();
                    // Instantly fetch live weather for the selected Mandi
                    loadLiveWeather(selected, null);
                })
                .setNegativeButton(getString(R.string.got_it), null)
                .show();
    }

    private void showEscrowWalletDialog() {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.wallet_escrow_title))
                .setMessage(getString(R.string.wallet_escrow_dialog_msg))
                .setPositiveButton(getString(R.string.add_money), (d, w) -> {
                    Toast.makeText(getContext(), "UPI Gateway: Add Money active!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(getString(R.string.withdraw), (d, w) -> {
                    Toast.makeText(getContext(), "Bank Transfer: ₹4,250 requested!", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton(getString(R.string.got_it), null)
                .show();
    }

    private void showTransportDialog() {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.transport_dialog_title))
                .setMessage(getString(R.string.transport_content))
                .setPositiveButton(getString(R.string.call_transporter), (d, w) -> {
                    try {
                        Intent callIntent = new Intent(Intent.ACTION_DIAL);
                        callIntent.setData(Uri.parse("tel:+919826012345"));
                        startActivity(callIntent);
                    } catch (Exception e) {
                        Toast.makeText(getContext(), getString(R.string.dialer_unavailable_toast), Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton(getString(R.string.got_it), null)
                .show();
    }

    private void showSchemesDialog() {
        if (getContext() == null) return;
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        View sheet = getLayoutInflater().inflate(R.layout.dialog_agribot_schemes, null);
        dialog.setContentView(sheet);

        if (dialog.getWindow() != null) {
            View bottomSheet = dialog.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackgroundResource(android.R.color.transparent);
            }
        }

        // Close handlers
        View btnCloseTop = sheet.findViewById(R.id.btnCloseSchemesTop);
        if (btnCloseTop != null) btnCloseTop.setOnClickListener(v -> dialog.dismiss());

        View btnCloseBottom = sheet.findViewById(R.id.btnCloseSchemesBottom);
        if (btnCloseBottom != null) btnCloseBottom.setOnClickListener(v -> dialog.dismiss());

        // Card 1: PM-Kisan
        View cardPmKisan = sheet.findViewById(R.id.cardSchemePmKisan);
        if (cardPmKisan != null) {
            cardPmKisan.setOnClickListener(v -> {
                dialog.dismiss();
                showHomeSchemeDetailsModal(0);
            });
        }

        // Card 2: PM Fasal Bima Yojana
        View cardFasalBima = sheet.findViewById(R.id.cardSchemeFasalBima);
        if (cardFasalBima != null) {
            cardFasalBima.setOnClickListener(v -> {
                dialog.dismiss();
                showHomeSchemeDetailsModal(1);
            });
        }

        // Card 3: Kisan Credit Card (KCC)
        View cardKcc = sheet.findViewById(R.id.cardSchemeKcc);
        if (cardKcc != null) {
            cardKcc.setOnClickListener(v -> {
                dialog.dismiss();
                showHomeSchemeDetailsModal(2);
            });
        }

        // Card 4: Equipment & Solar Pump Subsidies
        View cardSubsidies = sheet.findViewById(R.id.cardSchemeSubsidies);
        if (cardSubsidies != null) {
            cardSubsidies.setOnClickListener(v -> {
                dialog.dismiss();
                showHomeSchemeDetailsModal(3);
            });
        }

        dialog.show();
    }

    private void showHomeSchemeDetailsModal(int schemeIndex) {
        if (getContext() == null) return;
        BottomSheetDialog dialog = new BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme);
        View sheet = getLayoutInflater().inflate(R.layout.dialog_scheme_detail, null);
        dialog.setContentView(sheet);

        if (dialog.getWindow() != null) {
            View bottomSheet = dialog.getWindow().findViewById(com.google.android.material.R.id.design_bottom_sheet);
            if (bottomSheet != null) {
                bottomSheet.setBackgroundResource(android.R.color.transparent);
            }
        }

        ImageView ivIcon = sheet.findViewById(R.id.ivDetailSchemeIcon);
        View flContainer = sheet.findViewById(R.id.flDetailIconContainer);
        TextView tvTitle = sheet.findViewById(R.id.tvDetailSchemeTitle);
        TextView tvBadge = sheet.findViewById(R.id.tvDetailSchemeBadge);
        TextView tvBenefits = sheet.findViewById(R.id.tvDetailBenefits);
        TextView tvEligibility = sheet.findViewById(R.id.tvDetailEligibility);
        TextView tvDocuments = sheet.findViewById(R.id.tvDetailDocuments);
        TextView tvHowToApply = sheet.findViewById(R.id.tvDetailHowToApply);
        View btnClose = sheet.findViewById(R.id.btnDetailClose);
        View btnDismiss = sheet.findViewById(R.id.btnDetailDismiss);
        View btnLearnMore = sheet.findViewById(R.id.btnDetailLearnMore);

        String portalUrl = "https://pmkisan.gov.in";

        switch (schemeIndex) {
            case 0:
                if (ivIcon != null) {
                    ivIcon.setImageResource(R.drawable.ic_scheme_wheat);
                    ivIcon.setColorFilter(Color.parseColor("#2E7D32"));
                }
                if (flContainer != null) flContainer.setBackgroundResource(R.drawable.bg_circle_icon_green);
                if (tvTitle != null) tvTitle.setText("PM-Kisan Samman Nidhi");
                if (tvBadge != null) {
                    tvBadge.setText("₹6,000/Year");
                    tvBadge.setBackgroundResource(R.drawable.bg_badge_scheme_green);
                    tvBadge.setTextColor(Color.parseColor("#2E7D32"));
                }
                if (tvBenefits != null) {
                    tvBenefits.setText("• ₹6,000 प्रति वर्ष 3 समान किस्तों (₹2,000 प्रत्येक) में सीधे बैंक खाते में जमा।\n• 100% केंद्र सरकार द्वारा वित्तपोषित प्रत्यक्ष लाभ अंतरण (DBT Direct Benefit Transfer)।");
                }
                if (tvEligibility != null) {
                    tvEligibility.setText("• सभी भूमिधारक किसान परिवार जिनके नाम पर कृषि योग्य भूमि पंजीकृत है।\n• संस्थागत भूमिधारक, संवैधानिक पदधारक एवं आयकरदाता अपात्र हैं।");
                }
                if (tvDocuments != null) {
                    tvDocuments.setText("• आधार कार्ड (सक्रिय मोबाइल नंबर से लिंक)।\n• बैंक खाता पासबुक (आधार एवं NPCI डीबीटी लिंक)।\n• भू-अभिलेख खतौनी / खसरा नकल।");
                }
                if (tvHowToApply != null) {
                    tvHowToApply.setText("• ऑनलाइन: pmkisan.gov.in पोर्टल पर 'New Farmer Registration' द्वारा।\n• ऑफलाइन: नजदीकी सीएससी (CSC) केंद्र या कृषि विभाग कार्यालय में संपर्क करें।");
                }
                portalUrl = "https://pmkisan.gov.in";
                break;

            case 1:
                if (ivIcon != null) {
                    ivIcon.setImageResource(R.drawable.ic_scheme_shield);
                    ivIcon.setColorFilter(Color.parseColor("#1565C0"));
                }
                if (flContainer != null) flContainer.setBackgroundResource(R.drawable.bg_circle_icon_blue);
                if (tvTitle != null) tvTitle.setText("PM Fasal Bima Yojana (PMFBY)");
                if (tvBadge != null) {
                    tvBadge.setText("Crop Insurance");
                    tvBadge.setBackgroundResource(R.drawable.bg_badge_scheme_blue);
                    tvBadge.setTextColor(Color.parseColor("#1565C0"));
                }
                if (tvBenefits != null) {
                    tvBenefits.setText("• खरीफ फसलों के लिए मात्र 2%, रबी के लिए 1.5% और बागवानी फसलों के लिए 5% न्यूनतम प्रीमियम।\n• सूखा, बाढ़, बेमौसम बारिश, कीट प्रकोप या ओलावृष्टि से हुए नुकसान पर संपूर्ण वित्तीय मुआवजा।");
                }
                if (tvEligibility != null) {
                    tvEligibility.setText("• अधिसूचित क्षेत्रों में अधिसूचित फसलें उगाने वाले सभी किसान (बटाईदार व पट्टेदार सहित)।\n• बैंक से किसान क्रेडिट कार्ड (KCC) लेने वाले व गैर-ऋणी किसान दोनों पात्र।");
                }
                if (tvDocuments != null) {
                    tvDocuments.setText("• आधार कार्ड व पहचान प्रमाण।\n• बैंक खाता पासबुक (IFSC कोड सहित)।\n• बुवाई प्रमाण पत्र (पटवारी/ग्राम सेवक प्रदत्त)।\n• भूमि दस्तावेज (खसरा/खतौनी)।");
                }
                if (tvHowToApply != null) {
                    tvHowToApply.setText("• ऑनलाइन: pmfby.gov.in पोर्टल या 'Crop Insurance' मोबाइल ऐप से।\n• नुकसान होने पर 72 घंटे के भीतर हेल्पलाइन 1800-180-1551 पर सूचना दर्ज कराएं।");
                }
                portalUrl = "https://pmfby.gov.in";
                break;

            case 2:
                if (ivIcon != null) {
                    ivIcon.setImageResource(R.drawable.ic_scheme_kcc);
                    ivIcon.setColorFilter(Color.parseColor("#E65100"));
                }
                if (flContainer != null) flContainer.setBackgroundResource(R.drawable.bg_circle_icon_amber);
                if (tvTitle != null) tvTitle.setText("Kisan Credit Card (KCC)");
                if (tvBadge != null) {
                    tvBadge.setText("Low Interest (4%)");
                    tvBadge.setBackgroundResource(R.drawable.bg_badge_scheme_amber);
                    tvBadge.setTextColor(Color.parseColor("#E65100"));
                }
                if (tvBenefits != null) {
                    tvBenefits.setText("• ₹3 लाख तक का अल्पकालिक कृषि ऋण मात्र 4% प्रभावी वार्षिक ब्याज पर (समय पर चुकता करने पर 3% ब्याज छूट)।\n• ₹1.60 लाख तक का ऋण बिना किसी बंधक या गारंटी (Collateral-Free) के उपलब्ध।");
                }
                if (tvEligibility != null) {
                    tvEligibility.setText("• सभी किसान, काश्तकार, पट्टेदार किसान एवं स्वयं सहायता समूह (SHG)।\n• डेयरी, पशुपालन एवं मत्स्य पालन करने वाले किसान भी ऋण हेतु पात्र हैं।");
                }
                if (tvDocuments != null) {
                    tvDocuments.setText("• विधिवत भरा हुआ KCC आवेदन पत्र व 2 पासपोर्ट फोटो।\n• पहचान एवं निवास प्रमाण (आधार कार्ड / वोटर आईडी)।\n• जमीन का खसरा/खतौनी एवं पटवारी रिपोर्ट।");
                }
                if (tvHowToApply != null) {
                    tvHowToApply.setText("• नजदीकी बैंक शाखा (ग्रामीण, सहकारी या राष्ट्रीयकृत बैंक) में संपर्क करें।\n• पीएम-किसान पोर्टल से सरल एक पृष्ठीय KCC फॉर्म डाउनलोड कर जमा करें।");
                }
                portalUrl = "https://pmkisan.gov.in";
                break;

            case 3:
            default:
                if (ivIcon != null) {
                    ivIcon.setImageResource(R.drawable.ic_scheme_machinery);
                    ivIcon.setColorFilter(Color.parseColor("#6A1B9A"));
                }
                if (flContainer != null) flContainer.setBackgroundResource(R.drawable.bg_circle_icon_purple);
                if (tvTitle != null) tvTitle.setText("Agri Machinery & Solar Subsidies");
                if (tvBadge != null) {
                    tvBadge.setText("Subsidy (40%-80%)");
                    tvBadge.setBackgroundResource(R.drawable.bg_badge_scheme_purple);
                    tvBadge.setTextColor(Color.parseColor("#6A1B9A"));
                }
                if (tvBenefits != null) {
                    tvBenefits.setText("• पीएम-कुसुम (PM-KUSUM) योजना अंतर्गत 60% सरकारी अनुदान पर सोलर वाटर पंप स्थापना।\n• कृषि यंत्रीकरण (SMAM) में रोटावेटर, रीपर, बेलर व थ्रेशर पर 40% से 80% तक वित्तीय सब्सिडी।");
                }
                if (tvEligibility != null) {
                    tvEligibility.setText("• व्यक्तिगत किसान, किसान समूह (FPO) व ग्राम पंचायत स्तर की समितियां।\n• छोटे, सीमांत एवं महिला किसानों को वित्तीय सहायता में प्राथमिकता।");
                }
                if (tvDocuments != null) {
                    tvDocuments.setText("• आधार कार्ड, पैन कार्ड एवं बैंक पासबुक।\n• भू-अभिलेख खसरा/खतौनी नकल।\n• बिजली कनेक्शन न होने का शपथ पत्र (सोलर पंप हेतु)।");
                }
                if (tvHowToApply != null) {
                    tvHowToApply.setText("• राज्य कृषि यंत्र पोर्टल (e-Krishi Yantra / DBT Agriculture) पर ऑनलाइन आवेदन करें।\n• सोलर पंप हेतु pmkusum.mnre.gov.in पर पंजीकरण कराएं।");
                }
                portalUrl = "https://pmkusum.mnre.gov.in";
                break;
        }

        View.OnClickListener dismissListener = v -> {
            dialog.dismiss();
            showSchemesDialog();
        };
        if (btnClose != null) btnClose.setOnClickListener(dismissListener);
        if (btnDismiss != null) btnDismiss.setOnClickListener(dismissListener);

        final String finalPortalUrl = portalUrl;
        if (btnLearnMore != null) {
            btnLearnMore.setOnClickListener(v -> {
                try {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:18001801551"));
                    startActivity(callIntent);
                } catch (Exception e) {
                    try {
                        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(finalPortalUrl));
                        startActivity(browserIntent);
                    } catch (Exception ignored) {
                    }
                }
            });
        }

        dialog.show();
    }

    private void showAgribotDialog() {
        if (getContext() == null) return;
        String[] sampleQueries = {
                "• " + getString(R.string.agribot_sample_q1),
                "• " + getString(R.string.agribot_sample_q2),
                "• " + getString(R.string.agribot_sample_q3)
        };

        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.agribot_dialog_title))
                .setItems(sampleQueries, (dialog, which) -> {
                    String answer = "";
                    if (which == 0) answer = getString(R.string.agribot_ans_1);
                    else if (which == 1) answer = getString(R.string.agribot_ans_2);
                    else if (which == 2) answer = getString(R.string.agribot_ans_3);

                    new AlertDialog.Builder(requireContext())
                            .setTitle(sampleQueries[which])
                            .setMessage("🤖 KrishiSetu AI:\n\n" + answer)
                            .setPositiveButton(getString(R.string.got_it), null)
                            .show();
                })
                .setPositiveButton(getString(R.string.got_it), null)
                .show();
    }

    private void showNotificationsDialog() {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.notifications_dialog_title))
                .setMessage(getString(R.string.notifications_content))
                .setPositiveButton(getString(R.string.got_it), (d, w) -> d.dismiss())
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

    private void applyRoleDashboard(View view, String userRole, String nameToDisplay) {
        TextView tvHomeGreeting = view.findViewById(R.id.tvHomeGreeting);
        TextView tvWalletSubtext = view.findViewById(R.id.tvWalletSubtext);
        TextView tvEarnTag = view.findViewById(R.id.tvEarnTag);
        View cardDisease = view.findViewById(R.id.cardDisease);
        View cardLabor = view.findViewById(R.id.cardLabor);
        View cardEquipment = view.findViewById(R.id.cardEquipment);
        View cardTransport = view.findViewById(R.id.cardTransport);
        View cardWasteMarket = view.findViewById(R.id.cardWasteMarket);

        String lowerRole = userRole != null ? userRole.toLowerCase() : "farmer";

        if (lowerRole.contains("industry") || lowerRole.contains("buyer")) {
            if (tvHomeGreeting != null) {
                tvHomeGreeting.setText(nameToDisplay + " (" + getString(R.string.role_industry_name) + ")");
            }
            if (tvEarnTag != null) tvEarnTag.setText("🏭 Biomass Escrow");
            if (tvWalletSubtext != null) tvWalletSubtext.setText("₹56,350 Secured in Biomass Deals");
            // Hide farmer-only disease diagnosis for industry buyer
            if (cardDisease != null) cardDisease.setVisibility(View.GONE);
        } else if (lowerRole.contains("labour") || lowerRole.contains("worker")) {
            if (tvHomeGreeting != null) {
                tvHomeGreeting.setText(nameToDisplay + " (" + getString(R.string.role_labour_name) + ")");
            }
            if (tvEarnTag != null) tvEarnTag.setText("👨‍🌾 Worker Earnings");
            if (tvWalletSubtext != null) tvWalletSubtext.setText("Active Jobs: 3 • Rating: 4.9 ⭐");
            if (cardDisease != null) cardDisease.setVisibility(View.GONE);
            if (cardLabor != null) {
                cardLabor.setOnClickListener(v -> showWorkerProfileDialog());
            }
        } else if (lowerRole.contains("equipment")) {
            if (tvHomeGreeting != null) {
                tvHomeGreeting.setText(nameToDisplay + " (" + getString(R.string.role_equipment_name) + ")");
            }
            if (tvEarnTag != null) tvEarnTag.setText("🚜 Rental Income");
            if (tvWalletSubtext != null) tvWalletSubtext.setText("Active Fleet: 3 • Bookings: 2");
            if (cardDisease != null) cardDisease.setVisibility(View.GONE);
            if (cardEquipment != null) {
                cardEquipment.setOnClickListener(v -> showEquipmentOwnerDialog());
            }
        } else if (lowerRole.contains("contractor") || lowerRole.contains("thekedar")) {
            if (tvHomeGreeting != null) {
                tvHomeGreeting.setText(nameToDisplay + " (" + getString(R.string.role_contractor_name) + ")");
            }
            if (tvEarnTag != null) tvEarnTag.setText("📋 Contractor Dashboard");
            if (tvWalletSubtext != null) tvWalletSubtext.setText("Workers Managed: 35 • Active Contracts: 4");
            if (cardDisease != null) cardDisease.setVisibility(View.GONE);
            if (cardLabor != null) {
                cardLabor.setOnClickListener(v -> showContractorDialog());
            }
        } else if (lowerRole.contains("transport")) {
            if (tvHomeGreeting != null) {
                tvHomeGreeting.setText(nameToDisplay + " (" + getString(R.string.role_transport_name) + ")");
            }
            if (tvEarnTag != null) tvEarnTag.setText("🚚 Transport Logistics");
            if (tvWalletSubtext != null) tvWalletSubtext.setText("Active Trucks: 2 • Deliveries in Transit: 1");
            if (cardDisease != null) cardDisease.setVisibility(View.GONE);
            if (cardTransport != null) {
                cardTransport.setOnClickListener(v -> showTransporterFleetDialog());
            }
        } else {
            // Default Farmer
            if (tvHomeGreeting != null) {
                tvHomeGreeting.setText(String.format(getString(R.string.greeting_farmer), nameToDisplay));
            }
        }
    }

    private void showWorkerProfileDialog() {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("👨‍🌾 " + getString(R.string.my_worker_profile))
                .setMessage("Name: Radheshyam Kushwaha\n" +
                        "Skills: Harvesting, Planting, Irrigation, Machine Operation\n" +
                        "Experience: 6+ Years\n" +
                        "Location: Indore, MP\n" +
                        "Daily Rate: ₹600 / Day\n" +
                        "Availability: 🟢 Available for Work\n" +
                        "Completed Jobs: 28 Verified Tasks (4.9 ⭐)")
                .setPositiveButton("Toggle Availability", (d, w) -> {
                    Toast.makeText(getContext(), "Availability status updated: 🟢 Available", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("View Requests (3)", (d, w) -> {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).setSelectedTab(R.id.nav_deals);
                    }
                })
                .setNegativeButton(getString(R.string.got_it), null)
                .show();
    }

    private void showEquipmentOwnerDialog() {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("🚜 " + getString(R.string.my_equipment))
                .setMessage("My Registered Machinery:\n" +
                        "1. John Deere 5050D Tractor (45 HP) - ₹1,200/Day [ACTIVE]\n" +
                        "2. Rotavator & Seed Drill - ₹800/Day [AVAILABLE]\n" +
                        "3. Kubota Combined Harvester - ₹2,500/Day [RENTED]\n\n" +
                        "Incoming Rental Requests: 2 Pending\n" +
                        "This Month Earnings: ₹34,800.00")
                .setPositiveButton("Add Machinery", (d, w) -> {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).loadFragment(new EquipmentFragment());
                    }
                })
                .setNeutralButton("Manage Bookings", (d, w) -> {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).setSelectedTab(R.id.nav_deals);
                    }
                })
                .setNegativeButton(getString(R.string.got_it), null)
                .show();
    }

    private void showContractorDialog() {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("📋 " + getString(R.string.role_contractor_name))
                .setMessage("Contractor Workforce Management:\n" +
                        "• Active Farm Crews: 3 Teams (35 Workers)\n" +
                        "• Pending Worker Requests: 8 Applicants\n" +
                        "• Active Harvest Contracts: 4 Sites (Indore, Dewas, Ujjain)\n" +
                        "• Payment Safety: Escrow Backed Payouts")
                .setPositiveButton(getString(R.string.post_work_requirement), (d, w) -> {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).loadFragment(new LaborFragment());
                    }
                })
                .setNegativeButton(getString(R.string.got_it), null)
                .show();
    }

    private void showTransporterFleetDialog() {
        if (getContext() == null) return;
        new AlertDialog.Builder(getContext())
                .setTitle("🚚 " + getString(R.string.role_transport_name))
                .setMessage("Transporter Fleet Operations:\n" +
                        "1. Tata 12-Wheeler Truck (20 Ton Stubble Bed) [IN TRANSIT - Indore to Pithampur]\n" +
                        "2. High-Side Tractor Trolley (8 Ton) [AVAILABLE]\n\n" +
                        "Incoming Delivery Requests: 3 Pending\n" +
                        "Freight Rate: ₹42.00 / km\n" +
                        "Completed Deliveries: 42 Trips")
                .setPositiveButton("View Requests", (d, w) -> {
                    if (getActivity() instanceof MainActivity) {
                        ((MainActivity) getActivity()).setSelectedTab(R.id.nav_deals);
                    }
                })
                .setNegativeButton(getString(R.string.got_it), null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (getActivity() != null && getView() != null) {
            SharedPreferences prefs = getActivity().getSharedPreferences(KrishiSetuApplication.PREFS_NAME, Context.MODE_PRIVATE);
            String role = prefs.getString("user_role", "Farmer");
            String userName = prefs.getString("user_name", null);
            String userPhone = prefs.getString("user_phone", null);
            String rawName = !TextUtils.isEmpty(userName) ? userName : (!TextUtils.isEmpty(userPhone) ? userPhone : getString(R.string.default_farmer));
            applyRoleDashboard(getView(), role, getCleanDisplayName(rawName));
        }
    }
}
