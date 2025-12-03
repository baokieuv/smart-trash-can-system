package com.example.smart_bin_server.dto;

public record SendDataResponse(
        String deviceId,
        int code,
        String message
) {
}
