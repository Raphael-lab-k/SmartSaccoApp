package com.example.smartsaccoapp.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.smartsaccoapp.data.SaccoRepository;
import com.example.smartsaccoapp.databinding.FragmentFinancialReportsBinding;

import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class FinancialReportsFragment extends Fragment {

    private FragmentFinancialReportsBinding binding;
    @Inject SaccoRepository repository;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentFinancialReportsBinding.inflate(inflater, container, false);

        loadReports();

        return binding.getRoot();
    }

    private void loadReports() {
        executorService.execute(() -> {
            double deposits = repository.getTotalDeposits();
            double withdrawals = repository.getTotalWithdrawals();
            double net = deposits - withdrawals;

            // Ratio calculation
            int ratio = 0;
            if (deposits + withdrawals > 0) {
                ratio = (int) ((deposits / (deposits + withdrawals)) * 100);
            }

            if (getActivity() != null) {
                int finalRatio = ratio;
                getActivity().runOnUiThread(() -> {
                    binding.tvReportTotalDeposits.setText(String.format(Locale.getDefault(), "UGX %,.0f", deposits));
                    binding.tvReportTotalWithdrawals.setText(String.format(Locale.getDefault(), "UGX %,.0f", withdrawals));
                    binding.tvReportNetLiquidity.setText(String.format(Locale.getDefault(), "UGX %,.0f", net));
                    
                    binding.pbLiquidityRatio.setProgress(finalRatio);
                    binding.tvRatioText.setText(finalRatio + "% Deposits");

                    if (net < 0) {
                        binding.tvReportNetLiquidity.setTextColor(getResources().getColor(android.R.color.holo_red_dark, null));
                    } else {
                        binding.tvReportNetLiquidity.setTextColor(getResources().getColor(android.R.color.holo_green_dark, null));
                    }
                });
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
        executorService.shutdown();
    }
}