package com.example.battery.util;

import android.content.Context;
import android.content.SharedPreferences;

import com.example.battery.model.BatteryReportModel;
import com.google.gson.Gson;

/**
 * 将要上报的数据缓存当前应用文件下的工具
 * Created by Saturday on 2023/4/9.
 */

public class ReportCacheUtil {

    private static SharedPreferences sharedPreferences;
    private static final String DATA_REPORT_KEY = "reportModel";
    private static Gson gson = new Gson();

    private static void initPreferences(Context context) {
        if (sharedPreferences == null) {
            // 没有线程安全问题
            sharedPreferences = context.getSharedPreferences("REPORT_DATA", Context.MODE_PRIVATE);
        }
    }

    /**
     * 缓存上报数据到本地磁盘中
     *
     * @param context 上下文
     * @param data    上报数据
     */
    public static void save(Context context, BatteryReportModel data) {
        initPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(DATA_REPORT_KEY, gson.toJson(data));
        editor.apply();
    }

    /**
     * 获取本地磁盘中的上报数据
     *
     * @param context 上下文
     * @return 上报数据
     */
    public static BatteryReportModel getReportModel(Context context) {
        initPreferences(context);
        String data = sharedPreferences.getString(DATA_REPORT_KEY, "");
        return gson.fromJson(data, BatteryReportModel.class);
    }
}
