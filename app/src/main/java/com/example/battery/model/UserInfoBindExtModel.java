package com.example.battery.model;

import java.util.List;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import lombok.Data;

/**
 * Created by Saturday on 2023/10/24.
 */
@Data
public class UserInfoBindExtModel {

    private int yearItemPosition;
    private int monthItemPosition;
    private int userProfessionalItemPosition;
    private int phoneUsageItemPosition;
    private int phoneBrandItemPosition;
    private int cancelBtnVisible;

    private List<String> yearList;
    private List<String> monthList;
    private List<String> userProfessionalList;
    private List<String> phoneUsageList;
    private List<String> phoneBrandList;
}
