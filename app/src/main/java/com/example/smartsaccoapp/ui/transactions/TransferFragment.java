package com.example.smartsaccoapp.ui.transactions;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.smartsaccoapp.R;
import com.example.smartsaccoapp.data.AuthManager;
import com.example.smartsaccoapp.data.SaccoRepository;
import com.example.smartsaccoapp.databinding.FragmentTransferBinding;
import com.example.smartsaccoapp.util.BiometricHelper;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class TransferFragment extends Fragment {

    private FragmentTransferBinding binding;
    @Inject SaccoRepository repository;
    @Inject AuthManager authManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentTransferBinding.inflate(inflater, container, false);

        binding.btnConfirmTransfer.setOnClickListener(v -> {
            String recipient = binding.etRecipientEmail.getText().toString().trim();
            String amountStr = binding.etAmountTransfer.getText().toString().trim();

            if (TextUtils.isEmpty(recipient)) {
                binding.tilRecipient.setError("Required");
                return;
            }
            if (TextUtils.isEmpty(amountStr)) {
                binding.tilAmountTransfer.setError("Required");
                return;
            }

            double amount = Double.parseDouble(amountStr);
            
            if (BiometricHelper.isBiometricAvailable(requireContext())) {
                BiometricHelper.showBiometricPrompt(requireActivity(), "Transfer UGX " + amount, "Confirm transfer to " + recipient, new BiometricHelper.BiometricCallback() {
                    @Override
                    public void onAuthenticationSuccess() {
                        handleTransfer(recipient, amount);
                    }

                    @Override
                    public void onAuthenticationError(String error) {
                        Toast.makeText(getContext(), "Authentication failed: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                handleTransfer(recipient, amount);
            }
        });

        return binding.getRoot();
    }

    private void handleTransfer(String recipient, double amount) {
        repository.transferFunds(authManager.getUserEmail(), recipient, amount, new SaccoRepository.TransferCallback() {
            @Override
            public void onSuccess() {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), String.format(getString(R.string.msg_transfer_success), amount, recipient), Toast.LENGTH_LONG).show();
                        Navigation.findNavController(requireView()).navigateUp();
                    });
                }
            }

            @Override
            public void onError(String message) {
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        String error = message;
                        if ("RECIPIENT_NOT_FOUND".equals(message)) error = getString(R.string.error_recipient_not_found);
                        else if ("INSUFFICIENT_BALANCE".equals(message)) error = getString(R.string.error_insufficient_balance);
                        else if ("SELF_TRANSFER".equals(message)) error = getString(R.string.error_self_transfer);
                        
                        Toast.makeText(getContext(), error, Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}