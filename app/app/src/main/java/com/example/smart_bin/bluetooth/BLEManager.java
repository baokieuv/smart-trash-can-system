package com.example.smart_bin.bluetooth;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.BluetoothLeScanner;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

@SuppressLint("MissingPermission") // Quyền đã được kiểm tra ở Activity
public class BLEManager {
    private static final String TAG = "BLEManager";

    private static final UUID SERVICE_UUID = UUID.fromString("000000ff-0000-1000-8000-00805f9b34fb");
    private static final UUID CHAR_UUID_SSID = UUID.fromString("0000ff01-0000-1000-8000-00805f9b34fb");
    private static final UUID CHAR_UUID_PASS = UUID.fromString("0000ff02-0000-1000-8000-00805f9b34fb");

    private final BluetoothAdapter bluetoothAdapter;
    private BluetoothLeScanner bluetoothLeScanner;
    private BluetoothGatt bluetoothGatt;
    private final Handler handler;
    private final Context context;

    private String pendingPassword = null;
    private BLECallback currentCallback;

    public interface BLECallback {
        void onDeviceFound(BluetoothDevice device, int rssi);
        void onScanStopped();
        void onConnected();
        void onDisconnected();
        void onDataSentSuccess();
        void onError(String error);
    }

    public BLEManager(Context context) {
        this.context = context;
        this.bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        this.handler = new Handler(Looper.getMainLooper());
    }

    public boolean isBluetoothEnabled() {
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
    }

    public void connectToDevice(BluetoothDevice device, BLECallback callback) {
        this.currentCallback = callback;
        if (bluetoothGatt != null) {
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
        // autoconnect = false để kết nối nhanh hơn
        bluetoothGatt = device.connectGatt(context, false, gattCallback);
    }

    public void disconnect() {
        if (bluetoothGatt != null) {
            bluetoothGatt.disconnect();
            bluetoothGatt.close();
            bluetoothGatt = null;
        }
    }

    public Set<BluetoothDevice> getPairedDevices(){
        if(bluetoothAdapter != null && bluetoothAdapter.isEnabled()){
            try{
                return bluetoothAdapter.getBondedDevices();
            }catch (SecurityException e){
                Log.e("My bluetooth", "Security exception:" + e.getMessage());
                return Collections.emptySet();
            }
        }
        return Collections.emptySet();
    }

    // Hàm gửi WiFi Credentials (SSID trước, sau đó Pass)
    public void sendWifiCredentials(String ssid, String password) {
        if (bluetoothGatt == null) {
            if (currentCallback != null) currentCallback.onError("Not connected");
            return;
        }

        BluetoothGattService service = bluetoothGatt.getService(SERVICE_UUID);
        if (service == null) {
            if (currentCallback != null) currentCallback.onError("Service not found. Is device paired?");
            return;
        }

        BluetoothGattCharacteristic ssidChar = service.getCharacteristic(CHAR_UUID_SSID);
        if (ssidChar == null) {
            if (currentCallback != null) currentCallback.onError("SSID Characteristic not found");
            return;
        }

        // Lưu password lại để gửi sau khi gửi SSID thành công
        this.pendingPassword = password;

        Log.d(TAG, "Writing SSID: " + ssid);
        ssidChar.setValue(ssid.getBytes(StandardCharsets.UTF_8));
        boolean success = bluetoothGatt.writeCharacteristic(ssidChar);

        if (!success && currentCallback != null) {
            currentCallback.onError("Failed to initiate write SSID");
        }
    }

    private final BluetoothGattCallback gattCallback = new BluetoothGattCallback() {
        @Override
        public void onConnectionStateChange(BluetoothGatt gatt, int status, int newState) {
            if (newState == BluetoothProfile.STATE_CONNECTED) {
                Log.d(TAG, "Connected to GATT server.");
                // Quan trọng: Phải Discover Service ngay sau khi Connect
                gatt.discoverServices();
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                Log.d(TAG, "Disconnected from GATT server.");
                if (currentCallback != null) currentCallback.onDisconnected();
                bluetoothGatt = null;
            }
        }

        @Override
        public void onServicesDiscovered(BluetoothGatt gatt, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.d(TAG, "Services discovered success");
                if (currentCallback != null) currentCallback.onConnected();
            } else {
                Log.w(TAG, "onServicesDiscovered received: " + status);
            }
        }

        @Override
        public void onCharacteristicWrite(BluetoothGatt gatt, BluetoothGattCharacteristic characteristic, int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                UUID uuid = characteristic.getUuid();

                // Nếu vừa ghi xong SSID -> Ghi tiếp Password
                if (uuid.equals(CHAR_UUID_SSID)) {
                    Log.d(TAG, "SSID written successfully. Writing Password...");
                    if (pendingPassword != null) {
                        BluetoothGattService service = gatt.getService(SERVICE_UUID);
                        if (service != null) {
                            BluetoothGattCharacteristic passChar = service.getCharacteristic(CHAR_UUID_PASS);
                            if (passChar != null) {
                                passChar.setValue(pendingPassword.getBytes(StandardCharsets.UTF_8));
                                gatt.writeCharacteristic(passChar);
                                pendingPassword = null; // Clear
                            }
                        }
                    }
                }
                // Nếu vừa ghi xong Password -> Hoàn tất
                else if (uuid.equals(CHAR_UUID_PASS)) {
                    Log.d(TAG, "Password written successfully.");
                    if (currentCallback != null) {

                        handler.post(currentCallback::onDataSentSuccess);
                    }
                }
            } else {

                if (status == BluetoothGatt.GATT_INSUFFICIENT_AUTHENTICATION || status == BluetoothGatt.GATT_INSUFFICIENT_ENCRYPTION) {
                    Log.e(TAG, "Authentication required. Pairing dialog should appear.");
                } else {
                    if (currentCallback != null) {
                        handler.post(() -> currentCallback.onError("Write failed, status: " + status));
                    }
                }
            }
        }
    };

    public void cleanup() {
        disconnect();
    }
}