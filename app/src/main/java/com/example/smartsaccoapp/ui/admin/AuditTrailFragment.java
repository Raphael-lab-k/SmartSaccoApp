package com.example.smartsaccoapp.ui.admin;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.example.smartsaccoapp.data.SaccoRepository;
import com.example.smartsaccoapp.data.db.AuditLog;
import com.example.smartsaccoapp.databinding.FragmentAuditTrailBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class AuditTrailFragment extends Fragment {

    private FragmentAuditTrailBinding binding;
    @Inject SaccoRepository repository;
    private AuditAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentAuditTrailBinding.inflate(inflater, container, false);

        adapter = new AuditAdapter();
        binding.rvAuditLogs.setAdapter(adapter);

        repository.getAllAuditLogs().observe(getViewLifecycleOwner(), logs -> {
            adapter.setLogs(logs);
        });

        return binding.getRoot();
    }

    private static class AuditAdapter extends RecyclerView.Adapter<AuditAdapter.AuditViewHolder> {
        private List<AuditLog> logs = new ArrayList<>();

        void setLogs(List<AuditLog> logs) {
            this.logs = logs;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public AuditViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
            return new AuditViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull AuditViewHolder holder, int position) {
            AuditLog log = logs.get(position);
            holder.text1.setText(log.action + " by " + log.actorEmail);
            
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault());
            holder.text2.setText(sdf.format(new Date(log.timestamp)) + "\n" + log.details);
        }

        @Override
        public int getItemCount() {
            return logs.size();
        }

        static class AuditViewHolder extends RecyclerView.ViewHolder {
            TextView text1, text2;
            AuditViewHolder(View itemView) {
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