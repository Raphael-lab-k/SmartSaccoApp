package com.example.smartsaccoapp.ui.savings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartsaccoapp.data.db.Transaction;
import com.example.smartsaccoapp.databinding.FragmentSavingsBinding;
import com.example.smartsaccoapp.databinding.ItemDashboardBinding;
import com.example.smartsaccoapp.util.StatementExporter;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class SavingsFragment extends Fragment {

    private FragmentSavingsBinding binding;
    private List<Transaction> currentTransactions = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        SavingsViewModel savingsViewModel =
                new ViewModelProvider(this).get(SavingsViewModel.class);

        binding = FragmentSavingsBinding.inflate(inflater, container, false);
        View root = binding.getRoot();

        RecyclerView recyclerView = binding.recyclerviewSavings;
        TransactionAdapter adapter = new TransactionAdapter();
        recyclerView.setAdapter(adapter);
        savingsViewModel.getAllTransactions().observe(getViewLifecycleOwner(), transactions -> {
            currentTransactions = transactions;
            adapter.submitList(transactions);
        });

        binding.btnExportStatement.setOnClickListener(v -> {
            if (!currentTransactions.isEmpty()) {
                StatementExporter.exportTransactionsToPdf(requireContext(), currentTransactions);
            }
        });

        return root;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class TransactionAdapter extends ListAdapter<Transaction, TransactionViewHolder> {

        protected TransactionAdapter() {
            super(new DiffUtil.ItemCallback<Transaction>() {
                @Override
                public boolean areItemsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
                    return oldItem.id == newItem.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
                    return oldItem.amount == newItem.amount && oldItem.timestamp == newItem.timestamp;
                }
            });
        }

        @NonNull
        @Override
        public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemDashboardBinding binding = ItemDashboardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new TransactionViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
            Transaction transaction = getItem(position);
            holder.binding.textTransactionType.setText(transaction.type);
            holder.binding.textTransactionAmount.setText(String.format(Locale.getDefault(), "UGX %,.0f", transaction.amount));
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
            holder.binding.textTransactionDate.setText(sdf.format(new Date(transaction.timestamp)));
        }
    }

    private static class TransactionViewHolder extends RecyclerView.ViewHolder {
        private final ItemDashboardBinding binding;

        public TransactionViewHolder(ItemDashboardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}