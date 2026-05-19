package com.cboda.chatui.third.utils;

import static com.cboda.baselibrary.utils.ViewExpandUtilKt.getAppContext;

import androidx.annotation.NonNull;

import org.joda.time.DateTime;
import org.joda.time.DateTimeConstants;

import java.util.Calendar;

import com.cboda.chatui.R;

/**
 * @创建者 CSDN_LQR
 * @描述 时间工具（需要joda-time）
 */
public class TimeBroadBroadUtils {

    /**
     * 得到仿微信日期格式输出
     *
     * @param msgTimeMillis
     * @return
     */
    public static String getMsgFormatTime(long msgTimeMillis) {
        if (msgTimeMillis == 0) {
            return "";
        }

        DateTime msgTime = new DateTime(msgTimeMillis);
        Calendar nowCalendar = Calendar.getInstance();
        nowCalendar.get(Calendar.DAY_OF_YEAR);
        Calendar msgCalendar = Calendar.getInstance();
        msgCalendar.setTimeInMillis(msgTimeMillis);

        if (nowCalendar.get(Calendar.YEAR) == msgCalendar.get(Calendar.YEAR)) {
            if (nowCalendar.get(Calendar.DAY_OF_YEAR) == msgCalendar.get(Calendar.DAY_OF_YEAR)) {
                // 1:40
                return getTime(msgTime);
            } else if (msgCalendar.get(Calendar.DAY_OF_YEAR) + 1 == nowCalendar.get(Calendar.DAY_OF_YEAR)) {
                //昨天
                return getAppContext(null).getString(R.string.time_yesterday, getTime(msgTime));
            } else if (nowCalendar.get(Calendar.WEEK_OF_YEAR) == msgCalendar.get(Calendar.WEEK_OF_YEAR)) {
                //星期
                switch (msgTime.getDayOfWeek()) {
                    case DateTimeConstants.SUNDAY:
                        return getAppContext(null).getString(R.string.time_weekday_sun, getTime(msgTime));
                    case DateTimeConstants.MONDAY:
                        return getAppContext(null).getString(R.string.time_weekday_mon, getTime(msgTime));
                    case DateTimeConstants.TUESDAY:
                        return getAppContext(null).getString(R.string.time_weekday_tue, getTime(msgTime));
                    case DateTimeConstants.WEDNESDAY:
                        return getAppContext(null).getString(R.string.time_weekday_wed, getTime(msgTime));
                    case DateTimeConstants.THURSDAY:
                        return getAppContext(null).getString(R.string.time_weekday_thu, getTime(msgTime));
                    case DateTimeConstants.FRIDAY:
                        return getAppContext(null).getString(R.string.time_weekday_fri, getTime(msgTime));
                    case DateTimeConstants.SATURDAY:
                        return getAppContext(null).getString(R.string.time_weekday_sat, getTime(msgTime));
                    default:
                        break;
                }
                return "";
            } else {
                //12月22日
                String pattern = getAppContext(null).getString(R.string.time_month_day_time_pattern);
                return msgTime.toString(pattern);
            }
        } else {
            String pattern = getAppContext(null).getString(R.string.time_year_month_day_time_pattern);
            return msgTime.toString(pattern);
        }
    }



    public static String getJoinFormatTime(long msgTimeMillis) {
        if (msgTimeMillis == 0) {
            return "";
        }
        DateTime msgTime = new DateTime(msgTimeMillis);
        Calendar nowCalendar = Calendar.getInstance();
        nowCalendar.get(Calendar.DAY_OF_YEAR);
        Calendar msgCalendar = Calendar.getInstance();
        msgCalendar.setTimeInMillis(msgTimeMillis);
        String pattern = "yyyy-MM-dd";
        if (nowCalendar.get(Calendar.YEAR) == msgCalendar.get(Calendar.YEAR)) {
            pattern="MM-dd";
        }
        return msgTime.toString(pattern);
    }


    public static String getJoinFormatTime2(long msgTimeMillis) {
        if (msgTimeMillis == 0) {
            return "";
        }
        DateTime msgTime = new DateTime(msgTimeMillis);
        Calendar nowCalendar = Calendar.getInstance();
        nowCalendar.get(Calendar.DAY_OF_YEAR);
        Calendar msgCalendar = Calendar.getInstance();
        msgCalendar.setTimeInMillis(msgTimeMillis);
        String pattern = "yyyy-MM-dd HH:mm:ss";
        return msgTime.toString(pattern);
    }
    @NonNull
    private static String getTime(DateTime msgTime) {
        return msgTime.toString("HH:mm");
    }
}
