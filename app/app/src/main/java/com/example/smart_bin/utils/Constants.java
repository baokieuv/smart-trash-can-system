package com.example.smart_bin.utils;

public class Constants {
    // API Configuration
    // TODO: Replace with your actual IP address
    public static final String BASE_URL = "http://kvbhust.site";
    public static final String API_VERSION = "/api/v1";
    public static final String DEVICES_ENDPOINT = BASE_URL + API_VERSION + "/devices";

    // Timing Configuration
    public static final int REFRESH_INTERVAL = 30000; // 30 seconds in milliseconds
    public static final int CONNECTION_TIMEOUT = 5000; // 5 seconds
    public static final int READ_TIMEOUT = 5000; // 5 seconds

    // Intent Keys
    public static final String EXTRA_DEVICE_ID = "DEVICE_ID";
    public static final String EXTRA_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRA_DEVICE_STATUS = "DEVICE_STATUS";


    // API Response Keys
    public static final String KEY_ID = "id";
    public static final String KEY_NAME = "name";
    public static final String KEY_FILL_LEVEL = "fillLevel";
    public static final String KEY_BATTERY = "battery";
    public static final String KEY_RECYCLED_COUNT = "recycledWasteCount";
    public static final String KEY_NON_RECYCLED_COUNT = "nonRecycledWasteCount";
    public static final String KEY_COMPOSABLE_COUNT = "compostableWasteCount";
    public static final String KEY_STATUS = "status";

    // Status Values
    public static final String STATUS_ONLINE = "on";
    public static final String STATUS_OFFLINE = "off";
    public static final String STATUS_ACTIVE = "Active";
    public static final String STATUS_INACTIVE = "Inactive";

    // Color Thresholds
    public static final int FILL_LEVEL_LOW = 30;
    public static final int FILL_LEVEL_MEDIUM = 70;
    public static final int BATTERY_LOW = 20;
    public static final int BATTERY_MEDIUM = 50;

    // Request Codes
    public static final int REQUEST_BLUETOOTH_PERMISSIONS = 1;
    public static final int REQUEST_ENABLE_BT = 2;
    public static final int REQUEST_LOCATION_PERMISSION = 3;

    // Private constructor to prevent instantiation
    private Constants() {
        throw new AssertionError("Cannot instantiate Constants class");
    }
}