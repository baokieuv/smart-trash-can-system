package com.example.smart_bin.model;

public class DeviceData {
    private int fillLevel;
    private int battery;
    private int totalWaste;
    private int recycledCount;
    private int nonRecycledCount;
    private int composableCount;

    public DeviceData() {
    }

    public int getFillLevel() {
        return fillLevel;
    }

    public void setFillLevel(int fillLevel) {
        this.fillLevel = fillLevel;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public int getTotalWaste() {
        totalWaste = recycledCount + nonRecycledCount + composableCount;
        return totalWaste;
    }

    public void setTotalWaste(int totalWaste) {
        this.totalWaste = totalWaste;
    }

    public int getRecycledCount() {
        return recycledCount;
    }

    public void setRecycledCount(int recycledCount) {
        this.recycledCount = recycledCount;
    }

    public int getNonRecycledCount() {
        return nonRecycledCount;
    }

    public void setNonRecycledCount(int nonRecycledCount) {
        this.nonRecycledCount = nonRecycledCount;
    }

    public int getComposableCount() {
        return composableCount;
    }

    public void setComposableCount(int composableCount) {
        this.composableCount = composableCount;
    }
}
