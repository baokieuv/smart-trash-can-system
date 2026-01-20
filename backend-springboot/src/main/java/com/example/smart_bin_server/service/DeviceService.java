package com.example.smart_bin_server.service;

import com.example.smart_bin_server.config.Constants;
import com.example.smart_bin_server.dto.CreateDeviceRequest;
import com.example.smart_bin_server.dto.DeviceDto;
import com.example.smart_bin_server.dto.UpdateDeviceRequest;
import com.example.smart_bin_server.mapper.DeviceMapper;
import com.example.smart_bin_server.model.Device;
import com.example.smart_bin_server.model.DeviceData;
import com.example.smart_bin_server.model.Notification;
import com.example.smart_bin_server.repository.DeviceDataRepository;
import com.example.smart_bin_server.repository.DeviceRepository;
import okhttp3.*;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@EnableScheduling
public class DeviceService {
    private final DeviceRepository repository;
    private final DeviceMapper deviceMapper;
    private final DeviceDataRepository dataRepository;
    private final NotificationService notificationService;

    private final OkHttpClient client = new OkHttpClient();

    public DeviceService(DeviceRepository repository, DeviceMapper deviceMapper, DeviceDataRepository dataRepository, NotificationService notificationService){
        this.repository = repository;
        this.deviceMapper = deviceMapper;
        this.dataRepository = dataRepository;
        this.notificationService = notificationService;
    }

    @Transactional
    public DeviceDto createDevice(CreateDeviceRequest request, String userId){

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
        device.setUserId(userId);
        device.setCreatedAt(System.currentTimeMillis());
        device.setUpdatedAt(System.currentTimeMillis());

        DeviceData data = new DeviceData();
        data.setDeviceId(device.getId());
        data.setFull(false);
        data.setFillLevel(0);
        data.setCompostableWasteCount(0);
        data.setRecycledWasteCount(0);
        data.setNonRecycledWasteCount(0);
        data.setTimestamp(System.currentTimeMillis());

        dataRepository.save(data);

        Notification notification = new Notification();
        notification.setDeviceId(device.getId());
        notification.setDeviceName(device.getName());
        notification.setMessage("Create device successfully.");
        notification.setType(Constants.LogType.SUCCESS.toString());
        notification.setTimestamp(System.currentTimeMillis());
        notificationService.addNotification(notification);

        return deviceMapper.toDto(repository.save(device));
    }

    public List<DeviceDto> getDevices(String userId){
        List<Device> devices = repository.findByUserId(userId);

        return devices.stream()
                .map(deviceMapper::toDto)
                .toList();
    }

    public DeviceDto getDeviceById(String deviceId, String userId) {
        Device device = repository.findById(deviceId).orElse(null);

        if(device == null){
            throw new RuntimeException("Device not found");
        }

        if(!device.getUserId().equals(userId)){
            throw new RuntimeException("Unauthorized access to device");
        }
        return deviceMapper.toDto(device);
    }

    public DeviceDto updateDevice(String deviceId, UpdateDeviceRequest request, String userId) {
        Device device = repository.findById(deviceId).orElse(null);

        if(device == null){
            throw new RuntimeException("Device not found");
        }

        if(!device.getUserId().equals(userId)){
            throw new RuntimeException("Unauthorized access to device");
        }

        device.setName(Optional.ofNullable(request.name())
                .filter(n -> !n.isBlank())
                .orElse(device.getName()));

        device.setStatus(Optional.ofNullable(request.status())
                .filter(n -> !n.isBlank())
                .orElse(device.getStatus()));

        device.setUpdatedAt(System.currentTimeMillis());

        Notification notification = new Notification();
        notification.setDeviceId(deviceId);
        notification.setDeviceName(device.getName());
        notification.setMessage("Update device successfully.");
        notification.setType(Constants.LogType.SUCCESS.toString());
        notification.setTimestamp(System.currentTimeMillis());
        notificationService.addNotification(notification);

        return deviceMapper.toDto(repository.save(device));
    }

    public String deleteDevice(String deviceId, String userId) {
        Device device = repository.findById(deviceId).orElse(null);

        if (device == null) {
            throw new RuntimeException("Device not found");
        }

        if(!device.getUserId().equals(userId)){
            throw new RuntimeException("Unauthorized access to device");
        }

        repository.deleteById(deviceId);
        dataRepository.deleteById(deviceId);

        Notification notification = new Notification();
        notification.setDeviceId(deviceId);
        notification.setDeviceName(device.getName());
        notification.setMessage("Delete device successfully.");
        notification.setType(Constants.LogType.SUCCESS.toString());
        notification.setTimestamp(System.currentTimeMillis());
        notificationService.addNotification(notification);

        return deviceId;
    }

    @Scheduled(fixedRate = 2 * 60000)
    public void checkDevicesStatus(){
        List<Device> devices = repository.findByStatus(Constants.DeviceStatus.ONLINE.toString());
        long now = System.currentTimeMillis();

        for(Device d: devices){
            DeviceData data = dataRepository.findFirstByDeviceIdOrderByTimestampDesc(d.getId()).orElse(null);

            if(data == null) continue;

            if(now - data.getTimestamp() > Constants.TIMEOUT_MILLIS){

                Notification notification = new Notification();
                notification.setDeviceId(d.getId());
                notification.setDeviceName(d.getName());
                notification.setType(Constants.LogType.WARNING.toString());
                notification.setMessage("Device disconnected.");
                notification.setTimestamp(System.currentTimeMillis());

                notificationService.addNotification(notification);
                d.setStatus(String.valueOf(Constants.DeviceStatus.OFFLINE));
            }
        }
        repository.saveAll(devices);
    }
}
