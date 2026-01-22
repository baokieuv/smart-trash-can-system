package com.example.smart_bin.api;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.NonNull;

import com.example.smart_bin.model.AuthResponse;
import com.example.smart_bin.model.User;
import com.example.smart_bin.utils.Constants;
import com.example.smart_bin.utils.TokenManager;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
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
    private final Context context;

    public interface AuthCallback {
        void onSuccess(AuthResponse response);
        void onError(String error);
    }

    public interface MessageCallback {
        void onSuccess(String message);
        void onError(String error);
    }

    public interface RefreshCallback {
        void onSuccess();
        void onError(String error);
    }

    private AuthService(Context context) {
        this.context = context.getApplicationContext();
        executorService = Executors.newFixedThreadPool(2);
        mainHandler = new Handler(Looper.getMainLooper());
    }

    public static synchronized AuthService getInstance(Context context) {
        if (instance == null) {
            instance = new AuthService(context);
        }
        return instance;
    }

    public void register(String email, String password, String firstName, String lastName, MessageCallback callback) {
        executorService.execute(() -> {
            try {
                HttpURLConnection connection = getHttpURLConnection("/auth/register");
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
                    StringBuilder response = getMessage(connection.getInputStream());

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String message = jsonResponse.optString("message", "Registration successful");

                    mainHandler.post(() -> callback.onSuccess(message));
                } else {
                    StringBuilder response = getMessage(connection.getErrorStream());

                    JSONObject errorResponse = new JSONObject(response.toString());
                    String error = errorResponse.optString("error", "Registration failed");

                    mainHandler.post(() -> callback.onError(error));
                }

                connection.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Register error", e);
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }
        });
    }

    public void login(String email, String password, AuthCallback callback) {
        executorService.execute(() -> {
            try {
                HttpURLConnection connection = getHttpURLConnection("/auth/login");
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
                    StringBuilder response = getMessage(connection.getInputStream());

                    AuthResponse authResponse = parseAuthResponse(response.toString());
                    mainHandler.post(() -> callback.onSuccess(authResponse));
                } else {
                    StringBuilder response = getMessage(connection.getErrorStream());

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

    public void refreshToken(RefreshCallback callback) {
        executorService.execute(() -> {
            try {
                TokenManager tokenManager = TokenManager.getInstance(context);
                String refreshToken = tokenManager.getRefreshToken();

                if (refreshToken == null) {
                    mainHandler.post(() -> callback.onError("No refresh token available"));
                    return;
                }

                HttpURLConnection connection = getHttpURLConnection("/auth/refresh");
                connection.setDoOutput(true);

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("refreshToken", refreshToken);

                OutputStream os = connection.getOutputStream();
                os.write(jsonBody.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = connection.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    StringBuilder response = getMessage(connection.getInputStream());

                    AuthResponse authResponse = parseAuthResponse(response.toString());

                    // Update tokens in TokenManager
                    User user = authResponse.getUser();
                    tokenManager.saveAuthResponse(
                            authResponse.getAccessToken(),
                            authResponse.getRefreshToken(),
                            authResponse.getExpiresIn(),
                            user.getId(),
                            user.getEmail(),
                            user.getFirstName(),
                            user.getLastName(),
                            user.isEmailVerified()
                    );

                    mainHandler.post(callback::onSuccess);
                } else {
                    mainHandler.post(() -> callback.onError("Token refresh failed"));
                }

                connection.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Refresh token error", e);
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }
        });
    }

    public void logout(MessageCallback callback) {
        executorService.execute(() -> {
            try {
                TokenManager tokenManager = TokenManager.getInstance(context);
                String accessToken = tokenManager.getAccessToken();
                String refreshToken = tokenManager.getRefreshToken();

                if (accessToken == null) {
                    mainHandler.post(() -> {
                        tokenManager.clearTokens();
                        callback.onSuccess("Logged out");
                    });
                    return;
                }

                HttpURLConnection connection = getHttpURLConnection("/auth/logout");

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("refreshToken", refreshToken);

                OutputStream os = connection.getOutputStream();
                os.write(jsonBody.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = connection.getResponseCode();
                connection.disconnect();

                mainHandler.post(() -> {
                    tokenManager.clearTokens();
                    callback.onSuccess("Logged out successfully");
                });

            } catch (Exception e) {
                Log.e(TAG, "Logout error", e);
                mainHandler.post(() -> {
                    TokenManager.getInstance(context).clearTokens();
                    callback.onSuccess("Logged out");
                });
            }
        });
    }

    public void resendVerification(String email, MessageCallback callback) {
        executorService.execute(() -> {
            try {
                HttpURLConnection connection = getHttpURLConnection("/auth/resend-verification");
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

    public void changePassword(String currentPassword, String newPassword, String confirmPassword, MessageCallback callback) {
        executorService.execute(() -> {
            try {
                TokenManager tokenManager = TokenManager.getInstance(context);
                String accessToken = tokenManager.getAccessToken();

                if (accessToken == null) {
                    mainHandler.post(() -> callback.onError("Unauthorized"));
                    return;
                }


                URL url = new URL(Constants.BASE_URL + Constants.API_VERSION + "/auth/change-password");
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("POST");
                connection.setRequestProperty("Content-Type", "application/json");
                connection.setRequestProperty("Authorization", "Bearer " + accessToken);
                connection.setConnectTimeout(15000);
                connection.setReadTimeout(15000);
                connection.setDoOutput(true);

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("currentPassword", currentPassword);
                jsonBody.put("newPassword", newPassword);
                jsonBody.put("confirmPassword", confirmPassword);

                OutputStream os = connection.getOutputStream();
                os.write(jsonBody.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = connection.getResponseCode();

                if (responseCode == HttpURLConnection.HTTP_OK) {
                    mainHandler.post(() -> callback.onSuccess("Password changed successfully"));
                } else {
                    StringBuilder response = getMessage(connection.getErrorStream());

                    JSONObject errorResponse = new JSONObject(response.toString());
                    String error = errorResponse.optString("error", "Failed to change password");

                    mainHandler.post(() -> callback.onError(error));
                }

                connection.disconnect();
            } catch (Exception e) {
                Log.e(TAG, "Change password error", e);
                mainHandler.post(() -> callback.onError("Network error: " + e.getMessage()));
            }
        });
    }

    public void forgotPassword(String email, MessageCallback callback){
        executorService.execute(() -> {
            try{
                HttpURLConnection connection = getHttpURLConnection("/auth/forgot-password");
                connection.setDoOutput(true);

                JSONObject jsonBody = new JSONObject();
                jsonBody.put("email", email);

                OutputStream os = connection.getOutputStream();
                os.write(jsonBody.toString().getBytes());
                os.flush();
                os.close();

                int responseCode = connection.getResponseCode();

                if(responseCode == HttpURLConnection.HTTP_OK){
                    StringBuilder response = getMessage(connection.getInputStream());

                    JSONObject jsonResponse = new JSONObject(response.toString());
                    String message = jsonResponse.optString("message", "Password reset email sent");

                    mainHandler.post(() -> callback.onSuccess(message));
                }else{
                    StringBuilder response = getMessage(connection.getErrorStream());

                    JSONObject errorResponse = new JSONObject(response.toString());
                    String error = errorResponse.optString("error", "Password reset failed");

                    mainHandler.post(() -> callback.onError(error));
                }

                connection.disconnect();
            }catch (Exception e){
                Log.e(TAG, "Forgot password error", e);
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

    @NonNull
    private static HttpURLConnection getHttpURLConnection(String path) throws IOException {
        URL url = new URL(Constants.BASE_URL + Constants.API_VERSION + path);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("POST");
        connection.setRequestProperty("Content-Type", "application/json");
//                connection.setRequestProperty("Authorization", "Bearer " + accessToken);
        connection.setConnectTimeout(Constants.CONNECTION_TIMEOUT);
        connection.setReadTimeout(Constants.READ_TIMEOUT);
        return connection;
    }

    private static StringBuilder getMessage(InputStream inputStream) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
        StringBuilder response = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            response.append(line);
        }
        reader.close();
        return response;
    }

    public void shutdown() {
        executorService.shutdown();
    }
}