package com.example.battery.util;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * 通用缓存
 * Created by Saturday on 2023/4/9.
 */

public class CommonCacheUtil {

    private static SharedPreferences sharedPreferences;

    private static void initPreferences(Context context, String key) {
        if (sharedPreferences == null) {
            // 没有线程安全问题
            sharedPreferences = context.getSharedPreferences(key, Context.MODE_PRIVATE);
        }
    }

    /**
     * 缓存数据
     *
     * @param context 上下文
     * @param key 键
     * @param value 值
     */
    public static void save(Context context, String key, String value) {
        initPreferences(context, key);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * 获取本地磁盘中的缓存
     *
     * @param context 上下文
     * @param key 键
     * @return 缓存数据
     */
    public static String get(Context context, String key) {
        initPreferences(context, key);
        return sharedPreferences.getString(key, "");
    }
}
