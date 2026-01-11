package com.example.smart_bin.fragments;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.example.smart_bin.LoginActivity;
import com.example.smart_bin.databinding.FragmentSettingsBinding;
import com.example.smart_bin.utils.Constants;
import com.example.smart_bin.utils.TokenManager;

public class SettingsFragment extends Fragment {
    private static final String TAG = "SettingsFragment";

    private FragmentSettingsBinding binding;
    private SharedPreferences preferences;
    private TokenManager tokenManager;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        preferences = PreferenceManager.getDefaultSharedPreferences(requireContext());
        tokenManager = TokenManager.getInstance(requireContext());

        setupViews();
        loadSettings();
    }

    private void setupViews() {
        // User Info
        String userName = tokenManager.getUserFullName();
        String userEmail = tokenManager.getUserEmail();
        binding.tvUserName.setText(userName);
        binding.tvUserEmail.setText(userEmail);

        // Theme Setting
        binding.settingTheme.setOnClickListener(v -> showThemeDialog());

        // Notifications Setting
        binding.settingNotifications.setOnClickListener(v -> Toast.makeText(requireContext(), "Notification settings coming soon", Toast.LENGTH_SHORT).show());

        // Auto Refresh Switch
        binding.switchAutoRefresh.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean(Constants.KEY_PREF_AUTO_REFRESH, isChecked).apply();
            String message = isChecked ? "Auto refresh enabled" : "Auto refresh disabled";
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        });

        // Logout Button
        binding.btnLogout.setOnClickListener(v -> showLogoutDialog());

        // Server URL
        binding.tvServerUrl.setText(Constants.BASE_URL);
    }

    private void loadSettings() {
        // Load auto refresh preference
        boolean autoRefreshEnabled = preferences.getBoolean(Constants.KEY_PREF_AUTO_REFRESH, true);
        binding.switchAutoRefresh.setChecked(autoRefreshEnabled);

        int themeMode = preferences.getInt(Constants.KEY_PREF_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        String themeText = themeMode == AppCompatDelegate.MODE_NIGHT_NO ? "Light Mode" :
                themeMode == AppCompatDelegate.MODE_NIGHT_YES ? "Dark Mode" : "System Default";
        binding.tvTheme.setText(themeText);
        AppCompatDelegate.setDefaultNightMode(themeMode);

    }

    private void showThemeDialog() {
        String[] themes = {"Light Mode", "Dark Mode", "System Default"};

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Choose Theme")
                .setItems(themes, (dialog, which) -> {
                    switch (which){
                        case 0:
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                            preferences.edit().putInt(Constants.KEY_PREF_THEME_MODE, AppCompatDelegate.MODE_NIGHT_NO).apply();
                            break;
                        case 1:
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                            preferences.edit().putInt(Constants.KEY_PREF_THEME_MODE, AppCompatDelegate.MODE_NIGHT_YES).apply();
                            break;
                        case 3:
                            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
                            preferences.edit().putInt(Constants.KEY_PREF_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM).apply();
                            break;
                    }
                    binding.tvTheme.setText(themes[which]);
                    Toast.makeText(requireContext(), themes[which], Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle("Logout")
                .setMessage("Are you sure you want to logout?")
                .setPositiveButton("Logout", (dialog, which) -> performLogout())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void performLogout() {
        tokenManager.clearTokens();

        Intent intent = new Intent(requireContext(), LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }

    public static boolean isAutoRefreshEnabled(SharedPreferences prefs) {
        return prefs.getBoolean(Constants.KEY_PREF_AUTO_REFRESH, true);
    }

    public static int getThemeMode(SharedPreferences prefs) {
        return prefs.getInt(Constants.KEY_PREF_THEME_MODE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}