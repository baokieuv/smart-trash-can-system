package com.example.smart_bin_server.service;

import com.example.smart_bin_server.dto.CreateDeviceRequest;
import com.example.smart_bin_server.dto.DeviceDto;
import com.example.smart_bin_server.dto.UpdateDeviceRequest;
import com.example.smart_bin_server.mapper.DeviceMapper;
import com.example.smart_bin_server.model.Device;
import com.example.smart_bin_server.model.DeviceData;
import com.example.smart_bin_server.repository.DeviceDataRepository;
import com.example.smart_bin_server.repository.DeviceRepository;
import okhttp3.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
public class DeviceService {
    private final DeviceRepository repository;
    private final DeviceMapper deviceMapper;
    private final DeviceDataRepository dataRepository;

    private final OkHttpClient client = new OkHttpClient();

    public DeviceService(DeviceRepository repository, DeviceMapper deviceMapper, DeviceDataRepository dataRepository){
        this.repository = repository;
        this.deviceMapper = deviceMapper;
        this.dataRepository = dataRepository;
    }

    @Transactional
    public DeviceDto createDevice(CreateDeviceRequest request){
        Device device = new Device();
        device.setId(request.macAddress().replace(":", "_"));
        device.setName(Optional.ofNullable(request.name())
                .filter(n -> !n.isBlank())
                .orElse("Device" + request.macAddress()));
        device.setOnline("off");

        DeviceData data = new DeviceData();
        data.setDeviceId(device.getId());
        data.setFull(false);
        data.setFillLevel(0);
        data.setCompostableWasteCount(0);
        data.setRecycledWasteCount(0);
        data.setNonRecycledWasteCount(0);
        data.setTimestamp(System.currentTimeMillis());

        dataRepository.save(data);

        return deviceMapper.toDto(repository.save(device));
    }

    public List<DeviceDto> getDevices(){
        List<Device> devices = repository.findAll();

        return devices.stream()
                .map(deviceMapper::toDto)
                .toList();
    }

    public DeviceDto getDeviceById(String deviceId) {
        Device device = repository.findById(deviceId).orElse(null);

        if(device == null){
            throw new RuntimeException("Device not found");
        }
        return deviceMapper.toDto(device);
    }

    public DeviceDto updateDevice(String deviceId, UpdateDeviceRequest request) {
        Device device = repository.findById(deviceId).orElse(null);

        if(device != null){
            device.setName(Optional.ofNullable(request.name())
                    .filter(n -> !n.isBlank())
                    .orElse(device.getName()));

            device.setOnline(Optional.ofNullable(request.isOnline())
                    .filter(n -> !n.isBlank())
                    .orElse(device.isOnline()));
        } else{
            throw new RuntimeException("Device not found");
        }

        return deviceMapper.toDto(repository.save(device));
    }

    public String deleteDevice(String deviceId) {
        Device device = repository.findById(deviceId).orElse(null);

        if (device != null) {
            repository.deleteById(deviceId);
            dataRepository.deleteById(deviceId);
        } else {
            throw new RuntimeException("Device not found");
        }

        return deviceId;
    }
}
