package com.base.commonlibrary.utils

import android.content.Context
import com.base.commonlibrary.utils.DeviceUtil.getDeviceBrand
import com.blankj.utilcode.util.ThreadUtils
import com.tencent.bugly.crashreport.CrashReport
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

private const val tAG = "BuGlyUtilClass"

fun bgReport(errorMsg: String?) {
    bgReport(Throwable(errorMsg ?: ""))
}

fun bgReport(throwable: Throwable?) {
    bgReport(false, throwable)
}


fun bgReportLog(errorMsg: String?) {
    bgReportLog(Throwable(errorMsg ?: ""))
}

fun bgReportLog(throwable: Throwable?) {
    bgReport(true, throwable)
}

@OptIn(DelicateCoroutinesApi::class)
fun bgReport(canLog: Boolean, throwable: Throwable?) {
    throwable?.also { throwable ->
        try {
            if (canLog) {
                throwable.toLog(tAG)
            }
            GlobalScope.launch(Dispatchers.IO) {
                CrashReport.postCatchedException(throwable)
            }
        } catch (e: Exception) {
            e.toLog(tAG, false)
        }
    }
}

fun initBuGly(context: Context, buglyId: String) {
    try {
        val strategy = CrashReport.UserStrategy(context)
        strategy.deviceModel = getDeviceBrand()
        CrashReport.initCrashReport(
            context,
            buglyId,
            getIsDebug(),
            strategy
        )
    } catch (e: Exception) {
        e.toLog(tAG, false)
    }
}