package com.example.battery.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 首次登陆标记缓存
 * Created by Saturday on 2023/10/26.
 */

public class FirstLaunchCacheUtil {

    private static SharedPreferences sharedPreferences;
    private static final String KEY = "firstLaunch";

    private static void initPreferences(Context context) {
        if (sharedPreferences == null) {
            // 没有线程安全问题
            sharedPreferences = context.getSharedPreferences("REPORT_DATA", Context.MODE_PRIVATE);
        }
    }

    /**
     * 记录是否是第一次启动
     *
     * @param context 上下文
     * @param firstLaunch   第一次启动标记
     */
    public static void save(Context context, boolean firstLaunch) {
        initPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(KEY, firstLaunch);
        editor.apply();
    }

    /**
     * 获取第一次启动记录
     *
     * @param context 上下文
     * @return 上报数据
     */
    public static boolean isFirstLaunch(Context context) {
        initPreferences(context);
        return sharedPreferences.getBoolean(KEY, true);
    }
}
