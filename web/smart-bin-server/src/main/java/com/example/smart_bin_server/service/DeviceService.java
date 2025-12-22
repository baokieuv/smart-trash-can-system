package com.example.smart_bin_server.service;

import com.example.smart_bin_server.config.Constants;
import com.example.smart_bin_server.dto.CreateDeviceRequest;
import com.example.smart_bin_server.dto.DeviceDto;
import com.example.smart_bin_server.dto.UpdateDeviceRequest;
import com.example.smart_bin_server.mapper.DeviceMapper;
import com.example.smart_bin_server.model.Device;
import com.example.smart_bin_server.model.DeviceData;
import com.example.smart_bin_server.model.Log;
import com.example.smart_bin_server.repository.DeviceDataRepository;
import com.example.smart_bin_server.repository.DeviceRepository;
import okhttp3.*;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@EnableScheduling
public class DeviceService {
    private final DeviceRepository repository;
    private final DeviceMapper deviceMapper;
    private final DeviceDataRepository dataRepository;
    private final LogService logService;

    private final OkHttpClient client = new OkHttpClient();

    public DeviceService(DeviceRepository repository, DeviceMapper deviceMapper, DeviceDataRepository dataRepository, LogService logService){
        this.repository = repository;
        this.deviceMapper = deviceMapper;
        this.dataRepository = dataRepository;
        this.logService = logService;
    }

    @Transactional
    public DeviceDto createDevice(CreateDeviceRequest request){

        String id = request.macAddress().replace(":", "_");

        Device device = repository.findById(id).orElse(null);

        if(device != null){
            throw new RuntimeException("Device is existed");
        }

        device = new Device();
        device.setId(id);
        device.setName(Optional.ofNullable(request.name())
                .filter(n -> !n.isBlank())
                .orElse("Device" + request.macAddress()));
        device.setStatus(String.valueOf(Constants.DeviceStatus.OFFLINE));

        DeviceData data = new DeviceData();
        data.setDeviceId(device.getId());
        data.setFull(false);
        data.setFillLevel(0);
        data.setCompostableWasteCount(0);
        data.setRecycledWasteCount(0);
        data.setNonRecycledWasteCount(0);
        data.setTimestamp(System.currentTimeMillis());

        dataRepository.save(data);

        Log log = new Log();
        log.setDeviceId(device.getId());
        log.setMessage("Create device successfully.");
        log.setType(Constants.LogType.SUCCESS.toString());
        log.setTimestamp(System.currentTimeMillis());
        logService.addLog(log);

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

            device.setStatus(Optional.ofNullable(request.status())
                    .filter(n -> !n.isBlank())
                    .orElse(device.getStatus()));

            Log log = new Log();
            log.setDeviceId(deviceId);
            log.setMessage("Update device successfully.");
            log.setType(Constants.LogType.SUCCESS.toString());
            log.setTimestamp(System.currentTimeMillis());
            logService.addLog(log);
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

            Log log = new Log();
            log.setDeviceId(deviceId);
            log.setMessage("Delete device successfully.");
            log.setType(Constants.LogType.SUCCESS.toString());
            log.setTimestamp(System.currentTimeMillis());
            logService.addLog(log);
        } else {
            throw new RuntimeException("Device not found");
        }

        return deviceId;
    }

    @Scheduled(fixedRate = 10000)
    public void checkDevicesStatus(){
        List<Device> devices = repository.findByStatus(Constants.DeviceStatus.ONLINE.toString());
        long now = System.currentTimeMillis();

        for(Device d: devices){
            DeviceData data = dataRepository.findById(d.getId()).orElse(null);

            if(data == null) continue;

            if(now - data.getTimestamp() > Constants.TIMEOUT_MILLIS){

                Log log = new Log();
                log.setDeviceId(d.getId());
                log.setType(Constants.LogType.WARNING.toString());
                log.setMessage("Device disconnected.");
                log.setTimestamp(System.currentTimeMillis());

                logService.addLog(log);
                d.setStatus(String.valueOf(Constants.DeviceStatus.OFFLINE));
            }
        }
        repository.saveAll(devices);
    }
}
