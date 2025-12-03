package com.example.smart_bin_server.dto;

public record DeviceDto(
    String id,
    String name,
    boolean isOnline
) {}
