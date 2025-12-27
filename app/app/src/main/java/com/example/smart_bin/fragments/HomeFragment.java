package com.example.smart_bin.fragments;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.smart_bin.DeviceControlActivity;
import com.example.smart_bin.adapter.DeviceAdapter;
import com.example.smart_bin.api.ApiService;
import com.example.smart_bin.databinding.FragmentHomeBinding;
import com.example.smart_bin.model.Device;
import com.example.smart_bin.utils.Constants;
import com.example.smart_bin.utils.NetworkUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class HomeFragment extends Fragment implements DeviceAdapter.OnDeviceClickListener {
    private static final String TAG = "HomeFragment";

    private FragmentHomeBinding binding;
    private DeviceAdapter adapter;
    private Handler handler;
    private Runnable runnable;
    private List<Device> deviceList = new ArrayList<>();

    private boolean autoRefresh;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        autoRefresh = SettingsFragment.isAutoRefreshEnabled(PreferenceManager.getDefaultSharedPreferences(requireContext()));

        setupRecyclerView();
        setupSwipeRefresh();
        setupAutoRefresh();

        if (NetworkUtils.isNetworkAvailable(requireContext())) {
            loadDevices();
        } else {
            showNetworkError();
        }
    }

    private void setupRecyclerView() {
        adapter = new DeviceAdapter(this);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.recyclerView.setAdapter(adapter);
    }

    private void setupSwipeRefresh() {
        binding.swipeRefresh.setOnRefreshListener(() -> {
            if (NetworkUtils.isNetworkAvailable(requireContext())) {
                loadDevices();
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

    private void setupAutoRefresh() {
        handler = new Handler(Looper.getMainLooper());
        runnable = new Runnable() {
            @Override
            public void run() {
                if (autoRefresh && NetworkUtils.isNetworkAvailable(requireContext())) {
                    loadDevices();
                }
                handler.postDelayed(this, Constants.REFRESH_INTERVAL);
            }
        };
    }

    private void loadDevices() {
        Log.i(TAG, "Loading devices...");
        binding.progressBar.setVisibility(View.VISIBLE);
        ApiService.getInstance().fetchDevices(new ApiService.DevicesCallback() {
            @Override
            public void onSuccess(List<Device> devices) {
                if (!isAdded()) return;

                deviceList = devices;
                adapter.setDevices(devices);
                updateStats(devices);
                binding.setHasDevices(!devices.isEmpty());
                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);

                Log.d(TAG, "Loaded " + Objects.requireNonNull(devices).size() + " devices");
            }

            @Override
            public void onError(String error) {
                if (!isAdded()) return;

                binding.progressBar.setVisibility(View.GONE);
                binding.swipeRefresh.setRefreshing(false);
                Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                Log.e(TAG, "Error loading devices: " + error);
            }
        });
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

    private void showNetworkError() {
        String networkType = NetworkUtils.getNetworkTypeName(requireContext());
        String message = "No internet connection. Network: " + networkType;
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

    @Override
    public void onDeviceClick(Device device) {
        Log.i(TAG, "Device clicked: " + device.getName());
        Intent intent = new Intent(requireContext(), DeviceControlActivity.class);
        intent.putExtra(Constants.EXTRA_DEVICE_ID, device.getId());
        intent.putExtra(Constants.EXTRA_DEVICE_NAME, device.getName());
        intent.putExtra(Constants.EXTRA_DEVICE_STATUS, device.getStatus());
        startActivity(intent);
    }

    @Override
    public void onDeviceLongClick(Device device) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Device Options")
                .setItems(new CharSequence[]{"Rename", "Delete"}, (dialog, which) -> {
                    if (which == 0) {
                        showRenameDialog(device);
                    } else {
                        showDeleteDialog(device);
                    }
                })
                .show();
    }

    private void showRenameDialog(Device device) {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        final EditText input = new EditText(requireContext());
        input.setText(device.getName());
        input.setPadding(50, 30, 50, 30);

        builder.setTitle("Rename Device")
                .setView(input)
                .setPositiveButton("Rename", (dialog, which) -> {
                    String newName = input.getText().toString().trim();
                    if (!newName.isEmpty()) {
                        device.setName(newName);
                        ApiService.getInstance().updateDevice(device, new ApiService.DeviceCallback() {
                            @Override
                            public void onSuccess(Device device) {
                                if (!isAdded()) return;
                                Toast.makeText(requireContext(), "Device renamed", Toast.LENGTH_SHORT).show();
                                loadDevices();
                                Log.i(TAG, "Device renamed successfully");
                            }

                            @Override
                            public void onError(String error) {
                                if (!isAdded()) return;
                                Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                                Log.e(TAG, "Error renaming device:" + error);
                            }
                        });
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showDeleteDialog(Device device) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Delete Device")
                .setMessage("Are you sure you want to delete " + device.getName() + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    ApiService.getInstance().deleteDevice(device.getId(), new ApiService.DeviceCallback() {
                        @Override
                        public void onSuccess(Device device) {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), "Device deleted", Toast.LENGTH_SHORT).show();
                            loadDevices();
                            Log.i(TAG, "Device deleted successfully");
                        }

                        @Override
                        public void onError(String error) {
                            if (!isAdded()) return;
                            Toast.makeText(requireContext(), "Error: " + error, Toast.LENGTH_SHORT).show();
                            Log.e(TAG, "Error deleting device: " + error);
                        }
                    });
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadDevices();
        handler.postDelayed(runnable, Constants.REFRESH_INTERVAL);
    }

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}