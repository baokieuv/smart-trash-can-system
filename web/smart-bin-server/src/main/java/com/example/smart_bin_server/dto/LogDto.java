package com.example.smart_bin_server.dto;

public record LogDto(
        Long id,
        String deviceName,
        String message,
        String type,
        Long timestamp
) {
}
