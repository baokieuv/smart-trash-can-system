package com.example.smart_bin_server.dto;

public record SendDataRequest(
        int recycledWasteCount,
        int nonRecycledWasteCount,
        int compostableWasteCount,
        int fillLevel,
        boolean isFull
) {
}
