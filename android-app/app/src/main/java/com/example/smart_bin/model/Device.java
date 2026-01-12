package com.example.smart_bin.model;


public class Device {
    private String id;
    private String name;
    private String macAddress;
    private String status;

    public Device(){

    }

    public Device(String id, String name, String macAddress, String status) {
        this.id = id;
        this.name = name;
        this.macAddress = macAddress;
        this.status = status;
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
