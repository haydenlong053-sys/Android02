package com.cboda.baselibrary.utils

import android.content.Context
import android.text.TextUtils
import android.webkit.WebView
import com.cboda.baselibrary.BuildConfig
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.launch

private const val tAG = "CrashUtilClass"

interface CrashLoad {
    fun crashReport(canLog: Boolean, throwable: Throwable?)
    fun initFireBase(context: Context, fireId: String)
    fun crashWebView(webView: WebView?)
    fun crashU(context: Context?, tag1: String?, tag2: String?)
    fun crashTag(context: Context?, tagKey: String?, tagValue: String?)
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
            strategy.deviceID = DeviceBroadUtil.getAndroidId(context, isEncrypt = false)
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
                kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                    try {
                        com.tencent.bugly.crashreport.CrashReport.setJavascriptMonitor(webView, false)
                    } catch (e: Exception) {
                        e.toLog(tAG, false)
                    }
                }
            }
        } catch (e: Exception) {
            e.toLog(tAG, false)
        }
    }

    override fun crashU(context: Context?, tag1: String?, tag2: String?) {
        try {
            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val mContext = getAppContext(context)
                    if (!TextUtils.isEmpty(tag1) && !TextUtils.isEmpty(tag2)) {
                        com.tencent.bugly.crashreport.CrashReport.putUserData(mContext, "u1", tag1 + "#" + HttpsBroadConfig.currentHostHttpUrlType + "#" + tag2)
                    }
                } catch (e: Exception) {
                    e.toLog(tAG, false)
                }
            }
        } catch (e: Exception) {
            e.toLog(tAG, false)
        }
    }

    override fun crashTag(context: Context?, tagKey: String?, tagValue: String?) {
        try {
            kotlinx.coroutines.GlobalScope.launch(kotlinx.coroutines.Dispatchers.IO) {
                try {
                    val mContext = getAppContext(context)
                    if (!TextUtils.isEmpty(tagKey)) {
                        com.tencent.bugly.crashreport.CrashReport.putUserData(mContext, tagKey, tagValue)
                    }
                } catch (e: Exception) {
                    e.toLog(tAG, false)
                }
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

    override fun crashU(context: Context?, tag1: String?, tag2: String?) {
        try {

        } catch (e: Exception) {
            e.toLog(tAG, false)
        }
    }

    override fun crashTag(context: Context?, tagKey: String?, tagValue: String?) {
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