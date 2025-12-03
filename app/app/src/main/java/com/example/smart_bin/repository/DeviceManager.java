package com.example.smart_bin.repository;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.smart_bin.model.Device;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DeviceManager {
    private static final String PREF_NAME = "DevicePrefs";
    private static final String KEY_DEVICES = "devices";
    private static DeviceManager instance;
    private final SharedPreferences sharedPreferences;
    private final Gson gson;

    public DeviceManager(Context context){
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        gson = new Gson();
    }

    public static synchronized DeviceManager getInstance(Context context){
        if (instance == null){
            instance = new DeviceManager(context.getApplicationContext());
        }
        return instance;
    }

    public List<Device> getAllDevices(){
        String json = sharedPreferences.getString(KEY_DEVICES, null);
        if(json == null){
            return new ArrayList<>();
        }
        Type type = new TypeToken<ArrayList<Device>>() {}.getType();
        return gson.fromJson(json, type);
    }

    public void saveDevice(Device device){
        List<Device> devices = getAllDevices();
        if(device.getId() == null || device.getId().isEmpty()){
            device.setId(UUID.randomUUID().toString());
        }

        if(isDeviceExist(device.getMacAddress())){
            return;
        }
        devices.add(device);
        saveDevices(devices);
    }

    public void updateDevice(Device device){
        List<Device> devices = getAllDevices();
        for (int i = 0; i < devices.size(); i++){
            if(devices.get(i).getId().equals(device.getId())){
                devices.set(i, device);
                break;
            }
        }
        saveDevices(devices);
    }

    public void deleteDevice(String deviceId){
        List<Device> devices = getAllDevices();
        devices.removeIf(device -> device.getId().equals(deviceId));
        saveDevices(devices);
    }

    private void saveDevices(List<Device> devices) {
        String json = gson.toJson(devices);
        sharedPreferences.edit().putString(KEY_DEVICES, json).apply();
    }

    public Device getDeviceById(String deviceId){
        List<Device> devices = getAllDevices();
        for (Device device : devices){
            if(device.getId().equals(deviceId)) return device;
        }
        return null;
    }

    private boolean isDeviceExist(String macAddress){
        List<Device> devices = getAllDevices();

        Device device = devices.stream().filter(d -> d.getMacAddress().equals(macAddress)).findFirst().orElse(null);
        return device != null;
    }
}
