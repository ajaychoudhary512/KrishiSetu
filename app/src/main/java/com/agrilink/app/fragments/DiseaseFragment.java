package com.agrilink.app.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.ImageDecoder;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.agrilink.app.R;
import com.agrilink.app.ml.PlantDiseaseClassifier;
import com.google.android.material.bottomsheet.BottomSheetDialog;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DiseaseFragment extends Fragment {

    private static final String TAG = "DiseaseFragment";
    private static final int REQUEST_CAMERA = 101;
    private static final int REQUEST_GALLERY = 102;

    private PlantDiseaseClassifier classifier;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

    // UI elements for live inference
    private View layoutScanningProgress;
    private View cardAnalysisResult;
    private ImageView ivResultLeaf;
    private TextView tvResultDiseaseTitle;
    private TextView tvResultCropName;
    private TextView tvResultConfidence;
    private ProgressBar pbResultConfidence;
    private TextView tvResultRemedy;
    private View btnViewFullAdvisory;
    private View btnRescan;

    // Recent Detections Item 1 UI elements
    private ImageView imgDet1;
    private TextView tvDet1Title;
    private TextView tvDet1Confidence;

    // Cache latest recognition for full dialog advisory
    private PlantDiseaseClassifier.Recognition latestRecognition;
    private Bitmap latestBitmap;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_disease, container, false);

        // Initialize TFLite classifier
        try {
            classifier = new PlantDiseaseClassifier(
                requireContext(),
                PlantDiseaseClassifier.DEFAULT_MODEL_NAME,
                PlantDiseaseClassifier.DEFAULT_LABELS_NAME
            );
            Log.d(TAG, "PlantDiseaseClassifier loaded successfully.");
        } catch (Exception e) {
            Log.e(TAG, "Failed to initialize PlantDiseaseClassifier", e);
        }

        // Bind result views
        layoutScanningProgress = view.findViewById(R.id.layoutScanningProgress);
        cardAnalysisResult = view.findViewById(R.id.cardAnalysisResult);
        ivResultLeaf = view.findViewById(R.id.ivResultLeaf);
        tvResultDiseaseTitle = view.findViewById(R.id.tvResultDiseaseTitle);
        tvResultCropName = view.findViewById(R.id.tvResultCropName);
        tvResultConfidence = view.findViewById(R.id.tvResultConfidence);
        pbResultConfidence = view.findViewById(R.id.pbResultConfidence);
        tvResultRemedy = view.findViewById(R.id.tvResultRemedy);
        btnViewFullAdvisory = view.findViewById(R.id.btnViewFullAdvisory);
        btnRescan = view.findViewById(R.id.btnRescan);

        // Bind recent detection 1 views if present
        imgDet1 = view.findViewById(R.id.imgDet1);
        View cardImg1 = view.findViewById(R.id.cardImg1);
        if (cardImg1 != null && cardImg1.getParent() instanceof ViewGroup) {
            ViewGroup parent = (ViewGroup) cardImg1.getParent();
            for (int i = 0; i < parent.getChildCount(); i++) {
                View child = parent.getChildAt(i);
                if (child instanceof ViewGroup) {
                    ViewGroup textContainer = (ViewGroup) child;
                    if (textContainer.getChildCount() >= 2) {
                        if (textContainer.getChildAt(0) instanceof TextView) {
                            tvDet1Title = (TextView) textContainer.getChildAt(0);
                        }
                        if (textContainer.getChildAt(1) instanceof TextView) {
                            tvDet1Confidence = (TextView) textContainer.getChildAt(1);
                        }
                    }
                }
            }
        }

        View.OnClickListener scanListener = v -> showImageSourceDialog();

        if (view.findViewById(R.id.btnTakePhoto) != null) {
            view.findViewById(R.id.btnTakePhoto).setOnClickListener(v -> {
                Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                startActivityForResult(cameraIntent, REQUEST_CAMERA);
            });
        }

        if (view.findViewById(R.id.btnUploadGallery) != null) {
            view.findViewById(R.id.btnUploadGallery).setOnClickListener(v -> {
                Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                startActivityForResult(galleryIntent, REQUEST_GALLERY);
            });
        }

        if (view.findViewById(R.id.cardUploadLeaf) != null) view.findViewById(R.id.cardUploadLeaf).setOnClickListener(scanListener);

        if (btnRescan != null) {
            btnRescan.setOnClickListener(scanListener);
        }

        if (btnViewFullAdvisory != null) {
            btnViewFullAdvisory.setOnClickListener(v -> {
                if (latestRecognition != null) {
                    showDiseaseAnalysisResultDialog(latestRecognition);
                }
            });
        }

        if (view.findViewById(R.id.cardAgriBot) != null) {
            view.findViewById(R.id.cardAgriBot).setOnClickListener(v -> showAgriBotSchemeDialog());
        }

        return view;
    }

    private void showImageSourceDialog() {
        String[] options = {"📷 Take Photo with Camera", "🖼️ Choose from Gallery"};
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.select_image_source))
                .setItems(options, (dialog, which) -> {
                    if (which == 0) {
                        Intent cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, REQUEST_CAMERA);
                    } else {
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(galleryIntent, REQUEST_GALLERY);
                    }
                })
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        Bitmap selectedBitmap = null;

        try {
            if (requestCode == REQUEST_CAMERA && data != null) {
                if (data.getExtras() != null && data.getExtras().get("data") instanceof Bitmap) {
                    selectedBitmap = (Bitmap) data.getExtras().get("data");
                } else if (data.getData() != null) {
                    selectedBitmap = loadBitmapFromUri(data.getData());
                }
            } else if (requestCode == REQUEST_GALLERY && data != null && data.getData() != null) {
                selectedBitmap = loadBitmapFromUri(data.getData());
            }
        } catch (Exception e) {
            Log.e(TAG, "Error retrieving selected image", e);
            Toast.makeText(getContext(), "Error loading selected image: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        if (selectedBitmap != null) {
            analyzeLeafImage(selectedBitmap);
        } else {
            Toast.makeText(getContext(), "Unable to retrieve leaf image. Please try again.", Toast.LENGTH_SHORT).show();
        }
    }

    private Bitmap loadBitmapFromUri(Uri uri) throws IOException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            ImageDecoder.Source source = ImageDecoder.createSource(requireContext().getContentResolver(), uri);
            return ImageDecoder.decodeBitmap(source, (decoder, info, s) -> {
                decoder.setMutableRequired(true);
            });
        } else {
            return MediaStore.Images.Media.getBitmap(requireContext().getContentResolver(), uri);
        }
    }

    private void analyzeLeafImage(Bitmap bitmap) {
        latestBitmap = bitmap;

        // Show scanning progress on UI
        if (layoutScanningProgress != null) {
            layoutScanningProgress.setVisibility(View.VISIBLE);
        }
        if (cardAnalysisResult != null) {
            cardAnalysisResult.setVisibility(View.GONE);
        }
        Toast.makeText(getContext(), getString(R.string.ai_analyzing_leaf), Toast.LENGTH_SHORT).show();

        executor.execute(() -> {
            try {
                if (classifier == null) {
                    classifier = new PlantDiseaseClassifier(
                        requireContext(),
                        PlantDiseaseClassifier.DEFAULT_MODEL_NAME,
                        PlantDiseaseClassifier.DEFAULT_LABELS_NAME
                    );
                }

                PlantDiseaseClassifier.Recognition result = classifier.classify(bitmap);

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (isAdded()) {
                            displayClassificationResult(bitmap, result);
                        }
                    });
                }
            } catch (Exception e) {
                Log.e(TAG, "Inference execution error", e);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        if (isAdded()) {
                            if (layoutScanningProgress != null) {
                                layoutScanningProgress.setVisibility(View.GONE);
                            }
                            Toast.makeText(getContext(), "Inference error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    });
                }
            }
        });
    }

    private void displayClassificationResult(Bitmap bitmap, PlantDiseaseClassifier.Recognition result) {
        latestRecognition = result;

        if (layoutScanningProgress != null) {
            layoutScanningProgress.setVisibility(View.GONE);
        }

        if (cardAnalysisResult != null) {
            cardAnalysisResult.setVisibility(View.VISIBLE);
        }

        if (ivResultLeaf != null) {
            ivResultLeaf.setImageBitmap(bitmap);
        }

        if (tvResultDiseaseTitle != null) {
            tvResultDiseaseTitle.setText(result.getDisplayName());
        }

        if (tvResultCropName != null) {
            tvResultCropName.setText("Crop: " + result.getPlantName());
        }

        if (tvResultConfidence != null) {
            tvResultConfidence.setText(result.getConfidencePercentage() + " AI Confidence");
        }

        if (pbResultConfidence != null) {
            int progress = Math.round(result.getConfidence() * 100);
            pbResultConfidence.setProgress(Math.max(progress, 1));
        }

        if (tvResultRemedy != null) {
            tvResultRemedy.setText(result.getRemedyAdvice());
        }

        // Update Recent Detections Item 1 with the newly classified real data
        if (imgDet1 != null) {
            imgDet1.setImageBitmap(bitmap);
            imgDet1.setPadding(0, 0, 0, 0);
        }
        if (tvDet1Title != null) {
            tvDet1Title.setText(result.getDisplayName());
        }
        if (tvDet1Confidence != null) {
            tvDet1Confidence.setText("AI Confidence: " + result.getConfidencePercentage() + " • Treatment Advised");
        }

        // Show comprehensive analysis dialog
        showDiseaseAnalysisResultDialog(result);
    }

    private void showDiseaseAnalysisResultDialog(PlantDiseaseClassifier.Recognition result) {
        if (getContext() == null) return;

        BottomSheetDialog dialog = new BottomSheetDialog(requireContext());
        View sheet = LayoutInflater.from(getContext()).inflate(R.layout.dialog_disease_report, null);
        dialog.setContentView(sheet);

        TextView tvTitle = sheet.findViewById(R.id.tvReportTitle);
        TextView tvId = sheet.findViewById(R.id.tvReportId);
        TextView tvStatus = sheet.findViewById(R.id.tvReportStatusBadge);
        ImageView ivLeaf = sheet.findViewById(R.id.ivReportLeafImage);
        TextView tvCrop = sheet.findViewById(R.id.tvReportCrop);
        TextView tvDisease = sheet.findViewById(R.id.tvReportDisease);
        TextView tvSeverity = sheet.findViewById(R.id.tvReportSeverity);
        TextView tvConfidence = sheet.findViewById(R.id.tvReportConfidence);
        ProgressBar pbConfidence = sheet.findViewById(R.id.pbReportConfidence);
        TextView tvChemicalRemedy = sheet.findViewById(R.id.tvReportChemicalRemedy);
        TextView tvCulturalCare = sheet.findViewById(R.id.tvReportCulturalCare);

        boolean isHindi = getContext() != null && "hi".equalsIgnoreCase(com.agrilink.app.LocaleHelper.getPersistedLanguage(getContext()));

        if (latestBitmap != null && ivLeaf != null) {
            ivLeaf.setImageBitmap(latestBitmap);
        }

        int randId = 1000 + (int) (Math.random() * 9000);
        if (tvId != null) {
            tvId.setText(getString(R.string.report_id_prefix) + randId + " • " + getString(R.string.verified_by_ai));
        }

        if (tvCrop != null) {
            tvCrop.setText(isHindi ? "फसल: " + getCropHindi(result.getPlantName()) : "Crop: " + result.getPlantName());
        }

        if (tvDisease != null) {
            tvDisease.setText(isHindi ? getDiseaseHindi(result.getDiseaseName(), result.isHealthy()) :
                    (result.isHealthy() ? "Healthy Crop (No Disease)" : result.getDiseaseName()));
        }

        if (tvStatus != null) {
            if (result.isHealthy()) {
                tvStatus.setText(getString(R.string.status_healthy_badge));
                tvStatus.setBackgroundResource(R.drawable.bg_badge_verified);
            } else {
                tvStatus.setText(getString(R.string.status_infected_badge));
            }
        }

        if (tvSeverity != null) {
            if (result.isHealthy()) {
                tvSeverity.setText(isHindi ? "स्थिति: सामान्य • कोई रोग नहीं मिला" : "Status: Normal • No Pathogen Detected");
                tvSeverity.setTextColor(getResources().getColor(R.color.primary_green));
            } else {
                tvSeverity.setText(isHindi ? "गंभीरता: मध्यम संक्रमण • तुरंत उपचार आवश्यक" : "Severity: Moderate Infection • Action Required");
                tvSeverity.setTextColor(getResources().getColor(R.color.harvest_orange));
            }
        }

        if (tvConfidence != null) {
            tvConfidence.setText(isHindi ? "AI सटीकता: " + result.getConfidencePercentage() : "AI Confidence: " + result.getConfidencePercentage());
        }

        if (pbConfidence != null) {
            pbConfidence.setProgress((int) (result.getConfidence() * 100));
        }

        if (tvChemicalRemedy != null) {
            tvChemicalRemedy.setText(isHindi ? getRemedyHindi(result.getDiseaseName(), result.getRemedyAdvice(), result.isHealthy()) : result.getRemedyAdvice());
        }

        if (tvCulturalCare != null) {
            if (result.isHealthy()) {
                tvCulturalCare.setText(isHindi ?
                        "संतुलित खाद व सूक्ष्म पोषक तत्व दें। अत्यधिक जलभराव से बचें और हर 7 दिन में पत्तियों के निचले हिस्से की जांच करें।" :
                        "Maintain current fertilization, avoid over-irrigation, and inspect undersides of foliage every 7 days.");
            } else {
                tvCulturalCare.setText(isHindi ?
                        "संक्रमित पत्तियों को तुरंत काटकर खेत से दूर नष्ट करें या मिट्टी में दबा दें। सिंचाई केवल जड़ों में ड्रिप से दें।" :
                        "Prune severely damaged foliage immediately and bury away from field. Disinfect pruning shears in 10% bleach.");
            }
        }

        // Call Kisan Helpline
        View btnCall = sheet.findViewById(R.id.btnCallHelpline);
        if (btnCall != null) {
            btnCall.setOnClickListener(v -> {
                try {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:18001801551"));
                    startActivity(callIntent);
                } catch (Exception e) {
                    Toast.makeText(getContext(), getString(R.string.dialer_unavailable_toast), Toast.LENGTH_SHORT).show();
                }
            });
        }

        // Ask AgriBot
        View btnAgriBot = sheet.findViewById(R.id.btnReportAgriBot);
        if (btnAgriBot != null) {
            btnAgriBot.setOnClickListener(v -> showAIChatbotSolutionAndTechDetails());
        }

        // Govt Schemes
        View btnSchemes = sheet.findViewById(R.id.btnReportSchemes);
        if (btnSchemes != null) {
            btnSchemes.setOnClickListener(v -> showAgriBotSchemeDialog());
        }

        // Done button
        View btnDone = sheet.findViewById(R.id.btnReportDone);
        if (btnDone != null) {
            btnDone.setOnClickListener(v -> dialog.dismiss());
        }

        dialog.show();
    }

    private void showAgriBotSchemeDialog() {
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
                showSchemeDetailsModal(0);
            });
        }

        // Card 2: PM Fasal Bima Yojana
        View cardFasalBima = sheet.findViewById(R.id.cardSchemeFasalBima);
        if (cardFasalBima != null) {
            cardFasalBima.setOnClickListener(v -> {
                dialog.dismiss();
                showSchemeDetailsModal(1);
            });
        }

        // Card 3: Kisan Credit Card (KCC)
        View cardKcc = sheet.findViewById(R.id.cardSchemeKcc);
        if (cardKcc != null) {
            cardKcc.setOnClickListener(v -> {
                dialog.dismiss();
                showSchemeDetailsModal(2);
            });
        }

        // Card 4: Equipment & Solar Pump Subsidies
        View cardSubsidies = sheet.findViewById(R.id.cardSchemeSubsidies);
        if (cardSubsidies != null) {
            cardSubsidies.setOnClickListener(v -> {
                dialog.dismiss();
                showSchemeDetailsModal(3);
            });
        }

        dialog.show();
    }

    private void showSchemeDetailsModal(int schemeIndex) {
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
            case 0: // PM-Kisan Samman Nidhi
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

            case 1: // PM Fasal Bima Yojana
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

            case 2: // Kisan Credit Card
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

            case 3: // Equipment & Solar Pump Subsidies
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
            showAgriBotSchemeDialog(); // returns smoothly to main schemes modal
        };
        if (btnClose != null) btnClose.setOnClickListener(dismissListener);
        if (btnDismiss != null) btnDismiss.setOnClickListener(dismissListener);

        final String finalPortalUrl = portalUrl;
        if (btnLearnMore != null) {
            btnLearnMore.setOnClickListener(v -> {
                try {
                    Intent callIntent = new Intent(Intent.ACTION_DIAL);
                    callIntent.setData(Uri.parse("tel:18001801551")); // Kisan Call Center Toll-Free
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

    private void showAIChatbotSolutionAndTechDetails() {
        String msg = "🤖 KRISHISETU AI Agronomist — Disease Remedy & APIs\n\n" +
                "🌿 DIAGNOSIS & REMEDY ADVISORY:\n" +
                "1. Spray Copper Oxychloride 50% WP (3g/L water) or Mancozeb 75% WP (2.5g/L water).\n" +
                "2. Apply Trichoderma viride bio-fungicide to soil for root immunity.\n" +
                "3. Ensure drip irrigation to avoid leaf wetness and fungal spore multiplication.\n\n" +
                "⚡ REQUIRED APIs & TECHNICAL ARCHITECTURE:\n" +
                "1. Computer Vision Scan API:\n" +
                "   • Endpoint: POST /api/v1/disease-check/scan\n" +
                "   • Payload: Multipart UploadFile (leaf image blob) + crop_hint.\n" +
                "   • Model: MobileNetV3 / EfficientNetV2 trained on PlantVillage (38 disease classes, 94%+ accuracy).\n\n" +
                "2. Multimodal AI Chatbot API:\n" +
                "   • Endpoint: POST /api/v1/chat\n" +
                "   • Engine: Google Gemini 1.5 Flash Vision / OpenAPI LLM for natural language multi-turn crop advice.\n\n" +
                "3. Smart Escrow Payment API:\n" +
                "   • Endpoint: POST /api/v1/wallet/escrow/accept\n" +
                "   • Purpose: Secure online purchase of verified bio-pesticides & hiring expert agronomists.";

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(getString(R.string.ai_solution_breakdown_title))
                .setMessage(msg)
                .setPositiveButton(getString(R.string.got_it), (d, w) -> d.dismiss())
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if (classifier != null) {
            classifier.close();
            classifier = null;
        }
        executor.shutdown();
    }

    private String getCropHindi(String crop) {
        if (crop == null) return "फसल";
        String lower = crop.toLowerCase();
        if (lower.contains("tomato")) return "टमाटर (Tomato)";
        if (lower.contains("potato")) return "आलू (Potato)";
        if (lower.contains("corn") || lower.contains("maize")) return "मक्का (Corn)";
        if (lower.contains("grape")) return "अंगूर (Grape)";
        if (lower.contains("apple")) return "सेब (Apple)";
        if (lower.contains("pepper")) return "शिमला मिर्च (Pepper)";
        if (lower.contains("strawberry")) return "स्ट्रॉबेरी (Strawberry)";
        if (lower.contains("soybean")) return "सोयाबीन (Soybean)";
        if (lower.contains("rice") || lower.contains("paddy")) return "धान / चावल (Paddy)";
        if (lower.contains("wheat")) return "गेहूं (Wheat)";
        return crop;
    }

    private String getDiseaseHindi(String disease, boolean isHealthy) {
        if (isHealthy) return "फसल पूरी तरह स्वस्थ है (Healthy)";
        if (disease == null) return "अज्ञात रोग";
        String lower = disease.toLowerCase();
        if (lower.contains("early blight")) return "अगेती झुलसा रोग (Early Blight)";
        if (lower.contains("late blight")) return "पछेती झुलसा रोग (Late Blight)";
        if (lower.contains("bacterial spot")) return "जीवाणु धब्बा रोग (Bacterial Spot)";
        if (lower.contains("powdery mildew")) return "चूर्णिल आसिता / सफेद फफूंद (Powdery Mildew)";
        if (lower.contains("leaf mold")) return "पत्ती फफूंद रोग (Leaf Mold)";
        if (lower.contains("septoria")) return "सेप्टोरिया पत्ती धब्बा रोग (Septoria)";
        if (lower.contains("spider mite")) return "लाल मकड़ी कीट का प्रकोप (Spider Mites)";
        if (lower.contains("target spot")) return "टारगेट स्पॉट रोग (Target Spot)";
        if (lower.contains("curl")) return "पत्ती मरोड़ विषाणु (Yellow Leaf Curl)";
        if (lower.contains("mosaic")) return "मोज़ेक वायरस रोग (Mosaic Virus)";
        if (lower.contains("rust")) return "गेरुआ / रतुआ रोग (Rust)";
        if (lower.contains("scab")) return "स्कैब पपड़ी रोग (Scab)";
        if (lower.contains("black rot")) return "काली सड़न रोग (Black Rot)";
        return disease;
    }

    private String getRemedyHindi(String disease, String defaultRemedy, boolean isHealthy) {
        if (isHealthy) {
            return "🌿 फसल बिल्कुल स्वस्थ है। किसी रासायनिक दवा की आवश्यकता नहीं है। नियमित संतुलित सिंचाई और साप्ताहिक खेत निरीक्षण बनाए रखें।";
        }
        if (disease == null) return defaultRemedy;
        String lower = disease.toLowerCase();
        if (lower.contains("early blight")) {
            return "🧪 मैनकोजेब 75% WP @ 2.5 ग्राम/लीटर या कॉपर ऑक्सीक्लोराइड 50% WP @ 3 ग्राम/लीटर का घोल बनाकर पत्तियों पर समान छिड़काव करें। निचली सूखी पत्तियों को काटकर नष्ट करें।";
        } else if (lower.contains("late blight")) {
            return "🧪 मेटालेक्सिल 8% + मैनकोजेब 64% WP (रिडोमिल गोल्ड) @ 2 ग्राम/लीटर या साइमोक्सानिल @ 1.5 ग्राम/लीटर का तुरंत छिड़काव करें। खेत में जलनिकासी सुनिश्चित करें।";
        } else if (lower.contains("bacterial spot")) {
            return "🧪 स्ट्रेप्टोसाइक्लिन (1 ग्राम प्रति 10 लीटर पानी) + कॉपर हाइड्रोक्साइड 53.8% DF @ 2 ग्राम/लीटर का पर्णीय छिड़काव करें। गीली पत्तियों को छूने से बचें।";
        } else if (lower.contains("powdery mildew")) {
            return "🧪 हेक्साकोनाजोल 5% EC @ 1 मिली/लीटर या घुलनशील सल्फर 80% WP @ 3 ग्राम/लीटर का छिड़काव करें। फसल में धूप और हवा का आवागमन बनाए रखें।";
        } else if (lower.contains("spider mite")) {
            return "🧪 एबामेक्टिन 1.9% EC @ 0.5 मिली/लीटर या प्रोपारगाइट 57% EC @ 2 मिली/लीटर पानी में मिलाकर छिड़कें। खेत में नमी बनाए रखें।";
        } else if (lower.contains("curl") || lower.contains("mosaic")) {
            return "🧪 कीट नियंत्रक: सफेद मक्खी/माहू के नियंत्रण हेतु इमिडाक्लोप्रिड 17.8% SL @ 0.5 मिली/लीटर का छिड़काव करें और खेत में पीले चिपचिपे कार्ड लगाएं।";
        } else if (lower.contains("rust")) {
            return "🧪 प्रोपिकोनाजोल 25% EC (टिल्ट) @ 1 मिली/लीटर या मैनकोजेब 75% WP @ 2.5 ग्राम/लीटर का छिड़काव करें।";
        }
        return defaultRemedy;
    }
}
