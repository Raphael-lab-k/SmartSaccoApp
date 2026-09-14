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
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;

import android.app.ProgressDialog;
import android.os.Handler;
import android.os.Looper;
import android.widget.ArrayAdapter;

import com.example.smartsaccoapp.R;
import com.example.smartsaccoapp.databinding.FragmentDepositBinding;
import com.example.smartsaccoapp.util.NotificationHelper;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DepositFragment extends Fragment {

    private FragmentDepositBinding binding;
    private TransactionViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentDepositBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        setupPaymentMethods();

        binding.btnConfirmDeposit.setOnClickListener(v -> {
            String amountStr = binding.editAmount.getText().toString();
            String method = binding.dropdownPaymentMethod.getText().toString();

            if (TextUtils.isEmpty(method)) {
                binding.layoutPaymentMethod.setError("Select payment method");
                return;
            }
            binding.layoutPaymentMethod.setError(null);

            if (TextUtils.isEmpty(amountStr)) {
                binding.layoutAmount.setError("Enter amount");
                return;
            }
            binding.layoutAmount.setError(null);

            simulatePaymentGateway(Double.parseDouble(amountStr), method);
        });

        return binding.getRoot();
    }

    private void setupPaymentMethods() {
        String[] methods = {
                getString(R.string.method_mtn),
                getString(R.string.method_airtel),
                getString(R.string.method_card),
                getString(R.string.method_bank)
        };
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, methods);
        binding.dropdownPaymentMethod.setAdapter(adapter);
    }

    private void simulatePaymentGateway(double amount, String method) {
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setTitle(getString(R.string.title_processing_payment));
        progressDialog.setMessage("Connecting to " + method + "...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        // Simulate network delay for a "seamless" feel
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            viewModel.deposit(amount);
            progressDialog.dismiss();
            
            NotificationHelper.showNotification(getContext(), "Deposit Successful", 
                    "You have deposited UGX " + amount + " via " + method);
            
            Toast.makeText(getContext(), "Deposit Successful", Toast.LENGTH_SHORT).show();
            if (isAdded()) {
                Navigation.findNavController(requireView()).navigateUp();
            }
        }, 2000);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}