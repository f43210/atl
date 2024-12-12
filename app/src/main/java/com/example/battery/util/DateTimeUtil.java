package com.example.battery.util;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by Saturday on 2023/4/9.
 */

public class DateTimeUtil {

    /**
     * 格式化日期时间
     *
     * @param milliseconds 毫秒数
     * @param pattern      格式化模板
     * @return 格式化后的日期时间
     */
    public static String format(long milliseconds, String pattern) {
        SimpleDateFormat dateFormat = new SimpleDateFormat(pattern);
        return dateFormat.format(new Date(milliseconds));
    }
}
