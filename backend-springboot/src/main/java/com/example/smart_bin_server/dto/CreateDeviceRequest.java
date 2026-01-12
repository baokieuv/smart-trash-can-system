package com.example.smart_bin_server.dto;


import jakarta.validation.constraints.NotNull;

public record CreateDeviceRequest(

        @NotNull String macAddress,
        String name
) {
}
