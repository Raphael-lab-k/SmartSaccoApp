package com.example.smartsaccoapp.ui.loans;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.smartsaccoapp.R;
import com.example.smartsaccoapp.data.AuthManager;
import com.example.smartsaccoapp.data.SaccoRepository;
import com.example.smartsaccoapp.data.db.Loan;
import com.example.smartsaccoapp.databinding.FragmentLoanApplicationBinding;

import android.text.Editable;
import android.text.TextWatcher;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoanApplicationFragment extends Fragment {

    private FragmentLoanApplicationBinding binding;
    @Inject SaccoRepository repository;
    @Inject AuthManager authManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoanApplicationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        String[] loanTypes = {"Personal Loan", "Emergency Loan", "Development Loan", "Education Loan"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, loanTypes);
        binding.dropdownLoanType.setAdapter(adapter);

        setupRiskEngine();

        binding.buttonSubmitLoan.setOnClickListener(v -> {
            String type = binding.dropdownLoanType.getText().toString();
            String amountStr = binding.editLoanAmount.getText().toString();
            String durationStr = binding.editLoanDuration.getText().toString();
            String purpose = binding.editLoanPurpose.getText().toString();

            if (amountStr.isEmpty()) {
                binding.editLoanAmount.setError("Required");
                return;
            }

            double amount = Double.parseDouble(amountStr);
            long dueDate = System.currentTimeMillis() + (Long.parseLong(durationStr.isEmpty() ? "1" : durationStr) * 30L * 24L * 60L * 60L * 1000L);

            String riskLevel = repository.calculateRiskLevel(authManager.getUserEmail(), amount);
            Loan loan = new Loan(authManager.getUserEmail(), type, amount, 0.1, "PENDING", purpose, dueDate);
            loan.riskLevel = riskLevel;
            
            repository.insertLoan(loan, new SaccoRepository.LoanCallback() {
                @Override
                public void onSuccess(Loan insertedLoan) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            if ("APPROVED".equals(insertedLoan.status)) {
                                Toast.makeText(requireContext(), "Instantly Approved! Funds added to your account.", Toast.LENGTH_LONG).show();
                            } else {
                                Toast.makeText(requireContext(), "Loan Application Submitted for Review", Toast.LENGTH_LONG).show();
                            }
                            Navigation.findNavController(view).navigateUp();
                        });
                    }
                }

                @Override
                public void onError(String message) {
                    if (getActivity() != null) {
                        getActivity().runOnUiThread(() -> {
                            Toast.makeText(requireContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private double safeLimit = 0;

    private void setupRiskEngine() {
        new Thread(() -> {
            safeLimit = repository.getSafeLoanLimit(authManager.getUserEmail());
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    binding.tvMaxLimit.setText(String.format(Locale.getDefault(), "Safe Limit: UGX %,.0f", safeLimit));
                });
            }
        }).start();

        binding.editLoanAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updateRiskRealTime(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void updateRiskRealTime(String amountStr) {
        if (amountStr.isEmpty()) {
            binding.tvApplicationRisk.setText("Risk Level: N/A");
            binding.tvApplicationRisk.setTextColor(getResources().getColor(R.color.text_light, null));
            return;
        }

        try {
            double amount = Double.parseDouble(amountStr);
            new Thread(() -> {
                String risk = repository.calculateRiskLevel(authManager.getUserEmail(), amount);
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        binding.tvApplicationRisk.setText("Risk Level: " + risk);
                        
                        if (amount > safeLimit) {
                            binding.tvApplicationRisk.setText("Risk Level: EXCEEDS LIMIT");
                            binding.tvApplicationRisk.setTextColor(getResources().getColor(R.color.accent_red, null));
                            binding.buttonSubmitLoan.setEnabled(false);
                            binding.buttonSubmitLoan.setAlpha(0.5f);
                        } else {
                            binding.buttonSubmitLoan.setEnabled(true);
                            binding.buttonSubmitLoan.setAlpha(1.0f);
                            if ("HIGH".equals(risk)) {
                                binding.tvApplicationRisk.setTextColor(getResources().getColor(R.color.accent_red, null));
                            } else if ("MEDIUM".equals(risk)) {
                                binding.tvApplicationRisk.setTextColor(getResources().getColor(R.color.accent_orange, null));
                            } else {
                                binding.tvApplicationRisk.setTextColor(getResources().getColor(R.color.secondary_green, null));
                            }
                        }
                    });
                }
            }).start();
        } catch (Exception e) {
            // Ignore
        }
    }
}
