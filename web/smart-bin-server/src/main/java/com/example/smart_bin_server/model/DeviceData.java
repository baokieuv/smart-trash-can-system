package com.example.smart_bin_server.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceData {
    private String deviceId;
    private int recycledWasteCount;
    private int nonRecycledWasteCount;
    private int compostableWasteCount;
    private int fillLevel;
    private boolean isFull;
}
