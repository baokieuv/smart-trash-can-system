package com.example.smart_bin_server.dto;

import jakarta.validation.constraints.NotNull;

public record SendDataRequest(
        @NotNull int recycledWasteCount,
        @NotNull int nonRecycledWasteCount,
        @NotNull int compostableWasteCount,
        @NotNull int fillLevel,
        @NotNull boolean isFull
) {
}
