package com.example.smart_bin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smart_bin.api.AuthService;
import com.example.smart_bin.databinding.ActivityChangePasswordBinding;
import com.example.smart_bin.utils.TokenManager;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChangePasswordActivity extends AppCompatActivity {
    private static final String TAG = "ChangePasswordActivity";

    private ActivityChangePasswordBinding binding;
    private TokenManager tokenManager;
    private ExecutorService executorService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChangePasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        tokenManager = TokenManager.getInstance(this);
        executorService = Executors.newSingleThreadExecutor();

        setupToolbar();
        setupViews();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Change Password");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupViews() {
        binding.btnChangePassword.setOnClickListener(v -> handleChangePassword());
    }

    private void handleChangePassword() {
        String currentPassword = Objects.requireNonNull(binding.etCurrentPassword.getText()).toString().trim();
        String newPassword = Objects.requireNonNull(binding.etNewPassword.getText()).toString().trim();
        String confirmPassword = Objects.requireNonNull(binding.etConfirmPassword.getText()).toString().trim();

        // Validation
        if (currentPassword.isEmpty()) {
            binding.etCurrentPassword.setError("Current password is required");
            return;
        }

        if (newPassword.isEmpty()) {
            binding.etNewPassword.setError("New password is required");
            return;
        }

        if (newPassword.length() < 8) {
            binding.etNewPassword.setError("Password must be at least 8 characters");
            return;
        }

        if (confirmPassword.isEmpty()) {
            binding.etConfirmPassword.setError("Confirm password is required");
            return;
        }

        if (!newPassword.equals(confirmPassword)) {
            binding.etConfirmPassword.setError("Passwords do not match");
            return;
        }

        if (currentPassword.equals(newPassword)) {
            binding.etNewPassword.setError("New password must be different from current password");
            return;
        }

        showLoading(true);
//        changePassword(currentPassword, newPassword, confirmPassword);
        AuthService.getInstance(this).changePassword(currentPassword, newPassword, confirmPassword, new AuthService.MessageCallback() {
            @Override
            public void onSuccess(String message) {
                runOnUiThread(() -> {
                    showLoading(false);
                    showSuccessDialog();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    showLoading(false);
                    Toast.makeText(ChangePasswordActivity.this, error, Toast.LENGTH_LONG).show();
                });
            }
        });
    }

    private void showSuccessDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Password Changed")
                .setMessage("Your password has been successfully changed. Please login again with your new password.")
                .setPositiveButton("OK", (dialog, which) -> {
                    // Clear tokens and navigate to login
                    tokenManager.clearTokens();
                    navigateToLogin();
                })
                .setCancelable(false)
                .show();
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnChangePassword.setEnabled(!show);
        binding.etCurrentPassword.setEnabled(!show);
        binding.etNewPassword.setEnabled(!show);
        binding.etConfirmPassword.setEnabled(!show);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        executorService.shutdown();
        binding = null;
    }
}