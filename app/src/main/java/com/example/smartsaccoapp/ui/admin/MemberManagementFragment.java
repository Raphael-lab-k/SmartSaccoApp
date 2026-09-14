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

import com.example.smartsaccoapp.data.AuthManager;
import com.example.smartsaccoapp.data.SaccoRepository;
import com.example.smartsaccoapp.data.db.Member;
import com.example.smartsaccoapp.data.db.Vouch;
import com.example.smartsaccoapp.databinding.FragmentMemberManagementBinding;
import com.example.smartsaccoapp.databinding.ItemVouchBinding;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;

import android.text.Editable;
import android.text.TextWatcher;
import com.example.smartsaccoapp.R;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MemberManagementFragment extends Fragment {

    private FragmentMemberManagementBinding binding;
    @Inject SaccoRepository repository;
    @Inject AuthManager authManager;
    private MemberAdapter adapter;
    private List<Member> allMembers = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMemberManagementBinding.inflate(inflater, container, false);

        adapter = new MemberAdapter();
        binding.rvMembers.setAdapter(adapter);

        repository.getAllMembers().observe(getViewLifecycleOwner(), members -> {
            allMembers = members;
            filterMembers(binding.etSearchMembers.getText().toString());
        });

        setupSearch();

        return binding.getRoot();
    }

    private void setupSearch() {
        binding.etSearchMembers.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterMembers(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterMembers(String query) {
        List<Member> filteredList = new ArrayList<>();
        for (Member member : allMembers) {
            if (member.name.toLowerCase().contains(query.toLowerCase()) || 
                member.email.toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(member);
            }
        }
        
        adapter.setMembers(filteredList);
        
        if (filteredList.isEmpty()) {
            binding.tvEmptyMembers.setVisibility(View.VISIBLE);
            binding.rvMembers.setVisibility(View.GONE);
        } else {
            binding.tvEmptyMembers.setVisibility(View.GONE);
            binding.rvMembers.setVisibility(View.VISIBLE);
        }
    }

    private class MemberAdapter extends RecyclerView.Adapter<MemberAdapter.MemberViewHolder> {
        private List<Member> members = new ArrayList<>();

        void setMembers(List<Member> members) {
            this.members = members;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new MemberViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull MemberViewHolder holder, int position) {
            Member member = members.get(position);
            holder.text1.setText(member.name + " (" + member.role + ")");
            holder.text2.setText("Score: " + member.trustScore + " | KYC: " + member.kycStatus + " | Email: " + member.email);

            holder.itemView.setOnClickListener(v -> showMemberDetails(member));
        }

        @Override
        public int getItemCount() {
            return members.size();
        }

        class MemberViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;
            MemberViewHolder(View itemView) {
                super(itemView);
                text1 = itemView.findViewById(android.R.id.text1);
                text2 = itemView.findViewById(android.R.id.text2);
            }
        }
    }

    private void showMemberDetails(Member member) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Member Details")
                .setMessage("Name: " + member.name + "\nTrust Score: " + member.trustScore + "\nKYC Status: " + member.kycStatus + "\nID: " + member.idNumber)
                .setPositiveButton("Approve KYC", (dialog, which) -> {
                    member.kycStatus = "VERIFIED";
                    repository.updateMember(member);
                    repository.insertAuditLog(authManager.getUserEmail(), "KYC_APPROVE", "Verified member: " + member.email);
                    Toast.makeText(getContext(), "KYC Approved", Toast.LENGTH_SHORT).show();
                })
                .setNeutralButton("Vouch History", (dialog, which) -> showVouchHistory(member))
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showVouchHistory(Member member) {
        View view = LayoutInflater.from(getContext()).inflate(R.layout.dialog_vouch_history, null);
        RecyclerView rv = view.findViewById(R.id.rv_vouch_history);
        TextView emptyTv = view.findViewById(R.id.tv_empty_history);

        VouchHistoryAdapter histAdapter = new VouchHistoryAdapter();
        rv.setAdapter(histAdapter);

        repository.getVouchesForMember(member.email).observe(getViewLifecycleOwner(), vouches -> {
            if (vouches == null || vouches.isEmpty()) {
                emptyTv.setVisibility(View.VISIBLE);
                rv.setVisibility(View.GONE);
            } else {
                emptyTv.setVisibility(View.GONE);
                rv.setVisibility(View.VISIBLE);
                histAdapter.setVouches(vouches);
            }
        });

        new AlertDialog.Builder(requireContext())
                .setView(view)
                .setPositiveButton("Close", null)
                .show();
    }

    private static class VouchHistoryAdapter extends RecyclerView.Adapter<VouchHistoryAdapter.ViewHolder> {
        private List<Vouch> vouches = new ArrayList<>();
        private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        void setVouches(List<Vouch> vouches) {
            this.vouches = vouches;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemVouchBinding b = ItemVouchBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(b);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Vouch v = vouches.get(position);
            holder.binding.tvVoucherName.setText(v.voucherEmail);
            holder.binding.tvVouchDate.setText("Vouched on: " + sdf.format(new Date(v.timestamp)));
        }

        @Override
        public int getItemCount() {
            return vouches.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ItemVouchBinding binding;
            ViewHolder(ItemVouchBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
