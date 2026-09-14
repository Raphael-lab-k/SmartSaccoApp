package com.example.smartsaccoapp.ui.auth;

import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartsaccoapp.data.AuthManager;
import com.example.smartsaccoapp.data.FirebaseManager;
import com.example.smartsaccoapp.data.SaccoRepository;
import com.example.smartsaccoapp.data.db.Member;
import com.example.smartsaccoapp.databinding.ActivitySignupBinding;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class SignupActivity extends AppCompatActivity {

    private ActivitySignupBinding binding;
    @Inject AuthManager authManager;
    @Inject SaccoRepository repository;
    @Inject FirebaseManager firebaseManager;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivitySignupBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.btnSignup.setOnClickListener(v -> handleSignup());
        binding.tvLoginPrompt.setOnClickListener(v -> finish());
    }

    private void handleSignup() {
        String name = binding.etName.getText().toString().trim();
        String email = binding.etEmailSignup.getText().toString().trim();
        String phone = binding.etPhone.getText().toString().trim();
        String password = binding.etPasswordSignup.getText().toString().trim();
        String confirmPassword = binding.etConfirmPassword.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            binding.tilName.setError("Enter your full name");
            return;
        }
        binding.tilName.setError(null);

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmailSignup.setError("Enter a valid email address");
            return;
        }
        binding.tilEmailSignup.setError(null);

        if (TextUtils.isEmpty(phone)) {
            binding.tilPhone.setError("Enter phone number");
            return;
        }
        binding.tilPhone.setError(null);

        if (password.length() < 6) {
            binding.tilPasswordSignup.setError("Password must be at least 6 characters");
            return;
        }
        binding.tilPasswordSignup.setError(null);

        if (!password.equals(confirmPassword)) {
            binding.tilConfirmPassword.setError("Passwords do not match");
            return;
        }
        binding.tilConfirmPassword.setError(null);

        // Firebase Auth Registration
        firebaseManager.registerUser(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                executorService.execute(() -> {
                    Member newMember = new Member(name, email, password, "MEMBER", "PENDING", "", "", 1);
                    repository.insertMember(newMember);
                    firebaseManager.saveMemberProfile(newMember);
                    repository.insertAuditLog(email, "MEMBER_REGISTER", "New account created on Cloud");
                    
                    runOnUiThread(() -> {
                        Toast.makeText(this, "Account created successfully", Toast.LENGTH_SHORT).show();
                        finish();
                    });
                });
            } else {
                Toast.makeText(this, "Signup failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }
}