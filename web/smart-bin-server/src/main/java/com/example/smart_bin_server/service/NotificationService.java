package com.example.smart_bin_server.service;

import com.example.smart_bin_server.dto.NotificationDto;
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

    public List<NotificationDto> getNotifications() {
        Pageable pageable = PageRequest.of(
                0, 20, Sort.by(Sort.Direction.DESC, "id")
        );
        return repository.findAll(pageable).getContent()
                .stream()
                .map(this::parseToDto)
                .collect(Collectors.toList());
    }

    private NotificationDto parseToDto(Notification notification){
        Device device = deviceRepository.findById(notification.getDeviceId()).orElse(null);
        String deviceId = device == null ? "system" : device.getId();
        String deviceName = device == null ? "Unknown device" : device.getName();
        return new NotificationDto(notification.getId(), deviceId ,deviceName, notification.getMessage(), notification.getType(), notification.getTimestamp());
    }
}
