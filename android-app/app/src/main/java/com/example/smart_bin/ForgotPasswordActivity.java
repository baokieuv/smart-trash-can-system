package com.example.smart_bin;


import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smart_bin.api.AuthService;
import com.example.smart_bin.databinding.ActivityForgotPasswordBinding;

import java.util.Objects;

public class ForgotPasswordActivity extends AppCompatActivity {
    private static final String TAG = "ForgotPasswordActivity";

    private ActivityForgotPasswordBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityForgotPasswordBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupToolbar();
        setupViews();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Forgot Password");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupViews() {
        binding.btnResetPassword.setOnClickListener(v -> handleForgotPassword());
        binding.tvBackToLogin.setOnClickListener(v -> finish());
    }

    private void handleForgotPassword() {
        String email = Objects.requireNonNull(binding.etEmail.getText()).toString().trim();

        // Validation
        if (email.isEmpty()) {
            binding.etEmail.setError("Email is required");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.setError("Invalid email format");
            return;
        }

        showLoading(true);

        AuthService.getInstance(this).forgotPassword(email, new AuthService.MessageCallback() {
            @Override
            public void onSuccess(String message) {
                showLoading(false);
                showSuccessDialog(message);
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(ForgotPasswordActivity.this, error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showSuccessDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Password Reset Sent")
                .setMessage(message + "\n\nPlease check your email for the new password.")
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnResetPassword.setEnabled(!show);
        binding.etEmail.setEnabled(!show);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}