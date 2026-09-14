package com.example.smartsaccoapp.ui.dashboard;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import android.os.Looper;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartsaccoapp.data.AuthManager;
import com.example.smartsaccoapp.util.SmsBridgeHelper;
import com.example.smartsaccoapp.data.db.Account;
import com.example.smartsaccoapp.data.db.Loan;
import com.example.smartsaccoapp.data.db.Member;
import com.example.smartsaccoapp.data.db.SavingPot;
import com.example.smartsaccoapp.data.db.Transaction;
import com.example.smartsaccoapp.databinding.FragmentDashboardBinding;
import com.example.smartsaccoapp.ui.TransactionAdapter;
import com.example.smartsaccoapp.R;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class DashboardFragment extends Fragment {

    private FragmentDashboardBinding binding;
    @Inject AuthManager authManager;
    private boolean isBalanceVisible = true;
    private double currentBalance = 0;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        DashboardViewModel dashboardViewModel =
                new ViewModelProvider(this).get(DashboardViewModel.class);

        binding = FragmentDashboardBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        setupKycStatus();
        checkConnectivity();
        setupTrustScore(dashboardViewModel);
        setupBalanceToggle();
        setupRefreshLayout();

        dashboardViewModel.getAccount().observe(getViewLifecycleOwner(), account -> {
            if (account != null) {
                currentBalance = account.balance;
                updateBalanceDisplay();
                binding.textAccountNumber.setText("Acc: " + account.accountNumber);
            }
        });

        String userEmail = authManager.getUserEmail();
        if (userEmail != null) {
            dashboardViewModel.getMemberLoans(userEmail).observe(getViewLifecycleOwner(), loans -> {
                double totalDebt = 0;
                if (loans != null) {
                    for (Loan loan : loans) {
                        if (!"PAID".equals(loan.status) && !"REJECTED".equals(loan.status)) {
                            totalDebt += (loan.amount * (1 + loan.interestRate)) - loan.repaidAmount;
                        }
                    }
                }
                binding.textTotalLoans.setText(String.format(Locale.getDefault(), "UGX %,.0f", totalDebt));
            });

            dashboardViewModel.getMemberPots(userEmail).observe(getViewLifecycleOwner(), pots -> {
                double totalSavings = 0;
                if (pots != null) {
                    for (SavingPot pot : pots) {
                        totalSavings += pot.currentAmount;
                    }
                }
                binding.textTotalSavings.setText(String.format(Locale.getDefault(), "UGX %,.0f", totalSavings));
            });
        }

        RecyclerView recyclerView = binding.recyclerviewDashboard;
        TransactionAdapter adapter = new TransactionAdapter();
        recyclerView.setAdapter(adapter);
        dashboardViewModel.getLastFiveTransactions().observe(getViewLifecycleOwner(), transactions -> {
            adapter.submitList(transactions);
            if (transactions == null || transactions.isEmpty()) {
                binding.tvEmptyDashboard.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                binding.tvEmptyDashboard.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });

        binding.btnDeposit.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.nav_deposit));
        binding.btnWithdraw.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.nav_withdraw));
        binding.btnTransfer.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.nav_transfer));
        binding.btnLoans.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.nav_loans));
        binding.btnLookupMembers.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.nav_member_lookup));

        return root;
    }

    private void setupRefreshLayout() {
        binding.swipeRefreshDashboard.setOnRefreshListener(() -> {
            // Room LiveData updates automatically, so we just simulate a network delay
            // to show that the app is "syncing" with the server/local storage.
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (binding != null) {
                    binding.swipeRefreshDashboard.setRefreshing(false);
                    Toast.makeText(getContext(), "Dashboard Updated", Toast.LENGTH_SHORT).show();
                }
            }, 1000);
        });
        
        // Customize color
        binding.swipeRefreshDashboard.setColorSchemeResources(R.color.primary_blue);
    }

    private void setupBalanceToggle() {
        binding.btnToggleBalance.setOnClickListener(v -> {
            isBalanceVisible = !isBalanceVisible;
            updateBalanceDisplay();
            binding.btnToggleBalance.setImageResource(isBalanceVisible ? 
                    android.R.drawable.ic_menu_view : android.R.drawable.ic_partial_secure);
        });
    }

    private void updateBalanceDisplay() {
        if (isBalanceVisible) {
            binding.textDashboardBalance.setText(String.format(Locale.getDefault(), "UGX %,.0f", currentBalance));
        } else {
            binding.textDashboardBalance.setText("UGX ••••••••");
        }
    }

    private void setupTrustScore(DashboardViewModel viewModel) {
        String email = authManager.getUserEmail();
        if (email != null) {
            new Thread(() -> {
                Member member = viewModel.getMember(email);
                if (member != null) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        binding.textTrustScoreValue.setText(member.trustScore + " / 1000");
                    });
                }
            }).start();
        }
    }

    private void checkConnectivity() {
        if (SmsBridgeHelper.isOffline(requireContext())) {
            binding.cardKycStatus.setVisibility(View.VISIBLE);
            binding.cardKycStatus.setCardBackgroundColor(getResources().getColor(R.color.accent_orange, null));
            TextView tv = (TextView) binding.cardKycStatus.getChildAt(0);
            tv.setText("Offline Mode: SMS Bridge Active");
            tv.setCompoundDrawablesWithIntrinsicBounds(android.R.drawable.stat_notify_chat, 0, 0, 0);
        }
    }

    private void setupKycStatus() {
        if ("PENDING".equals(authManager.getKycStatus())) {
            binding.cardKycStatus.setVisibility(View.VISIBLE);
            binding.cardKycStatus.setOnClickListener(v -> Navigation.findNavController(v).navigate(R.id.nav_kyc));
        } else {
            binding.cardKycStatus.setVisibility(View.GONE);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}