package com.example.battery;

import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.provider.Settings;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import com.example.battery.databinding.ActivityUserInfoBinding;
import com.example.battery.model.UserInfoBindExtModel;
import com.example.battery.model.UserInfoModel;
import com.example.battery.service.UserInfoService;
import com.example.battery.service.impl.UserInfoServiceImpl;
import com.example.battery.util.CommonCacheUtil;
import com.example.battery.util.FirstLaunchCacheUtil;
import com.google.gson.Gson;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

/**
 * Created by Saturday on 2023/10/24.
 */

public class UserInfoActivity extends AppCompatActivity {

    ActivityUserInfoBinding binding;
    UserInfoBindExtModel userInfoBindExtModel;
    UserInfoModel userInfoModel;
    UserInfoService userInfoService;

    private final Gson gson =new Gson();

    private final String USER_INFO_CACHE_KEY = "USER_INFO";
    private final String USER_INFO_EXT_CACHE_KEY = "USER_INFO_EXT";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        userInfoService = new UserInfoServiceImpl();
        boolean update = getIntent().getBooleanExtra("UPDATE", false);
        // TODO test
//        binding = DataBindingUtil.setContentView(this, R.layout.activity_user_info);
        if (FirstLaunchCacheUtil.isFirstLaunch(this) || update) {
            // 首次启动
            binding = DataBindingUtil.setContentView(this, R.layout.activity_user_info);

            if (update) {
                // 从修改按钮跳转过来的
                initUserInfoViewFromCache();
            } else {
                readyToBindData();
            }
            // 独立设置单选数据
//            binding.rgChangeBattery.setOnCheckedChangeListener((radioGroup, i) -> userInfoModel.setIsChangeBattery(R.id.btnYes == i));
        } else {
            // 非首次启动，直接进入上报页面
            Intent intent = new Intent(this, MainActivity.class);
            startActivity(intent);
            finish();
        }
    }

    /*
    从缓存中的用户信息来渲染VIEW
     */
    private void initUserInfoViewFromCache(){
        // 修改用户信息，这边需要初始化用户的信息
        String userInfoStr = CommonCacheUtil.get(this, USER_INFO_CACHE_KEY);
        if (!TextUtils.isEmpty(userInfoStr)) {
            userInfoModel = gson.fromJson(userInfoStr, UserInfoModel.class);
            binding.setUser(userInfoModel);
        }
        // 初始化扩展信息
        String userInfoExtStr = CommonCacheUtil.get(this, USER_INFO_EXT_CACHE_KEY);
        if (!TextUtils.isEmpty(userInfoExtStr)) {
            userInfoBindExtModel = gson.fromJson(userInfoExtStr, UserInfoBindExtModel.class);
            userInfoBindExtModel.setCancelBtnVisible(View.VISIBLE);
            binding.setUserExt(userInfoBindExtModel);
        } else {
            readyToBindData();
        }
        // 初始化radioGroup状态
        if (binding.getUser().getIsChangeBattery()) {
            binding.btnYes.setChecked(true);
        } else {
            binding.btnNo.setChecked(true);
        }
    }

    /*
    第一次启动，直接初始化一些下拉数据
     */
    private void readyToBindData(){
        userInfoModel =  new UserInfoModel();
        userInfoBindExtModel = new UserInfoBindExtModel();
        Log.i("saturday058", "come to start get data list....");
        // 拉取服务端数据，初始化绑定对象
        userInfoService.listUserProfessional(list -> userInfoBindExtModel.setUserProfessionalList(list));
        userInfoService.listPhoneUsage(list -> userInfoBindExtModel.setPhoneUsageList(list));
        userInfoService.listPhoneBrand(list -> userInfoBindExtModel.setPhoneBrandList(list));
        if (userInfoBindExtModel.getUserProfessionalList() == null) {
            userInfoBindExtModel.setUserProfessionalList(new ArrayList());
        }
        if (userInfoBindExtModel.getPhoneUsageList() == null) {
            userInfoBindExtModel.setPhoneUsageList(new ArrayList());
        }
        if (userInfoBindExtModel.getPhoneBrandList() == null) {
            userInfoBindExtModel.setPhoneBrandList(new ArrayList());
        }
        Log.i("saturday058", "get data list finished!");

        // 获取手机的运行时间
        long batteryRuntime = SystemClock.elapsedRealtime();
        // 大约估算手机的开始使用时间
        Log.i("saturday058", "" + batteryRuntime);
        long startMilliTime = System.currentTimeMillis() - batteryRuntime;
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(startMilliTime);
        // 获取年和月
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);

        // 设置年份
        calendar.setTimeInMillis(System.currentTimeMillis());
        int currYear = calendar.get(Calendar.YEAR);
        List<String> yearList = new ArrayList<>();
        for (int i = 20; i >=0; i--) {
            int i1 = currYear - i;
            if (year == i1) {
                // 设置当前的年份，进行双向数据绑定
                userInfoBindExtModel.setYearItemPosition(20-i);
            }
            yearList.add(String.valueOf(i1));
        }
        userInfoBindExtModel.setYearList(yearList);

        // 设置月份
        List<String> monthList = new ArrayList<>();
        for (int i = 1; i <=12; i++) {
            monthList.add(String.valueOf(i));
        }
        userInfoBindExtModel.setMonthItemPosition(month);
        userInfoBindExtModel.setMonthList(monthList);
        userInfoBindExtModel.setCancelBtnVisible(View.VISIBLE);

        // 确保下拉数据都全部准备好，或者回调中使用binding.userInfoModel去异步设置数据
        while (userInfoBindExtModel.getPhoneBrandList().size() ==0
        || userInfoBindExtModel.getPhoneUsageList().size() == 0
        || userInfoBindExtModel.getUserProfessionalList().size() == 0) {
            SystemClock.sleep(50);
        }
        binding.setUser(userInfoModel);
        binding.setUserExt(userInfoBindExtModel);
    }

    // 提交事件，在xml中已经做绑定
    public void submitUserInfo(View view){
        String yearInd = userInfoBindExtModel.getYearList().get(userInfoBindExtModel.getYearItemPosition());
        if (TextUtils.isEmpty(yearInd)) {
            yearInd = "0";
        }
        userInfoModel.setYearData(Integer.parseInt(yearInd));
        String monthInd = userInfoBindExtModel.getMonthList().get(userInfoBindExtModel.getMonthItemPosition());
        if (TextUtils.isEmpty(monthInd)) {
            monthInd = "0";
        }
        userInfoModel.setMonthData(Integer.parseInt(monthInd));

        userInfoModel.setProfessionalSelect(userInfoBindExtModel.getUserProfessionalList().get(userInfoBindExtModel.getUserProfessionalItemPosition()));
        userInfoModel.setUsageSelect(userInfoBindExtModel.getPhoneUsageList().get(userInfoBindExtModel.getPhoneUsageItemPosition()));
        userInfoModel.setBrandSelect(userInfoBindExtModel.getPhoneBrandList().get(userInfoBindExtModel.getPhoneBrandItemPosition()));
        userInfoModel.setUId(getAndroidID());
        String userInfoJson = gson.toJson(userInfoModel);
        String userInfoExtJson = gson.toJson(userInfoBindExtModel);

        userInfoService.submitUserInfo(userInfoModel, respModel -> {
            // 更新首次启动的标志
            if (FirstLaunchCacheUtil.isFirstLaunch(UserInfoActivity.this)) {
                FirstLaunchCacheUtil.save(UserInfoActivity.this, false);
            }
            // 保存用户数据
            CommonCacheUtil.save(this, USER_INFO_CACHE_KEY, userInfoJson);
            CommonCacheUtil.save(this, USER_INFO_EXT_CACHE_KEY, userInfoExtJson);
            Log.i("[SATURDAY]", "get from cache = " + gson.toJson(CommonCacheUtil.get(this, USER_INFO_CACHE_KEY)));
            // 直接跳到下个页面
            startActivity(new Intent(this, MainActivity.class));
            finish();
        });
    }
    // 提交事件，在xml中已经做绑定
    public void onClickCancel(View view){
        startActivity(new Intent(this, MainActivity.class));
        finish();
    }

    // 获取用户UID
    private String getAndroidID() {
        return Settings.System.getString(
                getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID
        );
    }
}
