package com.example.smartsaccoapp.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartsaccoapp.R;
import com.example.smartsaccoapp.data.AuthManager;
import com.example.smartsaccoapp.data.FirebaseManager;
import com.example.smartsaccoapp.data.SaccoRepository;
import com.example.smartsaccoapp.data.db.Loan;
import com.example.smartsaccoapp.databinding.FragmentLoanManagementBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoanManagementFragment extends Fragment {

    private FragmentLoanManagementBinding binding;
    @Inject SaccoRepository repository;
    @Inject AuthManager authManager;
    @Inject FirebaseManager firebaseManager;
    private LoanAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentLoanManagementBinding.inflate(inflater, container, false);

        adapter = new LoanAdapter();
        binding.rvLoansAdmin.setAdapter(adapter);

        // Filter for PENDING loans by default for better management experience
        repository.getAllLoans().observe(getViewLifecycleOwner(), loans -> {
            List<Loan> pendingLoans = new ArrayList<>();
            for (Loan loan : loans) {
                if ("PENDING".equals(loan.status)) {
                    pendingLoans.add(loan);
                }
            }
            adapter.setLoans(pendingLoans);
        });

        return binding.getRoot();
    }

    private class LoanAdapter extends RecyclerView.Adapter<LoanAdapter.LoanViewHolder> {
        private List<Loan> loans = new ArrayList<>();

        void setLoans(List<Loan> loans) {
            this.loans = loans;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public LoanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_loan, parent, false);
            return new LoanViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull LoanViewHolder holder, int position) {
            Loan loan = loans.get(position);
            holder.member.setText(loan.memberEmail);
            holder.type.setText(loan.type);
            holder.amount.setText(String.format(Locale.getDefault(), "UGX %,.0f", loan.amount));
            holder.status.setText(loan.status);

            holder.itemView.setOnClickListener(v -> showLoanApprovalDialog(loan));
        }

        @Override
        public int getItemCount() {
            return loans.size();
        }

        class LoanViewHolder extends RecyclerView.ViewHolder {
            TextView member, type, amount, status;
            LoanViewHolder(View itemView) {
                super(itemView);
                member = itemView.findViewById(R.id.text_loan_member);
                type = itemView.findViewById(R.id.text_loan_type);
                amount = itemView.findViewById(R.id.text_loan_amount);
                status = itemView.findViewById(R.id.text_loan_status);
            }
        }
    }

    private void showLoanApprovalDialog(Loan loan) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Loan Approval Request")
                .setMessage("Member: " + loan.memberEmail + "\n" +
                            "Type: " + loan.type + "\n" +
                            "Amount: UGX " + String.format(Locale.getDefault(), "%,.0f", loan.amount) + "\n" +
                            "Purpose: " + loan.purpose + "\n" +
                            "Risk Level: " + loan.riskLevel + " (AI Computed)")
                .setPositiveButton("Approve", (dialog, which) -> {
                    repository.approveLoan(loan, authManager.getUserEmail());
                    firebaseManager.syncLoan(loan);
                    Toast.makeText(getContext(), "Loan Approved & Funds Disbursed", Toast.LENGTH_LONG).show();
                })
                .setNegativeButton("Reject", (dialog, which) -> {
                    loan.status = "REJECTED";
                    repository.updateLoan(loan);
                    firebaseManager.syncLoan(loan);
                    Toast.makeText(getContext(), "Loan Rejected", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}