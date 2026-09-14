package com.example.smartsaccoapp.ui.loans;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartsaccoapp.R;
import com.example.smartsaccoapp.data.db.Loan;
import com.example.smartsaccoapp.databinding.FragmentLoansBinding;
import com.example.smartsaccoapp.data.SaccoRepository;
import com.example.smartsaccoapp.databinding.ItemLoanBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import javax.inject.Inject;

import androidx.appcompat.app.AlertDialog;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoansFragment extends Fragment {

    private FragmentLoansBinding binding;
    @Inject SaccoRepository repository;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        LoansViewModel loansViewModel =
                new ViewModelProvider(this).get(LoansViewModel.class);

        binding = FragmentLoansBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView recyclerView = binding.recyclerviewLoans;
        LoanAdapter adapter = new LoanAdapter(this::showRepaymentDialog);
        recyclerView.setAdapter(adapter);
        loansViewModel.getAllLoans().observe(getViewLifecycleOwner(), loans -> {
            adapter.submitList(loans);
            if (loans == null || loans.isEmpty()) {
                binding.layoutEmptyLoans.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
            } else {
                binding.layoutEmptyLoans.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
            }
        });

        binding.buttonApplyLoan.setOnClickListener(v -> 
            Navigation.findNavController(v).navigate(R.id.action_nav_loans_to_loanApplicationFragment)
        );

        return root;
    }

    private void showRepaymentDialog(Loan loan) {
        if (!"APPROVED".equals(loan.status)) {
            Toast.makeText(getContext(), "Only approved loans can be repaid", Toast.LENGTH_SHORT).show();
            return;
        }

        new AlertDialog.Builder(requireContext())
                .setTitle("Loan Repayment")
                .setMessage("Amount Due: UGX " + String.format(Locale.getDefault(), "%,.0f", (loan.amount * (1.1))) + "\nRepaid: UGX " + String.format(Locale.getDefault(), "%,.0f", loan.repaidAmount))
                .setPositiveButton("Repay UGX 500,000", (dialog, which) -> {
                    repository.repayLoan(loan, 500000.0);
                })
                .setNeutralButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class LoanAdapter extends ListAdapter<Loan, LoanViewHolder> {
        private final OnLoanClickListener listener;

        interface OnLoanClickListener {
            void onRepayClick(Loan loan);
        }

        protected LoanAdapter(OnLoanClickListener listener) {
            super(new DiffUtil.ItemCallback<Loan>() {
                @Override
                public boolean areItemsTheSame(@NonNull Loan oldItem, @NonNull Loan newItem) {
                    return oldItem.id == newItem.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull Loan oldItem, @NonNull Loan newItem) {
                    return oldItem.amount == newItem.amount && oldItem.status.equals(newItem.status) && oldItem.repaidAmount == newItem.repaidAmount;
                }
            });
            this.listener = listener;
        }

        @NonNull
        @Override
        public LoanViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemLoanBinding binding = ItemLoanBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new LoanViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull LoanViewHolder holder, int position) {
            Loan loan = getItem(position);
            holder.binding.textLoanAmount.setText(String.format(Locale.getDefault(), "UGX %,.0f", loan.amount));
            holder.binding.textLoanStatus.setText(loan.status + " (" + (int)((loan.repaidAmount / (loan.amount * (1.1))) * 100) + "%)");
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
            holder.binding.textLoanDueDate.setText(String.format("Due: %s", sdf.format(new Date(loan.dueDate))));

            holder.binding.btnRepayLoan.setOnClickListener(v -> listener.onRepayClick(loan));
        }
    }

    private static class LoanViewHolder extends RecyclerView.ViewHolder {
        private final ItemLoanBinding binding;

        public LoanViewHolder(ItemLoanBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}