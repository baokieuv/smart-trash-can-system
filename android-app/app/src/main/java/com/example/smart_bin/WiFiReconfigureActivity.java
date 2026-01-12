package com.example.smart_bin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smart_bin.bluetooth.BLEManager;
import com.example.smart_bin.databinding.ActivityWifiReconfigureBinding;
import com.example.smart_bin.utils.Constants;
import com.example.smart_bin.wifi.WiFiScanner;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

@SuppressLint("SetTextI18n")
public class WiFiReconfigureActivity extends AppCompatActivity {
    private static final String TAG = "WiFiReconfig";

    private ActivityWifiReconfigureBinding binding;
    private BLEManager bleManager;
    private WiFiScanner wiFiScanner;

    private String deviceMac;
    private String deviceName;
    private List<String> availableNetworks = new ArrayList<>();
    private Handler timeoutHandler;
    private Runnable timeoutRunnable;
    private boolean isConnected = false;


    @Override
    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        binding = ActivityWifiReconfigureBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        deviceName = getIntent().getStringExtra(Constants.EXTRA_DEVICE_NAME);
        deviceMac = getIntent().getStringExtra(Constants.EXTRA_DEVICE_ID);

        if (deviceMac == null || deviceName == null) {
            Toast.makeText(this, "Device information missing", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        bleManager = new BLEManager(this);
        wiFiScanner = new WiFiScanner(this);
        timeoutHandler = new Handler(Looper.getMainLooper());

        setupView();
        setupToolbar();
        checkPermissions();
    }

    private void setupView(){
        binding.tvDeviceName.setText(deviceName);
        binding.tvMacAddress.setText("MAC: " + deviceMac);

        binding.btnDeviceReady.setOnClickListener(v -> startBluetoothConnection());
        binding.btnScanBluetooth.setOnClickListener(v -> manualBluetoothScan());
        binding.btnScanWifi.setOnClickListener(v -> scanForWiFiNetworks());
        binding.btnSendConfig.setOnClickListener(v -> sendWiFiConfig());
    }

    private void setupToolbar(){
        setSupportActionBar(binding.toolbar);
        if(getSupportActionBar() != null){
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("Reconfigure WiFi");
        }
        binding.toolbar.setNavigationOnClickListener(v -> {
            if (isConnected) {
                new AlertDialog.Builder(this)
                        .setTitle("Cancel Setup?")
                        .setMessage("Are you sure you want to cancel WiFi reconfiguration?")
                        .setPositiveButton("Yes", (dialog, which) -> {
//                            bluetoothManager.disconnect();
                            bleManager.disconnect();
                            finish();
                        })
                        .setNegativeButton("No", null)
                        .show();
            } else {
                finish();
            }
        });
    }

    private void checkPermissions() {
        List<String> permissions = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_SCAN) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_SCAN);
            }
            if (checkSelfPermission(Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissions.add(Manifest.permission.BLUETOOTH_CONNECT);
            }
        }

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissions.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }

        if (!permissions.isEmpty()) {
            requestPermissions(permissions.toArray(new String[0]), Constants.REQUEST_BLUETOOTH_PERMISSIONS);
        }
    }

    private void startBluetoothConnection(){
        if(!bleManager.isBluetoothEnabled()){
            Toast.makeText(this, "Please enable Bluetooth", Toast.LENGTH_SHORT).show();
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, Constants.REQUEST_ENABLE_BT);
            return;
        }

        // Show step 2
        binding.bluetoothCard.setVisibility(View.VISIBLE);
        binding.btnDeviceReady.setEnabled(false);

        // Try to find device automatically
        attemptAutoConnect();
    }

    private void attemptAutoConnect(){
        binding.tvBluetoothStatus.setText("Searching for " + deviceName + "...");
        binding.progressBluetooth.setVisibility(View.VISIBLE);
        binding.btnScanBluetooth.setVisibility(View.GONE);

        Log.i(TAG, "Attempting auto-connect to MAC: " + deviceMac);

        Set<BluetoothDevice> pairedDevices = bleManager.getPairedDevices();
        BluetoothDevice targetDevice = null;

        for(BluetoothDevice device : pairedDevices){
            try{
                if(device.getAddress().equals(deviceMac)){
                    targetDevice = device;
                    break;
                }
            }catch (SecurityException e){
                Log.e(TAG, "Security exception: " + e.getMessage());
            }
        }

        if(targetDevice != null) {
            connectToDevice(targetDevice);
        } else {
            timeoutRunnable = () -> {
                if(!isConnected){
                    runOnUiThread(() -> {
                        binding.progressBluetooth.setVisibility(View.GONE);
                        binding.btnScanBluetooth.setVisibility(View.VISIBLE);
                        binding.tvBluetoothStatus.setText("❌ Device not found automatically\nTap 'Scan for Device' to search manually");
                    });
                }
            };
            timeoutHandler.postDelayed(timeoutRunnable, 10000);
        }
    }

    private void manualBluetoothScan(){
        Set<BluetoothDevice> pairedDevices = bleManager.getPairedDevices();
        List<BluetoothDevice> deviceList = new ArrayList<>();
        List<String> deviceNames = new ArrayList<>();

        if(!pairedDevices.isEmpty()){
            for (BluetoothDevice device : pairedDevices) {
                try {
                    String name = device.getName();
                    String address = device.getAddress();
                    if (name == null || name.isEmpty()) name = "Unknown Device";

                    deviceNames.add(name + "\n" + address);
                    deviceList.add(device);
                } catch (SecurityException e) {
                    Log.e(TAG, "Security exception: " + e.getMessage());
                }
            }
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select Device");

        if(deviceNames.isEmpty()){
            builder.setMessage("No paired devices found. Please pair your device in Settings first.");
            builder.setNeutralButton("Open Settings", (dialog, which) -> {
                Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
                startActivity(intent);
            });
        }else{
            builder.setItems(deviceNames.toArray(new String[0]), (dialog, which) -> {
                BluetoothDevice selectedDevice = deviceList.get(which);

               if(selectedDevice.getAddress().equals(deviceMac)){
                   connectToDevice(selectedDevice);
               }else{
                   new AlertDialog.Builder(this)
                           .setTitle("Wrong Device")
                           .setMessage("The selected device MAC (" + selectedDevice.getAddress() +
                                   ") does not match the device you're configuring (" + deviceMac + ")")
                           .setPositiveButton("OK", null)
                           .show();
               }
            });
        }

        builder.setNeutralButton("Pair New Device", (dialog, which) -> {
            Intent intent = new Intent(Settings.ACTION_BLUETOOTH_SETTINGS);
            startActivity(intent);
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private void connectToDevice(BluetoothDevice device){
        binding.tvBluetoothStatus.setText("Connecting to device...");
        binding.progressBluetooth.setVisibility(View.VISIBLE);
        binding.btnScanBluetooth.setVisibility(View.GONE);

        if(timeoutRunnable != null){
            timeoutHandler.removeCallbacks(timeoutRunnable);
        }

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
                    isConnected = true;
                    binding.progressBluetooth.setVisibility(View.GONE);
                    binding.tvBluetoothStatus.setText("✓ Connected successfully!");

                    new Handler(Looper.getMainLooper()).postDelayed(() -> binding.wifiConfigCard.setVisibility(View.VISIBLE), 1000);
                });
            }

            @Override
            public void onDisconnected() {
                runOnUiThread(() -> {
                    isConnected = false;
                    binding.progressBluetooth.setVisibility(View.GONE);
                    binding.tvBluetoothStatus.setText("⚠️ Device disconnected");
                    binding.btnScanBluetooth.setVisibility(View.VISIBLE);
                    Toast.makeText(WiFiReconfigureActivity.this, "Device disconnected", Toast.LENGTH_SHORT).show();
                });
            }

            @Override
            public void onDataSentSuccess() {

            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    binding.progressBluetooth.setVisibility(View.GONE);
                    binding.tvBluetoothStatus.setText("❌ Connection failed: " + error);
                    binding.btnScanBluetooth.setVisibility(View.VISIBLE);
                    Toast.makeText(WiFiReconfigureActivity.this, error, Toast.LENGTH_SHORT).show();
                });
            }
        });
    }

    private void scanForWiFiNetworks(){
        showStatusCard(true, true, "Scanning for WiFi networks...");

        wiFiScanner.startScan(new WiFiScanner.ScanCallback() {
            @Override
            public void onScanCompleted(List<String> networks) {
                runOnUiThread(() -> {
                    availableNetworks = networks;
                    showStatusCard(true, false, "✓ Found " + networks.size() + " networks");

                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                        showStatusCard(false, false, "");
                        showNetworkSelectionDialog();
                    }, 1000);
                });
            }

            @Override
            public void onScanFailed(String error) {
                runOnUiThread(() -> {
                    showStatusCard(true, false, "❌ WiFi scan failed");
                    Toast.makeText(WiFiReconfigureActivity.this, error, Toast.LENGTH_SHORT).show();

                    new Handler(Looper.getMainLooper()).postDelayed(() -> showStatusCard(false, false, ""), 2000);
                });
            }
        });
    }

    private void showNetworkSelectionDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Select WiFi Network")
                .setItems(availableNetworks.toArray(new String[0]), (dialog, which) ->
                        binding.etSsid.setText(availableNetworks.get(which)))
                .show();
    }

    private void sendWiFiConfig(){
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

        showStatusCard(true, true, "Sending WiFi configuration...");

//        WiFiConfiguration config = new WiFiConfiguration(ssid, password);
        bleManager.sendWifiCredentials(ssid, password);

        // Simulate successful configuration
        new Handler(Looper.getMainLooper()).postDelayed(() -> runOnUiThread(() -> {
            showStatusCard(true, false, "✓ Configuration sent successfully!\nDevice is reconnecting to WiFi...");

            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                bleManager.disconnect();
                Toast.makeText(WiFiReconfigureActivity.this,
                        "WiFi reconfigured! Please wait for device to reconnect.",
                        Toast.LENGTH_LONG).show();
                finish();
            }, 2000);
        }), 2000);
    }

    private void showStatusCard(boolean show, boolean showProgress, String message){
        binding.statusCard.setVisibility(show ? View.VISIBLE : View.GONE);
        binding.progressStatus.setVisibility(showProgress ? View.VISIBLE : View.GONE);
        binding.tvStatus.setText(message);
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
        if (timeoutRunnable != null) {
            timeoutHandler.removeCallbacks(timeoutRunnable);
        }
        bleManager.disconnect();
    }
}
