package com.example.smart_bin.wifi;

public class WiFiConfiguration {
    private String ssid;
    private String password;

    public WiFiConfiguration(String ssid, String password) {
        this.ssid = ssid;
        this.password = password;
    }

    public String getSsid() {
        return ssid;
    }

    public void setSsid(String ssid) {
        this.ssid = ssid;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String toJsonString(){
        return "{\"ssid\":\"" + ssid + "\",\"password\":\"" + password + "\"}";
    }
}
