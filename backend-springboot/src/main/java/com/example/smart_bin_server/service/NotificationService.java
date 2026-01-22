package com.example.smart_bin_server.service;

import com.example.smart_bin_server.dto.NotificationDto;
import com.example.smart_bin_server.dto.UpdateNotiStatus;
import com.example.smart_bin_server.model.Notification;
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

    public NotificationService(NotificationRepository repository){
        this.repository = repository;
    }

    public void addNotification(Notification notification){
        parseToDto(repository.save(notification));
    }

    public List<NotificationDto> getNotificationsByUserId(String userId) {
        // Get all notifications
        Pageable pageable = PageRequest.of(
                0, 50, Sort.by(Sort.Direction.DESC, "id")
        );
        List<Notification> allNotifications = repository.findNotificationsByUserIdCustom(userId, pageable).getContent();

        return allNotifications.stream()
                .map(this::parseToDto)
                .collect(Collectors.toList());
    }

    public NotificationDto changeStatusNotification(Long notiId, UpdateNotiStatus request){
        Notification noti = repository.findById(notiId).orElseThrow(()
                -> new RuntimeException("Notification not found"));

        noti.setIsRead(request.status());

        return parseToDto(repository.save(noti));
    }

    private NotificationDto parseToDto(Notification notification){
        String deviceName = notification.getDeviceName() == null ? "Unknown device" : notification.getDeviceName();
        return new NotificationDto(
                notification.getId(),
                notification.getUserId(),
                notification.getDeviceId(),
                deviceName,
                notification.getMessage(),
                notification.getType(),
                notification.getIsRead(),
                notification.getTimestamp());
    }
}
