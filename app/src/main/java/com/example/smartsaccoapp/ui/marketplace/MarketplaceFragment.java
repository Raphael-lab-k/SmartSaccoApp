package com.example.smartsaccoapp.ui.marketplace;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartsaccoapp.data.AuthManager;
import com.example.smartsaccoapp.data.SaccoRepository;
import com.example.smartsaccoapp.data.db.MarketItem;
import com.example.smartsaccoapp.databinding.FragmentMarketplaceBinding;
import com.example.smartsaccoapp.databinding.ItemMarketProductBinding;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MarketplaceFragment extends Fragment {

    private FragmentMarketplaceBinding binding;
    @Inject SaccoRepository repository;
    @Inject AuthManager authManager;
    private MarketAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMarketplaceBinding.inflate(inflater, container, false);

        adapter = new MarketAdapter();
        binding.rvMarketItems.setAdapter(adapter);

        repository.getAvailableMarketItems().observe(getViewLifecycleOwner(), items -> {
            adapter.setItems(items);
        });

        binding.fabListItem.setOnClickListener(v -> showAddItemDialog());

        return binding.getRoot();
    }

    private void showAddItemDialog() {
        EditText input = new EditText(requireContext());
        input.setHint("Item Name (e.g. Heifer, 10 Bags of Maize)");
        
        new AlertDialog.Builder(requireContext())
                .setTitle("List for Sale")
                .setView(input)
                .setPositiveButton("List Item", (dialog, which) -> {
                    String title = input.getText().toString().trim();
                    if (!title.isEmpty()) {
                        MarketItem item = new MarketItem(authManager.getUserEmail(), title, "Locally sourced", 150000.0, "General");
                        repository.listMarketItem(item);
                        Toast.makeText(getContext(), "Item listed in Sacco Marketplace!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private class MarketAdapter extends RecyclerView.Adapter<MarketAdapter.MarketViewHolder> {
        private List<MarketItem> items = new ArrayList<>();

        void setItems(List<MarketItem> items) {
            this.items = items != null ? items : new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MarketViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemMarketProductBinding b = ItemMarketProductBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new MarketViewHolder(b);
        }

        @Override
        public void onBindViewHolder(@NonNull MarketViewHolder holder, int position) {
            MarketItem item = items.get(position);
            holder.binding.tvProductTitle.setText(item.title);
            holder.binding.tvProductCategory.setText(item.category);
            holder.binding.tvProductPrice.setText(String.format(Locale.getDefault(), "UGX %,.0f", item.price));

            holder.itemView.setOnClickListener(v -> showBuyDialog(item));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class MarketViewHolder extends RecyclerView.ViewHolder {
            ItemMarketProductBinding binding;
            MarketViewHolder(ItemMarketProductBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }

    private void showBuyDialog(MarketItem item) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Buy " + item.title)
                .setMessage("Price: UGX " + String.format(Locale.getDefault(), "%,.0f", item.price) + "\n" +
                            "Funds will be held in Escrow until you confirm receipt.")
                .setPositiveButton("Buy Now", (dialog, which) -> {
                    repository.buyMarketItem(item.id, authManager.getUserEmail());
                    Toast.makeText(getContext(), "Purchase initialised. Check your balance.", Toast.LENGTH_LONG).show();
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
