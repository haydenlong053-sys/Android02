package com.cboda.baselibrary.utils

import android.content.Context
import ir.programmerplus.realtime.RealTime
import java.text.SimpleDateFormat
import java.util.Locale

object RealTimeUtil {
    private val tAG = "RealTimeUtilClass"
    fun initTime(context: Context?, callBack: (Long) -> Unit = {}) {
        RealTime.builder(getAppContext(context))
            .withNtpServer("time.google.com") // 添加 NTP 服务器
            .withNtpServer("time.windows.com")
//            // .withTimeServer("https://your-server.com") // 也可以使用你自己的服务器
            .setLoggingEnabled(getIsDebug())
            .build { date ->
                // 初始化完成后的回调
                eLogD(tAG, "初始化成功: ${date.time}")
                callBack.invoke(date.time)
            }
    }

    fun getServerTime(context: Context?, callBack: (Long) -> Unit = {}) {
        if (!RealTime.isInitialized()) {
            eLogD(tAG, "未初始化时间")
            initTime(context, callBack)
        } else {
            try {
                val currentDate = RealTime.now().time
                // 使用 currentDate 进行后续操作
//                val sdf = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
//                val time = sdf.format(currentDate)
                eLogD(tAG, "time:$currentDate")
                callBack.invoke(currentDate)
            } catch (e: Exception) {
                e.toLog(tAG)
            }
        }
    }
}