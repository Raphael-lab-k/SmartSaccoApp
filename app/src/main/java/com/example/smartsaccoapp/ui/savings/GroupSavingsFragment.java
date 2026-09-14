package com.example.smartsaccoapp.ui.savings;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartsaccoapp.R;
import com.example.smartsaccoapp.data.AuthManager;
import com.example.smartsaccoapp.data.SaccoRepository;
import com.example.smartsaccoapp.data.db.SavingPot;
import com.example.smartsaccoapp.databinding.FragmentGroupSavingsBinding;
import com.example.smartsaccoapp.databinding.ItemSavingPotBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class GroupSavingsFragment extends Fragment {

    private FragmentGroupSavingsBinding binding;
    @Inject SaccoRepository repository;
    @Inject AuthManager authManager;
    private PotAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentGroupSavingsBinding.inflate(inflater, container, false);

        adapter = new PotAdapter();
        binding.rvSavingPots.setAdapter(adapter);

        repository.getActivePots().observe(getViewLifecycleOwner(), pots -> {
            adapter.setPots(pots);
        });

        binding.fabCreatePot.setOnClickListener(v -> showCreatePotDialog());

        return binding.getRoot();
    }

    private void showCreatePotDialog() {
        // Simplified for brevity, usually a custom layout
        EditText input = new EditText(requireContext());
        input.setHint("Pot Name (e.g. Land Fund)");
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Start a Saving Pot")
                .setView(input)
                .setPositiveButton("Create", (dialog, which) -> {
                    String name = input.getText().toString().trim();
                    if (!name.isEmpty()) {
                        SavingPot pot = new SavingPot(name, 1000000.0, authManager.getUserEmail(), "General");
                        repository.createSavingPot(pot);
                        Toast.makeText(getContext(), "Goal Pot Created!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private class PotAdapter extends RecyclerView.Adapter<PotAdapter.PotViewHolder> {
        private List<SavingPot> pots = new ArrayList<>();

        void setPots(List<SavingPot> pots) {
            this.pots = pots != null ? pots : new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public PotViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemSavingPotBinding b = ItemSavingPotBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new PotViewHolder(b);
        }

        @Override
        public void onBindViewHolder(@NonNull PotViewHolder holder, int position) {
            SavingPot pot = pots.get(position);
            holder.binding.tvPotName.setText(pot.name);
            holder.binding.tvPotCategory.setText(pot.category);
            
            int progress = (int) ((pot.currentAmount / pot.targetAmount) * 100);
            holder.binding.pbPotProgress.setProgress(progress);
            holder.binding.tvPotPercentage.setText(progress + "%");
            
            holder.binding.tvPotAmounts.setText(String.format(Locale.getDefault(), "UGX %,.0f / UGX %,.0f", pot.currentAmount, pot.targetAmount));

            holder.itemView.setOnClickListener(v -> showContributionDialog(pot));
        }

        @Override
        public int getItemCount() {
            return pots.size();
        }

        class PotViewHolder extends RecyclerView.ViewHolder {
            ItemSavingPotBinding binding;
            PotViewHolder(ItemSavingPotBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }

    private void showContributionDialog(SavingPot pot) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Contribute to " + pot.name)
                .setMessage("Add UGX 50,000 from your balance?")
                .setPositiveButton("Contribute", (dialog, which) -> {
                    repository.contributeToPot(pot.id, authManager.getUserEmail(), 50000.0);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
