package com.lxkj.aiim.app

import android.app.Activity
import android.app.ActivityManager
import android.app.Application
import android.content.Context
import android.graphics.Color
import android.net.http.HttpResponseCache
import android.os.Process
import android.text.TextUtils
import android.view.View
import androidx.lifecycle.Observer
import cn.wildfire.chat.kit.ChatManagerHolder
import cn.wildfire.chat.kit.Config
import cn.wildfire.chat.kit.WfcUIKit
import cn.wildfire.chat.kit.conversation.message.viewholder.BiaoQingMessageContentViewHolder
import cn.wildfire.chat.kit.conversation.message.viewholder.HongBaoMessageContentViewHolder
import cn.wildfire.chat.kit.conversation.message.viewholder.LingQuMessageContentViewHolder
import cn.wildfire.chat.kit.conversation.message.viewholder.MessageViewHolderManager
import cn.wildfire.chat.kit.conversation.message.viewholder.ZhuanZhangJieShouMessageContentViewHolder
import cn.wildfire.chat.kit.conversation.message.viewholder.ZhuanZhangMessageContentViewHolder
import cn.wildfire.chat.kit.third.location.viewholder.LocationMessageContentViewHolder
import cn.wildfirechat.remote.ChatManager
import cn.wildfirechat.remote.OnConnectToServerListener
import com.cboda.baselibrary.app.UtilApplication
import com.cboda.baselibrary.network.config.NetworkConfig
import com.cboda.baselibrary.toast.ToastUtils
import com.cboda.baselibrary.utils.initBuGly
import com.danikula.videocache.HttpProxyCacheServer
import com.didichuxing.doraemonkit.DoKit
import com.lxkj.aiim.BuildConfig
import com.lxkj.aiim.R
import com.lxkj.aiim.lj.configuration.AppConsts
import com.lxkj.aiim.lj.http.UrlConstants
import com.lxkj.aiim.lj.utils.SharePrefUtil
import com.lxkj.aiim.net.LiXinMyHttpLoggingInterceptor
import com.lxkj.aiim.service.AppService
import com.lxkj.aiim.utils.AppUtil
import com.cboda.baselibrary.bean.Constant
import com.cboda.baselibrary.utils.HttpsConfig.setHttpMode
import com.cboda.baselibrary.network.ResInterceptor
import com.lxkj.aiim.utils.DeviceUtils
import com.cboda.baselibrary.utils.MMKVUtils
import com.cboda.baselibrary.utils.clearUserDataLiveData
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.getIsDebug
import com.blankj.utilcode.util.ActivityUtils.addActivityLifecycleCallbacks
import com.blankj.utilcode.util.Utils
import com.lxkj.aiim.utils.PixelUtils
import com.lxkj.aiim.yehuo.service.WfcDefaultPortraitProvider
import com.lzy.okgo.OkGo
import com.lzy.okgo.interceptor.HttpLoggingInterceptor
import com.lzy.okgo.model.HttpHeaders
import com.scwang.smart.refresh.footer.BallPulseFooter
import com.scwang.smart.refresh.header.MaterialHeader
import com.scwang.smart.refresh.layout.SmartRefreshLayout
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit
import java.util.logging.Level


class LiXinApp : UtilApplication(), OnConnectToServerListener {


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
                footer.minimumHeight = PixelUtils.dip2px(context, 50f)
                footer
            }
        }

        fun initOkGo() {
            val builder = OkHttpClient.Builder()
            builder.connectTimeout(180000, TimeUnit.MILLISECONDS)
            builder.readTimeout(180000, TimeUnit.MILLISECONDS)
            val headers = HttpHeaders()
            headers.put("apptoken", Constant.getUid() ?: "")
            headers.put("sensorNo", DeviceUtils.getDeviceId(mContext!!))
            if (getIsDebug()) {
//                val loggingInterceptor = LiXinMyHttpLoggingInterceptor("OKGO")
//                //log打印级别，决定了log显示的详细程度
//                loggingInterceptor.setPrintLevel(HttpLoggingInterceptor.Level.BODY)
//                //log颜色级别，决定了log在控制台显示的颜色
//                loggingInterceptor.setColorLevel(Level.INFO)
                val loggingInterceptor = ResInterceptor("AppHttpOkGo")
                builder.addInterceptor(loggingInterceptor)
            }
            OkGo.getInstance().init(mContext).setOkHttpClient(builder.build())
                .addCommonHeaders(headers)
        }

        @JvmStatic
        fun getProxy(context: Context): HttpProxyCacheServer {
            val app = context.applicationContext as LiXinApp
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
        initBuGly(this, "03a4cc2bab")
        MMKVUtils.init(this)
        setHttpMode()
        ToastUtils.init(this)
        val cacheDir = File(applicationContext.cacheDir, "http")
        HttpResponseCache.install(cacheDir, 1024 * 1024 * 128)
//        initDoraemonKit()

        lateInitSdk()
        initYeHuo()
        addActivityLifecycleCallbacks(lifeListener)
        clearUserDataLiveData.removeObserver(clearUserDataListener)
        clearUserDataLiveData.observeForever(clearUserDataListener)
    }

    private val clearUserDataListener = Observer<Boolean> { _ ->
        AppUtil.clearUserData(this)
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
            eLogD(tAG, "onResumed:${activity.javaClass.simpleName}")
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

    private fun initDoraemonKit() {

        if (BuildConfig.DEBUG) {
            DoKit.Builder(this)
                .build()
        }
    }

    private fun initNetworkConfig() {
        NetworkConfig.init(UrlConstants.HOST + "/")
        NetworkConfig.updateAppToken(SharePrefUtil.getString(this, AppConsts.APPTOKEN, ""))
    }

    private fun lateInitSdk(): Unit {
//        if (!MMKVUtils.isPrivacyready()) {
//            JCollectionAuth.setAuth(this, false) // 后续初始化过程将被拦截
//        } else {
        initOkGo()
        initNetworkConfig()
//        }
    }

    private fun initYeHuo() {
        AppService.validateConfig(this)
        // 只在主进程初始化，否则会导致重复收到消息
        if (getCurProcessName(this) == BuildConfig.APPLICATION_ID) {

            val spconfig = getSharedPreferences("wfc_kit_config", MODE_PRIVATE)
            spconfig?.edit()?.putBoolean("darkTheme", false)?.apply()

            // 如果uikit是以aar的方式引入 ，那么需要在此对Config里面的属性进行配置，如：
            // Config.IM_SERVER_HOST = "im.example.com";
            val wfcUIKit = WfcUIKit.getWfcUIKit()
            wfcUIKit.init(this)
            wfcUIKit.setEnableNativeNotification(true)
            wfcUIKit.appServiceProvider = AppService.Instance()
            //            PushService.init(this, BuildConfig.APPLICATION_ID);
            MessageViewHolderManager.getInstance().registerMessageViewHolder(
                LocationMessageContentViewHolder::class.java,
                R.layout.conversation_item_location_send,
                R.layout.conversation_item_location_left
            )
            MessageViewHolderManager.getInstance().registerMessageViewHolder(
                ZhuanZhangMessageContentViewHolder::class.java,
                R.layout.chat_zhunzhang_layout, R.layout.chat_zhunzhang_layout_left
            )
            MessageViewHolderManager.getInstance().registerMessageViewHolder(
                ZhuanZhangJieShouMessageContentViewHolder::class.java,
                R.layout.chat_zhunzhang_layout, R.layout.chat_zhunzhang_layout_left
            )

            MessageViewHolderManager.getInstance().registerMessageViewHolder(
                HongBaoMessageContentViewHolder::class.java,
                com.cboda.baselibrary.R.layout.chat_hongbao_layout,
                com.cboda.baselibrary.R.layout.chat_hongbao_layout
            )

            MessageViewHolderManager.getInstance().registerMessageViewHolder(
                BiaoQingMessageContentViewHolder::class.java,
                R.layout.conversation_item_sticker_send, R.layout.conversation_item_sticker_receive
            )

            MessageViewHolderManager.getInstance().registerMessageViewHolder(
                LingQuMessageContentViewHolder::class.java,
                R.layout.chat_lingqu_layout, R.layout.chat_lingqu_layout
            )

            setupWFCDirs()
            val userId = MMKVUtils.getRealUserId()
            val yehuoUserToken = MMKVUtils.getYeHuoToken()
            if (userId?.isNotEmpty() == true && yehuoUserToken?.isNotEmpty() == true) {
                //需要注意token跟clientId是强依赖的，一定要调用getClientId获取到clientId，然后用这个clientId获取token，这样connect才能成功，如果随便使用一个clientId获取到的token将无法链接成功。
                //另外不能多次connect，如果需要切换用户请先disconnect，然后3秒钟之后再connect（如果是用户手动登录可以不用等，因为用户操作很难3秒完成，如果程序自动切换请等3秒）
                val connextStatus = ChatManagerHolder.gChatManager.connect(userId, yehuoUserToken)
                AppUtil.debug("野火", "connect----> $connextStatus")
            }
//
//            if (!TextUtils.isEmpty(Config.ORG_SERVER_ADDRESS)) {
//                val organizationService: OrganizationService = OrganizationService.Instance()
//                wfcUIKit.organizationServiceProvider = organizationService
//            }
            ChatManager.Instance()
                .setDefaultPortraitProviderClazz(WfcDefaultPortraitProvider::class.java)
            ChatManager.Instance().addConnectToServerListener(this)
            val sp = getSharedPreferences(Config.SP_CONFIG_FILE_NAME, MODE_PRIVATE)
            Config.ENABLE_AUDIO_MESSAGE_AMPLIFICATION = sp.getBoolean(
                "audioMessageAmplificationEnabled",
                Config.ENABLE_AUDIO_MESSAGE_AMPLIFICATION
            )

        }

    }

    private fun setupWFCDirs() {
        Config.VIDEO_SAVE_DIR = getDir("video", MODE_PRIVATE).absolutePath
        Config.AUDIO_SAVE_DIR = getDir("audio", MODE_PRIVATE).absolutePath
        Config.PHOTO_SAVE_DIR = getDir("photo", MODE_PRIVATE).absolutePath
        Config.FILE_SAVE_DIR = getDir("file", MODE_PRIVATE).absolutePath
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