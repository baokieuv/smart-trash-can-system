package com.example.smart_bin;

import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smart_bin.api.AuthService;
import com.example.smart_bin.databinding.ActivityRegisterBinding;

import java.util.Objects;

public class RegisterActivity extends AppCompatActivity {

    private static final String TAG = "RegisterActivity";

    private ActivityRegisterBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityRegisterBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupViews();
        setupToolbar();
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Create Account");
        }
        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }

    private void setupViews() {
        binding.btnRegister.setOnClickListener(v -> handleRegister());
        binding.tvLogin.setOnClickListener(v -> finish());
    }


    private void handleRegister() {
        String email = Objects.requireNonNull(binding.etEmail.getText()).toString().trim();
        String password = Objects.requireNonNull(binding.etPassword.getText()).toString().trim();
        String confirmPassword = Objects.requireNonNull(binding.etConfirmPassword.getText()).toString().trim();
        String firstName = Objects.requireNonNull(binding.etFirstName.getText()).toString().trim();
        String lastName = Objects.requireNonNull(binding.etLastName.getText()).toString().trim();

        // Validation
        if (firstName.isEmpty()) {
            binding.etFirstName.setError("First name is required");
            return;
        }

        if (lastName.isEmpty()) {
            binding.etLastName.setError("Last name is required");
            return;
        }

        if (email.isEmpty()) {
            binding.etEmail.setError("Email is required");
            return;
        }

        if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.setError("Invalid email format");
            return;
        }

        if (password.isEmpty()) {
            binding.etPassword.setError("Password is required");
            return;
        }

        if (password.length() < 8) {
            binding.etPassword.setError("Password must be at least 8 characters");
            return;
        }

        if (!password.equals(confirmPassword)) {
            binding.etConfirmPassword.setError("Passwords do not match");
            return;
        }

        showLoading(true);

        AuthService.getInstance(this).register(email, password, firstName, lastName, new AuthService.MessageCallback() {
            @Override
            public void onSuccess(String message) {
                showLoading(false);
                showSuccessDialog(message);
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(RegisterActivity.this, "Registration failed: " + error, Toast.LENGTH_LONG).show();
            }
        });
    }

    private void showSuccessDialog(String message) {
        new AlertDialog.Builder(this)
                .setTitle("Registration Successful")
                .setMessage(message + "\n\nPlease check your email to verify your account before logging in.")
                .setPositiveButton("OK", (dialog, which) -> finish())
                .setCancelable(false)
                .show();
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnRegister.setEnabled(!show);
        binding.etEmail.setEnabled(!show);
        binding.etPassword.setEnabled(!show);
        binding.etConfirmPassword.setEnabled(!show);
        binding.etFirstName.setEnabled(!show);
        binding.etLastName.setEnabled(!show);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }
}
