package com.lxkj.aiim.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.graphics.Point
import android.os.Build
import android.os.CountDownTimer
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewConfiguration
import android.view.WindowManager
import androidx.core.app.NotificationCompat
import com.cboda.baselibrary.event.AppEventCode
import com.lxkj.aiim.R
import com.lxkj.aiim.bean.MeetingData
import com.lxkj.aiim.lj.http.UrlConstants
import com.lxkj.aiim.utils.AUIVoiceRoomUIKit
import com.lxkj.aiim.net.manager.ServerManager
import com.lxkj.aiim.widge.VoiceWaveView
import com.example.asceneskit.AUIAPIConfig
import io.agora.auikit.model.AUICommonConfig
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.model.AUIRoomInfo
import io.agora.auikit.model.AUIUserThumbnailInfo
import io.agora.auikit.service.rtm.AUIRtmMessageRespObserver
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.util.concurrent.ConcurrentHashMap

/****
 * 这是会议功能的悬浮窗的服务
 */
class MeetingFloatService : Service(), AUIRtmMessageRespObserver {

    // 会议室状态管理
    private val roomTimers = ConcurrentHashMap<String, RoomTimerState>()

    private var windowManager: WindowManager? = null
    private var floatView: View? = null
    private var layoutParams: WindowManager.LayoutParams? = null
    private  var mRoomInfo: AUIRoomInfo?=null
    private  var mMeetData:MeetingData?=null
    private val handler = Handler(Looper.getMainLooper())


    override fun onBind(intent: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        EventBus.getDefault().register(this)
        isServiceRunning=true
        windowManager = getSystemService(Context.WINDOW_SERVICE) as WindowManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val roomInfo = intent?.getSerializableExtra(EXTRA_ROOM_INFO) as? AUIRoomInfo
        mRoomInfo=roomInfo

        initVoiceConfig()

//        if (!canDrawOverlays(this)) {
//            stopSelf()
//            return START_NOT_STICKY
//        }

        ensureForeground()
//        showFloatView(roomInfo)
        return START_STICKY
    }


    override fun onDestroy() {

        AUIVoiceRoomUIKit.getServiceByRoomId(mRoomInfo?.roomId?:"")?.let {
            it.rtmManager.unsubscribeMessage(this)
        }

        super.onDestroy()

        roomTimers.values.forEach { it.timer?.cancel() }
        roomTimers.clear()
        removeFloatView()
        isServiceRunning=false
        isFloatShow=false


        EventBus.getDefault().unregister(this)
        stopForeground(true)

    }




    /**
     * 初始化sdk的配置
     */
    private fun initVoiceConfig() {


        ServerManager.getUserInfo(this,""){
            it?.let {
                buildConfig(it.data?.mId?:"",it.data?.name?:"",it.data?.icon?:"")
            }
        }

    }

    private  fun buildConfig(id:String,userName:String,avatar:String){
        val config = AUICommonConfig()
        config.context = this
        config.appId = appId
        config.appCert = appCert
        config.basicAuth = ""
        config.host = host
        config.imAppKey = imAppKey
        config.imClientId = imClientId
        config.imClientSecret = imClientSecret
        config.owner = AUIUserThumbnailInfo().apply {
            userId = id
            this.userName = userName
            userAvatar = avatar
        }
        AUIVoiceRoomUIKit.init(
            config,
            AUIAPIConfig()
        )
        EventBus.getDefault().post(MeetingEvent(EVENT_SERVICE_STATE,config.owner))


    }

    private  fun getRoomInfo(meetingId:String,postHandler:Boolean){
        ServerManager.getMeetingRoomInfo(meetingId){
            it?.let {
                mMeetData=it
                // 更新会议室状态
                val state =roomTimers[it.roomId?:""]
                state?.endTime = it.endTime?:0
                state?.hasAlerted = false
                state?.isExtended = true

                // 重启计时器
                state?.timer?.cancel()
                startRoomTimer(it.roomId?:"",it.endTime?:0)


            }
        }

    }

    /**
     * 启动会议室计时器
     */
    private fun startRoomTimer(roomId: String, endTime: Long) {
        // 如果已有计时器，先取消
        cancelRoomTimer(roomId)

        val remainingMs = endTime - System.currentTimeMillis()

        // 如果会议已经结束或即将结束（小于1分钟），不创建计时器
        if (remainingMs <= 60 * 1000) return

        val state = RoomTimerState(
            roomId = roomId,
            endTime = endTime,
            hasAlerted = false,
            isExtended = false
        )

        state.timer = object : CountDownTimer(remainingMs, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                // 检查是否到达10分钟阈值且未提醒过
                if (millisUntilFinished <= 10 * 60 * 1000 &&
                    !state.hasAlerted &&
                    !state.isExtended) {
//                    showRenewalReminder(state)
                    //发送通知给页面续费提醒弹窗
                    EventBus.getDefault().post(MeetingEvent(AppEventCode.EVENT_MEETING_ADD_TIP,mMeetData))
                    state.hasAlerted = true
                }
            }

            override fun onFinish() {
                // 会议结束处理
                roomTimers.remove(roomId)
                // 可以添加会议结束的通知或自动退出逻辑
            }
        }.start()

        roomTimers[roomId] = state
    }

    /**
     * 取消会议室计时器
     */
    private fun cancelRoomTimer(roomId: String) {
        roomTimers[roomId]?.let {
            it.timer?.cancel()
            roomTimers.remove(roomId)
        }
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    fun handleEvent(event: MeetingEvent<*>?){
        event?.eventCode?.let {
            when(it){
                AppEventCode.EVENT_MEETING_UN_ADD_TIME->{
                    mRoomInfo?.roomId?.let {roomId->
                        roomTimers[roomId]?.let {
                            it.hasAlerted=true
                            roomTimers[roomId]=it
                        }
                    }
                }
                AppEventCode.EVENT_MEETING_ADD_TIME->{
                      mRoomInfo?.roomId?.let {
                          getRoomInfo(it,true)
                      }

                }
                EVENT_SHOW_FLOAT->{
                    val isShow = event.data as Boolean
                     if (isShow){
                         mRoomInfo?.let {
                             showFloatView(it)
                         }
                     }else{

                     }
                }
                EVENT_SUBSCRIBE_MSG->{
                    mRoomInfo?.let {
                        AUIVoiceRoomUIKit.getServiceByRoomId(it.roomId)?.let {
                            it.rtmManager.subscribeMessage(this)
                        }
                        if (AUIRoomContext.shared().isRoomOwner(it.roomId)) {
                            getRoomInfo(it.roomId,true)
                        }
                    }
                }
                EVENT_BACKGROUND_QUITE->{
                    mRoomInfo?.let {info->
                        val owner = (info.owner?.userId == AUIRoomContext.shared().currentUserInfo.userId)
                        if (owner){
                            ServerManager.updateMeetingRoomState(this,info.roomId?:"","destroyed"){
                                shutDownRoom(info.roomId,info.owner?.userId?:"")
                            }
                        }else{
                            shutDownRoom(info.roomId, info.owner?.userId?:"")
                        }
                    }

                }
                else -> {}

            }
        }
    }

    private  fun shutDownRoom(roomId: String, ownerId: String) {
        val isRoomOwner = (ownerId == AUIRoomContext.shared().currentUserInfo.userId)
        if(isRoomOwner){
            try {
                AUIVoiceRoomUIKit.getServiceByRoomId(roomId)?.let {
                    it.imManagerService?.userDestroyedChatroom()
                }

            }catch (e: Exception){
                Log.d("销毁房间失败","${e.message}",e)
            }
        }else {
            AUIVoiceRoomUIKit.getServiceByRoomId(roomId)?.let {
                it.imManagerService?.userQuitRoom()
            }
        }
        AUIVoiceRoomUIKit.destroyRoom(roomId)
        stopSelf()
    }

     fun showFloatView(roomInfo: AUIRoomInfo?) {
        if (floatView != null) return

        val inflater = LayoutInflater.from(this)
        val view = inflater.inflate(R.layout.view_meeting_float, null)
        floatView = view


        val type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
        } else {
            @Suppress("DEPRECATION")
            WindowManager.LayoutParams.TYPE_PHONE
        }

        val displaySize = Point()
        windowManager?.defaultDisplay?.getSize(displaySize)

        val lp = WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            type,
            WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE or
                    WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS or
                    WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
            PixelFormat.TRANSLUCENT
        )
        lp.gravity = Gravity.TOP or Gravity.START
        lp.x = displaySize.x - dp(84)
        lp.y = dp(120)
        layoutParams = lp
        isFloatShow=true
         view.findViewById<VoiceWaveView>(R.id.voice_wave)?.let {
             it.startLoopAnim()
         }

        setupDragAndClick(view, roomInfo)
        windowManager?.addView(view, lp)
    }

    private fun removeFloatView() {
        floatView?.let { v ->
            v.findViewById<VoiceWaveView>(R.id.voice_wave)?.let {
                it.stopLoopAnim()
            }
            try {
                windowManager?.removeView(v)
            } catch (_: Exception) {
            }
        }
        floatView = null

    }

    private fun setupDragAndClick(view: View, roomInfo: AUIRoomInfo?) {
        var lastX = 0f
        var lastY = 0f
        var downX = 0f
        var downY = 0f
        val touchSlop = ViewConfiguration.get(this).scaledTouchSlop

        view.setOnTouchListener { _, event ->
            val lp = layoutParams ?: return@setOnTouchListener false
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    downX = event.rawX
                    downY = event.rawY
                    lastX = event.rawX
                    lastY = event.rawY
                }

                MotionEvent.ACTION_MOVE -> {
                    val dx = (event.rawX - lastX).toInt()
                    val dy = (event.rawY - lastY).toInt()
                    lp.x += dx
                    lp.y += dy
                    windowManager?.updateViewLayout(view, lp)
                    lastX = event.rawX
                    lastY = event.rawY
                }

                MotionEvent.ACTION_UP -> {
                    val moved = (kotlin.math.abs(event.rawX - downX) > touchSlop) ||
                            (kotlin.math.abs(event.rawY - downY) > touchSlop)
                    if (!moved) {
                        // 点击返回房间页面
                        tryStartActivity(roomInfo)
                        removeFloatView()
//                        stopSelf()
                    }
                }
            }
            true
        }
    }

    private fun tryStartActivity(roomInfo: AUIRoomInfo?) {
//        if (roomInfo!=null){
//            MeetingRoomActivity.launch(this,roomInfo.roomId,roomInfo.owner?.userId?:"",roomInfo.roomName,false,true,false)
//        }

        val pkg = packageName
        val clazz = Class.forName("com.lxkj.aiim.activity.MeetingRoomActivity")
        val intent = Intent(this, clazz)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        if (roomInfo != null) {
            intent.putExtra(EXTRA_ROOM_INFO, roomInfo)
            // 透传是否创建房间与主题由外层自行处理，这里仅回到页面
            intent.putExtra(EXTRA_IS_CREATE_ROOM, false)
            intent.putExtra(EXTRA_HOST_ID, roomInfo.owner?.userId)
            intent.putExtra(EXTRA_TITLE_ID, roomInfo.roomName)
            intent.putExtra(EXTRA_ROOM_ID, roomInfo.roomId)
            intent.putExtra(EXTRA_THEME_ID,  com.examole.asceneskit.R.style.Theme_VoiceRoom_Light)
        }
        startActivity(intent)
    }

    private fun ensureForeground() {
        val channelId = "voice_room_float_channel"
        val channelName = "视频会议"
        val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (nm.getNotificationChannel(channelId) == null) {
                nm.createNotificationChannel(
                    NotificationChannel(channelId, channelName, NotificationManager.IMPORTANCE_MIN)
                )
            }
        }

        val contentIntent = PendingIntent.getActivity(
            this, 0,
            packageManager.getLaunchIntentForPackage(packageName),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("视频会议正在后台运行")
            .setContentText("点击回到视频会议")
            .setSmallIcon(android.R.drawable.ic_menu_info_details)
            .setOngoing(true)
            .setContentIntent(contentIntent)
            .build()

        startForeground(1001, notification)
    }

    private fun dp(value: Int): Int {
        return (resources.displayMetrics.density * value + 0.5f).toInt()
    }

    /**
     * {"meeting_cmd":"change_mic","cmd_params":{"allMic":"0"}}
     * {"meeting_cmd":"change_mute","cmd_params":{"allMute":"0"}}
     */
    override fun onMessageReceive(
        channelName: String,
        publisherId: String,
        message: String
    ) {
        try {
            val msgObj= JSONObject(message)
             if (!msgObj.has("meeting_cmd") || msgObj.isNull("meeting_cmd")){
                 return
             }
            val cmdAction= msgObj.optString("meeting_cmd")
            if (!msgObj.has("cmd_params")||msgObj.isNull("cmd_params")){
                return
            }
                val paramsObj= msgObj.getJSONObject("cmd_params")

                 handleMsgAction(cmdAction,paramsObj)

        }catch (e: Exception){
            Log.e("rtm消息解析出错", "处理消息出错: ${e.message}", e)
        }



    }

    /**
     * 处理rtm自定义消息的逻辑
     */
    private fun handleMsgAction(action: String, paramsObj: JSONObject) {

         when(action){
             "change_mute"->{
                 val allMute =if (paramsObj.has("allMute")&&!paramsObj.isNull("allMute")) {
                     paramsObj.getString("allMute") != "0"
                 }else{
                     true
                 }
                 mRoomInfo?.roomId?.let {
                     AUIVoiceRoomUIKit.setRoomMuteState(it,allMute)
                 }

                 EventBus.getDefault().post(MeetingEvent(EVENT_CHANGE_MUTE, MeetingAllEvent(null,allMute)))

             }
             "change_mic" ->{
                 val allMic =if (paramsObj.has("allMic")&&!paramsObj.isNull("allMic")) {
                     paramsObj.getString("allMic") == "1"
                 }else{
                     false
                 }
                 mRoomInfo?.roomId?.let {
                     AUIVoiceRoomUIKit.getServiceByRoomId(it)?.let {
//                         it.micSeatService.muteAllAudioSeat(allMic,object : AUICallback{
//                             override fun onResult(error: AUIException?) {
//                                 if (error!=null){
//                                     Log.e("rtm消息", "设置全禁麦失败: ${error.message}", error)
//                                 }else{
//
//                                 }
//                             }
//
//                         })
                     }
                 }



                 EventBus.getDefault().post(MeetingEvent(EVENT_CHANGE_MIC, MeetingAllEvent(allMic,null)))




             }
             else -> {

             }
         }


    }


    data class MeetingEvent<T>(val eventCode:Int,val data:T)

    data class MeetingAllEvent(val allMic: Boolean?=false,val allMute: Boolean?=true)


    /**
     * 会议室状态数据类
     */
    data class RoomTimerState(
        val roomId: String,
        var endTime: Long,
        var hasAlerted: Boolean,
        var isExtended: Boolean,
        var timer: CountDownTimer? = null
    )




    companion object{
         var isFloatShow = false
        var isServiceRunning = false

        private val EXTRA_IS_CREATE_ROOM = "isCreateRoom"
        private val EXTRA_ROOM_INFO = "roomInfo"
        private val EXTRA_THEME_ID = "themeId"
        private val EXTRA_ROOM_ID = "roomId"
        private val EXTRA_HOST_ID = "hostId"
        private val EXTRA_TITLE_ID = "titleId"

        //显示悬浮窗
        const val EVENT_SHOW_FLOAT=10001
        const val EVENT_SERVICE_STATE=10002
        const val EVENT_SUBSCRIBE_MSG=10003
        const val EVENT_RTM_MSG_EVENT=10004

        const val  EVENT_CHANGE_MIC=10005

        const val  EVENT_CHANGE_MUTE=10006
        const val  EVENT_BACKGROUND_QUITE=10007


        //显示提醒
        const val EVENT_SHOW_REMINING=10008


//        private  const val  appId="d1f140de133c4508a532c0033840a801"
//        private  const val  appCert="3c78a21e0b4b4db9a025b61c61c0a087"
//        private  const val  host=UrlConstants.HOST
//        private  const val  imAppKey="411369491#1583729"
//        private  const val  imClientId="0040aa35a98749ecb5b207948d5351f2";
//        private  const val  imClientSecret="aed98e56a615417aa0c9ace3fb5359cd";
        private  const val  appId="91d54a270a4b41ea809164bd0c2486ed"
        private  const val  appCert="a22b2b5d67614df2a123c7b7a88644d6"
        private   var  host=UrlConstants.HOST
        private  const val  imAppKey="411369491#1597850"
        private  const val  imClientId="0040aa35a98749ecb5b207948d5351f2";
        private  const val  imClientSecret="aed98e56a615417aa0c9ace3fb5359cd";


        fun start(context: Context, roomInfo: AUIRoomInfo) {
//            if (!canDrawOverlays(context)) return
            val i = Intent(context, MeetingFloatService::class.java)
            i.putExtra(EXTRA_ROOM_INFO, roomInfo)
            context.startService(i)
        }

        fun stop(context: Context) {
            isServiceRunning=false
            context.stopService(Intent(context, MeetingFloatService::class.java))
        }



        fun canDrawOverlays(context: Context): Boolean {
            return Settings.canDrawOverlays(context)
        }
    }


}