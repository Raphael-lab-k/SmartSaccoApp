package com.example.smartsaccoapp.ui.auth;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smartsaccoapp.MainActivity;
import com.example.smartsaccoapp.data.AuthManager;
import com.example.smartsaccoapp.data.FirebaseManager;
import com.example.smartsaccoapp.data.SaccoRepository;
import com.example.smartsaccoapp.data.db.Account;
import com.example.smartsaccoapp.data.db.Branch;
import com.example.smartsaccoapp.data.db.Member;
import com.example.smartsaccoapp.databinding.ActivityLoginBinding;
import com.example.smartsaccoapp.util.BiometricHelper;
import com.example.smartsaccoapp.R;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    @Inject AuthManager authManager;
    @Inject SaccoRepository repository;
    @Inject FirebaseManager firebaseManager;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (authManager.isLoggedIn()) {
            startMainActivity();
            finish();
            return;
        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ensureAdminExists();

        binding.btnLogin.setOnClickListener(v -> handleLogin());
        binding.btnBiometric.setOnClickListener(v -> handleBiometricLogin());
        binding.tvSignupPrompt.setOnClickListener(v -> {
            startActivity(new Intent(LoginActivity.this, SignupActivity.class));
        });

        if (!BiometricHelper.isBiometricAvailable(this) || authManager.getSavedEmail() == null) {
            binding.btnBiometric.setVisibility(View.GONE);
        }
    }

    private void handleLogin() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email) || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.setError("Enter a valid email address");
            return;
        }
        binding.tilEmail.setError(null);

        if (TextUtils.isEmpty(password)) {
            binding.tilPassword.setError("Enter password");
            return;
        }
        binding.tilPassword.setError(null);

        // Demo Admin Bypass
        if ("admin@sacco.com".equals(email) && "password123".equals(password)) {
            executorService.execute(() -> {
                Member admin = repository.getMemberByEmail(email);
                if (admin != null) {
                    runOnUiThread(() -> {
                        authManager.setLogin(true, admin.email, admin.name, admin.role, admin.kycStatus);
                        authManager.saveCredentials(email, password);
                        Toast.makeText(this, "Admin Demo Login", Toast.LENGTH_SHORT).show();
                        startMainActivity();
                        finish();
                    });
                }
            });
            return;
        }

        // Firebase Auth Login
        firebaseManager.loginUser(email, password).addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                // Fetch Profile from Firestore
                firebaseManager.getMemberProfile(email).addOnSuccessListener(documentSnapshot -> {
                    Member member = documentSnapshot.toObject(Member.class);
                    if (member != null) {
                        authManager.setLogin(true, member.email, member.name, member.role, member.kycStatus);
                        authManager.saveCredentials(email, password);
                        
                        // Also update local Room DB if needed
                        executorService.execute(() -> {
                            if (repository.getMemberByEmail(email) == null) {
                                repository.insertMember(member);
                            }
                        });

                        Toast.makeText(this, "Login Successful", Toast.LENGTH_SHORT).show();
                        startMainActivity();
                        finish();
                    } else {
                        Toast.makeText(this, "Profile not found on cloud", Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                Toast.makeText(this, "Login failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void handleBiometricLogin() {
        String savedEmail = authManager.getSavedEmail();
        String savedPassword = authManager.getSavedPassword();

        if (savedEmail != null && savedPassword != null) {
            BiometricHelper.showBiometricPrompt(this, 
                getString(R.string.biometric_title), 
                getString(R.string.biometric_subtitle), 
                new BiometricHelper.BiometricCallback() {
                    @Override
                    public void onAuthenticationSuccess() {
                        binding.etEmail.setText(savedEmail);
                        binding.etPassword.setText(savedPassword);
                        handleLogin();
                    }

                    @Override
                    public void onAuthenticationError(String error) {
                        Toast.makeText(LoginActivity.this, "Biometric failed: " + error, Toast.LENGTH_SHORT).show();
                    }
                });
        }
    }

    private void ensureAdminExists() {
        executorService.execute(() -> {
            if (repository.getMemberByEmail("admin@sacco.com") == null) {
                repository.insertMember(new Member("Sacco Admin", "admin@sacco.com", "password123", "ADMIN", "VERIFIED", "ADM001", "Head Office", 1));
            }
            
            // Seed Demo Account
            repository.getAccount().observe(this, account -> {
                if (account == null) {
                    executorService.execute(() -> {
                        repository.insertAccount(new Account(1000000.0, "ACC-001-DEMO"));
                    });
                }
            });

            // Seed Branches
            if (repository.getAllBranches() == null || repository.getAllBranches().isEmpty()) {
                repository.insertBranch(new Branch("Head Office", "Main Street"));
                repository.insertBranch(new Branch("West Branch", "West Side"));
                repository.insertBranch(new Branch("East Branch", "East Side"));
            }
        });
    }

    private void startMainActivity() {
        startActivity(new Intent(LoginActivity.this, MainActivity.class));
    }
}