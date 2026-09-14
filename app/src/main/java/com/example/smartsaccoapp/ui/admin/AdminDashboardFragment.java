package com.example.smartsaccoapp.ui.admin;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.smartsaccoapp.R;
import com.example.smartsaccoapp.data.SaccoRepository;
import com.example.smartsaccoapp.data.db.Member;
import com.example.smartsaccoapp.data.db.Loan;
import com.example.smartsaccoapp.databinding.FragmentAdminDashboardBinding;
import com.example.smartsaccoapp.ui.TransactionAdapter;

import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AdminDashboardFragment extends Fragment {

    private FragmentAdminDashboardBinding binding;
    @Inject SaccoRepository repository;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();
    private TransactionAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAdminDashboardBinding.inflate(inflater, container, false);

        adapter = new TransactionAdapter();
        binding.rvRecentTransactionsAdmin.setAdapter(adapter);

        binding.btnManageMembers.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.nav_member_mgmt));
        binding.btnManageLoans.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.nav_loan_mgmt));
        binding.btnFinancialReports.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.nav_financial_reports));

        setupRefreshLayout();
        loadSummaries();
        loadRecentTransactions();

        return binding.getRoot();
    }

    private void setupRefreshLayout() {
        binding.swipeRefreshAdmin.setOnRefreshListener(() -> {
            loadSummaries();
            // Recent transactions are already observed via LiveData, but loadSummaries uses ExecutorService
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (binding != null) {
                    binding.swipeRefreshAdmin.setRefreshing(false);
                }
            }, 1500);
        });
        binding.swipeRefreshAdmin.setColorSchemeResources(R.color.primary_blue);
    }

    private void loadRecentTransactions() {
        repository.getLastFiveTransactions().observe(getViewLifecycleOwner(), transactions -> {
            if (transactions != null) {
                adapter.submitList(transactions);
            }
        });
    }

    private void loadSummaries() {
        executorService.execute(() -> {
            int memberCount = repository.getMemberCount();
            int pendingKyc = repository.getPendingKycCount();
            int loanCount = repository.getLoanCount();
            
            // Financials
            double deposits = repository.getTotalDeposits();
            double withdrawals = repository.getTotalWithdrawals();
            
            // Risk Exposure calculation
            List<Loan> allLoans = repository.getAllLoansSync();
            double riskExposure = 0;
            if (allLoans != null) {
                for (Loan loan : allLoans) {
                    if ("APPROVED".equals(loan.status)) {
                        double multiplier = 1.0;
                        if ("HIGH".equals(loan.riskLevel)) multiplier = 1.5;
                        else if ("LOW".equals(loan.riskLevel)) multiplier = 0.5;
                        riskExposure += (loan.amount * multiplier);
                    }
                }
            }

            // Trust Insights
            List<Member> members = repository.getMembersSync(); 
            int totalScore = 0;
            int high = 0, med = 0, low = 0;
            for (Member m : members) {
                totalScore += m.trustScore;
                if (m.trustScore >= 700) high++;
                else if (m.trustScore >= 400) med++;
                else low++;
            }
            int avgScore = !members.isEmpty() ? (totalScore / members.size()) : 0;

            if (getActivity() != null) {
                String distText = String.format(Locale.getDefault(), "High: %d | Med: %d | Low: %d", high, med, low);
                double finalRiskExposure = riskExposure;
                getActivity().runOnUiThread(() -> {
                    binding.tvTotalMembers.setText(String.valueOf(memberCount));
                    binding.tvPendingKyc.setText(String.valueOf(pendingKyc));
                    binding.tvTotalLoans.setText(String.valueOf(loanCount));
                    
                    binding.tvAvgTrustScore.setText(String.valueOf(avgScore));
                    binding.tvTrustDistribution.setText(distText);
                    
                    binding.tvAdminTotalDeposits.setText(String.format(Locale.getDefault(), "UGX %,.0f", deposits));
                    binding.tvAdminTotalWithdrawals.setText(String.format(Locale.getDefault(), "UGX %,.0f", withdrawals));
                    binding.tvRiskExposure.setText(String.format(Locale.getDefault(), "UGX %,.0f", finalRiskExposure));
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