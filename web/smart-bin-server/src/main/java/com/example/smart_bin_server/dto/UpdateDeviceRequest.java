package com.example.smart_bin_server.dto;

public record UpdateDeviceRequest(
        String name,
        boolean isOnline
) {
}
