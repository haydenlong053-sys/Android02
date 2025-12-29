package com.base.commonlibrary.utils


import android.content.Intent
import android.os.Process
import android.text.TextUtils
import com.base.commonlibrary.app.UtilApplication
import com.base.commonlibrary.utils.MMKVUtils.Companion.DEV_INPUT_BASE_URL
import com.base.commonlibrary.utils.MMKVUtils.Companion.NET_TYPE
import com.blankj.utilcode.util.ThreadUtils
import com.tencent.mmkv.MMKV
import kotlin.system.exitProcess

object HttpsConfig {

    val appBaseUrl
        get():String {
            return "$appHost/lixin/api"
        }
    var appHost = "http://119.45.9.228:82"
    var appImServerHost = "119.45.9.228"
    var appIcePassword = "aT3BxjPy4MRdC6@"
    var appGroupAvatarHost = "http://119.45.9.228:8888"
    const val appIceServer = "turn:turn.seekweb4.net:3478"
    const val appIceUserName = "seekweb4"
    const val appWebHost = "https://static.seekweb4.net"
    private val netItems = arrayListOf(
        "82\nhttp://119.45.9.228:82",
        "https\nhttps://api.seekweb4.net",
        "fat\nhttps://api-fat.seekweb4.net",
        "自定义"
    )
    private const val RELEASE_TYPE = 0
    private const val HTTP_TYPE = 1
    private const val FAT_TYPE = 2
    private const val INPUT_TYPE = 3

    private fun setServer(type: Int) {
        when (type) {
            FAT_TYPE -> {
                appHost = "https://api-fat.seekweb4.net"
                appImServerHost = "fat.seekweb4.net"
                appIcePassword = "aT3BxjPy4MRdC6@"
                appGroupAvatarHost = "https://api-fat.seekweb4.net"
            }

            HTTP_TYPE -> {
                appHost = "https://api.seekweb4.net"
                appImServerHost = "ws.seekweb4.net"
                appIcePassword = "aT3BxjPy4MRdC6@"
                appGroupAvatarHost = "https://api.seekweb4.net"
            }


            INPUT_TYPE -> {
                appHost = MMKV.defaultMMKV().decodeString(DEV_INPUT_BASE_URL)
                    ?: "https://api-fat.seekweb4.net"
                appImServerHost = "fat.seekweb4.net"
                appIcePassword = "aT3BxjPy4MRdC6@"
                appGroupAvatarHost = "https://api-fat.seekweb4.net"
            }

            RELEASE_TYPE -> {
                appHost = "http://119.45.9.228:82"
                appImServerHost = "119.45.9.228"
                appIcePassword = "aT3BxjPy4MRdC6@"
                appGroupAvatarHost = "http://119.45.9.228:8888"
            }

            else -> {
                appHost = "http://119.45.9.228:82"
                appImServerHost = "119.45.9.228"
                appIcePassword = "aT3BxjPy4MRdC6@"
                appGroupAvatarHost = "http://119.45.9.228:8888"
            }
        }

    }


    fun setHttpMode() {
        if (getIsDebug()) {
            setServer(getNetType())
        } else {
            setServer(RELEASE_TYPE)
//            setServer(HTTP_TYPE)
        }
    }


    private fun getNetType() = MMKV.defaultMMKV().decodeInt(NET_TYPE, RELEASE_TYPE)

    fun getDevType() = netItems[getNetType()]

    fun showSwitchDialog() {
        showAlertDialogList(netItems) { p1, _ ->
            if (p1 == INPUT_TYPE) {
                showEditDialog { url ->
                    if (!TextUtils.isEmpty(url)) {
                        MMKV.defaultMMKV().encode(DEV_INPUT_BASE_URL, url)
                        updateHttpMode(p1)
                    }
                }
            } else {
                updateHttpMode(p1)
            }
        }
    }


    private fun updateHttpMode(devType: Int) {
        clearUserDataLiveData.postValue(true)
        MMKV.defaultMMKV().encode(NET_TYPE, devType)
        ThreadUtils.getMainHandler().postDelayed({ restartApp() }, 1000)
    }

    private fun restartApp() {
        val intent = UtilApplication.cxt.getOtherAppIntent(getAppPackName())
        intent?.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    or Intent.FLAG_ACTIVITY_CLEAR_TASK
        )
        UtilApplication.cxt.startActivity(intent)
        Process.killProcess(Process.myPid())
        exitProcess(0)
    }

}

