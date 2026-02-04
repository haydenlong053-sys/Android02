package com.cboda.baselibrary.utils


import android.content.Intent
import android.os.Process
import android.text.TextUtils
import com.cboda.baselibrary.app.UtilBroadApplication
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.DEV_INPUT_BASE_URL
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.NET_TYPE
import com.blankj.utilcode.util.ThreadUtils
import com.tencent.mmkv.MMKV
import kotlin.system.exitProcess

object HttpsBroadConfig {

    val appBaseUrl
        get():String {
            return "$appHost2/api"
        }
    var appHost2 = ""

    //    var appBaseUrl2 = ""
    var appHost = "http://119.45.9.228:82"
    var appImServerHost = "119.45.9.228"
    var appIcePassword = "aT3BxjPy4MRdC6@"
    var appAesPwd = "1q30vqwerf8yiac3"
    var appGroupAvatarHost = "http://119.45.9.228:8888"
    const val appIceServer = "turn:turn.seekweb4.net:3478"
    const val appIceUserName = "seekweb4"
    @JvmField
    var appWebHost = "https://app.seekweb4.net" //协议默认用美区
    private val netItems = arrayListOf(
        "82\nhttp://119.45.9.228:82",
        "https\nhttps://api.seekweb4.net",
        "fat\nhttps://api-fat.seekweb4.net",
        "自定义",
        "US\nhttps://app.seekweb4.net",
        "HongKong\nhttps://api.seekweb4.ai",
    )
    private const val RELEASE_TYPE = 0
    private const val HTTP_TYPE = 1
    private const val FAT_TYPE = 2
    private const val INPUT_TYPE = 3
    private const val US_TYPE = 4
    private const val HONGKONG_TYPE = 5

    private fun setServer(type: Int) {
        when (type) {
            US_TYPE -> {
                appHost = "https://app.seekweb4.net"
                appHost2 = appHost
                appImServerHost = "us.seekweb4.net"
                appIcePassword = "aT3BxjPy4MRdC6@"
                appGroupAvatarHost = "https://app.seekweb4.net"
                appAesPwd = "f81q30ryiacasda1"
            }
            HONGKONG_TYPE -> {
                appHost = "https://api.seekweb4.ai"
                appHost2 = appHost
                appImServerHost = "im.seekweb4.ai"
                appIcePassword = "aT3BxjPy4MRdC6@"
                appGroupAvatarHost = "https://api.seekweb4.ai"
                appAesPwd = "f81q30ryiacasda1"
                appWebHost=appHost
            }

            FAT_TYPE -> {
                appHost = "https://api-fat.seekweb4.net"
                appHost2 = appHost
                appImServerHost = "fat.seekweb4.net"
                appIcePassword = "aT3BxjPy4MRdC6@"
                appGroupAvatarHost = "https://api-fat.seekweb4.net"
                appAesPwd = "f81q30ryiacasda1"
            }

            HTTP_TYPE -> {
                appHost = "https://api.seekweb4.net"
                appHost2 = "$appHost/lixin"
                appImServerHost = "ws.seekweb4.net"
                appIcePassword = "aT3BxjPy4MRdC6@"
                appGroupAvatarHost = "https://api.seekweb4.net"
                appAesPwd = "1q30vqwerf8yiac3"
            }


            INPUT_TYPE -> {
                appHost = MMKV.defaultMMKV().decodeString(DEV_INPUT_BASE_URL)
                    ?: "https://api-fat.seekweb4.net"
                appHost2 = "$appHost/lixin"
                appImServerHost = "fat.seekweb4.net"
                appIcePassword = "aT3BxjPy4MRdC6@"
                appGroupAvatarHost = "https://api-fat.seekweb4.net"
                appAesPwd = "1q30vqwerf8yiac3"
            }

            RELEASE_TYPE -> {
                appHost = "http://119.45.9.228:82"
                appHost2 = "$appHost/lixin"
                appImServerHost = "119.45.9.228"
                appIcePassword = "aT3BxjPy4MRdC6@"
                appGroupAvatarHost = "http://119.45.9.228:8888"
                appAesPwd = "1q30vqwerf8yiac3"
            }

            else -> {
                appHost = "https://app.seekweb4.net"
                appHost2 = appHost
                appImServerHost = "us.seekweb4.net"
                appIcePassword = "aT3BxjPy4MRdC6@"
                appGroupAvatarHost = "https://api-fat.seekweb4.net"
                appAesPwd = "f81q30ryiacasda1"
            }
        }

    }


    fun setHttpMode() {
        if (getIsDebug()) {
            setServer(getNetType())
        } else {
//            setServer(RELEASE_TYPE)
            setServer(HONGKONG_TYPE)
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

    fun restartApp() {
        val intent = UtilBroadApplication.cxt.getOtherAppIntent(getAppPackName())
        intent?.addFlags(
            Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
                    or Intent.FLAG_ACTIVITY_CLEAR_TASK
        )
        UtilBroadApplication.cxt.startActivity(intent)
        Process.killProcess(Process.myPid())
        exitProcess(0)
    }

}
