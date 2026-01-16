package com.example.smart_bin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smart_bin.api.DeviceService;
import com.example.smart_bin.bluetooth.BLEManager;
import com.example.smart_bin.databinding.ActivityAddDeviceBinding;
import com.example.smart_bin.model.Device;
import com.example.smart_bin.utils.Constants;
import com.example.smart_bin.wifi.WiFiScanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@SuppressLint("SetTextI18n")
public class AddDeviceActivity extends AppCompatActivity {
    private final String TAG = "AddDeviceActivity";

    private ActivityAddDeviceBinding binding;
    private BLEManager bleManager;
    private WiFiScanner wifiScanner;

    private String receivedMacAddress;
    private String receivedDeviceName;
    private List<String> availableNetworks = new ArrayList<>();
    private final List<BluetoothDevice> discoveredDevices = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        binding = ActivityAddDeviceBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        bleManager = new BLEManager(this);
        wifiScanner = new WiFiScanner(this);

        setupViews();
        checkPermissions();
        setupToolbar();
    }

    private void setupViews() {
        binding.btnScanBluetooth.setText("Select Paired Device");
        binding.btnScanBluetooth.setOnClickListener(v -> showPairedDevicesDialog());
        binding.btnScanWifi.setOnClickListener(v -> scanForWiFiNetworks());
        binding.btnConnect.setOnClickListener(v -> sendWiFiConfig());
    }

    private void checkPermissions() {
        List<String> permissions = new ArrayList<>();

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.S){
            if(checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.BLUETOOTH_SCAN);
            }
            if(checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED){
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
        }else {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH);
            }
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_ADMIN) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_ADMIN);
            }
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (!permissions.isEmpty()) {
            requestPermissions(permissions.toArray(new String[0]), Constants.REQUEST_BLUETOOTH_PERMISSIONS);
        }
    }

    private void showPairedDevicesDialog(){
        if(!bleManager.isBluetoothEnabled()){
            Toast.makeText(this, "Enabling Bluetooth...", Toast.LENGTH_SHORT).show();
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
            return;
        }

        Log.i(TAG, "Getting paired devices");
        Set<BluetoothDevice> pairedDevices = bleManager.getPairedDevices();
        discoveredDevices.clear();
        List<String> deviceNames = new ArrayList<>();

        if (!pairedDevices.isEmpty()) {
            for (BluetoothDevice device : pairedDevices) {
                try {
                    String name = device.getName();
                    String address = device.getAddress();
                    if (name == null || name.isEmpty()) name = "Unknown Device";

                    deviceNames.add(name + "\n" + address);
                    discoveredDevices.add(device);
                } catch (SecurityException e) {
                    // Permission not granted
                    Log.e(TAG, "Security exception: " + e.getMessage());
                }
            }
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Device");

        if (deviceNames.isEmpty()) {
            builder.setMessage("No paired devices found. Please pair your device in Settings first.");
        } else {
            Log.i("My bluetooth", "Showing paired devices dialog");
            builder.setItems(deviceNames.toArray(new String[0]), (dialog, which) ->
                    connectToDevice(discoveredDevices.get(which)));
        }

        builder.setNeutralButton("Pair New Device", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
            startActivity(intent);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void connectToDevice(BluetoothDevice device) {
        showStatusCard(true);
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvStatus.setText("Connecting to device...");

        // Get MAC address from BluetoothDevice
        try {
            receivedMacAddress = device.getAddress();
            receivedDeviceName = device.getName();
            if (receivedDeviceName == null || receivedDeviceName.isEmpty()) {
                receivedDeviceName = "Unknown Device";
            }
        } catch (SecurityException e) {
            Log.e(TAG, "Security exception: " + e.getMessage());
            receivedDeviceName = "Unknown Device";
        }

        Log.i(TAG, "Connecting to device: " + receivedDeviceName + " - " + receivedMacAddress);

        bleManager.connectToDevice(device, new BLEManager.BLECallback() {
            @Override
            public void onDeviceFound(BluetoothDevice device, int rssi) {

            }

            @Override
            public void onScanStopped() {

            }

            @Override
            public void onConnected() {
                runOnUiThread(() -> {
                    binding.tvMacAddress.setText("Connected: " + receivedMacAddress);
                    binding.tvMacAddress.setVisibility(View.VISIBLE);

                    binding.tvStatus.setText("✓ Connected successfully!");
                    binding.layoutWifiConfig.setVisibility(View.VISIBLE);
                    binding.progressBar.setVisibility(View.GONE);

                    binding.getRoot().postDelayed(() -> showStatusCard(false), 2000);
                });
            }

            @Override
            public void onDisconnected() {
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.tvStatus.setText("⚠️ Device disconnected");
                    Toast.makeText(AddDeviceActivity.this, "Device disconnected", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onDataSentSuccess() {
                runOnUiThread(() -> {
                    binding.tvStatus.setText("Received: " + Objects.requireNonNull(binding.etSsid.getText()));
                    Log.d(TAG, "Data received: " + binding.etSsid.getText().toString());
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.tvStatus.setText("❌ Error: " + error);
                    Toast.makeText(AddDeviceActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void scanForWiFiNetworks(){
        showStatusCard(true);
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvStatus.setText("Scanning for WiFi networks...");
        Log.i(TAG, "Scanning for WiFi networks...");

        wifiScanner.startScan(new WiFiScanner.ScanCallback() {
            @Override
            public void onScanCompleted(List<String> networks) {
                runOnUiThread(() -> {
                    availableNetworks = networks;
                    binding.progressBar.setVisibility(View.GONE);
                    binding.tvStatus.setText("✓ Found " + networks.size() + " networks");
                    Log.i(TAG, "Found " + networks.size() + " networks");
                    binding.getRoot().postDelayed(() -> {
                        showStatusCard(false);
                        showNetworkSelectionDialog();
                    }, 1000);
                });
            }

            @Override
            public void onScanFailed(String error) {
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.tvStatus.setText("❌ WiFi scan failed");
                    Toast.makeText(AddDeviceActivity.this, error, Toast.LENGTH_SHORT).show();
                    binding.getRoot().postDelayed(() -> showStatusCard(false), 2000);
                });
            }
        });
    }

    private void showNetworkSelectionDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select WiFi Network")
                .setItems(availableNetworks.toArray(new String[0]), (dialog, which) -> binding.etSsid.setText(availableNetworks.get(which)))
                .show();
    }

    private void sendWiFiConfig() {
        String ssid = Objects.requireNonNull(binding.etSsid.getText()).toString().trim();
        String password = Objects.requireNonNull(binding.etPassword.getText()).toString().trim();

        if (ssid.isEmpty()) {
            Toast.makeText(this, "Please select a WiFi network", Toast.LENGTH_SHORT).show();
            return;
        }

        if (password.isEmpty()) {
            Toast.makeText(this, "Please enter WiFi password", Toast.LENGTH_SHORT).show();
            return;
        }

        showStatusCard(true);
        binding.progressBar.setVisibility(View.VISIBLE);
        binding.tvStatus.setText("Sending WiFi configuration...");

//        WiFiConfiguration config = new WiFiConfiguration(ssid, password);
        bleManager.sendWifiCredentials(ssid, password);

        // Simulate successful configuration after 2 seconds
        binding.getRoot().postDelayed(() -> saveDevice(ssid), 2000);
    }

    private void saveDevice(String ssid){
        Device device = new Device();
        device.setName(receivedDeviceName);
        device.setMacAddress(receivedMacAddress);
        device.setStatus(Constants.STATUS_OFFLINE);

        DeviceService.getInstance().createDevice(device, new DeviceService.DeviceCallback() {
            @Override
            public void onSuccess(Device device) {
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.tvStatus.setText("✓ Device added successfully!");
                    Toast.makeText(AddDeviceActivity.this, "Device added!", Toast.LENGTH_SHORT).show();

                    binding.getRoot().postDelayed(() -> {
                        bleManager.disconnect();
                        finish();
                    }, 1500);
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    binding.progressBar.setVisibility(View.GONE);
                    binding.tvStatus.setText("⚠️ Warning: " + error);
                    Toast.makeText(AddDeviceActivity.this, "Error: " + error, Toast.LENGTH_SHORT).show();

                    // Still finish after error since device might be configured
                    binding.getRoot().postDelayed(() -> {
                        bleManager.disconnect();
                        finish();
                    }, 2000);
                });
            }
        });
    }

    private void showStatusCard(boolean show) {
        binding.statusCard.setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void setupToolbar() {
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Add Device");
        }

        binding.toolbar.setNavigationOnClickListener(v -> finish());
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == Constants.REQUEST_ENABLE_BT) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, "Bluetooth enabled", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, "Bluetooth is required", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        bleManager.cleanup();
    }
}
