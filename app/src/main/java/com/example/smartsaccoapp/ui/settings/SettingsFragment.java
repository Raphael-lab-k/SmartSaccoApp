package com.example.smartsaccoapp.ui.settings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.content.Intent;
import com.example.smartsaccoapp.data.AuthManager;
import com.example.smartsaccoapp.databinding.FragmentSettingsBinding;
import com.example.smartsaccoapp.ui.auth.LoginActivity;

public class SettingsFragment extends Fragment {

    private FragmentSettingsBinding binding;
    private AuthManager authManager;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        authManager = new AuthManager(requireContext());
        View root = binding.getRoot();

        setupProfile();
        
        binding.btnLogoutSettings.setOnClickListener(v -> {
            authManager.logout();
            startActivity(new Intent(requireContext(), LoginActivity.class));
            requireActivity().finish();
        });

        return root;
    }

    private void setupProfile() {
        binding.tvProfileName.setText(authManager.getUserName());
        binding.tvProfileEmail.setText(authManager.getUserEmail());
        binding.tvProfileRole.setText("Role: " + authManager.getUserRole());
        binding.tvProfileKyc.setText("KYC Status: " + authManager.getKycStatus());
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}