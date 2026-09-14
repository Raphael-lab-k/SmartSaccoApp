package com.example.smartsaccoapp.ui;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartsaccoapp.R;
import com.example.smartsaccoapp.data.db.Transaction;
import com.example.smartsaccoapp.databinding.ItemDashboardBinding;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class TransactionAdapter extends ListAdapter<Transaction, TransactionAdapter.TransactionViewHolder> {

    public TransactionAdapter() {
        super(new DiffUtil.ItemCallback<Transaction>() {
            @Override
            public boolean areItemsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
                return oldItem.id == newItem.id;
            }

            @Override
            public boolean areContentsTheSame(@NonNull Transaction oldItem, @NonNull Transaction newItem) {
                return oldItem.amount == newItem.amount && 
                       oldItem.timestamp == newItem.timestamp && 
                       oldItem.type.equals(newItem.type);
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
        
        String amountPrefix;
        int color;
        int icon;

        if ("DEPOSIT".equals(transaction.type) || "TRANSFER_IN".equals(transaction.type) || "LOAN_DISBURSEMENT".equals(transaction.type)) {
            amountPrefix = "+";
            color = holder.itemView.getContext().getResources().getColor(R.color.secondary_green, null);
            icon = android.R.drawable.ic_input_add;
        } else {
            amountPrefix = "-";
            color = holder.itemView.getContext().getResources().getColor(R.color.accent_red, null);
            icon = android.R.drawable.ic_menu_upload;
        }

        holder.binding.textTransactionAmount.setText(amountPrefix + String.format(Locale.getDefault(), "UGX %,.0f", transaction.amount));
        holder.binding.textTransactionAmount.setTextColor(color);
        holder.binding.ivTransactionIcon.setImageResource(icon);
        
        SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy • HH:mm", Locale.getDefault());
        holder.binding.textTransactionDate.setText(sdf.format(new Date(transaction.timestamp)));
    }

    public static class TransactionViewHolder extends RecyclerView.ViewHolder {
        public final ItemDashboardBinding binding;

        public TransactionViewHolder(ItemDashboardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
