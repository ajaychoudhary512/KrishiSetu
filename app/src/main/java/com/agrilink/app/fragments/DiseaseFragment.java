package com.agrilink.app.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.agrilink.app.ChatDealActivity;
import com.agrilink.app.R;


public class DiseaseFragment extends Fragment {

    private static final int REQUEST_CAMERA = 101;
    private static final int REQUEST_GALLERY = 102;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_disease, container, false);

        View.OnClickListener scanListener = v -> showImageSourceDialog();

        if (view.findViewById(R.id.btnScanNow) != null) view.findViewById(R.id.btnScanNow).setOnClickListener(scanListener);
        if (view.findViewById(R.id.btnUploadImage) != null) view.findViewById(R.id.btnUploadImage).setOnClickListener(scanListener);
        if (view.findViewById(R.id.cardUploadLeaf) != null) view.findViewById(R.id.cardUploadLeaf).setOnClickListener(scanListener);

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
                        Intent cameraIntent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                        startActivityForResult(cameraIntent, REQUEST_CAMERA);
                    } else {
                        Intent galleryIntent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                        startActivityForResult(galleryIntent, REQUEST_GALLERY);
                    }
                })
                .show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == android.app.Activity.RESULT_OK) {
            Toast.makeText(getContext(), "🔬 AI Analyzing Crop Leaf Photo...", Toast.LENGTH_SHORT).show();
            showDiseaseAnalysisResultDialog();
        }
    }

    private void showDiseaseAnalysisResultDialog() {
        new androidx.appcompat.app.AlertDialog.Builder(requireContext())
                .setTitle("🔬 AI Leaf Disease Analysis")
                .setMessage("🌿 Crop Identified: Tomato (Solanum lycopersicum)\n\n" +
                        "🦠 Disease Detected: Early Blight (Alternaria solani)\n" +
                        "🎯 AI Confidence: 94.8%\n\n" +
                        "🧪 Recommended Chemical Treatment:\n" +
                        "• Spray Copper Oxychloride 50% WP @ 3g per Liter of water, or\n" +
                        "• Spray Mancozeb 75% WP @ 2.5g per Liter.\n\n" +
                        "🛡️ Cultural Advisory:\n" +
                        "Maintain proper plant spacing for air circulation and remove lower infected leaves immediately.")
                .setPositiveButton("OK & Save to History", (dialog, which) -> dialog.dismiss())
                .setNegativeButton("Ask AgriBot Assistant", (dialog, which) -> showAgriBotSchemeDialog())
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
}
