package com.example.battery.enums;

/**
 * Created by Saturday on 2024/6/3.
 */

public enum ReportConfigEnum {

    LEVEL_1(1, 5, 5, 60),
    LEVEL_2(0, 1, 1, 5),
    LEVEL_NORMAL(5, 500, -1, -1),
    ;

    private final int minVoltage;
    private final int maxVoltage;
    private final int collectFrequency;
    private final int uploadFrequency;

    ReportConfigEnum(int minVoltage, int maxVoltage, int collectFrequency, int uploadFrequency) {
        this.collectFrequency = collectFrequency;
        this.minVoltage = minVoltage;
        this.maxVoltage =maxVoltage;
        this.uploadFrequency = uploadFrequency;
    }

    public int getCollectFrequency() {
        return collectFrequency;
    }

    public int getMaxVoltage() {
        return maxVoltage;
    }

    public int getMinVoltage() {
        return minVoltage;
    }

    public int getUploadFrequency() {
        return uploadFrequency;
    }
}
