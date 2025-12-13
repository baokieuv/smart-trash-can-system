package com.example.smart_bin;

import android.Manifest;
import android.annotation.SuppressLint;
import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import com.example.smart_bin.bluetooth.BluetoothManager;
import com.example.smart_bin.databinding.ActivityWifiReconfigureBinding;
import com.example.smart_bin.utils.Constants;
import com.example.smart_bin.wifi.WiFiScanner;

import java.util.ArrayList;
import java.util.List;

@SuppressLint("SetTextI18n")
public class WiFiReconfigureActivity extends AppCompatActivity {
    private static final String TAG = "WiFiReconfig";

    private ActivityWifiReconfigureBinding binding;
    private BluetoothManager bluetoothManager;
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

        bluetoothManager = new BluetoothManager(this);
        wiFiScanner = new WiFiScanner(this);

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
                            bluetoothManager.disconnect();
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

    }

    private void attemptAutoConnect(){

    }

    private void manualBluetoothScan(){

    }

    private void connectToDevice(BluetoothDevice device){

    }

    private void scanForWiFiNetworks(){

    }

    private void showNetworkSelectionDialog(){

    }

    private void sendWiFiConfig(){

    }

    private void showStatusCard(boolean show, boolean showProgress, String message){

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
        bluetoothManager.disconnect();
    }
}
