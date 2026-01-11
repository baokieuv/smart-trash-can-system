package com.example.smart_bin.api;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.smart_bin.model.AuthResponse;
import com.example.smart_bin.model.User;
import com.example.smart_bin.utils.Constants;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AuthService {
    private static final String TAG = "AuthService";
    private static AuthService instance;
    private final ExecutorService executorService;
    private final Handler mainHandler;


    public interface AuthCallback {
        void onSuccess(AuthResponse response);
        void onError(String error);
    }

    public interface MessageCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    private AuthService() {
        executorService = Executors.newFixedThreadPool(2);
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized AuthService getInstance() {
        if (instance == null) {
            instance = new AuthService();
        }
        return instance;
    }

    public void register(String email, String password, String firstName, String lastName, MessageCallback callback){
        executorService.execute(() -> {
            try{
                URL url = new URL(Constants.BASE_URL + Constants.API_VERSION + "/auth/register");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setConnectTimeout(Constants.CONNECTION_TIMEOUT);
                connection.setReadTimeout(Constants.READ_TIMEOUT);
                connection.setDoOutput(true);

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("email", email);
                jsonBody.put("password", password);
                jsonBody.put("firstName", firstName);
                jsonBody.put("lastName", lastName);

                OutputStream os = connection.getOutputStream();
                os.write(jsonBody.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String message = jsonResponse.optString("message", "Registration successful");

                    mainHandler.post(() -> callback.onSuccess(message));
                }else{
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONObject errorResponse = new JSONObject(response.toString());
                    String error = errorResponse.optString("error", "Registration failed");

                    mainHandler.post(() -> callback.onError(error));
                }
                connection.disconnect();
            }catch (Exception e){
                Log.e(TAG, "Register error", e);
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }
        });
    }


    public void login(String email, String password, AuthCallback callback) {
        executorService.execute(() -> {
            try {
                URL url = new URL(Constants.BASE_URL + Constants.API_VERSION + "/auth/login");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setConnectTimeout(Constants.CONNECTION_TIMEOUT);
                connection.setReadTimeout(Constants.READ_TIMEOUT);
                connection.setDoOutput(true);

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("email", email);
                jsonBody.put("password", password);

                OutputStream os = connection.getOutputStream();
                os.write(jsonBody.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    AuthResponse authResponse = parseAuthResponse(response.toString());
                    mainHandler.post(() -> callback.onSuccess(authResponse));
                } else {
                    BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getErrorStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    reader.close();

                    JSONObject errorResponse = new JSONObject(response.toString());
                    String error = errorResponse.optString("error", "Login failed");

                    mainHandler.post(() -> callback.onError(error));
                }

                connection.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Login error", e);
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }
        });
    }

    public void resendVerification(String email, MessageCallback callback) {
        executorService.execute(() -> {
            try {
                URL url = new URL(Constants.BASE_URL + Constants.API_VERSION + "/auth/resend-verification");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setConnectTimeout(Constants.CONNECTION_TIMEOUT);
                connection.setReadTimeout(Constants.READ_TIMEOUT);
                connection.setDoOutput(true);

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("email", email);

                OutputStream os = connection.getOutputStream();
                os.write(jsonBody.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    mainHandler.post(() -> callback.onSuccess("Verification email sent successfully"));
                } else {
                    mainHandler.post(() -> callback.onError("Failed to resend verification email"));
                }

                connection.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Resend verification error", e);
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }
        });
    }

    private AuthResponse parseAuthResponse(String jsonString) throws Exception {
        JSONObject json = new JSONObject(jsonString);

        AuthResponse authResponse = new AuthResponse();
        authResponse.setAccessToken(json.getString("accessToken"));
        authResponse.setRefreshToken(json.getString("refreshToken"));
        authResponse.setExpiresIn(json.getLong("expiresIn"));
        authResponse.setTokenType(json.optString("tokenType", "Bearer"));

        JSONObject userJson = json.getJSONObject("user");
        User user = new User();
        user.setId(userJson.getString("id"));
        user.setEmail(userJson.getString("email"));
        user.setFirstName(userJson.getString("firstName"));
        user.setLastName(userJson.getString("lastName"));
        user.setEmailVerified(userJson.getBoolean("emailVerified"));
        user.setCreatedAt(userJson.getLong("createdAt"));

        authResponse.setUser(user);
        return authResponse;
    }

    public void shutdown() {
        executorService.shutdown();
    }
}
