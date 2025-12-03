package com.example.smart_bin;

import android.os.Bundle;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import com.example.smart_bin.databinding.ActivityDeviceControlBinding;
import com.example.smart_bin.model.Device;
import com.example.smart_bin.repository.DeviceManager;

public class DeviceControlActivity extends AppCompatActivity {
    private ActivityDeviceControlBinding binding;
    private Device device;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityDeviceControlBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        String deviceId = getIntent().getStringExtra("DEVICE_ID");
        Log.i("My bluetooth", "Device ID: " + deviceId);
        if (deviceId != null) {
            DeviceManager deviceManager = DeviceManager.getInstance(this);
            device = deviceManager.getDeviceById(deviceId);

            if (device != null) {
                binding.setDevice(device);
                setupToolbar();
            } else {
                finish();
            }
        } else {
            finish();
        }
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(device.getName());
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }
}
