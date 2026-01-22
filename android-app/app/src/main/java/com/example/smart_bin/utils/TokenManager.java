package com.example.smart_bin.utils;

import android.content.Context;
import android.content.SharedPreferences;

public class TokenManager {
    private static final String PREF_NAME = "smart_bin_prefs";
    private static final String KEY_ACCESS_TOKEN = "access_token";
    private static final String KEY_REFRESH_TOKEN = "refresh_token";
    private static final String KEY_TOKEN_EXPIRY = "token_expiry";
    private static final String KEY_USER_ID = "user_id";
    private static final String KEY_USER_EMAIL = "user_email";
    private static final String KEY_USER_FIRST_NAME = "user_first_name";
    private static final String KEY_USER_LAST_NAME = "user_last_name";
    private static final String KEY_EMAIL_VERIFIED = "email_verified";

    private static TokenManager instance;
    private final SharedPreferences prefs;

    // Buffer time before actual expiry to refresh token (1 minute)
    private static final long REFRESH_BUFFER_TIME = 60L * 1000L;

    private TokenManager(Context context) {
        prefs = context.getApplicationContext().getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized TokenManager getInstance(Context context) {
        if (instance == null) {
            instance = new TokenManager(context);
        }
        return instance;
    }

    public void saveAuthResponse(String accessToken, String refreshToken, long expiresIn,
                                 String userId, String email, String firstName, String lastName, boolean emailVerified) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_ACCESS_TOKEN, accessToken);
        editor.putString(KEY_REFRESH_TOKEN, refreshToken);

        // Calculate expiry time with buffer
        long expiryTime = System.currentTimeMillis() + (expiresIn * 1000) - REFRESH_BUFFER_TIME;
        editor.putLong(KEY_TOKEN_EXPIRY, expiryTime);

        editor.putString(KEY_USER_ID, userId);
        editor.putString(KEY_USER_EMAIL, email);
        editor.putString(KEY_USER_FIRST_NAME, firstName);
        editor.putString(KEY_USER_LAST_NAME, lastName);
        editor.putBoolean(KEY_EMAIL_VERIFIED, emailVerified);
        editor.apply();
    }

    public void updateAccessToken(String accessToken, long expiresIn) {
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString(KEY_ACCESS_TOKEN, accessToken);

        long expiryTime = System.currentTimeMillis() + (expiresIn * 1000) - REFRESH_BUFFER_TIME;
        editor.putLong(KEY_TOKEN_EXPIRY, expiryTime);

        editor.apply();
    }

    public void updateRefreshToken(String refreshToken) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, refreshToken).apply();
    }

    public String getAccessToken() {
        return prefs.getString(KEY_ACCESS_TOKEN, null);
    }

    public String getRefreshToken() {
        return prefs.getString(KEY_REFRESH_TOKEN, null);
    }

    public boolean isTokenValid() {
        long expiry = prefs.getLong(KEY_TOKEN_EXPIRY, 0);
        return System.currentTimeMillis() < expiry;
    }

    public boolean needsRefresh() {
        long expiry = prefs.getLong(KEY_TOKEN_EXPIRY, 0);
        // Check if token expires in less than 2 minutes
        return System.currentTimeMillis() + (2 * 60 * 1000) >= expiry;
    }

    public boolean isLoggedIn() {
        String accessToken = getAccessToken();
        String refreshToken = getRefreshToken();
        return accessToken != null && refreshToken != null;
    }

    public String getUserId() {
        return prefs.getString(KEY_USER_ID, null);
    }

    public String getUserEmail() {
        return prefs.getString(KEY_USER_EMAIL, null);
    }

    public String getUserFirstName() {
        return prefs.getString(KEY_USER_FIRST_NAME, null);
    }

    public String getUserLastName() {
        return prefs.getString(KEY_USER_LAST_NAME, null);
    }

    public boolean isEmailVerified() {
        return prefs.getBoolean(KEY_EMAIL_VERIFIED, false);
    }

    public String getUserFullName() {
        String firstName = getUserFirstName();
        String lastName = getUserLastName();
        if (firstName != null && lastName != null) {
            return firstName + " " + lastName;
        }
        return getUserEmail();
    }

    public void clearTokens() {
        SharedPreferences.Editor editor = prefs.edit();
        editor.remove(KEY_ACCESS_TOKEN);
        editor.remove(KEY_REFRESH_TOKEN);
        editor.remove(KEY_TOKEN_EXPIRY);
        editor.remove(KEY_USER_ID);
        editor.remove(KEY_USER_EMAIL);
        editor.remove(KEY_USER_FIRST_NAME);
        editor.remove(KEY_USER_LAST_NAME);
        editor.remove(KEY_EMAIL_VERIFIED);
        editor.apply();
    }
}