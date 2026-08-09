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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_disease, container, false);

        View.OnClickListener scanListener = v -> {
            Toast.makeText(getContext(), "🔍 AI Scanning Leaf Photo: Result -> Tomato Early Blight (92% Confidence)", Toast.LENGTH_LONG).show();
        };

        if (view.findViewById(R.id.btnScanNow) != null) view.findViewById(R.id.btnScanNow).setOnClickListener(scanListener);
        if (view.findViewById(R.id.btnUploadImage) != null) view.findViewById(R.id.btnUploadImage).setOnClickListener(scanListener);

        if (view.findViewById(R.id.cardAgriBot) != null) {
            view.findViewById(R.id.cardAgriBot).setOnClickListener(v -> {
                startActivity(new Intent(getContext(), ChatDealActivity.class));
            });
        }

        return view;
    }
}
