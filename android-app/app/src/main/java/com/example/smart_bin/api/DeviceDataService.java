package com.example.smart_bin.api;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.smart_bin.LoginActivity;
import com.example.smart_bin.model.DeviceData;
import com.example.smart_bin.utils.Constants;
import com.example.smart_bin.utils.TokenManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

public class DeviceDataService {
    private static final String TAG = "DeviceDataService";
    private static DeviceDataService instance;
    private final ExecutorService executorService;
    private final Handler mainHandler;
    private final Context context;
    private final AtomicBoolean isRefreshing = new AtomicBoolean(false);

    public interface DeviceDataCallback{
        void onSuccess(DeviceData data);
        void onError(String error);
    }


    private DeviceDataService(Context context){
        this.context = context.getApplicationContext();
        executorService = Executors.newFixedThreadPool(3);
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized DeviceDataService getInstance(Context context){
        if(instance == null){
            instance = new DeviceDataService(context);
        }
        return instance;
    }

    public static synchronized DeviceDataService getInstance(){
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

        if (isRefreshing.get()) {
            // Wait for ongoing refresh
            mainHandler.postDelayed(() -> {
                if (tokenManager.isTokenValid()) {
                    onSuccess.run();
                } else {
                    onFailure.run();
                }
            }, 1000);
            return;
        }

        isRefreshing.set(true);
        AuthService.getInstance(context).refreshToken(new AuthService.RefreshCallback() {
            @Override
            public void onSuccess() {
                isRefreshing.set(false);
                onSuccess.run();
            }

            @Override
            public void onError(String error) {
                isRefreshing.set(false);
                tokenManager.clearTokens();

                Intent intent = new Intent(context, LoginActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                context.startActivity(intent);

                onFailure.run();
            }
        });
    }

    public void getDeviceData(String deviceId, DeviceDataCallback callback){
        refreshTokenIfNeeded(() -> executorService.execute(() -> {
            try{
                HttpURLConnection connection = createAuthenticatedConnection(
                        Constants.DEVICES_ENDPOINT + "/" + deviceId.replace(":", "_") + "/data",
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

                    DeviceData data = parseDeviceData(response.toString());
                    mainHandler.post(() -> callback.onSuccess(data));
                } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    handleUnauthorized();
                    mainHandler.post(() -> callback.onError("Session expired. Please login again."));
                } else{
                    mainHandler.post(() -> callback.onError("Failed to fetch device data " + responseCode));
                }

                connection.disconnect();
            }catch (Exception e){
                Log.e(TAG, "Failed to fetch device data", e);
                mainHandler.post(() -> callback.onError("Failed to fetch device data: " + e.getMessage()));
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
}
