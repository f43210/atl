package com.example.battery.util;

import android.content.Context;

/**
 * Created by Saturday on 2023/4/9.
 */

public class DeviceUtil {

    /**
     * 获取设备宽度（px）
     **/
    public static int getDeviceWidth(Context context) {
        return context.getResources().getDisplayMetrics().widthPixels;
    }

    /**
     * 获取设备高度（px）
     */
    public static int getDeviceHeight(Context context) {
        return context.getResources().getDisplayMetrics().heightPixels;
    }

//    /** * 获取设备的唯一标识， 需要 “android.permission.READ_Phone_STATE”权限*/
//    public static String getIMEI(Context context) {
//        TelephonyManager tm=(TelephonyManager) context .getSystemService(Context.TELEPHONY_SERVICE);
//        String deviceId = tm.getDeviceId();
//        if(deviceId ==null) {
//            return"UnKnown";
//        }else{
//            return deviceId;
//        }
//    }
}
