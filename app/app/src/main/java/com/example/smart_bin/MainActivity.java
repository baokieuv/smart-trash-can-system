package com.example.smart_bin;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.smart_bin.adapter.DeviceAdapter;
import com.example.smart_bin.api.ApiService;
import com.example.smart_bin.databinding.ActivityMainBinding;
import com.example.smart_bin.model.Device;
import com.example.smart_bin.utils.Constants;
import com.example.smart_bin.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class MainActivity extends AppCompatActivity implements DeviceAdapter.OnDeviceClickListener {
    private static final String TAG = "MainActivity";

    private ActivityMainBinding binding;
    private DeviceAdapter adapter;
    private Handler handler;
    private Runnable runnable;
    private List<Device> deviceList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Log.i(TAG, "onCreate: MainActivity");

        setupRecyclerView();
        setupFab();
        setupSwipeRefresh();
        setupAutoRefresh();

        if (NetworkUtils.isNetworkAvailable(this)) {
            loadDevices();
        } else {
            showNetworkError();
        }
    }

    private void setupRecyclerView() {
        adapter = new DeviceAdapter(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
    }

    private void setupFab() {
        binding.fabAdd.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddDeviceActivity.class);
            startActivity(intent);
        });
    }

    private void setupSwipeRefresh(){
        binding.swipeRefresh.setOnRefreshListener(() -> {
            if(NetworkUtils.isNetworkAvailable(this)){
                loadDevices();
            }else{
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

    private void setupAutoRefresh() {
        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                if(NetworkUtils.isNetworkAvailable(MainActivity.this)){
                    loadDevices();
                }
                handler.postDelayed(this, Constants.REFRESH_INTERVAL);
            }
        };
    }

    private void loadDevices() {
        Log.i(TAG, "loadDevices: Loading devices...");
//        viewModel.loadDevices();
        binding.progressBar.setVisibility(View.VISIBLE);
        ApiService.getInstance().fetchDevices(new ApiService.DevicesCallback() {
            @Override
            public void onSuccess(List<Device> devices) {
                deviceList = devices;
                adapter.setDevices(devices);
                updateStats(devices);
                binding.setHasDevices(devices != null && !devices.isEmpty());
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);

                Log.d(TAG, "Loaded " + Objects.requireNonNull(devices).size() + " devices");

                if(Objects.requireNonNull(devices).isEmpty()){
                    Toast.makeText(MainActivity.this, "No devices found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onError(String error) {
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading devices: " + error);
            }
        });
    }

    private void showNetworkError() {
        String networkType = NetworkUtils.getNetworkTypeName(this);
        String message = "No internet connection. Network: " + networkType;
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    private void updateStats(List<Device> devices) {
        int totalDevices = devices.size();
        int onlineDevices = 0;
        int offlineDevices = 0;

        for (Device device : devices) {
            if ("ONLINE".equalsIgnoreCase(device.getStatus()) ||
                    "on".equalsIgnoreCase(device.getStatus())) {
                onlineDevices++;
            } else {
                offlineDevices++;
            }
        }

        binding.tvTotalDevices.setText(String.valueOf(totalDevices));
        binding.tvOnlineDevices.setText(String.valueOf(onlineDevices));
        binding.tvOfflineDevices.setText(String.valueOf(offlineDevices));
    }

    @Override
    public void onDeviceClick(Device device) {
        Log.i(TAG, "Device clicked: " + device.getName());
        Intent intent = new Intent(this, DeviceControlActivity.class);
        intent.putExtra(Constants.EXTRA_DEVICE_ID, device.getId());
        intent.putExtra(Constants.EXTRA_DEVICE_NAME, device.getName());
        intent.putExtra(Constants.EXTRA_DEVICE_STATUS, device.getStatus());
        startActivity(intent);
    }

    @Override
    public void onDeviceLongClick(Device device) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Device Options")
                .setItems(new CharSequence[]{"Rename", "Delete"}, (dialog, which)->{
                    if(which == 0){
                        showRenameDialog(device);
                    }else{
                        showDeleteDialog(device);
                    }
                })
                .show();
    }

    private void showRenameDialog(Device device) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        final EditText input = new EditText(this);
        input.setText(device.getName());
        input.setPadding(50, 30, 50, 30);

        builder.setTitle("Rename Device")
                .setView(input)
                .setPositiveButton("Rename", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()){
                        device.setName(newName);
//                        viewModel.updateDevice(device);
                        ApiService.getInstance().updateDevice(device, new ApiService.DeviceCallback() {
                            @Override
                            public void onSuccess(Device device) {
                                Toast.makeText(MainActivity.this, "Device renamed", Toast.LENGTH_SHORT).show();
//                                deviceList.set(deviceList.indexOf(device), device);
//                                adapter.notifyItemChanged(deviceList.indexOf(device));
                                loadDevices();
                                Log.i(TAG, "Device renamed successfully");
                            }

                            @Override
                            public void onError(String error) {
                                Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Error renaming device:" + error);
                            }
                        });
                        Toast.makeText(this, "Device renamed", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteDialog(Device device) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Device")
                .setMessage("Are you sure you want to delete " + device.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
//                    viewModel.deleteDevice(device.getId());
                    ApiService.getInstance().deleteDevice(device.getId(), new ApiService.DeviceCallback() {
                        @Override
                        public void onSuccess(Device device) {
                            Toast.makeText(MainActivity.this, "Device deleted", Toast.LENGTH_SHORT).show();
//                            deviceList.remove(device);
//                            adapter.notifyDataSetChanged();
                            loadDevices();
                            Log.i(TAG, "Device deleted successfully");
                        }

                        @Override
                        public void onError(String error) {
                            Toast.makeText(MainActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Error deleting device: " + error);
                        }
                    });
                    Toast.makeText(this, "Device deleted", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onResume(){
        super.onResume();
//        viewModel.loadDevices();
        loadDevices();
        handler.postDelayed(runnable, Constants.REFRESH_INTERVAL);
    }

    @Override
    protected void onPause(){
        super.onPause();
        handler.removeCallbacks(runnable);
    }
}