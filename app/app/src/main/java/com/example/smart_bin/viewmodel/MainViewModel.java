package com.example.smart_bin.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.smart_bin.model.Device;
import com.example.smart_bin.repository.DeviceManager;

import java.util.List;

public class MainViewModel extends AndroidViewModel {
    private final MutableLiveData<List<Device>> devicesLiveData;
    private final DeviceManager deviceManager;

    public MainViewModel(@NonNull Application application) {
        super(application);
        deviceManager = DeviceManager.getInstance(application);
        devicesLiveData = new MutableLiveData<>();
        loadDevices();
    }

    public LiveData<List<Device>> getDevices(){
        return devicesLiveData;
    }

    public void loadDevices() {
        List<Device> devices = deviceManager.getAllDevices();
        devicesLiveData.setValue(devices);
    }

    public void deleteDevice(String deviceId){
        deviceManager.deleteDevice(deviceId);
        loadDevices();
    }

    public void updateDevice(Device device){
        deviceManager.updateDevice(device);
        loadDevices();
    }

}
