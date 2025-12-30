package com.lxkj.aiim.utils

import android.annotation.SuppressLint
import android.text.format.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * 简述： <功能描述>
 * Created by ysh on 2023/3/24.
 */
class TimeUtils {

    companion object {
        const val SECOND = 1000L
        const val MINUTE = 60 * 1000L
        const val HOUR = 60 * 60 * 1000L
        const val DAY = 24 * 60 * 60 * 1000L

        /**
         * 格式化long时间
         *
         * @param time 时间戳(秒)
         * @return 格式化后的时间
         */
        fun getFormatTime(date: Date?): String {
            var timeString = ""
            try {
                timeString = DateFormat.format("yyyy-MM-dd", date).toString()
            } catch (e: Exception) {
                AppUtil.debug("getFormatTime", "excepiton:$e")
            }
            return timeString
        }

        fun getFormatTimeHour(date: Date?): String {
            var timeString = ""
            try {
                timeString = DateFormat.format("yyyy-MM-dd HH", date).toString()
            } catch (e: Exception) {
                AppUtil.debug("getFormatTime", "excepiton:$e")
            }
            return timeString
        }

        @SuppressLint("SimpleDateFormat")
        fun dateToStamp(time: String): Long {
            var longTime = 0L
            try {
                val format = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val date = format.parse(time)
                longTime = date.time
            } catch (e: Exception) {
                AppUtil.debug("dateToStamp", "excepiton:$e")
            }
            return longTime
        }

        @SuppressLint("SimpleDateFormat")
        fun dateToStamp1(time: String): Long {
            var longTime = 0L
            try {
                val format = SimpleDateFormat("yyyy-MM-dd")
                val date = format.parse(time)
                longTime = date.time
            } catch (e: Exception) {
                AppUtil.debug("dateToStamp", "excepiton:$e")
            }
            return longTime
        }

        @SuppressLint("SimpleDateFormat")
        fun stampToDate(time: Long): String {
            var s = ""
            try {
                val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                val date = Date(time)
                s = simpleDateFormat.format(date)
            } catch (e: Exception) {
                AppUtil.debug("stampToDate", "excepiton:$e")
            }
            return s
        }

        fun getLeftTime(time: Long): String {
            val sb = StringBuilder()
            val dm = time % DAY
            val hm = dm % HOUR
            if (time >= DAY) {
                //x天x小时x分
                val day = "${(time / DAY)}天"
                sb.append(day)
                val hour = "${dm / HOUR}时"
                sb.append(hour)
                val mins = hm / MINUTE
                sb.append("${mins}分后自动确认收货")
            } else {
                //x小时x分
                if (time >= HOUR) {
                    val hour = "${time / HOUR}时"
                    sb.append(hour)
                    val mins = hm / MINUTE
                    sb.append("${mins}分后自动确认收货")
                } else {
                    //x分
                    val mins = time / MINUTE + 1
                    sb.append("${mins}分后自动确认收货")
                }
            }
            return sb.toString()
        }


        fun getDressLeftTime(time: Long): String {
            val sb = StringBuilder()
            val dm = time % DAY
            if (time >= DAY) {
                val day = "${(time / DAY)}天"
                sb.append(day)
            } else {
                //x小时x分
                if (time >= HOUR) {
                    val hour = "${time / HOUR}时"
                    sb.append(hour)
                } else if (time >= MINUTE) {
                    //x分
                    val mins = time / MINUTE + 1
                    sb.append("${mins}分")
                } else {
                    val sec = time / SECOND + 1
                    sb.append("${sec}秒")
                }
            }
            return sb.toString()
        }

        fun getLiveTime(time: Long): String {
            return if (time <= 0) {
                ""
            } else {
                val hour = time.toInt() / (60 * 60 * 1000)
                val minute = time.toInt() / (60 * 1000) % 60
                val seconds = time.toInt() / 1000 % 60
                val stringBuffer = StringBuilder()
                if (hour>0){
                    if (hour.toString().length > 1) {
                        stringBuffer.append(hour)
                    } else {
                        stringBuffer.append("0").append(hour)
                    }
                    stringBuffer.append("小时")
                    if (minute.toString().length > 1) {
                        stringBuffer.append(minute)
                    } else {
                        stringBuffer.append("0").append(minute)
                    }
                    stringBuffer.append("分")
                    if (seconds.toString().length > 1) {
                        stringBuffer.append(seconds)
                    } else {
                        stringBuffer.append("0").append(seconds)
                    }
                    stringBuffer.append("秒")
                } else if(minute>0){
                    if (minute.toString().length > 1) {
                        stringBuffer.append(minute)
                    } else {
                        stringBuffer.append("0").append(minute)
                    }
                    stringBuffer.append("分")
                    if (seconds.toString().length > 1) {
                        stringBuffer.append(seconds)
                    } else {
                        stringBuffer.append("0").append(seconds)
                    }
                    stringBuffer.append("秒")
                } else {
                    stringBuffer.append(seconds)
                    stringBuffer.append("秒")
                }

                stringBuffer.toString()
            }
        }
    }
}