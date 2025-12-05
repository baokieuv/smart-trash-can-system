package com.example.smart_bin.bluetooth;

import android.Manifest;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;

import com.example.smart_bin.utils.Constants;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;

public class BluetoothManager {
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private final BluetoothAdapter bluetoothAdapter;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;
    private final Handler handler;

    public interface BluetoothCallback {
        void onConnected();
        void onDisconnected();
        void onDataReceived(String data);
        void onError(String error);
    }

    public BluetoothManager(Context context){
//        this.context = context;
        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        handler = new Handler(Looper.getMainLooper());
//        discoveredDevices = new ArrayList<>();
    }

    public boolean isBluetoothEnabled(){
        return bluetoothAdapter != null && bluetoothAdapter.isEnabled();
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

    public void connectToDevice(BluetoothDevice device, BluetoothCallback callback){
        new Thread(() -> {
           try{
               Log.i("My bluetooth", "Connecting to device");
               if (bluetoothAdapter.isDiscovering()) {
                   Log.i("My bluetooth", "Cancelling discovery");
                   bluetoothAdapter.cancelDiscovery();
               }
               socket = device.createRfcommSocketToServiceRecord(MY_UUID);
               socket.connect();
               outputStream = socket.getOutputStream();
               inputStream = socket.getInputStream();

               handler.post(callback::onConnected);

               listenForData(callback);
           }catch (IOException e){
               Log.i("My bluetooth", "Connection failed:" + e.getMessage());
               handler.post(() -> callback.onError("Connection failed: " + e.getMessage()));
           }
        }).start();
    }

    private void listenForData(BluetoothCallback callback) {
        new Thread(() -> {
            byte[] buffer = new byte[1024];
            int bytes;

            while(socket.isConnected()){
                try{
                    Log.i("My bluetooth", "Listening for data");
                    bytes = inputStream.read(buffer);
                    String data = new String(buffer, 0, bytes);
                    handler.post(() -> callback.onDataReceived(data));
                }catch (IOException e){
                    handler.post(callback::onDisconnected);
                }
            }
        }).start();
    }

    public void sendData(String data, BluetoothCallback callback){
        new Thread(() -> {
            try{
                if(outputStream != null){
                    outputStream.write(data.getBytes());
                    outputStream.flush();
                }
            }catch (IOException e){
                handler.post(() -> callback.onError("Failed to send data: " + e.getMessage()));
            }
        }).start();
    }

    public void disconnect(){
        try {
            if(socket != null){
                socket.close();
            }
        }catch (IOException e){
            e.printStackTrace();
        }
    }
}
