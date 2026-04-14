package com.cboda.baselibrary.utils

import android.content.Context
import com.cboda.baselibrary.BuildConfig
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch

private const val tAG = "CrashUtilClass"

interface CrashLoad {
    fun crashReport(canLog: Boolean, throwable: Throwable?)
    fun initFireBase(context: Context, buglyId: String)
}

object CrashFactory {
    val crashLoad: CrashLoad by lazy(LazyThreadSafetyMode.NONE) {
        if (BuildConfig.ENABLE_CRASH) {
            CrashLoaderProxy()
        } else {
            CrashLoaderFirebase()
        }
    }
}

class CrashLoaderProxy : CrashLoad {
    @OptIn(DelicateCoroutinesApi::class)
    override fun crashReport(canLog: Boolean, throwable: Throwable?) {
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

    override fun initFireBase(context: Context, buglyId: String) {
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
}

class CrashLoaderFirebase : CrashLoad {
    override fun crashReport(canLog: Boolean, throwable: Throwable?) {
        throwable?.also { throwable ->
            try {

            } catch (e: Exception) {
                e.toLog(tAG, false)
            }
        }
    }

    override fun initFireBase(context: Context, buglyId: String) {
        try {

        } catch (e: Exception) {
            e.toLog(tAG, false)
        }
    }
}

fun crashReport(errorMsg: String?) {
    crashReport(Throwable(errorMsg ?: ""))
}

fun crashReport(throwable: Throwable?) {
    CrashFactory.crashLoad.crashReport(false, throwable)
}


fun crashReportLog(errorMsg: String?) {
    crashReportLog(Throwable(errorMsg ?: ""))
}

fun crashReportLog(throwable: Throwable?) {
    CrashFactory.crashLoad.crashReport(true, throwable)
}