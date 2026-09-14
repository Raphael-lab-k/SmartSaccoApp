package com.example.smartsaccoapp.ui.admin;

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
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartsaccoapp.data.SaccoRepository;
import com.example.smartsaccoapp.data.db.Branch;
import com.example.smartsaccoapp.databinding.FragmentBranchManagementBinding;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class BranchManagementFragment extends Fragment {

    private FragmentBranchManagementBinding binding;
    @Inject SaccoRepository repository;
    private BranchAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentBranchManagementBinding.inflate(inflater, container, false);

        adapter = new BranchAdapter();
        binding.rvBranches.setAdapter(adapter);

        loadBranches();

        binding.btnAddBranch.setOnClickListener(v -> showAddBranchDialog());

        return binding.getRoot();
    }

    private void loadBranches() {
        new Thread(() -> {
            List<Branch> branches = repository.getAllBranches();
            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> adapter.setBranches(branches));
            }
        }).start();
    }

    private void showAddBranchDialog() {
        View view = getLayoutInflater().inflate(android.R.layout.two_line_list_item, null);
        // Using simple layout for speed, usually we'd have a custom one
        EditText nameInput = new EditText(requireContext());
        nameInput.setHint("Branch Name");
        
        new AlertDialog.Builder(requireContext())
                .setTitle("Add Branch")
                .setView(nameInput)
                .setPositiveButton("Add", (dialog, which) -> {
                    String name = nameInput.getText().toString().trim();
                    if (!name.isEmpty()) {
                        repository.insertBranch(new Branch(name, "General Location"));
                        loadBranches();
                        Toast.makeText(getContext(), "Branch Added", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private static class BranchAdapter extends RecyclerView.Adapter<BranchAdapter.BranchViewHolder> {
        private List<Branch> branches = new ArrayList<>();

        void setBranches(List<Branch> branches) {
            this.branches = branches != null ? branches : new ArrayList<>();
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public BranchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new BranchViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull BranchViewHolder holder, int position) {
            Branch branch = branches.get(position);
            holder.text1.setText(branch.name);
            holder.text2.setText(branch.location);
        }

        @Override
        public int getItemCount() {
            return branches.size();
        }

        static class BranchViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;
            BranchViewHolder(View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}