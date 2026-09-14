package com.example.smartsaccoapp.ui.auth;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;

import com.example.smartsaccoapp.data.AuthManager;
import com.example.smartsaccoapp.data.FirebaseManager;
import com.example.smartsaccoapp.data.SaccoRepository;
import com.example.smartsaccoapp.data.db.Branch;import com.example.smartsaccoapp.data.db.Member;
import com.example.smartsaccoapp.databinding.FragmentKycBinding;import java.util.List;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class KycFragment extends Fragment {

    private FragmentKycBinding binding;
    @Inject SaccoRepository repository;
    @Inject AuthManager authManager;
    @Inject FirebaseManager firebaseManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentKycBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        new Thread(() -> {
            List<Branch> branchList = repository.getAllBranches();
            if (branchList != null && !branchList.isEmpty()) {
                String[] branchNames = new String[branchList.size()];
                for (int i = 0; i < branchList.size(); i++) {
                    branchNames[i] = branchList.get(i).name;
                }
                
                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_dropdown_item_1line, branchNames);
                        binding.dropdownBranch.setAdapter(adapter);
                    });
                }
            }
        }).start();

        binding.btnSubmitKyc.setOnClickListener(v -> {
            String idNumber = binding.etIdNumber.getText().toString().trim();
            String address = binding.etAddress.getText().toString().trim();
            String branchName = binding.dropdownBranch.getText().toString().trim();

            if (idNumber.isEmpty()) {
                binding.tilIdNumber.setError("Required");
                return;
            }
            if (address.isEmpty()) {
                binding.tilAddress.setError("Required");
                return;
            }

            executorSubmitKyc(idNumber, address, branchName);
        });
    }

    private void executorSubmitKyc(String idNumber, String address, String branchName) {
        String email = authManager.getUserEmail();
        binding.btnSubmitKyc.setEnabled(false);
        binding.btnSubmitKyc.setText("");
        binding.progressKyc.setVisibility(View.VISIBLE);
        
        new Thread(() -> {
            try {
                Thread.sleep(1500); // Feel professional delay
            } catch (InterruptedException e) {}
            
            Member member = repository.getMemberByEmail(email);
            if (member != null) {
                member.idNumber = idNumber;
                member.address = address;
                
                // Find branch ID
                List<Branch> branches = repository.getAllBranches();
                if (branches != null) {
                    for (Branch b : branches) {
                        if (b.name.equals(branchName)) {
                            member.branchId = b.id;
                            break;
                        }
                    }
                }
                
                member.kycStatus = "PENDING";
                repository.updateMember(member);
                firebaseManager.saveMemberProfile(member);
                authManager.setKycStatus("PENDING");

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        binding.progressKyc.setVisibility(View.GONE);
                        Toast.makeText(requireContext(), "KYC Details Submitted Successfully", Toast.LENGTH_LONG).show();
                        Navigation.findNavController(requireView()).navigateUp();
                    });
                }
            }
        }).start();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}