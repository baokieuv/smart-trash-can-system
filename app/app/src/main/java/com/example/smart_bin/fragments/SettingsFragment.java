package com.example.smart_bin.fragments;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.example.smart_bin.databinding.FragmentSettingsBinding;
import com.example.smart_bin.utils.Constants;

public class SettingsFragment extends Fragment {
    private static final String TAG = "SettingsFragment";
    private static final String PREF_AUTO_REFRESH = "auto_refresh_enabled";

    private FragmentSettingsBinding binding;
    private SharedPreferences preferences;

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

        setupViews();
        loadSettings();
    }

    private void setupViews() {
        // Theme Setting
        binding.settingTheme.setOnClickListener(v -> showThemeDialog());

        // Notifications Setting
        binding.settingNotifications.setOnClickListener(v -> {
            Toast.makeText(requireContext(), "Notification settings coming soon", Toast.LENGTH_SHORT).show();
        });

        // Auto Refresh Switch
        binding.switchAutoRefresh.setOnCheckedChangeListener((buttonView, isChecked) -> {
            preferences.edit().putBoolean(PREF_AUTO_REFRESH, isChecked).apply();
            String message = isChecked ? "Auto refresh enabled" : "Auto refresh disabled";
            Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
        });

        // Server URL
        binding.tvServerUrl.setText(Constants.BASE_URL);
    }

    private void loadSettings() {
        // Load auto refresh preference
        boolean autoRefreshEnabled = preferences.getBoolean(PREF_AUTO_REFRESH, true);
        binding.switchAutoRefresh.setChecked(autoRefreshEnabled);
    }

    private void showThemeDialog() {
        String[] themes = {"Light Mode", "Dark Mode", "System Default"};

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Choose Theme")
                .setItems(themes, (dialog, which) -> {
                    String selectedTheme = themes[which];
                    Toast.makeText(requireContext(),
                            "Theme: " + selectedTheme + " (Coming soon)",
                            Toast.LENGTH_SHORT).show();
                })
                .show();
    }

    public static boolean isAutoRefreshEnabled(SharedPreferences prefs) {
        return prefs.getBoolean(PREF_AUTO_REFRESH, true);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}