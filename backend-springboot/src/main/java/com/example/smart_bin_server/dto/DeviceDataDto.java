package com.example.smart_bin_server.dto;

public record DeviceDataDto(
        Long id,
        String deviceId,
        int recycledWasteCount,
        int nonRecycledWasteCount,
        int compostableWasteCount,
        int fillLevel,
        boolean isFull,
        long timestamp
) {
}
