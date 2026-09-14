package com.example.smartsaccoapp.ui.dashboard;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartsaccoapp.data.AuthManager;
import com.example.smartsaccoapp.data.SaccoRepository;
import com.example.smartsaccoapp.data.db.Member;
import com.example.smartsaccoapp.data.db.Vouch;
import com.example.smartsaccoapp.databinding.FragmentMemberLookupBinding;
import com.example.smartsaccoapp.databinding.ItemMemberLookupBinding;
import com.example.smartsaccoapp.databinding.ItemVouchBinding;
import com.example.smartsaccoapp.R;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.text.SimpleDateFormat;

import androidx.appcompat.app.AlertDialog;
import android.widget.TextView;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MemberLookupFragment extends Fragment {

    private FragmentMemberLookupBinding binding;
    @Inject SaccoRepository repository;
    @Inject AuthManager authManager;
    private MemberLookupAdapter adapter;
    private List<Member> allMembers = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentMemberLookupBinding.inflate(inflater, container, false);

        adapter = new MemberLookupAdapter();
        binding.rvMembersLookup.setAdapter(adapter);

        repository.getAllMembers().observe(getViewLifecycleOwner(), members -> {
            allMembers = members;
            filterMembers(binding.etSearchMembersLookup.getText().toString());
        });

        setupSearch();

        return binding.getRoot();
    }

    private void setupSearch() {
        binding.etSearchMembersLookup.addTextChangedListener(new TextWatcher() {
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
        String currentUserEmail = authManager.getUserEmail();
        for (Member member : allMembers) {
            if (!member.email.equals(currentUserEmail) && 
                (member.name.toLowerCase().contains(query.toLowerCase()) || 
                 member.email.toLowerCase().contains(query.toLowerCase()))) {
                filteredList.add(member);
            }
        }
        
        adapter.setMembers(filteredList);
        
        if (filteredList.isEmpty()) {
            binding.tvEmptyMembersLookup.setVisibility(View.VISIBLE);
            binding.rvMembersLookup.setVisibility(View.GONE);
        } else {
            binding.tvEmptyMembersLookup.setVisibility(View.GONE);
            binding.rvMembersLookup.setVisibility(View.VISIBLE);
        }
    }

    private class MemberLookupAdapter extends RecyclerView.Adapter<MemberLookupAdapter.ViewHolder> {
        private List<Member> members = new ArrayList<>();

        void setMembers(List<Member> members) {
            this.members = members;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemMemberLookupBinding binding = ItemMemberLookupBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new ViewHolder(binding);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            Member member = members.get(position);
            holder.binding.textMemberName.setText(member.name);
            holder.binding.textMemberEmail.setText(member.email);
            holder.binding.textMemberScore.setText("Trust Score: " + member.trustScore);

            holder.itemView.setOnClickListener(v -> showVouchHistory(member));

            holder.binding.btnVouchAction.setOnClickListener(v -> {
                repository.vouchMember(authManager.getUserEmail(), member.email, new SaccoRepository.VouchCallback() {
                    @Override
                    public void onSuccess() {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                Toast.makeText(getContext(), "Vouched for " + member.name, Toast.LENGTH_SHORT).show();
                                holder.binding.btnVouchAction.setEnabled(false);
                                holder.binding.btnVouchAction.setText("Vouched");
                            });
                        }
                    }

                    @Override
                    public void onError(String message) {
                        if (getActivity() != null) {
                            getActivity().runOnUiThread(() -> {
                                if ("ALREADY_VOUCHED".equals(message)) {
                                    Toast.makeText(getContext(), "You already vouched for this member", Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(getContext(), "Error: " + message, Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                });
            });
        }

        @Override
        public int getItemCount() {
            return members.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ItemMemberLookupBinding binding;
            ViewHolder(ItemMemberLookupBinding binding) {
                super(binding.getRoot());
                this.binding = binding;
            }
        }
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
                .setTitle("Vouches for " + member.name)
                .setView(view)
                .setPositiveButton("Close", null)
                .show();
    }

    private class VouchHistoryAdapter extends RecyclerView.Adapter<VouchHistoryAdapter.HistViewHolder> {
        private List<Vouch> vouches = new ArrayList<>();
        private final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        void setVouches(List<Vouch> vouches) {
            this.vouches = vouches;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public HistViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            ItemVouchBinding b = ItemVouchBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
            return new HistViewHolder(b);
        }

        @Override
        public void onBindViewHolder(@NonNull HistViewHolder holder, int position) {
            Vouch v = vouches.get(position);
            holder.binding.tvVoucherName.setText(v.voucherEmail);
            holder.binding.tvVouchDate.setText("Vouched on: " + sdf.format(new Date(v.timestamp)));
        }

        @Override
        public int getItemCount() {
            return vouches.size();
        }

        class HistViewHolder extends RecyclerView.ViewHolder {
            ItemVouchBinding binding;
            HistViewHolder(ItemVouchBinding binding) {
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
