package com.example.smart_bin_server.service;

import com.example.smart_bin_server.dto.CreateDeviceRequest;
import com.example.smart_bin_server.dto.DeviceDto;
import com.example.smart_bin_server.dto.UpdateDeviceRequest;
import com.example.smart_bin_server.mapper.DeviceMapper;
import com.example.smart_bin_server.model.Device;
import com.example.smart_bin_server.repository.DeviceRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class DeviceService {
    private final DeviceRepository repository;
    private final DeviceMapper deviceMapper;

    public DeviceService(DeviceRepository repository, DeviceMapper deviceMapper){
        this.repository = repository;
        this.deviceMapper = deviceMapper;
    }

    public DeviceDto createDevice(CreateDeviceRequest request){
        Device device = new Device();
        device.setId(request.macAddress());
        device.setName(Optional.ofNullable(request.name())
                .filter(n -> !n.isBlank())
                .orElse("Device" + request.macAddress()));
        device.setOnline(false);

        return deviceMapper.toDto(repository.save(device));
    }

    public DeviceDto getDevice(String deviceId) {
        Device device = repository.findById(deviceId).orElse(null);

        if(device == null){
            throw new RuntimeException("Device not found");
        }
        return deviceMapper.toDto(device);
    }

    public String deleteDevice(String deviceId) {
        Device device = repository.findById(deviceId).orElse(null);

        if(device != null){
            repository.deleteById(deviceId);
        } else{
            throw new RuntimeException("Device not found");
        }

        return deviceId;
    }

    public DeviceDto updateDevice(String deviceId, UpdateDeviceRequest request) {
        Device device = repository.findById(deviceId).orElse(null);

        if(device != null){
            device.setName(request.name());
            device.setOnline(request.isOnline());
        } else{
            throw new RuntimeException("Device not found");
        }

        return deviceMapper.toDto(repository.save(device));
    }
}
