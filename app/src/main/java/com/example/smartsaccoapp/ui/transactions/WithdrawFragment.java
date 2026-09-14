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
import com.example.smartsaccoapp.databinding.FragmentWithdrawBinding;
import com.example.smartsaccoapp.util.NotificationHelper;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class WithdrawFragment extends Fragment {

    private FragmentWithdrawBinding binding;
    private TransactionViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentWithdrawBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(TransactionViewModel.class);

        setupPaymentMethods();

        binding.btnConfirmWithdraw.setOnClickListener(v -> {
            String amountStr = binding.editAmountWithdraw.getText().toString();
            String method = binding.dropdownPaymentMethodWithdraw.getText().toString();

            if (TextUtils.isEmpty(method)) {
                binding.layoutPaymentMethodWithdraw.setError("Select withdrawal method");
                return;
            }
            binding.layoutPaymentMethodWithdraw.setError(null);

            if (TextUtils.isEmpty(amountStr)) {
                binding.layoutAmountWithdraw.setError("Enter amount");
                return;
            }
            binding.layoutAmountWithdraw.setError(null);

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
        binding.dropdownPaymentMethodWithdraw.setAdapter(adapter);
    }

    private void simulatePaymentGateway(double amount, String method) {
        ProgressDialog progressDialog = new ProgressDialog(requireContext());
        progressDialog.setTitle("Processing Withdrawal...");
        progressDialog.setMessage("Authenticating with " + method + "...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            viewModel.withdraw(amount);
            progressDialog.dismiss();
            
            NotificationHelper.showNotification(getContext(), "Withdrawal Successful", 
                    "You have withdrawn UGX " + amount + " to your " + method + " account.");
            
            Toast.makeText(getContext(), "Withdrawal Successful", Toast.LENGTH_SHORT).show();
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