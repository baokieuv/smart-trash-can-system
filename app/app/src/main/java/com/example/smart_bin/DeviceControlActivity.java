package com.example.smart_bin;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smart_bin.api.ApiService;
import com.example.smart_bin.databinding.ActivityDeviceControlBinding;
import com.example.smart_bin.model.Device;
import com.example.smart_bin.model.DeviceData;
import com.example.smart_bin.utils.Constants;
import com.example.smart_bin.utils.NetworkUtils;

@SuppressLint("SetTextI18n")
public class DeviceControlActivity extends AppCompatActivity {
    private static final String TAG = "DeviceControl";

    private ActivityDeviceControlBinding binding;
    private String deviceId;
    private String deviceName;
    private String status;
    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeviceControlBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        deviceId = getIntent().getStringExtra(Constants.EXTRA_DEVICE_ID);
        deviceName = getIntent().getStringExtra(Constants.EXTRA_DEVICE_NAME);
        status = getIntent().getStringExtra(Constants.EXTRA_DEVICE_STATUS);

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
                loadDeviceData();
                handler.postDelayed(this, Constants.REFRESH_INTERVAL);
            }
        };
    }

    private void loadDeviceData() {
        binding.progressBar.setVisibility(View.VISIBLE);

        ApiService.getInstance().fetchDeviceData(deviceId, new ApiService.DeviceDataCallback() {
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

        ApiService.getInstance().getDevice(deviceId, new ApiService.DeviceCallback() {
            @Override
            public void onSuccess(Device device) {
                status = device.getStatus();
                updateHeaderStatus();
            }

            @Override
            public void onError(String error) {
                Log.i("My bluetooth", "Error getting device: " + error);
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
    }

    private void updateHeaderStatus() {
        boolean isOnline = "ONLINE".equalsIgnoreCase(status) || "on".equalsIgnoreCase(status);

        if (isOnline) {
            binding.statusBadgeHeader.setBackgroundColor(Color.parseColor("#D1FAE5")); // green-100
            binding.statusIconHeader.setImageResource(android.R.drawable.presence_online);
            binding.statusIconHeader.setImageTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#10B981"))
            );
            binding.tvIsOnline.setText("ONLINE");
            binding.tvIsOnline.setTextColor(Color.parseColor("#10B981"));
        } else {
            binding.statusBadgeHeader.setBackgroundColor(Color.parseColor("#F1F5F9")); // slate-100
            binding.statusIconHeader.setImageResource(android.R.drawable.presence_offline);
            binding.statusIconHeader.setImageTintList(
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#94A3B8"))
            );
            binding.tvIsOnline.setText("OFFLINE");
            binding.tvIsOnline.setTextColor(Color.parseColor("#94A3B8"));
        }
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
        if (fillLevel < 30) {
            fillColor = Color.parseColor("#10B981"); // Green
        } else if (fillLevel < 70) {
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
        if (battery > 50) {
            batteryColor = Color.parseColor("#10B981");
        } else if (battery > 20) {
            batteryColor = Color.parseColor("#F59E0B");
        } else {
            batteryColor = Color.parseColor("#EF4444");
        }
        binding.tvBattery.setTextColor(batteryColor);

        // Total Waste
        binding.tvTotalWaste.setText(String.valueOf(data.getTotalWaste()));

        // Status
        boolean isOnline = "ONLINE".equalsIgnoreCase(status) || "on".equalsIgnoreCase(status);
        binding.tvStatus.setText(isOnline ? "Active" : "Inactive");
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
