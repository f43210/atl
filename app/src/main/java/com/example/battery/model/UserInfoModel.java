package com.example.battery.model;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

/**
 * Created by Saturday on 2023/10/24.
 */
@Data
public class UserInfoModel {

    private int yearData;
    private int monthData;
    private String professionalSelect;
    private String professionalValue = "";
    private String usageSelect;
    private String usageValue = "";
    private String brandSelect;
    private String brandValue = "";
    private boolean isChangeBattery;
    private String uId;

    public boolean getIsChangeBattery() {
        return this.isChangeBattery;
    }

    public void setIsChangeBattery(boolean isChangeBattery){
        this.isChangeBattery = isChangeBattery;
    }
}
