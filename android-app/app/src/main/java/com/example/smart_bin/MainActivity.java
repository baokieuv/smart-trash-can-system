package com.example.smart_bin;

import static androidx.core.content.ContentProviderCompat.requireContext;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import com.example.smart_bin.api.DeviceDataService;
import com.example.smart_bin.api.DeviceService;
import com.example.smart_bin.api.NotificationService;
import com.example.smart_bin.databinding.ActivityMainBinding;
import com.example.smart_bin.fragments.HomeFragment;
import com.example.smart_bin.fragments.NotificationsFragment;
import com.example.smart_bin.fragments.SettingsFragment;
import com.example.smart_bin.model.Notification;
import com.example.smart_bin.utils.TokenManager;
import com.google.android.material.badge.BadgeDrawable;

import java.util.List;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";
    private static final String SELECTED_ITEM_ID = "selected_item_id";

    private ActivityMainBinding binding;
    private TokenManager tokenManager;
    private Handler notificationHandler;
    private Runnable notificationRunnable;
    private static final long NOTIFICATION_REFRESH_INTERVAL = 30 * 1000;

    private int selectedItemId = R.id.navigation_home;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        tokenManager = TokenManager.getInstance(this);
        DeviceService.getInstance(this);
        DeviceDataService.getInstance(this);
        NotificationService.getInstance(this);

        if (!tokenManager.isLoggedIn()) {
            navigateToLogin();
            return;
        }

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);

        int themeMode = SettingsFragment.getThemeMode(preferences);
        AppCompatDelegate.setDefaultNightMode(themeMode);

        if (savedInstanceState != null) {
            selectedItemId = savedInstanceState.getInt(SELECTED_ITEM_ID, R.id.navigation_home);
        }

        setupNotificationAutoRefresh();
        setupBottomNavigation();
        setupFab();

        // Load initial fragment
        if (savedInstanceState == null) {
            loadFragment(new HomeFragment());
        }
    }

    private void setupNotificationAutoRefresh(){
        notificationHandler = new Handler(Looper.getMainLooper());
        notificationRunnable = new Runnable()  {
            @Override
            public void run() {
                updateNotification();
                notificationHandler.postDelayed(this, NOTIFICATION_REFRESH_INTERVAL);
            }
        };
    }

    private void startNotificationAutoRefresh() {
        if(notificationHandler != null && notificationRunnable != null){
            notificationHandler.removeCallbacks(notificationRunnable);

            notificationHandler.post(notificationRunnable);

            Log.d(TAG, "Notification auto refresh started");

        }
    }

    private void stopNotificationAutoRefresh() {
        if(notificationHandler != null && notificationRunnable != null) {
            notificationHandler.removeCallbacks(notificationRunnable);
            Log.d(TAG, "Notification auto refresh stopped");
        }
    }


    private void setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int itemId = item.getItemId();
            Fragment selectedFragment = null;

            if (itemId == R.id.navigation_home) {
                selectedFragment = new HomeFragment();
            } else if (itemId == R.id.navigation_noti) {
                selectedFragment = new NotificationsFragment();
                // Clear notification badge when opening
//                clearNotificationBadge();
            } else if (itemId == R.id.navigation_setting) {
                selectedFragment = new SettingsFragment();
            }

            if (selectedFragment != null) {
                selectedItemId = itemId;
                loadFragment(selectedFragment);
                return true;
            }
            return false;
        });

        // Set selected item
        binding.bottomNavigation.setSelectedItemId(selectedItemId);

        // Setup notification badge (example)
//        updateNotificationBadge(5); // Mock 5 unread notifications
        updateNotification();
    }

    private void setupFab() {
        binding.fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddDeviceActivity.class);
            startActivity(intent);
        });
    }

    private void loadFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit();
    }

    public void updateNotificationBadge(int count) {
        if (count > 0) {
            BadgeDrawable badge = binding.bottomNavigation.getOrCreateBadge(R.id.navigation_noti);
            badge.setNumber(count);
            badge.setVisible(true);
        } else {
            clearNotificationBadge();
        }
    }

    private void clearNotificationBadge() {
        binding.bottomNavigation.removeBadge(R.id.navigation_noti);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    public void updateNotification(){
        NotificationService.getInstance().getNotifications(new NotificationService.NotificationCallback() {
            @Override
            public void onSuccess(List<Notification> notifications) {
                runOnUiThread(()->{
                    int count = notifications.stream()
                            .filter(item -> item.getRead() != null && !item.getRead())
                            .mapToInt(item -> 1)
                            .sum();
                    updateNotificationBadge(count);
                });
            }

            @Override
            public void onError(String error) {
                Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading notifications: " + error);
            }
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putInt(SELECTED_ITEM_ID, selectedItemId);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (!tokenManager.isLoggedIn()) {
            navigateToLogin();
        } else {
            startNotificationAutoRefresh();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopNotificationAutoRefresh();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        stopNotificationAutoRefresh();
    }
}