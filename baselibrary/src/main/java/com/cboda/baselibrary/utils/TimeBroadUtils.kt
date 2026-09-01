package com.cboda.baselibrary.utils

import android.os.Build
import android.os.SystemClock
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.math.abs

object TimeBroadBroadUtils {

    fun getNowTimeHoursDifference(timestamp2: Long): Long {
        return try {
            val diffInMillis = abs(getNowTime() - timestamp2)
            diffInMillis / (60 * 60 * 1000)
        } catch (_: Exception) {
            0L
        }
    }

    fun getNowTime(): Long {
        return try {
            System.currentTimeMillis()
        } catch (_: Exception) {
            0L
        }
    }

    fun getDeviceOpenTime(): Long {
        return try {
            anyToInt((System.currentTimeMillis() - SystemClock.elapsedRealtime()) / 100L).toLong()
        } catch (e: Exception) {
            e.toLog("TimeBroadBroadUtilsClass")
            0L
        }
    }

    /**
     * 格式化日期
     */
    fun formatDate(date: Date, pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
        return SimpleDateFormat(pattern, Locale.getDefault()).format(date)
    }

    /**
     * 获取当前时间的字符串
     */
    fun now(pattern: String = "yyyy-MM-dd HH:mm:ss"): String {
        return formatDate(Date(), pattern)
    }

    /**
     * 秒数转时分秒，带智能裁剪
     */
    fun formatDuration(seconds: Long): String {
        val hours = TimeUnit.SECONDS.toHours(seconds)
        val minutes = TimeUnit.SECONDS.toMinutes(seconds) % 60
        val secs = seconds % 60
        return "${hours}:${minutes}:${secs}"
//        when {
//            hours > 0 -> "${hours}:${minutes}:${secs}"
//            minutes > 0 -> "${minutes}:${secs}"
//            else -> "${secs}"
//        }
    }

    /**
     * 秒数转 "HH:mm:ss" 格式（补0）
     */
    fun formatDurationClock(seconds: Long): String {
        val hours = seconds / 3600
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d:%02d", hours, minutes, secs)
    }

    fun formatDateOnly(date: Date): String = formatDate(date, "yyyy-MM-dd")

    fun formatTimeOnly(date: Date): String = formatDate(date, "HH:mm:ss")

    fun formatMinutesSeconds(seconds: Long): String {
        val minutes = (seconds % 3600) / 60
        val secs = seconds % 60
        return String.format("%02d:%02d", minutes, secs)
    }

    /**
     * String 时间戳转日期字符串
     * @param timestampStr 字符串时间戳 (支持秒/毫秒)
     * @param pattern 输出格式，默认 yyyy-MM-dd HH:mm:ss
     * @return 格式化后的日期字符串 或 null（非法时间戳）
     */
    fun formatTimestamp(timestampStr: String?, pattern: String = "yyyy-MM-dd HH:mm:ss"): String? {
        if (timestampStr.isNullOrBlank()) return null
        if (!timestampStr.all { it.isDigit() }) return null // 必须全是数字
        return try {
            formatTimestamp(timestampStr.toLong(), pattern)
        } catch (_: Exception) {
            null
        }

    }


    fun formatTimestamp(ts: Long?, pattern: String = "yyyy-MM-dd HH:mm:ss"): String? {
        if (ts == null) return null
        return try {
            val millis = when (ts.toString().length) {
                10 -> ts * 1000 // 秒级
                13 -> ts       // 毫秒级
                else -> return null // 非常见长度
            }
            formatDate(Date(millis), pattern)
        } catch (_: Exception) {
            null
        }
    }
}
