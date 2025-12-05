package com.example.smart_bin.model;


public class Device {
    private String id;
    private String name;
    private String macAddress;
    private boolean isOnline;

    public Device(){

    }

    public Device(String id, String name, String macAddress, boolean isOnline) {
        this.id = id;
        this.name = name;
        this.macAddress = macAddress;
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

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean online) {
        isOnline = online;
    }
}
