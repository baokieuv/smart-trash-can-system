package com.example.smart_bin_server.config;

public class Constants {
    public enum DeviceStatus {
        ONLINE,
        OFFLINE
    }

    public enum LogType{
        SUCCESS,
        ERROR,
        WARNING,
        INFO
    }

    public static final long TIMEOUT_MILLIS = 60 * 1000; //60s
}
