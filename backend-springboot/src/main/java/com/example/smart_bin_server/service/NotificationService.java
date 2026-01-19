package com.example.smart_bin_server.service;

import com.example.smart_bin_server.dto.NotificationDto;
import com.example.smart_bin_server.dto.UpdateNotiStatus;
import com.example.smart_bin_server.model.Device;
import com.example.smart_bin_server.model.Notification;
import com.example.smart_bin_server.repository.DeviceRepository;
import com.example.smart_bin_server.repository.NotificationRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class NotificationService {
    private final NotificationRepository repository;
    private final DeviceRepository deviceRepository;

    public NotificationService(NotificationRepository repository, DeviceRepository deviceRepository){
        this.repository = repository;
        this.deviceRepository = deviceRepository;
    }

    public NotificationDto addNotification(Notification notification){
        return parseToDto(repository.save(notification));
    }

    public List<NotificationDto> getNotificationsByUserId(String userId) {
        // Get all devices owned by user
        List<Device> userDevices = deviceRepository.findByUserId(userId);

        // Get device IDs
        List<String> deviceIds = userDevices.stream()
                .map(Device::getId)
                .toList();

        // Get all notifications
        Pageable pageable = PageRequest.of(
                0, 50, Sort.by(Sort.Direction.DESC, "id")
        );
        List<Notification> allNotifications = repository.findAll(pageable).getContent();

        // Filter notifications for user's devices or system notifications
        return allNotifications.stream()
                .filter(notification -> {
                    String deviceId = notification.getDeviceId();
                    // Include system notifications or notifications for user's devices
                    return deviceId == null ||
                            "system".equals(deviceId) ||
                            deviceIds.contains(deviceId);
                })
                .map(this::parseToDto)
                .collect(Collectors.toList());
    }

    public NotificationDto changeStatusNotification(Long notiId, UpdateNotiStatus request){
        Notification noti = repository.findById(notiId).orElseThrow(()
                -> new RuntimeException("Notification not found"));

        noti.setIsRead(request.status());

        return parseToDto(repository.save(noti));
    }
//    public List<NotificationDto> getNotifications() {
//        Pageable pageable = PageRequest.of(
//                0, 20, Sort.by(Sort.Direction.DESC, "id")
//        );
//        return repository.findAll(pageable).getContent()
//                .stream()
//                .map(this::parseToDto)
//                .collect(Collectors.toList());
//    }

    private NotificationDto parseToDto(Notification notification){
//        Device device = deviceRepository.findById(notification.getDeviceId()).orElse(null);
//        String deviceId = device == null ? "system" : device.getId();
//        String deviceName = device == null ? "Unknown device" : device.getName();
        return new NotificationDto(
                notification.getId(),
                notification.getDeviceId(),
                notification.getDeviceName(),
                notification.getMessage(),
                notification.getType(),
                notification.getIsRead(),
                notification.getTimestamp());
    }
}
