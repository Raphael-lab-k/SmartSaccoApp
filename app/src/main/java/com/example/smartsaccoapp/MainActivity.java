package com.example.smartsaccoapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.Menu;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.navigation.NavigationView;

import androidx.annotation.NonNull;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;
import androidx.appcompat.app.AppCompatActivity;
import androidx.work.PeriodicWorkRequest;
import androidx.work.WorkManager;

import android.view.View;
import android.widget.TextView;

import com.example.smartsaccoapp.data.AuthManager;
import com.example.smartsaccoapp.data.SaccoWorker;
import com.example.smartsaccoapp.databinding.ActivityMainBinding;
import com.example.smartsaccoapp.ui.auth.LoginActivity;

import java.util.concurrent.TimeUnit;

import dagger.hilt.android.AndroidEntryPoint;

@AndroidEntryPoint
public class MainActivity extends AppCompatActivity {

    private AppBarConfiguration mAppBarConfiguration;
    private AuthManager authManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        authManager = new AuthManager(this);
        ActivityMainBinding binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        scheduleSaccoWork();

        setSupportActionBar(binding.appBarMain.toolbar);
        if (binding.appBarMain.fab != null) {
            binding.appBarMain.fab.setOnClickListener(view -> Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                    .setAction("Action", null).setAnchorView(R.id.fab).show());
        }
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_content_main);
        assert navHostFragment != null;
        NavController navController = navHostFragment.getNavController();

        NavigationView navigationView = binding.navView;
        if (navigationView != null) {
            mAppBarConfiguration = new AppBarConfiguration.Builder(
                    R.id.nav_dashboard, R.id.nav_admin_dashboard, R.id.nav_savings, R.id.nav_loans, R.id.nav_guarantors, 
                    R.id.nav_marketplace, R.id.nav_member_lookup, R.id.nav_settings)
                    .setOpenableLayout(binding.drawerLayout)
                    .build();
            NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
            NavigationUI.setupWithNavController(navigationView, navController);

            // Set user info in nav header
            View headerView = navigationView.getHeaderView(0);
            TextView navHeaderTitle = headerView.findViewById(R.id.nav_header_title);
            TextView navHeaderSubtitle = headerView.findViewById(R.id.nav_header_subtitle);

            if (navHeaderTitle != null) navHeaderTitle.setText(authManager.getUserName());
            if (navHeaderSubtitle != null) navHeaderSubtitle.setText(authManager.getUserEmail());

            setupNavigationBasedOnRole(navigationView);
        }

        BottomNavigationView bottomNavigationView = binding.appBarMain.contentMain.bottomNavView;
        if (bottomNavigationView != null) {
            if ("ADMIN".equals(authManager.getUserRole())) {
                bottomNavigationView.setVisibility(View.GONE);
            } else {
                mAppBarConfiguration = new AppBarConfiguration.Builder(
                        R.id.nav_dashboard, R.id.nav_savings, R.id.nav_loans, R.id.nav_guarantors)
                        .build();
                NavigationUI.setupActionBarWithNavController(this, navController, mAppBarConfiguration);
                NavigationUI.setupWithNavController(bottomNavigationView, navController);
            }
        }

        // Redirect Admin to Admin Dashboard on first load
        if (savedInstanceState == null && "ADMIN".equals(authManager.getUserRole())) {
            navController.navigate(R.id.nav_admin_dashboard);
        }
    }

    private void scheduleSaccoWork() {
        PeriodicWorkRequest workRequest = new PeriodicWorkRequest.Builder(SaccoWorker.class, 24, TimeUnit.HOURS)
                .build();
        WorkManager.getInstance(this).enqueue(workRequest);
    }

    private void setupNavigationBasedOnRole(NavigationView navigationView) {
        Menu menu = navigationView.getMenu();
        String role = authManager.getUserRole();

        boolean isAdmin = "ADMIN".equals(role);

        // Member items
        menu.findItem(R.id.nav_savings).setVisible(!isAdmin);
        menu.findItem(R.id.nav_loans).setVisible(!isAdmin);
        menu.findItem(R.id.nav_guarantors).setVisible(!isAdmin);

        // Admin items
        menu.findItem(R.id.nav_admin_dashboard).setVisible(isAdmin);
        menu.findItem(R.id.nav_member_mgmt).setVisible(isAdmin);
        menu.findItem(R.id.nav_loan_mgmt).setVisible(isAdmin);
        menu.findItem(R.id.nav_financial_reports).setVisible(isAdmin);
        menu.findItem(R.id.nav_audit_trail).setVisible(isAdmin);
        menu.findItem(R.id.nav_branch_mgmt).setVisible(isAdmin);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        boolean result = super.onCreateOptionsMenu(menu);
        // Using findViewById because NavigationView exists in different layout files
        // between w600dp and w1240dp
        NavigationView navView = findViewById(R.id.nav_view);
        if (navView == null) {
            // The navigation drawer already has the items including the items in the overflow menu
            // We only inflate the overflow menu if the navigation drawer isn't visible
            getMenuInflater().inflate(R.menu.overflow, menu);
        }
        return result;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.nav_settings) {
            NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
            navController.navigate(R.id.nav_settings);
            return true;
        } else if (item.getItemId() == R.id.action_logout) {
            authManager.logout();
            startActivity(new Intent(this, LoginActivity.class));
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_content_main);
        return NavigationUI.navigateUp(navController, mAppBarConfiguration)
                || super.onSupportNavigateUp();
    }
}