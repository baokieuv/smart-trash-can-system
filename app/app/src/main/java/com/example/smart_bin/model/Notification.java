package com.example.smart_bin.model;

public class Notification {
    private Long id;

    private String deviceId;

    private String deviceName;

    private String message;

    private String type;

    private Long timestamp;

    public Notification(){

    }

    public Notification(Long id, String deviceId, String deviceName, String message, String type, Long timestamp) {
        this.id = id;
        this.deviceId = deviceId;
        this.deviceName = deviceName;
        this.message = message;
        this.type = type;
        this.timestamp = timestamp;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public String getDeviceName() {
        return deviceName;
    }

    public void setDeviceName(String deviceName) {
        this.deviceName = deviceName;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(Long timestamp) {
        this.timestamp = timestamp;
    }
}
