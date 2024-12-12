package com.example.battery.service;

import com.example.battery.model.RespModel;
import com.example.battery.model.UserInfoModel;
import com.example.battery.service.callback.CommonCallback;

import java.util.List;

import androidx.annotation.NonNull;

/**
 * 用户信息收集模块相关信息服务
 * Created by Saturday on 2023/10/24.
 */

public interface UserInfoService {

    /**
     * 获取用户职业列表
     */
    void listUserProfessional(@NonNull CommonCallback<List<String>> cb);

    /**
     * 获取手机用途列表
     */
    void listPhoneUsage(@NonNull CommonCallback<List<String>> cb);

    /**
     * 获取手机品牌列表
     */
    void listPhoneBrand(@NonNull CommonCallback<List<String>> cb);

    /**
     * 提交用户信息
     */
    void submitUserInfo(@NonNull UserInfoModel userInfoModel,  @NonNull CommonCallback<RespModel> cb);
}
