package com.example.smart_bin;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;

import com.example.smart_bin.api.DeviceDataService;
import com.example.smart_bin.api.DeviceService;
import com.example.smart_bin.databinding.ActivityDeviceControlBinding;
import com.example.smart_bin.fragments.SettingsFragment;
import com.example.smart_bin.model.Device;
import com.example.smart_bin.model.DeviceData;
import com.example.smart_bin.utils.Constants;
import com.example.smart_bin.utils.NetworkUtils;

import java.util.Objects;

@SuppressLint("SetTextI18n")
public class DeviceControlActivity extends AppCompatActivity {
    private static final String TAG = "DeviceControl";

    private ActivityDeviceControlBinding binding;
    private String deviceId;
    private String deviceName;
    private String status;
    private Handler handler;
    private Runnable runnable;

    private boolean autoRefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeviceControlBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        deviceId = Objects.requireNonNull(getIntent().getStringExtra(Constants.EXTRA_DEVICE_ID)).replace("_", ":");
        deviceName = getIntent().getStringExtra(Constants.EXTRA_DEVICE_NAME);
        status = getIntent().getStringExtra(Constants.EXTRA_DEVICE_STATUS);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(this);
        autoRefresh = SettingsFragment.isAutoRefreshEnabled(preferences);

        Log.i(TAG, "Device ID: " + deviceId);
        if (deviceId != null && deviceName != null) {
            setupToolbar();
            updateHeaderStatus();
            setupAutoRefresh();
            setupSwipeRefresh();
            if (NetworkUtils.isNetworkAvailable(this)) {
                loadDeviceData();
            } else {
                showNetworkError();
            }
        } else {
            finish();
        }
    }

    private void setupAutoRefresh(){
        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                if(autoRefresh && NetworkUtils.isNetworkAvailable(DeviceControlActivity.this)) {
                    loadDeviceData();
                }
                handler.postDelayed(this, Constants.REFRESH_INTERVAL);
            }
        };
    }

    private void loadDeviceData() {
        binding.progressBar.setVisibility(View.VISIBLE);

        DeviceDataService.getInstance().getDeviceData(deviceId, new DeviceDataService.DeviceDataCallback() {
            @Override
            public void onSuccess(DeviceData data) {
                updateUI(data);
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
            }

            @Override
            public void onError(String error) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                Toast.makeText(DeviceControlActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading device data: " + error);
            }
        });

        DeviceService.getInstance().getDevice(deviceId, new DeviceService.DeviceCallback() {
            @Override
            public void onSuccess(Device device) {
                status = device.getStatus();
                deviceName = device.getName();
                updateHeaderStatus();
            }

            @Override
            public void onError(String error) {
                Log.i(TAG, "Error getting device: " + error);
            }
        });
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(deviceName);
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());
        // Set device name in header
        binding.tvDeviceName.setText(deviceName);
        binding.tvMacAddress.setText(deviceId);

        binding.btnReconfigureWifi.setOnClickListener(v -> {
            Intent intent = new Intent(this, WiFiReconfigureActivity.class);
            intent.putExtra(Constants.EXTRA_DEVICE_ID, deviceId);
            intent.putExtra(Constants.EXTRA_DEVICE_NAME, deviceName);
            startActivity(intent);
        });
    }

    private void updateHeaderStatus() {
        boolean isOnline = Constants.STATUS_ONLINE.equalsIgnoreCase(status);
// Cập nhật status badge với thiết kế mới
        if (isOnline) {
            // Online status - màu xanh lá
            binding.statusBadgeHeader.setBackgroundResource(R.drawable.rounded_background);
            binding.statusBadgeHeader.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.success_light))
            );

            // Status dot (View thay vì ImageView)
            binding.statusIconHeader.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.success))
            );

            // Status text
            binding.tvIsOnline.setText(Constants.STATUS_ONLINE);
            binding.tvIsOnline.setTextColor(ContextCompat.getColor(this, R.color.success));

            // Ẩn warning card và hiển thị stats bình thường
            binding.offlineWarningCard.setVisibility(View.GONE);
            binding.statsCard.setAlpha(1.0f);

        } else {
            // Offline status - màu xám
            binding.statusBadgeHeader.setBackgroundResource(R.drawable.rounded_background);
            binding.statusBadgeHeader.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.surface_variant))
            );

            // Status dot
            binding.statusIconHeader.setBackgroundTintList(
                    ColorStateList.valueOf(ContextCompat.getColor(this, R.color.text_hint))
            );

            // Status text
            binding.tvIsOnline.setText(Constants.STATUS_OFFLINE);
            binding.tvIsOnline.setTextColor(ContextCompat.getColor(this, R.color.text_hint));

            // Hiện warning card và làm mờ stats
            binding.offlineWarningCard.setVisibility(View.VISIBLE);
            binding.statsCard.setAlpha(0.6f);
        }

//        if (isOnline) {
//            binding.statusBadgeHeader.setBackgroundColor(Color.parseColor("#D1FAE5")); // green-100
//            binding.statusIconHeader.setImageResource(android.R.drawable.presence_online);
//            binding.statusIconHeader.setImageTintList(
//                    android.content.res.ColorStateList.valueOf(Color.parseColor("#10B981"))
//            );
//            binding.tvIsOnline.setText(Constants.STATUS_ONLINE);
//            binding.tvIsOnline.setTextColor(Color.parseColor("#10B981"));
//            binding.offlineWarningCard.setVisibility(View.GONE);
//            binding.statsCard.setAlpha(1.0f);
//        } else {
//            binding.statusBadgeHeader.setBackgroundColor(Color.parseColor("#F1F5F9")); // slate-100
//            binding.statusIconHeader.setImageResource(android.R.drawable.presence_offline);
//            binding.statusIconHeader.setImageTintList(
//                    android.content.res.ColorStateList.valueOf(Color.parseColor("#94A3B8"))
//            );
//            binding.tvIsOnline.setText(Constants.STATUS_OFFLINE);
//            binding.tvIsOnline.setTextColor(Color.parseColor("#94A3B8"));
//            binding.offlineWarningCard.setVisibility(View.VISIBLE);
//            binding.statsCard.setAlpha(0.6f); // Dim stats when offline
//        }
    }

    private void setupSwipeRefresh(){
        binding.swipeRefresh.setOnRefreshListener(() -> {
            if (NetworkUtils.isNetworkAvailable(this)) {
                loadDeviceData();
            } else {
                binding.swipeRefresh.setRefreshing(false);
                showNetworkError();
            }
        });
        binding.swipeRefresh.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light
        );
    }

    private void updateUI(DeviceData data) {
        // Fill Level
        int fillLevel = data.getFillLevel();
        binding.tvFillLevel.setText(fillLevel + "%");
        binding.progressFillLevel.setProgress(fillLevel);

        int fillColor;
        if (fillLevel < Constants.FILL_LEVEL_LOW) {
            fillColor = Color.parseColor("#10B981"); // Green
        } else if (fillLevel < Constants.FILL_LEVEL_MEDIUM) {
            fillColor = Color.parseColor("#F59E0B"); // Orange
        } else {
            fillColor = Color.parseColor("#EF4444"); // Red
        }
        binding.progressFillLevel.setProgressTintList(android.content.res.ColorStateList.valueOf(fillColor));
        binding.tvFillLevel.setTextColor(fillColor);

        // Battery
        int battery = data.getBattery();
        binding.tvBattery.setText(battery + "%");

        int batteryColor;
        if (battery > Constants.BATTERY_MEDIUM) {
            batteryColor = Color.parseColor("#10B981");
        } else if (battery > Constants.BATTERY_LOW) {
            batteryColor = Color.parseColor("#F59E0B");
        } else {
            batteryColor = Color.parseColor("#EF4444");
        }
        binding.tvBattery.setTextColor(batteryColor);

        // Total Waste
        binding.tvTotalWaste.setText(String.valueOf(data.getTotalWaste()));

        // Status
        boolean isOnline = Constants.STATUS_ONLINE.equalsIgnoreCase(status);
        binding.tvStatus.setText(isOnline ? Constants.STATUS_ACTIVE : Constants.STATUS_INACTIVE);
        binding.tvStatus.setTextColor(isOnline ? Color.parseColor("#10B981") : Color.parseColor("#94A3B8"));
        binding.tvStatusSubtitle.setText(isOnline ? "Connected" : "Disconnected");

        // Waste Breakdown
        binding.tvRecycledCount.setText(String.valueOf(data.getRecycledCount()));
        binding.tvNonRecycledCount.setText(String.valueOf(data.getNonRecycledCount()));
        binding.tvComposableCount.setText(String.valueOf(data.getComposableCount()));
    }

    private void showNetworkError() {
        String networkType = NetworkUtils.getNetworkTypeName(this);
        String message = "No internet connection. Network: " + networkType;
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onResume(){
        super.onResume();
        handler.postDelayed(runnable, Constants.REFRESH_INTERVAL);
    }

    @Override
    protected void onPause(){
        super.onPause();
        handler.removeCallbacks(runnable);
    }
}
