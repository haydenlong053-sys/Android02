package com.cboda.baselibrary.utils

import android.content.Context
import android.webkit.WebView
import com.cboda.baselibrary.BuildConfig
import com.tencent.bugly.crashreport.CrashReport
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch

private const val tAG = "CrashUtilClass"

interface CrashLoad {
    fun crashReport(canLog: Boolean, throwable: Throwable?)
    fun initFireBase(context: Context, fireId: String)

    fun crashWebView(webView: WebView?)

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

    override fun initFireBase(context: Context, fireId: String) {
        try {
            val strategy = com.tencent.bugly.crashreport.CrashReport.UserStrategy(context)
            strategy.deviceModel = DeviceBroadUtil.getDeviceBrand()
            strategy.deviceID = DeviceBroadUtil.getDeviceId() + " # " + DeviceBroadUtil.getAndroidId()
            strategy.isEnableCatchAnrTrace = getIsDebug() // 设置anr时是否获取系统trace文件，默认为false
            strategy.isEnableRecordAnrMainStack = true// 设置是否获取anr过程中的主线程堆栈，默认为true
            com.tencent.bugly.crashreport.CrashReport.initCrashReport(
                context,
                fireId,
                getIsDebug(),
                strategy
            )
        } catch (e: Exception) {
            e.toLog(tAG, false)
        }
    }

    override fun crashWebView(webView: WebView?) {
        try {
            webView?.let {
                CrashReport.setJavascriptMonitor(webView, false)
            }
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

    override fun initFireBase(context: Context, fireId: String) {
        try {

        } catch (e: Exception) {
            e.toLog(tAG, false)
        }
    }

    override fun crashWebView(webView: WebView?) {
        try {

        } catch (e: Exception) {
            e.toLog(tAG, false)
        }
    }
}


fun crashTag(context: Context?, tag: String?) {
//    CrashReport.putUserData(context, "userkey", "uservalue");

    val id = anyToInt(tag ?: "-1")
    if (id > 0) {
        CrashReport.setUserSceneTag(getAppContext(context), id)
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