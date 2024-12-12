package com.example.battery.model;

import java.io.Serializable;

import androidx.annotation.NonNull;
import lombok.Data;

@Data
public class BatteryInfo implements Cloneable, Serializable {

    private String ts;

    private String batteryStatus;

    private String charging;

    private double current;

    private double electricity;

    private double capacity;

    private double voltage;

    private double batteryLevel;

    private double temperature;

    private String batteryType;

    private String batteryHealth;

    private String longitude;

    private String latitude;

    private double memoryUsage;

//    private double cpuUsage;

    @NonNull
    @Override
    public BatteryInfo clone() throws CloneNotSupportedException {
        return (BatteryInfo) super.clone();
    }
}