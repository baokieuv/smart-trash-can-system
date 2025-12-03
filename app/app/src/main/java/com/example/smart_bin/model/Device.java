package com.example.smart_bin.model;


public class Device {
    private String id;
    private String name;
    private String macAddress;
    private String imageUrl;
    private boolean isOnline;

    public Device(){

    }

    public Device(String id, String name, String macAddress, String imageUrl, boolean isOnline) {
        this.id = id;
        this.name = name;
        this.macAddress = macAddress;
        this.imageUrl = imageUrl;
        this.isOnline = isOnline;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMacAddress() {
        return macAddress;
    }

    public void setMacAddress(String macAddress) {
        this.macAddress = macAddress;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }
}
