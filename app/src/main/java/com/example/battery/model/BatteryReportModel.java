package com.example.battery.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import lombok.Data;

/**
 * 上报数据model
 * Created by Saturday on 2023/4/8.
 */

@Data
public class BatteryReportModel implements Serializable {

    private String phoneUid;

    private String activationTime;

    private String phoneModel;

    private List<BatteryInfo> batteryInfoList;

    public BatteryReportModel() {
        this.batteryInfoList = new ArrayList<>();
    }
}
