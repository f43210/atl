package com.example.battery.model;

import lombok.Data;

/**
 * {"collectFrequency":20,"uploadFrequency":300,"isUploadOffLine":true,"maxOffLineQty":100}
 * Created by Saturday on 2023/4/9.
 */
@Data
public class ReportSetting {

    /**
     * 数据采集频率
     */
    private int collectFrequency = 20;

    /**
     * 上报频率
     */
    private int uploadFrequency = 300;

    /**
     * 是否进行离线上报数据缓存
     */
    private boolean isUploadOffLine = true;

    /**
     * 离线缓冲数据记录长度
     */
    private int maxOffLineQty = 100;

}
