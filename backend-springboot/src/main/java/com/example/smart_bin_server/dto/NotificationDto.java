package com.example.smart_bin_server.dto;

public record NotificationDto(
        Long id,
        String userId,
        String deviceId,
        String deviceName,
        String message,
        String type,
        Boolean isRead,
        Long timestamp
) {
}
