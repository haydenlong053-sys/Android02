package com.cboda.chatui.third.utils;

import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;

import android.content.Context;
import android.text.format.DateFormat;

import com.cboda.select.picture.utils.LanguageUtil;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

//会话新时间展示格式
public class MyTimeFormatter {
    public static String getSessionTimeString(long timestamp, Context context) {
        if (timestamp < 1) {
            return "";
        }
//        String languageKey = LanguageUtil.INSTANCE.getAppLanguageKey(context, false);
//        String languageValue = LanguageUtil.INSTANCE.getLanList(languageKey);
        Locale locale = LanguageUtil.INSTANCE.getSystemLocal(context);
        // 获取语言设置
//        String language = Locale.getDefault().getLanguage();

        // 转换为秒
        long timeInSeconds = timestamp / 1000;
        Date date = new Date(timeInSeconds * 1000);
        Date currentDate = new Date();

        Calendar dateCalendar = Calendar.getInstance();
        dateCalendar.setTime(date);
        Calendar currentCalendar = Calendar.getInstance();
        currentCalendar.setTime(currentDate);

        SimpleDateFormat formatter;

        // 判断日期差异并设置格式
        if (dateCalendar.get(Calendar.YEAR) != currentCalendar.get(Calendar.YEAR)) {
            formatter = new SimpleDateFormat("yyyy/MM", locale);
        } else if (dateCalendar.get(Calendar.WEEK_OF_YEAR) != currentCalendar.get(Calendar.WEEK_OF_YEAR)) {
            if (locale.getLanguage().startsWith("zh")) {
                formatter = new SimpleDateFormat("M月d日", locale);
            } else {
                formatter = new SimpleDateFormat("MM-dd", locale);
            }
        } else if (!isToday(date)) {
            formatter = new SimpleDateFormat("EEE", locale);
        } else {
            // 在Android中判断是否为12小时制
            boolean is24HourFormat = DateFormat.is24HourFormat(context);
            if (!is24HourFormat) {
                formatter = new SimpleDateFormat("h:mm aa", locale);
            } else {
                formatter = new SimpleDateFormat("HH:mm", locale);
            }
        }
        return formatter.format(date);
    }

    private static boolean isToday(Date date) {
        Calendar today = Calendar.getInstance();
        Calendar targetDate = Calendar.getInstance();
        targetDate.setTime(date);

        return today.get(Calendar.YEAR) == targetDate.get(Calendar.YEAR) &&
                today.get(Calendar.DAY_OF_YEAR) == targetDate.get(Calendar.DAY_OF_YEAR);
    }
}
