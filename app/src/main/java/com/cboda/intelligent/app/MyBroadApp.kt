package com.cboda.intelligent.app

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.graphics.Color
import android.net.http.HttpResponseCache
import android.os.Process
import android.text.TextUtils
import androidx.lifecycle.Observer
import com.cboda.chatui.ChatManagerBroadHolder
import com.cboda.chatui.BroadConfig
import com.cboda.chatui.WfcUIBroadKit
import com.cboda.chatui.conversation.message.viewholder.BiaoQingMessageContentViewBroadHolder
import com.cboda.chatui.conversation.message.viewholder.HongBaoMessageContentViewBroadHolder
import com.cboda.chatui.conversation.message.viewholder.LingQuMessageContentViewBroadHolder
import com.cboda.chatui.conversation.message.viewholder.MessageViewHolderBroadManager
import com.cboda.chatui.conversation.message.viewholder.ZhuanZhangJieShouMessageContentViewBroadHolder
import com.cboda.chatui.conversation.message.viewholder.ZhuanZhangMessageContentViewBroadHolder
import com.cboda.chatui.third.location.viewholder.LocationMessageContentViewBroadHolder
import cn.wildfirechat.remote.ChatManager
import cn.wildfirechat.remote.OnConnectToServerListener
import com.cboda.baselibrary.app.UtilBroadApplication
import com.cboda.baselibrary.network.config.NetworkBroadConfig
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.danikula.videocache.HttpProxyCacheServer
import com.cboda.intelligent.BuildConfig
import com.cboda.intelligent.R
import com.cboda.intelligent.configuration.AppBroadConsts
import com.cboda.intelligent.utils.UrlBroadBroadConstants
import com.cboda.intelligent.utils.SharePrefBroadBroadUtil
import com.cboda.intelligent.service.AppBroadService
import com.cboda.intelligent.utils.AppBroadUtils
import com.cboda.baselibrary.bean.BroadConstant
import com.cboda.baselibrary.utils.HttpsBroadConfig.setHttpMode
import com.cboda.baselibrary.network.ResBroadInterceptor
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.clearUserDataLiveData
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.getIsDebug
import com.blankj.utilcode.util.ActivityUtils.addActivityLifecycleCallbacks
import com.blankj.utilcode.util.Utils
import com.cboda.baselibrary.utils.CrashFactory
import com.cboda.baselibrary.utils.DeviceBroadUtil
import com.cboda.baselibrary.utils.getAppVersion
import com.cboda.chatui.conversation.message.viewholder.MeetEndMessageContentViewBroadHolder
import com.cboda.chatui.conversation.message.viewholder.MeetStartMessageContentViewBroadHolder
import com.cboda.intelligent.utils.PixelBroadBroadUtils
import com.cboda.intelligent.myim.service.WfcDefaultPortraitBroadProvider
import com.lzy.okgo.OkGo
import com.lzy.okgo.model.HttpHeaders
import com.scwang.smart.refresh.footer.BallPulseFooter
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit


class MyBroadApp : UtilBroadApplication(), OnConnectToServerListener {


    companion object {
        var mContext: Application? = null

        var routeHost: String? = null
        var routePort = 0

        var longLinkHost: String? = null
        var longLinkPort = 0

        var proxy: HttpProxyCacheServer? = null

        init {
            SmartRefreshLayout.setDefaultRefreshHeaderCreator { context, _ ->
                MaterialHeader(
                    context
                )
            }

            SmartRefreshLayout.setDefaultRefreshFooterCreator { context, _ ->
                val footer = BallPulseFooter(context)
                footer.setAnimatingColor(Color.parseColor("#9B4EDC"))
                footer.minimumHeight = PixelBroadBroadUtils.dip2px(context, 50f)
                footer
            }
        }

        fun initOkGo() {
            val builder = OkHttpClient.Builder()
            builder.connectTimeout(180000, TimeUnit.MILLISECONDS)
            builder.readTimeout(180000, TimeUnit.MILLISECONDS)
            val headers = HttpHeaders()
            headers.put("apptoken", BroadConstant.getAppToken() ?: "")
            headers.put("sensorNo", DeviceBroadUtil.getAndroidId(mContext))
            headers.put("App-Platform", "Android")
            headers.put("App-Version", getAppVersion(null))
            // 多语言头
            headers.put("Accept-Language", NetworkBroadConfig.getNetAppLanguage())
            if (getIsDebug()) {
//                val loggingInterceptor = LiXinMyHttpLoggingBroadInterceptor("OKGO")
//                //log打印级别，决定了log显示的详细程度
//                loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY)
//                //log颜色级别，决定了log在控制台显示的颜色
//                loggingInterceptor.setColorLevel(Level.INFO)
                val loggingInterceptor = ResBroadInterceptor("AppHttpOkGo")
                builder.addInterceptor(loggingInterceptor)
            }
            OkGo.getInstance().init(mContext).setOkHttpClient(builder.build())
                .addCommonHeaders(headers)
        }

        @JvmStatic
        fun getProxy(context: Context): HttpProxyCacheServer {
            val app = context.applicationContext as MyBroadApp
            return if (Companion.proxy == null) (app.newProxy().also {
                Companion.proxy = it
            }) else Companion.proxy!!
        }


    }


    private fun newProxy(): HttpProxyCacheServer {
        return HttpProxyCacheServer(this)
    }


    override fun onCreate() {
        super.onCreate()
        mContext = this
//        Fresco.initialize(this)
        MMKVBroadUtils.init(this)
        CrashFactory.crashLoad.initFireBase(this, com.cboda.baselibrary.BuildConfig.CRASH_KEY)
        setHttpMode()
        ToastBroadUtils.init(this)
        val cacheDir = File(applicationContext.cacheDir, "http")
        HttpResponseCache.install(cacheDir, 1024 * 1024 * 128)
        initDoraKit()
        lateInitSdk()
        initMyIm()
        addActivityLifecycleCallbacks(lifeListener)
        clearUserDataLiveData.removeObserver(clearUserDataListener)
        clearUserDataLiveData.observeForever(clearUserDataListener)
    }

    private val clearUserDataListener = Observer<Boolean> { _ ->
        AppBroadUtils.clearUserData(this)
    }
    private val lifeListener = object : Utils.ActivityLifecycleCallbacks() {
        private val tAG = "newActivity"
        override fun onActivityCreated(activity: Activity) {
            super.onActivityCreated(activity)
//            activity.window.decorView.systemUiVisibility = (
//                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                    )
            eLogD(tAG, "onCreated:${activity.javaClass.simpleName}")
        }

        override fun onActivityResumed(activity: Activity) {
            super.onActivityResumed(activity)
//            activity.window.decorView.systemUiVisibility = (
//                    View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
//                            or View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY
//                    )
            eLogD(tAG, "onResumed:${activity.javaClass.simpleName}")
        }

        override fun onActivityStarted(activity: Activity) {
            super.onActivityStarted(activity)
            eLogD(tAG, "onStarted:${activity.javaClass.simpleName}")
        }

        override fun onActivityPaused(activity: Activity) {
            super.onActivityPaused(activity)
            eLogD(tAG, "onPaused:${activity.javaClass.simpleName}")
        }

        override fun onActivityDestroyed(activity: Activity) {
            super.onActivityDestroyed(activity)
            eLogD(tAG, "onDestroyed:${activity.javaClass.simpleName}")
        }

        override fun onActivityStopped(activity: Activity) {
            super.onActivityStopped(activity)
            eLogD(tAG, "onStopped:${activity.javaClass.simpleName}")
        }
    }

    private fun initDoraKit() {
        if (getIsDebug()) {
//            DoKit.Builder(this)
//                .build()
        }
    }

    private fun initNetworkBroadConfig() {
        NetworkBroadConfig.init(UrlBroadBroadConstants.HOST + "/")
        NetworkBroadConfig.updateAppToken(
            SharePrefBroadBroadUtil.getString(
                this,
                AppBroadConsts.APPTOKEN,
                ""
            )
        )
    }

    private fun lateInitSdk(): Unit {
//        if (!MMKVBroadUtils.isPrivacyready()) {
//            JCollectionAuth.setAuth(this, false) // 后续初始化过程将被拦截
//        } else {
        initOkGo()
        initNetworkBroadConfig()
//        }
    }

    private fun initMyIm() {
        AppBroadService.validateConfig(this)
        // 只在主进程初始化，否则会导致重复收到消息
        if (getCurProcessName(this) == BuildConfig.APPLICATION_ID) {

            val spconfig = getSharedPreferences("wfc_kit_config", MODE_PRIVATE)
            spconfig?.edit()?.putBoolean("darkTheme", false)?.apply()

            // 如果uikit是以aar的方式引入 ，那么需要在此对Config里面的属性进行配置，如：
            // BroadConfig.IM_SERVER_HOST = "im.example.com";
            val wfcUIKit = WfcUIBroadKit.getWfcUIKit()
            wfcUIKit.init(this)
            wfcUIKit.setEnableNativeNotification(true)
            wfcUIKit.appServiceProvider = AppBroadService.Instance()
            //            PushService.init(this, BuildConfig.APPLICATION_ID);
            MessageViewHolderBroadManager.getInstance().registerMessageViewHolder(
                LocationMessageContentViewBroadHolder::class.java,
                R.layout.conversation_item_location_broad_send,
                R.layout.conversation_item_location_broad_left
            )
            MessageViewHolderBroadManager.getInstance().registerMessageViewHolder(
                ZhuanZhangMessageContentViewBroadHolder::class.java,
                R.layout.chat_zh_broad_un_zhang_layout, R.layout.chat_zh_broad_un_zhang_layout_left
            )
            MessageViewHolderBroadManager.getInstance().registerMessageViewHolder(
                ZhuanZhangJieShouMessageContentViewBroadHolder::class.java,
                R.layout.chat_zh_broad_un_zhang_layout, R.layout.chat_zh_broad_un_zhang_layout_left
            )

            MessageViewHolderBroadManager.getInstance().registerMessageViewHolder(
                HongBaoMessageContentViewBroadHolder::class.java,
                com.cboda.baselibrary.R.layout.chat_hongbao_broad_layout,
                com.cboda.baselibrary.R.layout.chat_hongbao_broad_layout
            )

            MessageViewHolderBroadManager.getInstance().registerMessageViewHolder(
                BiaoQingMessageContentViewBroadHolder::class.java,
                R.layout.conversation_item_sticker_broad_send, R.layout.conversation_item_sticker_broad_receive
            )

            MessageViewHolderBroadManager.getInstance().registerMessageViewHolder(
                LingQuMessageContentViewBroadHolder::class.java,
                R.layout.chat_ling_broad_qu_layout, R.layout.chat_ling_broad_qu_layout
            )
            MessageViewHolderBroadManager.getInstance().registerMessageViewHolder(
                MeetStartMessageContentViewBroadHolder::class.java,
                R.layout.chat_meet_start_end_layout, R.layout.chat_meet_start_end_layout
            )

            MessageViewHolderBroadManager.getInstance().registerMessageViewHolder(
                MeetEndMessageContentViewBroadHolder::class.java,
                R.layout.chat_meet_start_end_layout, R.layout.chat_meet_start_end_layout
            )



            setupWFCDirs()
            val userId = MMKVBroadUtils.getRealUserId()
            val myImUserToken = MMKVBroadUtils.getMyImToken()
            if (userId?.isNotEmpty() == true && myImUserToken?.isNotEmpty() == true) {
                //需要注意token跟clientId是强依赖的，一定要调用getClientId获取到clientId，然后用这个clientId获取token，这样connect才能成功，如果随便使用一个clientId获取到的token将无法链接成功。
                //另外不能多次connect，如果需要切换用户请先disconnect，然后3秒钟之后再connect（如果是用户手动登录可以不用等，因为用户操作很难3秒完成，如果程序自动切换请等3秒）
                val contextStatus =
                    ChatManagerBroadHolder.gChatManager.connect(userId, myImUserToken)
                eLogD("野火", "connect----> $contextStatus")
            }
//
//            if (!TextUtils.isEmpty(BroadConfig.ORG_SERVER_ADDRESS)) {
//                val organizationService: OrganizationService = OrganizationService.Instance()
//                wfcUIKit.organizationServiceProvider = organizationService
//            }
            ChatManager.Instance()
                .setDefaultPortraitProviderClazz(WfcDefaultPortraitBroadProvider::class.java)
            ChatManager.Instance().addConnectToServerListener(this)
            val sp = getSharedPreferences(BroadConfig.SP_CONFIG_FILE_NAME, MODE_PRIVATE)
            BroadConfig.ENABLE_AUDIO_MESSAGE_AMPLIFICATION = sp.getBoolean(
                "audioMessageAmplificationEnabled",
                BroadConfig.ENABLE_AUDIO_MESSAGE_AMPLIFICATION
            )

        }

    }

    private fun setupWFCDirs() {
        BroadConfig.VIDEO_SAVE_DIR = getDir("video", MODE_PRIVATE).absolutePath
        BroadConfig.AUDIO_SAVE_DIR = getDir("audio", MODE_PRIVATE).absolutePath
        BroadConfig.PHOTO_SAVE_DIR = getDir("photo", MODE_PRIVATE).absolutePath
        BroadConfig.FILE_SAVE_DIR = getDir("file", MODE_PRIVATE).absolutePath
    }

    private fun getCurProcessName(context: Context): String? {
        val pid = Process.myPid()
        val activityManager = context
            .getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (appProcess in activityManager
            .runningAppProcesses) {
            if (appProcess.pid == pid) {
                return appProcess.processName
            }
        }
        return null
    }

    override fun onConnectToServer(host: String?, ip: String?, port: Int) {
        if (TextUtils.isEmpty(ip)) {
            routeHost = host
            routePort = port
        } else {
            longLinkHost = host
            longLinkPort = port
        }
    }


}