package com.example.smart_bin.api;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.smart_bin.LoginActivity;
import com.example.smart_bin.model.Notification;
import com.example.smart_bin.utils.Constants;
import com.example.smart_bin.utils.TokenManager;

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
import java.util.concurrent.atomic.AtomicBoolean;

public class NotificationService {
    private static final String TAG = "NotificationService";
    private static NotificationService instance;
    private final ExecutorService executorService;
    private final Handler mainHandler;
    private final Context context;
    private final AtomicBoolean isRefreshing = new AtomicBoolean(false);

    private final List<Runnable> pendingSuccessCallbacks = new ArrayList<>();
    private final List<Runnable> pendingFailureCallbacks = new ArrayList<>();


    public interface NotificationCallback {
        void onSuccess(List<Notification> notifications);
        void onError(String error);
    }

    private NotificationService(Context context){
        this.context = context.getApplicationContext();
        executorService = Executors.newFixedThreadPool(3);
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized NotificationService getInstance(Context context){
        if(instance == null){
            instance = new NotificationService(context);
        }
        return instance;
    }

    public static synchronized NotificationService getInstance(){
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
//        if (isRefreshing.get()) {
//            // Wait for ongoing refresh
//            mainHandler.postDelayed(() -> {
//                if (tokenManager.isTokenValid()) {
//                    onSuccess.run();
//                } else {
//                    onFailure.run();
//                }
//            }, 1000);
//            return;
//        }

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

    public void getNotifications(NotificationCallback callback){
        refreshTokenIfNeeded(() -> executorService.execute(() -> {
            try{
                HttpURLConnection connection = createAuthenticatedConnection(
                        Constants.BASE_URL + Constants.API_VERSION + "/notifications",
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

                    List<Notification> notifications = parseNotifications(response.toString());
                    mainHandler.post(() -> callback.onSuccess(notifications));
                } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    handleUnauthorized();
                    mainHandler.post(() -> callback.onError("Session expired. Please login again."));
                } else{
                    Log.e(TAG, "Failed to fetch notifications " + responseCode);
                    mainHandler.post(() -> callback.onError("Failed to fetch notifications " + responseCode));
                }
            }catch (Exception e){
                Log.e(TAG, "Failed to fetch notifications", e);
                mainHandler.post(() -> callback.onError("Failed to fetch notifications: " + e.getMessage()));
            }
        }), () -> mainHandler.post(() -> callback.onError("Authentication failed")));
    }

    public void updateNotificationStatus(Notification notification, NotificationCallback callback){
        refreshTokenIfNeeded(() -> executorService.execute(() -> {
            try{
                HttpURLConnection connection = createAuthenticatedConnection(
                        Constants.BASE_URL + Constants.API_VERSION + "/notifications/" + notification.getId(),
                        "PUT"
                );

                JSONObject jsonObject = new JSONObject();
                jsonObject.put("status", notification.getRead());

                connection.setDoOutput(true);
                connection.getOutputStream().write(jsonObject.toString().getBytes());

                int responseCode = connection.getResponseCode();
                if(responseCode == HttpURLConnection.HTTP_OK){
                    mainHandler.post(() -> callback.onSuccess(null));
                } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                    handleUnauthorized();
                    mainHandler.post(() -> callback.onError("Session expired. Please login again."));
                } else{
                    Log.e(TAG, "Failed to fetch notifications " + responseCode);
                    mainHandler.post(() -> callback.onError("Failed to fetch notifications " + responseCode));
                }
            }catch (Exception e){
                Log.e(TAG, "Failed to update notification status", e);
                mainHandler.post(() -> callback.onError("Failed to update notification status"));
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


    private List<Notification> parseNotifications(String data) throws Exception {
        List<Notification> notifications = new ArrayList<>();
        JSONArray jsonArray = new JSONArray(data);

        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject obj = jsonArray.getJSONObject(i);
            Notification notification = new Notification();

            notification.setId(obj.optLong("id"));
            notification.setDeviceId(obj.optString("deviceId"));
            notification.setDeviceName(obj.optString("deviceName", "Unknown Device"));
            notification.setMessage(obj.optString("message"));
            notification.setType(obj.optString("type", "INFO"));
            notification.setTimestamp(obj.optLong("timestamp"));
            notification.setRead(obj.optBoolean("isRead", false));

            notifications.add(notification);
        }

        return notifications;
    }
}
