package com.example.smart_bin;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smart_bin.api.AuthService;
import com.example.smart_bin.databinding.ActivityLoginBinding;
import com.example.smart_bin.model.AuthResponse;
import com.example.smart_bin.model.User;
import com.example.smart_bin.utils.TokenManager;

import java.util.Objects;

public class LoginActivity extends AppCompatActivity {
    private static final String TAG = "LoginActivity";

    private ActivityLoginBinding binding;
    private TokenManager tokenManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tokenManager = TokenManager.getInstance(this);

        if(tokenManager.isLoggedIn()){
            navigateToMain();
            return;
        }

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setupViews();
    }

    private void setupViews() {
        binding.btnLogin.setOnClickListener(v -> handleLogin());
        binding.tvRegister.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });
        binding.tvResendVerification.setOnClickListener(v -> handleResendVerification());

        binding.tvForgotPassword.setOnClickListener(v -> {
            Intent intent = new Intent(this, ForgotPasswordActivity.class);
            startActivity(intent);
        });
    }

    private void handleLogin(){
        String email = Objects.requireNonNull(binding.etEmail.getText().toString());
        String password = Objects.requireNonNull(binding.etPassword.getText().toString());

        if(email.isEmpty()){
            binding.etEmail.setError("Email is required");
            return;
        }
        if (password.isEmpty()) {
            binding.etPassword.setError("Password is required");
            return;
        }

        showLoading(true);

        AuthService.getInstance(this).login(email, password, new AuthService.AuthCallback() {

            @Override
            public void onSuccess(AuthResponse response) {
                User user = response.getUser();

                tokenManager.saveAuthResponse(
                        response.getAccessToken(),
                        response.getRefreshToken(),
                        response.getExpiresIn(),
                        user.getId(),
                        user.getEmail(),
                        user.getFirstName(),
                        user.getLastName(),
                        user.isEmailVerified()
                );


                showLoading(false);
                Toast.makeText(LoginActivity.this, "Login successful!", Toast.LENGTH_SHORT).show();
                navigateToMain();
            }

            @Override
            public void onError(String error) {
                showLoading(false);

                if (error.contains("Email not verified")) {
                    binding.layoutResendVerification.setVisibility(View.VISIBLE);
                    Toast.makeText(LoginActivity.this, error, Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(LoginActivity.this, "Login failed: " + error, Toast.LENGTH_LONG).show();
                }
            }
        });
    }

    private void handleResendVerification(){
        String email = Objects.requireNonNull(binding.etEmail.getText()).toString().trim();

        if (email.isEmpty()) {
            binding.etEmail.setError("Email is required");
            return;
        }

        showLoading(true);

        AuthService.getInstance(this).resendVerification(email, new AuthService.MessageCallback() {
            @Override
            public void onSuccess(String message) {
                showLoading(false);
                Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(String error) {
                showLoading(false);
                Toast.makeText(LoginActivity.this, "Failed to resend verification: " + error, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showLoading(boolean show) {
        binding.progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.btnLogin.setEnabled(!show);
        binding.etEmail.setEnabled(!show);
        binding.etPassword.setEnabled(!show);
    }

    private void navigateToMain() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

}
