package com.example.smartsaccoapp.ui.guarantors;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartsaccoapp.data.db.Guarantor;
import com.example.smartsaccoapp.databinding.FragmentGuarantorsBinding;
import com.example.smartsaccoapp.databinding.ItemGuarantorBinding;

import java.util.Locale;

public class GuarantorsFragment extends Fragment {

    private FragmentGuarantorsBinding binding;
    private GuarantorViewModel viewModel;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGuarantorsBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(GuarantorViewModel.class);

        GuarantorAdapter adapter = new GuarantorAdapter();
        binding.recyclerviewGuarantors.setAdapter(adapter);

        viewModel.getAllGuarantors().observe(getViewLifecycleOwner(), adapter::submitList);

        binding.fabAddGuarantor.setOnClickListener(v -> {
            // Mock adding a guarantor for demonstration
            viewModel.insert(new Guarantor("John Doe", 101, "PENDING"));
        });

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private static class GuarantorAdapter extends ListAdapter<Guarantor, GuarantorViewHolder> {

        protected GuarantorAdapter() {
            super(new DiffUtil.ItemCallback<Guarantor>() {
                @Override
                public boolean areItemsTheSame(@NonNull Guarantor oldItem, @NonNull Guarantor newItem) {
                    return oldItem.id == newItem.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull Guarantor oldItem, @NonNull Guarantor newItem) {
                    return oldItem.status.equals(newItem.status) && oldItem.memberName.equals(newItem.memberName);
                }
            });
        }

        @NonNull
        @Override
        public GuarantorViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemGuarantorBinding binding = ItemGuarantorBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new GuarantorViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull GuarantorViewHolder holder, int position) {
            Guarantor guarantor = getItem(position);
            holder.binding.textGuarantorName.setText(guarantor.memberName);
            holder.binding.textLoanId.setText(String.format(Locale.getDefault(), "Loan ID: %d", guarantor.loanId));
            holder.binding.textGuarantorStatus.setText(guarantor.status);
        }
    }

    private static class GuarantorViewHolder extends RecyclerView.ViewHolder {
        private final ItemGuarantorBinding binding;

        public GuarantorViewHolder(ItemGuarantorBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}