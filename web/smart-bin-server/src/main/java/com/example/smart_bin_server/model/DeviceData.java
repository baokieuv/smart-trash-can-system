package com.example.smart_bin_server.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.*;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeviceData {
    @Id
    private String deviceId;
    private int recycledWasteCount;
    private int nonRecycledWasteCount;
    private int compostableWasteCount;
    private int fillLevel;
    private boolean isFull;
    private long timestamp;

    public String getDeviceId() {
        return deviceId;
    }

    public void setDeviceId(String deviceId) {
        this.deviceId = deviceId;
    }

    public int getRecycledWasteCount() {
        return recycledWasteCount;
    }

    public void setRecycledWasteCount(int recycledWasteCount) {
        this.recycledWasteCount = recycledWasteCount;
    }

    public int getNonRecycledWasteCount() {
        return nonRecycledWasteCount;
    }

    public void setNonRecycledWasteCount(int nonRecycledWasteCount) {
        this.nonRecycledWasteCount = nonRecycledWasteCount;
    }

    public int getCompostableWasteCount() {
        return compostableWasteCount;
    }

    public void setCompostableWasteCount(int compostableWasteCount) {
        this.compostableWasteCount = compostableWasteCount;
    }

    public int getFillLevel() {
        return fillLevel;
    }

    public void setFillLevel(int fillLevel) {
        this.fillLevel = fillLevel;
    }

    public boolean isFull() {
        return isFull;
    }

    public void setFull(boolean full) {
        isFull = full;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }
}
