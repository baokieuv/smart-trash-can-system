package com.example.smart_bin.api;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.smart_bin.model.Device;
import com.example.smart_bin.model.DeviceData;
import com.example.smart_bin.utils.Constants;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ApiService {
    private static final String TAG = "ApiService";
    private static ApiService instance;
    private final ExecutorService executorService;
    private final Handler mainHandler;

    public interface DevicesCallback{
        void onSuccess(List<Device> devices);
        void onError(String error);
    }

    public interface DeviceDataCallback{
        void onSuccess(DeviceData data);
        void onError(String error);
    }

    public interface DeviceCallback{
        void onSuccess(Device device);
        void onError(String error);
    }

    private ApiService(){
        executorService = Executors.newFixedThreadPool(3);
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized ApiService getInstance(){
        if(instance == null){
            instance = new ApiService();
        }
        return instance;
    }

    public void fetchDevices(DevicesCallback callback) {
        Log.i(TAG, "fetchDevices: Fetching devices...");
        executorService.execute(() -> {
            try {
                URL url = new URL(Constants.DEVICES_ENDPOINT);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(Constants.CONNECTION_TIMEOUT);
                connection.setReadTimeout(Constants.READ_TIMEOUT);

                int responseCode = connection.getResponseCode();
                if(responseCode == HttpURLConnection.HTTP_OK){
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream())
                    );
                    StringBuilder response = new StringBuilder();
                    String line;

                    while((line = reader.readLine()) != null){
                        response.append(line);
                    }
                    reader.close();

                    List<Device> devices = parseDevices(response.toString());
                    mainHandler.post(() -> callback.onSuccess(devices));
                }else{
                    Log.i(TAG, "fetchDevices: Failed to fetch devices" + responseCode);
                    mainHandler.post(() -> callback.onError("Failed to fetch devices" + responseCode));
                }

                connection.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Failed to fetch devices", e);
                mainHandler.post(() -> callback.onError("Failed to fetch devices " + e.getMessage()));
            }
        });
    }

    public void fetchDeviceData(String deviceId, DeviceDataCallback callback){
        executorService.execute(() -> {
           try{
               URL url = new URL(Constants.DEVICES_ENDPOINT + "/" + deviceId + "/data");
               HttpURLConnection connection = (HttpURLConnection) url.openConnection();
               connection.setRequestMethod("GET");
               connection.setConnectTimeout(Constants.CONNECTION_TIMEOUT);
               connection.setReadTimeout(Constants.READ_TIMEOUT);

               int responseCode = connection.getResponseCode();
               if(responseCode == HttpURLConnection.HTTP_OK){
                   BufferedReader reader = new BufferedReader(
                           new InputStreamReader(connection.getInputStream())
                   );
                   StringBuilder response = new StringBuilder();
                   String line;

                   while((line = reader.readLine()) != null){
                       response.append(line);
                   }
                   reader.close();

                   DeviceData data = parseDeviceData(response.toString());
                   mainHandler.post(() -> callback.onSuccess(data));
               }else{
                   mainHandler.post(() -> callback.onError("Failed to fetch device data " + responseCode));
               }

               connection.disconnect();
           }catch (Exception e){
               Log.e(TAG, "Failed to fetch device data", e);
               mainHandler.post(() -> callback.onError("Failed to fetch device data " + e.getMessage()));
           }
        });
    }

    public void createDevice(Device device, DeviceCallback callback){
        executorService.execute(() -> {
            try{
                URL url = new URL(Constants.DEVICES_ENDPOINT);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setConnectTimeout(Constants.CONNECTION_TIMEOUT);
                connection.setReadTimeout(Constants.READ_TIMEOUT);
                connection.setRequestProperty("Content-Type", "application/json");

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("macAddress", device.getMacAddress());
                jsonObject.put(Constants.KEY_NAME, device.getName());

                connection.setDoOutput(true);
                connection.getOutputStream().write(jsonObject.toString().getBytes());

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.i(TAG, "createDevice: Device created successfully");
                    mainHandler.post(() -> callback.onSuccess(device));
                } else {
                    Log.i(TAG, "createDevice: Failed to create device " + responseCode);
                    mainHandler.post(() -> callback.onError("Failed to create device " + responseCode));
                }

                connection.disconnect();
            }catch (Exception e){
                Log.e(TAG, "Failed to create device", e);
                mainHandler.post(() -> callback.onError("Failed to create device" + e.getMessage()));
            }
        });
    }

    public void getDevice(String deviceId, DeviceCallback callback){
        executorService.execute(() -> {
            try{
                URL url = new URL(Constants.DEVICES_ENDPOINT + "/" + deviceId);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(Constants.CONNECTION_TIMEOUT);
                connection.setReadTimeout(Constants.READ_TIMEOUT);

                int responseCode = connection.getResponseCode();
                if(responseCode == HttpURLConnection.HTTP_OK){
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream())
                    );
                    StringBuilder response = new StringBuilder();
                    String line;

                    while((line = reader.readLine()) != null){
                        response.append(line);
                    }
                    reader.close();

                    Device device = parseDevice(response.toString());

                    mainHandler.post(() -> callback.onSuccess(device));
                }else{
                    mainHandler.post(() -> callback.onError("Failed to get device " + responseCode));
                }
            }catch (Exception e){
                mainHandler.post(() -> callback.onError("Failed to get device " + e.getMessage()));
            }

        });
    }

    public void updateDevice(Device device, DeviceCallback callback){
        executorService.execute(() -> {
            try {
                URL url = new URL(Constants.DEVICES_ENDPOINT + "/" + device.getId());
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("PUT");
                connection.setConnectTimeout(Constants.CONNECTION_TIMEOUT);
                connection.setReadTimeout(Constants.READ_TIMEOUT);
                connection.setRequestProperty("Content-Type", "application/json");

                JSONObject jsonObject = new JSONObject();
                jsonObject.put(Constants.KEY_NAME, device.getName());

                connection.setDoOutput(true);
                connection.getOutputStream().write(jsonObject.toString().getBytes());

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.i(TAG, "updateDevice: Device updated successfully");
                    mainHandler.post(() -> callback.onSuccess(device));
                } else {
                    Log.i(TAG, "updateDevice: Failed to update device " + responseCode);
                    mainHandler.post(() -> callback.onError("Failed to update device " + responseCode));
                }

                connection.disconnect();
            }catch (Exception e){
                Log.e(TAG, "Failed to update device", e);
                mainHandler.post(() -> callback.onError("Failed to update device " + e.getMessage()));
            }
        });
    }

    public void deleteDevice(String deviceId, DeviceCallback callback) {
        executorService.execute(() -> {
            try{
                URL url = new URL(Constants.DEVICES_ENDPOINT + "/" + deviceId);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("DELETE");
                connection.setConnectTimeout(Constants.CONNECTION_TIMEOUT);
                connection.setReadTimeout(Constants.READ_TIMEOUT);

                int responseCode = connection.getResponseCode();
                if(responseCode == HttpURLConnection.HTTP_OK){
                    Log.i(TAG, "deleteDevice: Device deleted successfully");
                    mainHandler.post(() -> callback.onSuccess(null));
                }else{
                    Log.i(TAG, "deleteDevice: Failed to delete device " + responseCode);
                    mainHandler.post(() -> callback.onError("Failed to delete device " + responseCode));
                }

                connection.disconnect();
            }catch (Exception e){
                Log.e(TAG, "Failed to delete device", e);
                mainHandler.post(() -> callback.onError("Failed to delete device " + e.getMessage()));
            }
        });

    }
    private List<Device> parseDevices(String string) throws Exception {
        List<Device> devices = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(string);

        for(int i = 0; i < jsonArray.length(); i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Device device = new Device();
            device.setId(jsonObject.getString(Constants.KEY_ID));
            device.setName(jsonObject.optString(Constants.KEY_NAME, "Device " + (i + 1)));

            device.setMacAddress(jsonObject.getString(Constants.KEY_ID));
            device.setStatus(jsonObject.getString(Constants.KEY_STATUS));
            devices.add(device);
        }
        return devices;
    }

    private DeviceData parseDeviceData(String string) throws Exception{
        JSONObject obj = new JSONObject(string);
        DeviceData data = new DeviceData();

        data.setFillLevel(obj.optInt(Constants.KEY_FILL_LEVEL, 0));
        data.setBattery(obj.optInt(Constants.KEY_BATTERY, 50));
        data.setRecycledCount(obj.optInt(Constants.KEY_RECYCLED_COUNT, 0));
        data.setNonRecycledCount(obj.optInt(Constants.KEY_NON_RECYCLED_COUNT, 0));
        data.setComposableCount(obj.optInt(Constants.KEY_COMPOSABLE_COUNT, 0));
        data.setTotalWaste(data.getRecycledCount() + data.getNonRecycledCount() + data.getComposableCount());

        return data;
    }

    private Device parseDevice(String string) throws Exception{
        JSONObject obj = new JSONObject(string);
        Device device = new Device();
        device.setId(obj.optString(Constants.KEY_ID, null));
        device.setName(obj.optString(Constants.KEY_NAME, null));
        device.setStatus(obj.getString(Constants.KEY_STATUS));

        return device;
    }
    public void shutdown() {
        executorService.shutdown();
    }
}