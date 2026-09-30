package com.agrilink.app.fragments;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
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

        if (view.findViewById(R.id.btnScanNow) != null) view.findViewById(R.id.btnScanNow).setOnClickListener(scanListener);
        if (view.findViewById(R.id.btnUploadImage) != null) view.findViewById(R.id.btnUploadImage).setOnClickListener(scanListener);
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
                .setTitle("Select Leaf Image Source")
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
        Toast.makeText(getContext(), "🔬 AI Analyzing Crop Leaf with Local TFLite...", Toast.LENGTH_SHORT).show();

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
        String msg = "🌿 Crop Identified: " + result.getPlantName() + "\n\n" +
                "🦠 Disease Detected: " + (result.isHealthy() ? "None (Crop is Healthy)" : result.getDiseaseName()) + "\n" +
                "🎯 Local TFLite Confidence: " + result.getConfidencePercentage() + "\n\n" +
                "💡 Agronomic Recommendation:\n" +
                result.getRemedyAdvice();

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("🔬 AI Leaf Disease Analysis Result")
                .setMessage(msg)
                .setPositiveButton("OK & Save", (dialog, which) -> dialog.dismiss())
                .setNeutralButton("🤖 AI Chatbot Solution & API Info", (dialog, which) -> showAIChatbotSolutionAndTechDetails())
                .setNegativeButton("Schemes & Subsidies", (dialog, which) -> showAgriBotSchemeDialog())
                .show();
    }

    private void showAgriBotSchemeDialog() {
        String[] schemeTopics = {
            "🌾 PM-Kisan Samman Nidhi (₹6,000/yr Direct Benefit)",
            "🛡️ PM Fasal Bima Yojana (Crop Insurance & Compensation)",
            "💳 Kisan Credit Card (KCC 4% Concessional Loan)",
            "🚜 Subsidies on Agricultural Equipment & Solar Pumps"
        };

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("🤖 AgriBot AI — Schemes & Subsidies")
                .setItems(schemeTopics, (dialog, which) -> {
                    String selected = schemeTopics[which];
                    showSchemeDetailsDialog(selected);
                })
                .setNegativeButton("Close", (d, w) -> d.dismiss())
                .show();
    }

    private void showSchemeDetailsDialog(String schemeTitle) {
        String details = "";
        if (schemeTitle.contains("PM-Kisan")) {
            details = "🌾 PM-Kisan Samman Nidhi Scheme:\n\n" +
                    "• Financial Benefit: ₹6,000 per year transferred directly into farmer bank accounts in 3 equal installments of ₹2,000.\n" +
                    "• Eligibility: Small & Marginal landholder farmers.\n" +
                    "• How to Apply: Visit pmkisan.gov.in or nearest CSC center with Aadhaar Card & Land Records (Khatauni).";
        } else if (schemeTitle.contains("Fasal Bima")) {
            details = "🛡️ Pradhan Mantri Fasal Bima Yojana (PMFBY):\n\n" +
                    "• Low Premium Rates: 2% for Kharif crops, 1.5% for Rabi crops, 5% for commercial/horticultural crops.\n" +
                    "• Coverage: Comprehensive crop loss compensation due to natural calamities, drought, flood, or pests.\n" +
                    "• Claim Support: Register loss within 72 hours on PMFBY App or helpline 1800-180-1551.";
        } else if (schemeTitle.contains("Credit Card")) {
            details = "💳 Kisan Credit Card (KCC):\n\n" +
                    "• Credit Limit: Up to ₹3 Lakhs collateral-free credit at 4% effective interest rate (with 3% prompt repayment subvention).\n" +
                    "• Uses: Purchase of seeds, fertilizers, pesticides, and farm machinery operational expenses.";
        } else {
            details = "🚜 Agri Machinery & Solar Pump Subsidies:\n\n" +
                    "• PM-KUSUM Solar Scheme: 60% subsidy for installing solar agriculture pumps.\n" +
                    "• Sub-Mission on Ag Machinery (SMAM): 40% to 80% subsidy for buying tractors, rotavators, and harvesters.";
        }

        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle(schemeTitle)
                .setMessage(details)
                .setPositiveButton("Got It", (dialog, which) -> dialog.dismiss())
                .show();
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
                .setTitle("🤖 KRISHISETU AI Solution & Technical Breakdown")
                .setMessage(msg)
                .setPositiveButton("Understood", (d, w) -> d.dismiss())
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
}
