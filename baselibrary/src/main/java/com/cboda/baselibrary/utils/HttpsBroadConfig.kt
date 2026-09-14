package com.cboda.baselibrary.utils


import android.content.Intent
import android.os.Process
import android.text.TextUtils
import com.blankj.utilcode.util.ThreadUtils
import com.cboda.baselibrary.BuildConfig
import com.cboda.baselibrary.app.UtilBroadApplication
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.DEV_INPUT_BASE_URL
import com.cboda.baselibrary.utils.MMKVBroadUtils.Companion.NET_TYPE
import com.tencent.mmkv.MMKV
import kotlin.system.exitProcess

object HttpsBroadConfig {
    // 此处提审的时候改成true
    const val isReviewEnv = BuildConfig.IS_REVIEW_ENV
    const val openTPay = !BuildConfig.IS_DEBUG

    /**
     * 仅用自研 IM：保留野火 ChatManager.init（登录 clientId / MeetPlugin），
     * 但跳过野火长连 connect，Main 不等野火 ClientService。
     */
    const val useSelfImOnly = true
    val appBaseUrl
        get():String {
            return "$appHost2/api"
        }
    @JvmField
//    var useLiveKitGroupMeeting = false
    var useLiveKitGroupMeeting = true

    //自研的长连接host
    private const val MY_IM_SOCKET_HOST_FAT: String = "[{\"host\":\"95.40.9.142\", \"port\":1081}]"
    private const val MY_IM_SOCKET_HOST_RELEASE: String = "[{\"host\":\"socket.seekweb4.ai\", \"port\":80}]"

    //自研的短连接http的host
    private const val MY_IM_HTTP_HOST_FAT: String = "https://im-test.seekweb4.ai/"
    private const val MY_IM_HTTP_HOST_FAT_IP: String = "http://95.40.9.142/"

    private const val MY_IM_HTTP_HOST_RELEASE: String = "https://im-prd.seekweb4.ai/"

    //   private const val MY_IM_HTTP_HOST_RELEASE: String = "http://18.163.110.184/"
    @JvmField
    var myImSocketHost = MY_IM_SOCKET_HOST_RELEASE

    @JvmField
    var myImHttpsHost = MY_IM_HTTP_HOST_RELEASE

    var appHost2 = ""

    //    var appBaseUrl2 = ""
    var appHost = "http://119.45.9.228:82"
    var appImServerHost = "119.45.9.228"
    var appIcePassword = "aT3BxjPy4MRdC6@"
    var appAesPwd = "1q30vqwerf8yiac3"
    var appGroupAvatarHost = "http://119.45.9.228:8888"
    const val appIceServer = "turn:turn.seekweb4.net:3478"
    const val appIceUserName = "seekweb4"

    const val H5_BEAUTIFUL = "https://www.dualx.us"//信用分H5链接

    @JvmField
    var appWebHost = "https://app.seekweb4.net" //协议默认用美区

    private val netItems = arrayListOf(
        "82\nhttp://119.45.9.228:82",
        "https\nhttps://api.seekweb4.net",
        "fat\nhttps://api-fat.seekweb4.net",
        "自定义",
        "US\nhttps://app.seekweb4.net",
        "HongKong\nhttps://api.seekweb4.ai",
        "fat_ip\nhttp://95.40.9.142",
    )
    const val RELEASE_TYPE = 0
    private const val HTTP_TYPE = 1
    private const val FAT_TYPE = 2
    private const val INPUT_TYPE = 3
    private const val US_TYPE = 4
    private const val HONGKONG_TYPE = 5

    private const val FAT_IP = 6

    @JvmField
    var currentHostHttpUrlType = RELEASE_TYPE

    private fun setServer(type: Int) {
        currentHostHttpUrlType = type
        when (type) {
            US_TYPE -> {
                appHost = "https://app.seekweb4.net"
                appHost2 = appHost
                appImServerHost = "us.seekweb4.net"
                appIcePassword = "aT3BxjPy4MRdC6@"
                appGroupAvatarHost = "https://app.seekweb4.net"
                appAesPwd = "f81q30ryiacasda1"
                myImSocketHost = MY_IM_SOCKET_HOST_RELEASE
                myImHttpsHost = MY_IM_HTTP_HOST_RELEASE
            }

            HONGKONG_TYPE -> {
                appHost = "https://api.seekweb4.ai"
                appHost2 = appHost
                appImServerHost = "im.seekweb4.ai"
                appIcePassword = "aT3BxjPy4MRdC6@"
                appGroupAvatarHost = "https://api.seekweb4.ai"
                appAesPwd = "PgGMe2BWyp8G3SFxdn5fDjL5rFUoTYm1"
                appWebHost = appHost
                myImSocketHost = MY_IM_SOCKET_HOST_RELEASE
                myImHttpsHost = MY_IM_HTTP_HOST_RELEASE

            }

            FAT_TYPE -> {
                appHost = "https://api-fat.seekweb4.net"
                appHost2 = appHost
                appImServerHost = "testim.seekweb4.ai"
                appIcePassword = "aT3BxjPy4MRdC6@"
                appGroupAvatarHost = "https://api-fat.seekweb4.net"
                appAesPwd = "f81q30ryiacasda1"
                myImSocketHost = MY_IM_SOCKET_HOST_FAT
                myImHttpsHost = MY_IM_HTTP_HOST_FAT
            }

            HTTP_TYPE -> {
                appHost = "https://api.seekweb4.net"
                appHost2 = "$appHost/lixin"
                appImServerHost = "ws.seekweb4.net"
                appIcePassword = "aT3BxjPy4MRdC6@"
                appGroupAvatarHost = "https://api.seekweb4.net"
                appAesPwd = "1q30vqwerf8yiac3"
                myImSocketHost = MY_IM_SOCKET_HOST_RELEASE
                myImHttpsHost = MY_IM_HTTP_HOST_RELEASE
            }


            INPUT_TYPE -> {
                appHost = MMKV.defaultMMKV().decodeString(DEV_INPUT_BASE_URL)
                    ?: "https://api-fat.seekweb4.net"
                appHost2 = "$appHost/lixin"
                appImServerHost = "fat.seekweb4.net"
                appIcePassword = "aT3BxjPy4MRdC6@"
                appGroupAvatarHost = "https://api-fat.seekweb4.net"
                appAesPwd = "1q30vqwerf8yiac3"
                myImSocketHost = MY_IM_SOCKET_HOST_RELEASE
                myImHttpsHost = MY_IM_HTTP_HOST_RELEASE
            }

            RELEASE_TYPE -> {
                appHost = "http://119.45.9.228:82"
                appHost2 = "$appHost/lixin"
                appImServerHost = "119.45.9.228"
                appIcePassword = "aT3BxjPy4MRdC6@"
                appGroupAvatarHost = "http://119.45.9.228:8888"
                appAesPwd = "1q30vqwerf8yiac3"
                myImSocketHost = MY_IM_SOCKET_HOST_RELEASE
                myImHttpsHost = MY_IM_HTTP_HOST_RELEASE
            }

            FAT_IP -> {
                appHost = "https://api-fat.seekweb4.net"
                appHost2 = appHost
                appImServerHost = "testim.seekweb4.ai"
                appIcePassword = "aT3BxjPy4MRdC6@"
                appGroupAvatarHost = "https://api-fat.seekweb4.net"
                appAesPwd = "f81q30ryiacasda1"
                myImSocketHost = MY_IM_SOCKET_HOST_FAT
                myImHttpsHost = MY_IM_HTTP_HOST_FAT_IP
            }

            else -> {
                appHost = "https://app.seekweb4.net"
                appHost2 = appHost
                appImServerHost = "us.seekweb4.net"
                appIcePassword = "aT3BxjPy4MRdC6@"
                appGroupAvatarHost = "https://api-fat.seekweb4.net"
                appAesPwd = "f81q30ryiacasda1"
                myImSocketHost = MY_IM_SOCKET_HOST_RELEASE
                myImHttpsHost = MY_IM_HTTP_HOST_RELEASE
            }
        }

    }


    fun setHttpMode() {
        if (getIsDebug()) {
            setServer(getNetType())
        } else {
            setServer(BuildConfig.APP_ENVIRONMENT)
        }
    }


    private fun getNetType() = MMKV.defaultMMKV().decodeInt(NET_TYPE, FAT_TYPE)

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
