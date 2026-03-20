package com.cboda.baselibrary.utils

import android.content.Context
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch

private const val tAG = "CrashUtilClass"

fun crashReport(errorMsg: String?) {
    crashReport(Throwable(errorMsg ?: ""))
}

fun crashReport(throwable: Throwable?) {
    crashReport(false, throwable)
}


fun crashReportLog(errorMsg: String?) {
    crashReportLog(Throwable(errorMsg ?: ""))
}

fun crashReportLog(throwable: Throwable?) {
    crashReport(true, throwable)
}

@OptIn(DelicateCoroutinesApi::class)
fun crashReport(canLog: Boolean, throwable: Throwable?) {
    throwable?.also { throwable ->
        try {
            if (canLog) {
                throwable.toLog(tAG)
            }
            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                com.tencent.bugly.crashreport.CrashReport.postCatchedException(throwable)
            }
        } catch (e: Exception) {
            e.toLog(tAG, false)
        }
    }
}

fun initFireBase(context: Context, buglyId: String) {
    try {
        val strategy = com.tencent.bugly.crashreport.CrashReport.UserStrategy(context)
        strategy.deviceModel = DeviceBroadUtil.getDeviceBrand()
        strategy.deviceID = DeviceBroadUtil.getDeviceId()
        com.tencent.bugly.crashreport.CrashReport.initCrashReport(
            context,
            buglyId,
            getIsDebug(),
            strategy
        )
    } catch (e: Exception) {
        e.toLog(tAG, false)
    }
}