package com.example.smart_bin_server.dto;

public record NotificationDto(
        Long id,
        String deviceId,
        String deviceName,
        String message,
        String type,
        Long timestamp
) {
}
