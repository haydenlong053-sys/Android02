//package com.cboda.intelligent.activity
//
//import android.content.Context
//import android.content.Intent
//import android.content.pm.PackageManager
//import android.net.Uri
//import android.os.Build
//import android.os.Bundle
//import android.provider.Settings
//import android.util.Log
//import android.view.View
//import android.view.View.VISIBLE
//import android.view.WindowManager
//import android.widget.Toast
//import androidx.activity.OnBackPressedCallback
//import androidx.lifecycle.lifecycleScope
//import com.cboda.chatui.conversation.forward.ForwardBroadActivity
//import cn.wildfirechat.message.MeetingInviteMessageContent
//import cn.wildfirechat.message.Message
//import com.cboda.agora.ui.ui.micseats.MicSeatBroadType
//import com.cboda.baselibrary.event.AppBroadEvent
//import com.cboda.baselibrary.event.AppEventBroadCode
//import com.cboda.baselibrary.event.rtc.NetworkQualityEventBroadData
//import com.cboda.baselibrary.network.model.NetworkBroadResponse
//import com.cboda.baselibrary.toast.ToastBroadUtils
//import com.google.gson.reflect.TypeToken
//import com.cboda.baselibrary.bean.BaseBroadResponse
//import com.cboda.agora.AUIVoiceRoomBroadObserver
//import com.cboda.intelligent.bean.MeetingData
//import com.cboda.intelligent.bean.NoResultBroadBean
//import com.cboda.intelligent.utils.UrlBroadBroadConstants
//import com.cboda.intelligent.service.MeetingFloatBroadService
//import com.cboda.intelligent.service.MeetingFloatBroadService.MeetingEvent
//import com.cboda.intelligent.utils.AUIVoiceRoomUIBroadKit
//import com.cboda.intelligent.utils.JsonBroadUtils
//import com.cboda.intelligent.utils.OKGoBroadHelper
//import com.cboda.intelligent.utils.QuickJsonBroadUtils
//import com.cboda.intelligent.net.manager.ServerBroadManager
//import com.cboda.intelligent.utils.CountdownTimerBroadFlow
//import com.lzy.okgo.OkGo
//import com.lzy.okgo.callback.StringCallback
//import com.lzy.okgo.model.Response
//import com.cboda.agora.R
//import com.cboda.agora.databinding.VoiceRoomBroadActivityBinding
//import com.cboda.agora.impl.MeetingOperateBroadView
//import com.cboda.agora.ui.ui.basic.AUIAlertBroadDialog
//import com.cboda.agora.ui.ui.basic.AUIAlertSecondBroadDialog
//import com.cboda.agora.ui.utils.PermissionBroadHelp
//import com.cboda.intelligent.utils.AUIVoiceRoomUIBroadKit.renewToken
//import com.cboda.agora.service.model.AUIRoomBroadContext
//import com.cboda.agora.service.model.AUIRoomBroadInfo
//import com.cboda.agora.service.model.AUIUserThumbnailBroadInfo
//import com.cboda.agora.service.utils.AUIBroadLogger
//import com.cboda.baselibrary.base.BaseActivity
//import com.cboda.baselibrary.utils.TimeBroadBroadUtils
//import io.agora.rtc2.Constants
//import kotlinx.coroutines.delay
//import kotlinx.coroutines.launch
//import org.greenrobot.eventbus.EventBus
//import org.greenrobot.eventbus.Subscribe
//import org.greenrobot.eventbus.ThreadMode
//
//class MeetingRoomBroadActivity : BaseActivity(), AUIVoiceRoomBroadObserver {
//    private val mViewBinding by lazy { VoiceRoomBroadActivityBinding.inflate(layoutInflater) }
//    private val mPermissionHelp = PermissionBroadHelp(this)
//    private  var countdownFlow: CountdownTimerBroadFlow?=null
//
//
//
//    companion object {
//        private val EXTRA_IS_CREATE_ROOM = "isCreateRoom"
//        private val EXTRA_ROOM_INFO = "roomInfo"
//        private val EXTRA_THEME_ID = "themeId"
//        private val EXTRA_MUTE_STATE = "room_mute"
//        private val EXTRA_MIC_STATE = "room_mic"
//        private val EXTRA_ROOM_ID = "roomId"
//        private val EXTRA_HOST_ID = "hostId"
//        private val EXTRA_TITLE_ID = "titleId"
//
//        fun launch(context: Context, isCreateRoom: Boolean, roomInfo: AUIRoomBroadInfo, themeId: Int = R.style.Theme_VoiceRoom_Light) {
//            val intent = Intent(context, MeetingRoomBroadActivity::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            intent.putExtra(EXTRA_IS_CREATE_ROOM, isCreateRoom)
//            intent.putExtra(EXTRA_ROOM_INFO, roomInfo)
//            intent.putExtra(EXTRA_THEME_ID, themeId)
//            context.startActivity(intent)
//        }
//
//
//        fun launch(context: Context, roomId: String, ownerId: String,meetingTitle:String,isCreateRoom: Boolean=false,isMute: Boolean?=false,isMic: Boolean?=true) {
//            val intent = Intent(context, MeetingRoomBroadActivity::class.java)
//            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
//            intent.putExtra(EXTRA_ROOM_ID, roomId)
//            intent.putExtra(EXTRA_IS_CREATE_ROOM, isCreateRoom)
//            intent.putExtra(EXTRA_HOST_ID, ownerId)
//            intent.putExtra(EXTRA_MIC_STATE, isMic)
//            intent.putExtra(EXTRA_MUTE_STATE, isMute)
//            intent.putExtra(EXTRA_TITLE_ID, meetingTitle)
//            intent.putExtra(EXTRA_THEME_ID,  R.style.Theme_VoiceRoom_Light)
//            context.startActivity(intent)
//
//
//        }
//    }
//
//    private val themeId by lazy { intent.getIntExtra(EXTRA_THEME_ID, View.NO_ID) }
//    private val isCreateRoom by lazy { intent.getBooleanExtra(EXTRA_IS_CREATE_ROOM, false) }
//    private val roomId by lazy {
//        intent.getStringExtra(EXTRA_ROOM_ID).toString()
//    }
//    private val hostId by lazy {
//        intent.getStringExtra(EXTRA_HOST_ID).toString()
//    }
//    private val mHostRoomTitle by lazy {
//        intent.getStringExtra(EXTRA_TITLE_ID).toString()
//    }
//    private var roomInfo: AUIRoomBroadInfo? = null
//    private  var isJoinSuccess=false
//    private  var meetingData:MeetingData?=null
//
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        if (savedInstanceState != null) {
//            roomInfo?.roomId = savedInstanceState.getString("roomId").toString()
//            val owner = AUIUserThumbnailBroadInfo()
//            owner.userId = savedInstanceState.getString("userId").toString()
//            owner.userName = savedInstanceState.getString("userName").toString()
//            owner.userAvatar = savedInstanceState.getString("userAvatar").toString()
//            roomInfo?.owner = owner
//            roomInfo?.memberCount = savedInstanceState.getInt("onlineUsers")
//            roomInfo?.createTime = savedInstanceState.getLong("createTime")
//            roomInfo?.roomName = savedInstanceState.getString("roomName").toString()
//            roomInfo?.thumbnail = savedInstanceState.getString("thumbnail").toString()
//            roomInfo?.micSeatCount = savedInstanceState.getInt("micSeatCount")
//            roomInfo?.password = savedInstanceState.getString("password").toString()
//            roomInfo?.micSeatStyle = savedInstanceState.getString("micSeatStyle").toString()
//        }
//
//        if (themeId != View.NO_ID) {
//            setTheme(themeId)
//        }
//        setContentView(mViewBinding.root)
//        EventBus.getDefault().register(this)
//        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
//
//        bindPreRoomInfo()
//        mViewBinding.ivLeftRoom.setOnClickListener {
//            shutDownRoom()
//        }
//
//
//
//        mViewBinding.VoiceRoomView.setFragmentActivity(this)
//
//        mViewBinding.VoiceRoomView.setOnShutDownClick {
//            onUserLeaveRoom()
//        }
//        mViewBinding.VoiceRoomView.setOnRoomDestroyEvent {
//            shutDownRoom()
//        }
//        mViewBinding.VoiceRoomView.setPIPEvent {
//            if (checkOverlayPermission()){
//                EventBus.getDefault().post(MeetingEvent(MeetingFloatBroadService.EVENT_SHOW_FLOAT,true))
//                finish()
//
//            }else{
//
////                onUserLeaveRoom()
//            }
//
//        }
//
//        // 使用 OnBackPressedDispatcher 拦截所有返回操作（包括手势返回）
//        setupBackPressedHandler()
//
//        mViewBinding.VoiceRoomView.setClickMore {
//            val isRoomOwner = (roomInfo?.owner?.userId == AUIRoomBroadContext.shared().currentUserInfo.userId)
//            ServerBroadManager.getMeetingRoomInfo(roomId){
//                it?.let {
//                    meetingData=it
//                    runOnUiThread {
//                        val operateView = MeetingOperateBroadView(this,isRoomOwner,it.allMute?:false,it.allMic?:true)
//                        operateView.setOperateListener(object : MeetingOperateBroadView.OnOperateListener{
//                            override fun quiteRoom(isOwner: Boolean?) {
//                                onUserLeaveRoom()
//
//                            }
//
//                            override fun shareRoom() {
//                                ServerBroadManager.getUserInfo(this@MeetingRoomBroadActivity){
//                                    it?.let {
//                                        val meetingMessage=
//                                            MeetingInviteMessageContent()
//                                                .apply {
//                                                    meetingId=roomId
//                                                    idno=it.data?.mId?:""
//                                                    title=mHostRoomTitle
//                                                    desc="我在会议室等你，快进来吧"
//                                                    isAllMic = true
//                                                    isAllMute= AUIVoiceRoomUIBroadKit.isRoomMuted(roomId)
//
//                                                }
//                                        val message = Message()
//                                        message.content=meetingMessage
//                                        runOnUiThread {
//                                            val intent = Intent(this@MeetingRoomBroadActivity, ForwardBroadActivity::class.java)
//                                            intent.putExtra("message",message)
//                                            this@MeetingRoomBroadActivity.startActivity(intent)
//                                        }
//                                    }
//                                }
//
//
//
//                            }
//
//                            override fun openAllMic(isMic: Boolean) {
//                                val updateRoomId=it.roomId?:""
//                                ServerBroadManager.updateMeetingRoomInfo(this@MeetingRoomBroadActivity,updateRoomId, allMic = isMic,
//                                    allMute = it.allMute?:false ){
//                                    it?.let {roomData ->
//                                        AUIVoiceRoomUIBroadKit.getServiceByRoomId(updateRoomId)?.let {service->
//                                            val switchObj= QuickJsonBroadUtils.build(
////                                        "allMute" to  if (it.payload?.allMute?:false) "1" else "0",
//                                                "allMic" to  if (isMic) "1" else "0"
//                                            )
//                                            val msgObj= QuickJsonBroadUtils.build(
//                                                "meeting_cmd" to "change_mic",
//                                                "cmd_params" to switchObj
//                                            )
//                                            val jsonStr= QuickJsonBroadUtils.toJson(msgObj)
//                                            service.rtmManager.publish(service.channelName,"",jsonStr?:""){error->
//                                                if (error!=null){
//                                                    ToastBroadUtils.show(error.message)
//                                                }else{
//                                                    runOnUiThread {
//                                                        service.micSeatService?.muteAllAudioSeat(!isMic) {
//                                                            if (it==null){
//                                                                Log.e("muteAllAudioSeat","rtm麦位发送切换成功")
//                                                            }else{
//                                                                Log.e("muteAllAudioSeat","${it.code} 发送失败${it.message}",it)
//                                                            }
//
//                                                        }
//                                                    }
//                                                    //切换麦位及下方的开关
//
//                                                }
//
//                                            }
//                                        }
//                                    }
//                                }
//
//
//                            }
//
//                            override fun makeAllMute(isMute: Boolean) {
//                                ServerBroadManager.updateMeetingRoomInfo(this@MeetingRoomBroadActivity,it.roomId?:"", allMic = it.allMic?:true ,
//                                    allMute = isMute){ roomData ->
//                                    AUIVoiceRoomUIBroadKit.getServiceByRoomId(it.roomId?:"")?.let {service->
//                                        val switchObj= QuickJsonBroadUtils.build(
////                                       "allMic" to  if (it.payload?.allMic?:false) "1" else "0",
//                                            "allMute" to  if (isMute) "1" else "0"
//                                        )
//                                        val msgObj= QuickJsonBroadUtils.build(
//                                            "meeting_cmd" to "change_mute",
//                                            "cmd_params" to switchObj
//                                        )
//                                        val jsonStr= QuickJsonBroadUtils.toJson(msgObj)
//                                        service.rtmManager.publish(service.channelName,"",jsonStr?:""){error->
//                                            if (error!=null){
//                                                ToastBroadUtils.show(error.message)
//                                            }else{
//                                                //切换输入法
//                                                ToastBroadUtils.show("房主/管理员已${(if (isMute)"开启禁言" else "关闭禁言")}")
//
//                                            }
//
//                                        }
//                                    }
//                                }
//
//
//                            }
//
//                        })
//
//                        operateView.showPopupWindow()
//
//                    }
//                }
//            }
//
//        }
//
//
//        mViewBinding.VoiceRoomView.setKickUserClick {
////            ServerBroadManager.kickUser(this,roomInfo?.roomId?:"",it){
////
////            }
//        }
//
//
//
//
//    }
//
//    private  fun isFloatShow() : Boolean{
//        return MeetingFloatBroadService.isServiceRunning && MeetingFloatBroadService.isFloatShow
//    }
//
//    private  fun isServiceRun(): Boolean{
//        return MeetingFloatBroadService.isServiceRunning
//    }
//
//
//    private  fun showJoiningLayout(){
//        mViewBinding.clCountDown.visibility=View.GONE
//        mViewBinding.ivLeftRoom.visibility=View.VISIBLE
//        mViewBinding.tvRoomName.visibility=View.VISIBLE
//        mViewBinding.VoiceRoomView.visibility=View.GONE
//        mViewBinding.loadingView.visibility=View.VISIBLE
//    }
//
//    private  fun showCountDownLayout(meetingData: MeetingData) {
//        mViewBinding.ivLeftRoom.visibility=View.VISIBLE
//        mViewBinding.tvRoomName.visibility=View.VISIBLE
//        mViewBinding.VoiceRoomView.visibility=View.GONE
//        mViewBinding.loadingView.visibility=View.GONE
//        mViewBinding.clCountDown.visibility=View.VISIBLE
//        updateCountLayout(meetingData)
//
//    }
//
//    private  fun updateCountLayout(meetingData: MeetingData){
//        if (countdownFlow!=null){
////            countdownFlow?.cancel()
//            countdownFlow=null
//        }
//        countdownFlow = CountdownTimerBroadFlow(lifecycle,(meetingData.remainingTime?:0))
//        countdownFlow?.start()
//
//        // 订阅remainingTime，在countdownFlow初始化之后
//        lifecycleScope.launch {
//            countdownFlow?.remainingTime?.collect{
////                Log.e("倒计时","$it")
//                mViewBinding.tvCountDown.text="剩余${TimeBroadBroadUtils.formatDuration(it.toLong())}"
//            }
//        }
//
//        lifecycleScope.launch {
//            countdownFlow?.isFinished?.collect{isFinish->
//                if (isFinish){
//                    hideCountDownLayout()
//                    delay(3000)
//                    roomInfo?.let {roomInfo->
//                        mPermissionHelp.checkMicPerm(
//                            {
//                                AUIVoiceRoomUIBroadKit.generateToken(
//                                    roomInfo.roomId,
//                                    onSuccess = { roomConfig ->
//
//                                        ServerBroadManager.getMeetingRoomInfo(roomId){
//                                            if (it==null){
//                                                shutDownRoom()
//                                            }else{
//                                                this@MeetingRoomBroadActivity.meetingData=it
//                                                when(it.status?.lowercase()){
//                                                    ROOM_ACTIVE_STATE->{
//                                                        runOnUiThread {  showJoiningLayout() }
//                                                        AUIVoiceRoomUIBroadKit.launchRoom(
//                                                            roomInfo,
//                                                            roomConfig,
//                                                            mViewBinding.VoiceRoomView,
//                                                            completion = { error, _ ->
//                                                                if (error != null) {
//                                                                    shutDownRoom()
//                                                                }else{
//                                                                    runOnUiThread {
//                                                                        showJoinSuccessLayout()
//                                                                        it.allMute?.let {
//                                                                            AUIVoiceRoomUIBroadKit.setRoomMuteState(roomId,it)
//                                                                            mViewBinding.VoiceRoomView.setMuteEditLayout(AUIRoomBroadContext.shared().isRoomOwner(roomId),
//                                                                                AUIVoiceRoomUIBroadKit.isRoomMuted(roomId))
//                                                                        }
//                                                                        it.status.let {
//                                                                            when(it.lowercase()){
//                                                                                ROOM_ACTIVE_STATE->{
//
//                                                                                }
//                                                                                else -> {
//                                                                                    ToastBroadUtils.show("当前房间不是活动状态")
//                                                                                    shutDownRoom()
//                                                                                }
//                                                                            }
//                                                                        }
//                                                                    }
////                                                         loadMeetingInfo()
//                                                                }
//                                                            }
//                                                        )
//                                                        //                                             AUIVoiceRoomUIBroadKit.registerRespObserver(roomId = roomInfo.roomId, this@MeetingRoomBroadActivity)
//                                                    }
//                                                    ROOM_DESTORY_STATE->{
//                                                        runOnUiThread { showJoiningLayout() }
//                                                        mViewBinding.progress.setProgress(100,true)
//                                                        mViewBinding.tvStatusTips.text="该会议室已销毁"
////                                             AUIAlertBroadDialog(this).apply {
////                                                 setTitle("会议室状态异常")
////                                                 setMessage("此会议室已销毁")
////                                                 setPositiveButton("好的"){
////                                                     dismiss()
////                                                     shutDownRoom()
////                                                 }
////                                                 setCancelable(false)
////                                                 show()
////
////                                             }
//                                                    }
//                                                    ROOM_WAITE_CREATE_STATE->{
//                                                        mViewBinding.progress.setProgress(100,true)
//                                                        mViewBinding.tvStatusTips.text="该会议室待创建"
//                                                        showCountDownLayout(it)
//
//                                                    }
//
//                                                }
//
//                                            }
//                                        }
//
//
//                                    },
//                                    onFailure = {
//                                        shutDownRoom()
//                                    }
//                                )
//                            },
//                            {
//                                finish()
//                            },
//                            true
//                        )
//                    }
//                }
//
//
//            }
//        }
//
//        mViewBinding.tvTimeName.text=meetingData.meetingTimeName
//        mViewBinding.tvStartDate.text=
//            TimeBroadBroadUtils.formatTimestamp((meetingData.startTime?:0L).toString(),"yyyy年MM月dd日")
//        mViewBinding.tvEndDate.text=TimeBroadBroadUtils.formatTimestamp((meetingData.endTime?:0L).toString(),"yyyy年MM月dd日")
//        mViewBinding.tvStartTime.text=TimeBroadBroadUtils.formatTimestamp((meetingData.startTime?:0L).toString(),"HH:mm")
//        mViewBinding.tvEndTime.text=TimeBroadBroadUtils.formatTimestamp((meetingData.endTime?:0L).toString(),"HH:mm")
//
//
//
//
//    }
//    private  fun hideCountDownLayout(){
//        showJoiningLayout()
//    }
//
//    private  fun  showJoinSuccessLayout(){
//        Log.e("showJoinSuccessLayout","切换成功布局")
//        mViewBinding.ivLeftRoom.visibility=View.GONE
//        mViewBinding.tvRoomName.visibility=View.GONE
//        mViewBinding.VoiceRoomView.visibility=View.VISIBLE
//        mViewBinding.loadingView.visibility=View.GONE
//    }
//
//    private  fun bindPreRoomInfo(){
//
//        //判断悬浮窗是否存在，存在则是点击悬浮窗进来
//        if (isFloatShow()){
//            roomInfo= intent.getSerializableExtra(EXTRA_ROOM_INFO) as AUIRoomBroadInfo
//            showJoinSuccessLayout()
//            roomInfo?.let {info->
//                AUIVoiceRoomUIBroadKit.getServiceByRoomId(info.roomId)?.let {
//                    mViewBinding.VoiceRoomView.bindService(it)
//                    mViewBinding.VoiceRoomView.setMuteEditLayout(AUIRoomBroadContext.shared().isRoomOwner(info.roomId),
//                        AUIVoiceRoomUIBroadKit.isRoomMuted(info.roomId))
//                }
//            }
//        }else{
//            showJoiningLayout()
//            roomInfo = AUIRoomBroadInfo().apply {
//                this.roomId = this@MeetingRoomBroadActivity.roomId
//                roomName = mHostRoomTitle
//                owner = AUIUserThumbnailBroadInfo().apply {
//                    userId = hostId
//                    userName = "会议主持${hostId}"
//                    userAvatar = ""
//                }
//                micSeatStyle= MicSeatBroadType.EightTag.value.toString()
//
//            }
//            mViewBinding.tvRoomName.text=mHostRoomTitle
//
//            if (!isServiceRun()){
//                roomInfo?.let { MeetingFloatBroadService.start(this,it) }
//            }
//        }
//
//
//
//    }
//
//    private fun loadMeetingInfo() {
//        val params = HashMap<String, Any>()
//        OkGo.get<String>(UrlBroadBroadConstants.HOST + UrlBroadBroadConstants.GET_ROOM_INFO + roomId).execute(
//            object : StringCallback() {
//                override fun onSuccess(response: Response<String?>?) {
//                    var body = response?.body()
//
//
//                    val baseBean = JsonBroadUtils.getSuccessJsonBean(body)
//                    if (baseBean?.code == "200") {
//                        val type = object : TypeToken<NetworkBroadResponse<MeetingData>>() {}.type
//                        val roomBean =
//                            Gson().fromJson<NetworkBroadResponse<MeetingData>>(body, type)
////                        val roomBean = Gson().fromJson(body, CreateRoomBroadBean::class.java)
//                        roomBean.data?.let {
//                            meetingData=it
//                            it.allMute?.let {
//                                AUIVoiceRoomUIBroadKit.setRoomMuteState(roomId,it)
//                                mViewBinding.VoiceRoomView.setMuteEditLayout(AUIRoomBroadContext.shared().isRoomOwner(roomId),
//                                    AUIVoiceRoomUIBroadKit.isRoomMuted(roomId))
//                            }
//                            it.status?.let {
//                                when(it.lowercase()){
//                                    ROOM_ACTIVE_STATE->{
//
//                                    }
//                                    else -> {
//                                        ToastBroadUtils.show("当前房间不是活动状态")
//                                        shutDownRoom()
//                                    }
//                                }
//                            }
//                        }
////                        roomBean.data?.let {
////                            roomInfo?.roomName=it.payload?.roomName?:""
////                            roomInfo?.owner?.userName=it.payload?.user?.username?:""
////
////
////
////
////
////                        }
//
//
//                    }
//
//                }
//
//                override fun onError(response: Response<String>?) {
//                    super.onError(response)
//
//                }
//
//            }
//        )
//
//    }
//
//
//    override fun onSaveInstanceState(outState: Bundle) {
//        super.onSaveInstanceState(outState)
//        roomInfo?.let {
//            outState.putInt("themeId", themeId)
//            outState.putString("roomId", it.roomId)
//            outState.putString("userId", it.owner?.userId)
//            outState.putString("userName", it.owner?.userName)
//            outState.putString("userAvatar", it.owner?.userAvatar)
//            outState.putInt("onlineUsers", it.memberCount)
//            outState.putLong("createTime", it.createTime)
//            outState.putString("roomName", it.roomName)
//            outState.putString("thumbnail", it.thumbnail)
//            outState.putInt("micSeatCount", it.micSeatCount)
//            outState.putString("password", it.password)
//            outState.putString("micSeatStyle", it.micSeatStyle)
//        }
//    }
//
//    /**
//     * 设置返回键处理（包括手势返回）
//     * 使用 OnBackPressedDispatcher 可以拦截所有返回操作，包括：
//     * 1. 物理返回键
//     * 2. 系统手势返回（边缘滑动）
//     * 3. 其他返回操作
//     */
//    private fun setupBackPressedHandler() {
//        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                // 处理返回逻辑
//                if (mViewBinding.loadingView.visibility == VISIBLE || mViewBinding.clCountDown.visibility == VISIBLE) {
//                    shutDownRoom()
//                    return
//                }
//                if (checkOverlayPermission()) {
//                    EventBus.getDefault().post(MeetingEvent(MeetingFloatBroadService.EVENT_SHOW_FLOAT, true))
//                    finish()
//                } else {
//                    // 如果需要显示确认对话框，可以取消注释
//                    // onUserLeaveRoom()
//                }
//            }
//        })
//    }
//
//    /**
//     * @deprecated 在 Android 13+ 上可能无法拦截手势返回
//     * 请使用 setupBackPressedHandler() 中的 OnBackPressedDispatcher
//     */
////    @Deprecated("使用 OnBackPressedDispatcher 替代，以确保能拦截手势返回")
////    override fun onBackPressed() {
////        // 保留此方法以兼容旧版本，但实际处理逻辑已迁移到 setupBackPressedHandler()
////        // 如果 OnBackPressedDispatcher 的回调未启用，则执行默认行为
////        super.onBackPressed()
////    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//        EventBus.getDefault().unregister(this)
//        window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
//    }
//
//    private fun onUserLeaveRoom() {
//        val owner = (roomInfo?.owner?.userId == AUIRoomBroadContext.shared().currentUserInfo.userId)
//        AUIAlertBroadDialog(this).apply {
//
//            if (owner) {
//                setTitle("您要解散${roomInfo?.roomName}”吗？")
//                setMessage("解散房间并销毁群聊记录")
//            } else {
//                setTitle("您要退出${roomInfo?.roomName}”吗？")
//                setMessage("是否离开房间？")
//            }
//            setPositiveButton(getString(R.string.voice_room_confirm)) {
//                dismiss()
//                if (owner){
//                    ServerBroadManager.updateMeetingRoomState(this@MeetingRoomBroadActivity,roomInfo?.roomId?:"",ROOM_DESTORY_STATE){
//                        shutDownRoom()
//                    }
//                }else{
//
//                    shutDownRoom()
//                }
//            }
//            setNegativeButton("取消") {
//                dismiss()
//            }
//            show()
//        }
//    }
//
//    override fun onTokenPrivilegeWillExpire(roomId: String) {
//        super.onTokenPrivilegeWillExpire(roomId)
//        AUIVoiceRoomUIBroadKit.generateToken(
//            roomId,
//            onSuccess = {
//                renewToken(roomId, it)
//            },
//            onFailure = {
//                AUIBroadLogger.logger()
//                    .e("VoiceRoomActivity", "onTokenPrivilegeWillExpire generateToken error $it")
//            })
//    }
//
//    private fun shutDownRoom() {
//        AUIBroadLogger.logger().d("VoiceRoomActivity", "shutDownRoom ...")
//        val roomId = roomId
//        mViewBinding.VoiceRoomView.destoryRoom()
//        AUIVoiceRoomUIBroadKit.destroyRoom(roomId)
//        AUIVoiceRoomUIBroadKit.unRegisterRespObserver(roomId, this)
//        MeetingFloatBroadService.stop(this)
//        finish()
//    }
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun handleEventOnlyCode(evetCode:Int){
//        when(evetCode){
//            1001->{
//                shutDownRoom()
//            }
//        }
//    }
//
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun onAppEventReceiver(event: AppBroadEvent<*>?){
//        Log.d("MeetingRoomBroadActivity", "收到EventBus事件: eventCode=${event?.eventCode}")
//        event?.let {
//            when(event.eventCode){
//                AppEventBroadCode.EVENT_QUALITY_CHANGE->{
////                    Log.d("MeetingRoomBroadActivity", "收到网络质量变化事件")
//                    val networkQualityEventData = it.data as NetworkQualityEventBroadData
////                    Log.d("MeetingRoomBroadActivity", "事件数据: uid=${networkQualityEventData.uid}, txQuality=${networkQualityEventData.txQuality}, rxQuality=${networkQualityEventData.rxQuality}")
////                    Log.d("MeetingRoomBroadActivity", "当前用户ID: ${AUIRoomBroadContext.shared().currentUserInfo.userId}")
//                    if (networkQualityEventData.uid == AUIRoomBroadContext.shared().currentUserInfo.userId||networkQualityEventData.uid=="0") {
////                        Log.d("MeetingRoomBroadActivity", "用户ID匹配，处理网络质量事件")
//                        if (networkQualityEventData.txQuality>= Constants.QUALITY_VBAD||networkQualityEventData.rxQuality>= Constants.QUALITY_VBAD){
////                            ToastBroadUtils.show("网络质量不佳")
//                        }else{
////                            Log.e("网络质量","上行:${networkQualityEventData.txQuality} 下行:${networkQualityEventData.rxQuality}")
//                        }
//                    } else {
////                        Log.d("MeetingRoomBroadActivity", "用户ID不匹配，忽略事件")
//                    }
//
//                }
//                else -> {
//                    Log.d("MeetingRoomBroadActivity", "未处理的事件类型: ${event.eventCode}")
//                }
//            }
//        }
//
//    }
//
//
//
//
//    @Subscribe(threadMode = ThreadMode.MAIN)
//    fun handleEvent(event: MeetingEvent<*>?){
//        event?.eventCode?.let {
//            when(it){
//                MeetingFloatBroadService.EVENT_SERVICE_STATE->{
//                    val owner = event.data as AUIUserThumbnailBroadInfo
//                    owner?.let {
//                        checkMicPerm(owner)
//                    }
//
//                }
//                MeetingFloatBroadService.EVENT_CHANGE_MIC->{
//                    val data = event.data as MeetingFloatBroadService.MeetingAllEvent
//                    ToastBroadUtils.show(if (data.allMic!=true){"房主/管理员开启了全员禁麦"} else {"房主/管理员取消了全员禁麦"})
//                }
//                MeetingFloatBroadService.EVENT_CHANGE_MUTE->{
//                    val data = event.data as MeetingFloatBroadService.MeetingAllEvent
//                    ToastBroadUtils.show(if (data.allMute==true){"房主/管理员开启了全员禁言"} else {"房主/管理员取消了全员禁言"})
//                    val roomOwner = AUIRoomBroadContext.shared().isRoomOwner(roomId)
//                    mViewBinding.VoiceRoomView.setMuteEditLayout(roomOwner,data.allMute?:false)
//
//                }
//                AppEventBroadCode.EVENT_MEETING_ADD_TIP->{
//                    val postData = event.data as MeetingData
//                    AUIAlertSecondBroadDialog(this).apply {
//                        setTitle("提示")
//                        setMessage("还有10分钟结束,是否加时延长会议?")
//                        if (postData.isNonPayPwd == true){
//                            setMessage2("${postData.minMeetingTimeName}")
//                            setPositiveButton("加时"){
//                                dismiss()
//                                ServerBroadManager.addMeetingTime(this@MeetingRoomBroadActivity,postData.groupId?:"",
//                                    postData.ownerId?:"",
//                                    (postData.minMeetingTime?:0).toString(),postData.roomId?:""){
//                                    if (it!=null){
//                                        ToastBroadUtils.show(it.msg)
//                                        if (it.success){
//                                            EventBus.getDefault().post(MeetingEvent(AppEventBroadCode.EVENT_MEETING_ADD_TIME,true))
//                                        }else{
//                                            if ((it.data?.createout?:-1)==1){
//                                                startActivity(Intent(this@MeetingRoomBroadActivity,QianbaoBroadActivity::class.java))
//                                            }
//                                        }
////                                                              if ((it.data?.createout?:-1)=)
//
//                                    }
//
//
//                                }
//                            }
//                            setNegativeButton("不用"){
//                                dismiss()
//                                EventBus.getDefault().post(MeetingEvent(AppEventBroadCode.EVENT_MEETING_UN_ADD_TIME,false))
//                            }
//                        }else{
//                            setPositiveButton("确定"){
//                                dismiss()
//                                EventBus.getDefault().post(MeetingEvent(AppEventBroadCode.EVENT_MEETING_UN_ADD_TIME,false))
//                            }
//                        }
//
//                        setCancelable(false)
//                        show()
//
//                    }
//                }
//                else -> {}
//
//            }
//        }
//    }
//
//    private  fun checkMicPerm(owner: AUIUserThumbnailBroadInfo){
//        roomInfo?.let {roomInfo->
//            mPermissionHelp.checkMicPerm(
//                {
//                    AUIVoiceRoomUIBroadKit.generateToken(
//                        roomInfo.roomId,
//                        onSuccess = { roomConfig ->
//                            if (isCreateRoom) {
//                                roomInfo.owner=owner
//                                AUIVoiceRoomUIBroadKit.createRoom(
//                                    roomInfo,
//                                    roomConfig,
//                                    mViewBinding.VoiceRoomView,
//                                    completion = { error, _ ->
//                                        if (error != null) {
//                                            runOnUiThread { shutDownRoom() }
//                                        }else{
//                                            //加入成功 显示界面
//                                            runOnUiThread {  showJoinSuccessLayout() }
//                                            //创建房间成功并且加入成功 更新信息
//                                            val mute=intent.getBooleanExtra(EXTRA_MUTE_STATE,false)
//                                            val mic =intent.getBooleanExtra(EXTRA_MIC_STATE,true)
//                                            updateRoomInfoByServer(roomInfo.roomId,ROOM_ACTIVE_STATE,mic,mute)
//                                        }
//                                    }
//                                )
//                            } else {
//                                ServerBroadManager.getMeetingRoomInfo(roomId){
//                                    if (it==null){
//                                        shutDownRoom()
//                                    }else{
//                                        meetingData=it
//                                        when(it.status?.lowercase()){
//                                            ROOM_ACTIVE_STATE->{
//                                                runOnUiThread {  showJoiningLayout() }
//                                                AUIVoiceRoomUIBroadKit.launchRoom(
//                                                    roomInfo,
//                                                    roomConfig,
//                                                    mViewBinding.VoiceRoomView,
//                                                    completion = { error, _ ->
//                                                        if (error != null) {
//                                                            shutDownRoom()
//                                                        }else{
//                                                            runOnUiThread {
//                                                                showJoinSuccessLayout()
//                                                                it.allMute?.let {
//                                                                    AUIVoiceRoomUIBroadKit.setRoomMuteState(roomId,it)
//                                                                    mViewBinding.VoiceRoomView.setMuteEditLayout(AUIRoomBroadContext.shared().isRoomOwner(roomId),
//                                                                        AUIVoiceRoomUIBroadKit.isRoomMuted(roomId))
//                                                                }
//                                                                it.status.let {
//                                                                    when(it.lowercase()){
//                                                                        ROOM_ACTIVE_STATE->{
//
//                                                                        }
//                                                                        else -> {
//                                                                            ToastBroadUtils.show("当前房间不是活动状态")
//                                                                            shutDownRoom()
//                                                                        }
//                                                                    }
//                                                                }
//                                                            }
////                                                         loadMeetingInfo()
//                                                        }
//                                                    }
//                                                )
//                                                //                                             AUIVoiceRoomUIBroadKit.registerRespObserver(roomId = roomInfo.roomId, this@MeetingRoomBroadActivity)
//                                            }
//                                            ROOM_DESTORY_STATE->{
//                                                runOnUiThread { showJoiningLayout() }
//                                                mViewBinding.progress.setProgress(100,true)
//                                                mViewBinding.tvStatusTips.text="该会议室已销毁"
////                                             AUIAlertBroadDialog(this).apply {
////                                                 setTitle("会议室状态异常")
////                                                 setMessage("此会议室已销毁")
////                                                 setPositiveButton("好的"){
////                                                     dismiss()
////                                                     shutDownRoom()
////                                                 }
////                                                 setCancelable(false)
////                                                 show()
////
////                                             }
//                                            }
//                                            ROOM_WAITE_CREATE_STATE->{
//                                                mViewBinding.progress.setProgress(100,true)
//                                                mViewBinding.tvStatusTips.text="该会议室待创建"
//                                                showCountDownLayout(it)
//
//                                            }
//
//                                        }
//
//                                    }
//                                }
//
//                            }
//
//                        },
//                        onFailure = {
//                            shutDownRoom()
//                        }
//                    )
//                },
//                {
//                    finish()
//                },
//                true
//            )
//        }
//    }
//
//
//
//
//    private  val ROOM_ACTIVE_STATE="active"
//    private  val ROOM_WAITE_CREATE_STATE="pending_create"
//    private  val ROOM_DESTORY_STATE="destroyed"
//    private  fun updateRoomInfoByServer(updateRoomId:String,status:String=ROOM_ACTIVE_STATE,roomMic: Boolean?=true,roomMute: Boolean?=false){
//        val map = mapOf(
//            "status" to status,
//            "roomId" to updateRoomId,
//            "allMic" to roomMic,
//            "allMute" to roomMute
//        )
//        OKGoBroadHelper().postNoAes(this, UrlBroadBroadConstants.HOST + UrlBroadBroadConstants.UPDATE_MEETING_INFO, Gson().toJson(map),
//            NoResultBroadBean(), callBack = object :
//                OKGoBroadHelper.RequestCallBack {
//                override fun onSuccess(bean: BaseBroadResponse?) {
//                    (bean as? NoResultBroadBean)?.let {
//
//
//                    }
//                }
//
//                override fun onFail(bean: BaseBroadResponse?) {
//                    loadMeetingInfo()
//
//                }
//
//            })
//
//    }
//
//    fun checkOverlayPermission(): Boolean {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            if (!Settings.canDrawOverlays(this)) {
//                AUIAlertBroadDialog(this).apply {
//                    setTitle("缩小会议室")
//                    setMessage("点击浮窗可回到这里哦")
//                    setPositiveButton("好的"){
//                        dismiss()
//                        Toast.makeText(this@MeetingRoomBroadActivity, "需要悬浮窗权限", Toast.LENGTH_LONG).show()
//                        val intent = Intent(
//                            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
//                            Uri.parse("package:" + getPackageName())
//                        )
//
//                        val infos = this@MeetingRoomBroadActivity.getPackageManager().queryIntentActivities(
//                            intent,
//                            PackageManager.MATCH_DEFAULT_ONLY
//                        )
//                        if (infos == null || infos.isEmpty()) {
////                            return true
//                        }else{
//
//                            startActivity(intent)
//                        }
////                preventShowFloatingViewOnStop = true
//                    }
//                    show()
//                }
//
//                return false
//            }
//        }
//        return true
//    }
//
//
//}