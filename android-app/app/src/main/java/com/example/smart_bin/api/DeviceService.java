package com.example.smart_bin.api;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.smart_bin.LoginActivity;
import com.example.smart_bin.model.Device;
import com.example.smart_bin.utils.Constants;
import com.example.smart_bin.utils.TokenManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class DeviceService {
    private static final String TAG = "DeviceService";
    private static DeviceService instance;
    private final ExecutorService executorService;
    private final Handler mainHandler;
    private final Context context;
    private final AtomicBoolean isRefreshing = new AtomicBoolean(false);
    private final List<Runnable> pendingSuccessCallbacks = new ArrayList<>();
    private final List<Runnable> pendingFailureCallbacks = new ArrayList<>();


    public interface DevicesCallback{
        void onSuccess(List<Device> devices);
        void onError(String error);
    }

    public interface DeviceCallback{
        void onSuccess(Device device);
        void onError(String error);
    }

    private DeviceService(Context context){
        this.context = context.getApplicationContext();
        executorService = Executors.newFixedThreadPool(3);
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized DeviceService getInstance(Context context){
        if(instance == null){
            instance = new DeviceService(context);
        }
        return instance;
    }

    public static synchronized DeviceService getInstance(){
        if(instance == null){
            throw new IllegalStateException("DeviceService must be initialized with context first");
        }
        return instance;
    }

    private void refreshTokenIfNeeded(Runnable onSuccess, Runnable onFailure) {
        TokenManager tokenManager = TokenManager.getInstance(context);

        if (!tokenManager.needsRefresh()) {
            onSuccess.run();
            return;
        }

        synchronized (this){
            if (isRefreshing.get()) {
                pendingSuccessCallbacks.add(onSuccess);
                pendingFailureCallbacks.add(onFailure);
                return;
            }

            isRefreshing.set(true);
            pendingSuccessCallbacks.add(onSuccess);
            pendingFailureCallbacks.add(onFailure);
        }

        isRefreshing.set(true);
        AuthService.getInstance(context).refreshToken(new AuthService.RefreshCallback() {
            @Override
            public void onSuccess() {
                isRefreshing.set(false);
//                onSuccess.run();
                for (Runnable callback : pendingSuccessCallbacks) {
                    if (callback != null) callback.run();
                }
                pendingFailureCallbacks.clear();
                pendingSuccessCallbacks.clear();
            }

            @Override
            public void onError(String error) {
                Log.e(TAG, "Error: " + error);
                isRefreshing.set(false);
                tokenManager.clearTokens();

                Intent intent = new Intent(context, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);

//                onFailure.run();
                for (Runnable callback : pendingFailureCallbacks) {
                    if (callback != null) callback.run();
                }
                pendingFailureCallbacks.clear();
                pendingSuccessCallbacks.clear();
            }
        });
    }

    public void getDevices(DevicesCallback callback) {
        refreshTokenIfNeeded(() -> executorService.execute(() -> {
            try {
                HttpURLConnection connection = createAuthenticatedConnection(
                        Constants.DEVICES_ENDPOINT,
                        "GET"
                );

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
                } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    handleUnauthorized();
                    mainHandler.post(() -> callback.onError("Session expired. Please login again."));
                } else{
                    mainHandler.post(() -> callback.onError("Failed to fetch devices " + responseCode));
                }

                connection.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Failed to fetch devices", e);
                mainHandler.post(() -> callback.onError("Failed to fetch devices: " + e.getMessage()));
            }
        }), () -> mainHandler.post(() -> callback.onError("Authentication failed")));
    }

    public void createDevice(Device device, DeviceCallback callback){
        refreshTokenIfNeeded(() -> executorService.execute(() -> {
            try {
                HttpURLConnection connection = createAuthenticatedConnection(
                        Constants.DEVICES_ENDPOINT,
                        "POST"
                );

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("macAddress", device.getMacAddress());
                jsonObject.put(Constants.KEY_NAME, device.getName());

                connection.setDoOutput(true);
                connection.getOutputStream().write(jsonObject.toString().getBytes());

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.i(TAG, "createDevice: Device created successfully");
                    mainHandler.post(() -> callback.onSuccess(device));
                } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    mainHandler.post(() -> callback.onError("Unauthorized. Please login again."));
                } else {
                    Log.i(TAG, "createDevice: Failed to create device " + responseCode);
                    mainHandler.post(() -> {
                        try {
                            callback.onError("Failed to create device: " + connection.getResponseMessage());
                        } catch (IOException e) {
                            callback.onError("Failed to create device.");
                        }
                    });
                }

                connection.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Failed to create device", e);
                mainHandler.post(() -> callback.onError("Failed to create device: " + e.getMessage()));
            }
        }), () -> mainHandler.post(() -> callback.onError("Authentication failed")));
    }

    public void getDevice(String deviceId, DeviceCallback callback){
        refreshTokenIfNeeded(() -> executorService.execute(() -> {
            try {
                HttpURLConnection connection = createAuthenticatedConnection(
                        Constants.DEVICES_ENDPOINT + "/" + deviceId.replace(":", "_"),
                        "GET"
                );

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(
                            new InputStreamReader(connection.getInputStream())
                    );
                    StringBuilder response = new StringBuilder();
                    String line;

                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    Device device = parseDevice(response.toString());

                    mainHandler.post(() -> callback.onSuccess(device));
                } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    mainHandler.post(() -> callback.onError("Unauthorized. Please login again."));
                } else {
                    mainHandler.post(() -> callback.onError("Failed to get device " + responseCode));
                }
            } catch (Exception e) {
                mainHandler.post(() -> callback.onError("Failed to get device: " + e.getMessage()));
            }

        }), () -> mainHandler.post(() -> callback.onError("Authentication failed")));
    }

    public void updateDevice(Device device, DeviceCallback callback){
        refreshTokenIfNeeded(() -> executorService.execute(() -> {
            try {
                HttpURLConnection connection = createAuthenticatedConnection(
                        Constants.DEVICES_ENDPOINT + "/" + device.getId().replace(":", "_"),
                        "PUT"
                );

                JSONObject jsonObject = new JSONObject();
                jsonObject.put(Constants.KEY_NAME, device.getName());

                connection.setDoOutput(true);
                connection.getOutputStream().write(jsonObject.toString().getBytes());

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.i(TAG, "updateDevice: Device updated successfully");
                    mainHandler.post(() -> callback.onSuccess(device));
                } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    mainHandler.post(() -> callback.onError("Unauthorized. Please login again."));
                } else {
                    Log.i(TAG, "updateDevice: Failed to update device " + responseCode);
                    mainHandler.post(() -> callback.onError("Failed to update device " + responseCode));
                }

                connection.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Failed to update device", e);
                mainHandler.post(() -> callback.onError("Failed to update device: " + e.getMessage()));
            }
        }), () -> mainHandler.post(() -> callback.onError("Authentication failed")));
    }

    public void deleteDevice(String deviceId, DeviceCallback callback) {
        refreshTokenIfNeeded(() -> executorService.execute(() -> {
            try {
                HttpURLConnection connection = createAuthenticatedConnection(
                        Constants.DEVICES_ENDPOINT + "/" + deviceId.replace(":", "_"),
                        "DELETE"
                );

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    Log.i(TAG, "deleteDevice: Device deleted successfully");
                    mainHandler.post(() -> callback.onSuccess(null));
                } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    mainHandler.post(() -> callback.onError("Unauthorized. Please login again."));
                } else {
                    Log.i(TAG, "deleteDevice: Failed to delete device " + responseCode);
                    mainHandler.post(() -> callback.onError("Failed to delete device " + responseCode));
                }

                connection.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Failed to delete device", e);
                mainHandler.post(() -> callback.onError("Failed to delete device: " + e.getMessage()));
            }
        }), () -> mainHandler.post(() -> callback.onError("Authentication failed")));
    }

    private HttpURLConnection createAuthenticatedConnection(String endpoint, String method) throws Exception {
        TokenManager tokenManager = TokenManager.getInstance(context);
        String token = tokenManager.getAccessToken();

        if (token == null || !tokenManager.isLoggedIn()) {
            throw new Exception("Authentication required");
        }

        URL url = new URL(endpoint);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod(method);
        connection.setRequestProperty("Authorization", "Bearer " + token);
        connection.setRequestProperty("Content-Type", "application/json");
        connection.setConnectTimeout(Constants.CONNECTION_TIMEOUT);
        connection.setReadTimeout(Constants.READ_TIMEOUT);

        return connection;
    }

    private void handleUnauthorized() {
        TokenManager tokenManager = TokenManager.getInstance(context);
        tokenManager.clearTokens();

        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    private List<Device> parseDevices(String string) throws Exception {
        List<Device> devices = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(string);

        for(int i = 0; i < jsonArray.length(); i++){
            JSONObject jsonObject = jsonArray.getJSONObject(i);
            Device device = new Device();
            device.setId(jsonObject.getString(Constants.KEY_ID).replace("_", ":"));
            device.setName(jsonObject.optString(Constants.KEY_NAME, "Device " + (i + 1)));
            device.setMacAddress(jsonObject.getString(Constants.KEY_ID));
            device.setStatus(jsonObject.getString(Constants.KEY_STATUS));
            devices.add(device);
        }
        return devices;
    }


    private Device parseDevice(String string) throws Exception{
        JSONObject obj = new JSONObject(string);
        Device device = new Device();
        device.setId(obj.optString(Constants.KEY_ID, null).replace("_", ":"));
        device.setName(obj.optString(Constants.KEY_NAME, null));
        device.setStatus(obj.getString(Constants.KEY_STATUS));

        return device;
    }

    public void shutdown() {
        executorService.shutdown();
    }

}
