package com.example.smart_bin;

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
import com.example.smart_bin.model.DeviceData;
import com.example.smart_bin.utils.Constants;
import com.example.smart_bin.utils.NetworkUtils;

public class DeviceControlActivity extends AppCompatActivity {
    private static final String TAG = "DeviceControl";

    private ActivityDeviceControlBinding binding;
    private String deviceId;
    private String deviceName;
    private Handler handler;
    private Runnable runnable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeviceControlBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        deviceId = getIntent().getStringExtra("DEVICE_ID");
        deviceName = getIntent().getStringExtra("DEVICE_NAME");
        Log.i(TAG, "Device ID: " + deviceId);
        if (deviceId != null && deviceName != null) {
            setupToolbar();
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
                Toast.makeText(DeviceControlActivity.this, "Error: " + error, Toast.LENGTH_SHORT);
                Log.e("My bluetooth", "Error loading device data: " + error);
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
        // Device Name
        binding.tvDeviceName.setText(deviceName);
        binding.tvMacAddress.setText(deviceId);

        // Fill Level
        int fillLevel = data.getFillLevel();
        binding.tvFillLevel.setText(fillLevel + "%");
        binding.progressFillLevel.setProgress(fillLevel);

        int fillColor;
        if (fillLevel < 30) {
            fillColor = Color.parseColor("#4CAF50"); // Green
        } else if (fillLevel < 70) {
            fillColor = Color.parseColor("#FFA726"); // Orange
        } else {
            fillColor = Color.parseColor("#EF5350"); // Red
        }
        binding.progressFillLevel.setProgressTintList(android.content.res.ColorStateList.valueOf(fillColor));
        binding.tvFillLevel.setTextColor(fillColor);

        // Battery
        int battery = data.getBattery();
        binding.tvBattery.setText(battery + "%");

        // Change battery color
        int batteryColor;
        if (battery > 50) {
            batteryColor = Color.parseColor("#4CAF50");
        } else if (battery > 20) {
            batteryColor = Color.parseColor("#FFA726");
        } else {
            batteryColor = Color.parseColor("#EF5350");
        }
        binding.tvBattery.setTextColor(batteryColor);

        // Total Waste
        binding.tvTotalWaste.setText(String.valueOf(data.getTotalWaste()));

        // Status
        binding.tvStatus.setText(data.getStatus());
        binding.tvStatusSubtitle.setText("Connected");

        // Waste Breakdown
        binding.tvRecycledCount.setText(String.valueOf(data.getRecycledCount()));
        binding.tvNonRecycledCount.setText(String.valueOf(data.getNonRecycledCount()));
        binding.tvComposableCount.setText(String.valueOf(data.getComposableCount()));

        // Calculate percentages
        int total = data.getRecycledCount() + data.getNonRecycledCount() + data.getComposableCount();
        if (total > 0) {
            int recycledPercent = (data.getRecycledCount() * 100) / total;
            int nonRecycledPercent = (data.getNonRecycledCount() * 100) / total;
            int composablePercent = (data.getComposableCount() * 100) / total;

            binding.tvRecycledPercent.setText(recycledPercent + "%");
            binding.tvNonRecycledPercent.setText(nonRecycledPercent + "%");
            binding.tvComposablePercent.setText(composablePercent + "%");
        }
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
