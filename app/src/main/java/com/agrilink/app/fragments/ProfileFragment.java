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
                if (!android.text.TextUtils.isEmpty(userName)) {
                    tvUserName.setText(userName);
                } else if (!android.text.TextUtils.isEmpty(userPhone)) {
                    tvUserName.setText("User (" + userPhone + ")");
                } else {
                    tvUserName.setText("AgriLink Member");
                }
            }

            String userRole = prefs.getString("user_role", "Farmer");
            android.widget.TextView tvUserRole = view.findViewById(R.id.tvUserRole);
            if (tvUserRole != null) {
                tvUserRole.setText(userRole + " • Verified ✓");
            }
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
}
