package com.cboda.chatui.quickmeet

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.ThreadUtils.runOnUiThread
import com.cboda.baselibrary.app.UtilBroadApplication
import com.cboda.baselibrary.common.PlatformParams
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.baselibrary.utils.APP_TAG
import com.cboda.baselibrary.utils.HttpsBroadConfig
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.getAppContext
import com.cboda.baselibrary.utils.getResumeActivity
import com.cboda.baselibrary.utils.meetEndMsgLiveData
import com.cboda.chatui.quickmeet.bean.CallData
import com.cboda.chatui.quickmeet.bean.GroupMemberRole
import com.cboda.chatui.quickmeet.bean.CallDirect
import com.cboda.chatui.quickmeet.bean.CallStatus
import com.cboda.chatui.quickmeet.bean.MeetRole
import com.cboda.chatui.quickmeet.bean.QuickDetailMeetBean
import com.freddy.webtrc.bean.CallEvent
import com.freddy.webtrc.utils.MeetLiveKitUtil
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.ClientRoleOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.Constants.CONNECTION_CHANGED_LEAVE_CHANNEL
import io.agora.rtc2.Constants.CONNECTION_STATE_DISCONNECTED
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.IRtcEngineEventHandler.AudioVolumeInfo
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig
import kotlinx.coroutines.GlobalScope

/**
 * 会议管理类（声网 Agora / LiveKit 双栈，由 [HttpsBroadConfig.useLiveKitGroupMeeting] 切换）。
 */
class MeetCallManager private constructor() {
    private val PERMISSION_REQ_ID: Int = 22
    private val mCallEvents = ArrayList<CallEventCallback>(1)
    val mCallData: CallData = CallData()
    private var callEngine: RtcEngine? = null
    private var isCallServiceStart = false

    companion object {
        const val KEY_CALL_TIMER = "call_timer"
        private var instance = MeetCallManager()

        /**
         * 是否启用最小化功能
         */
        var isEnabledZoomMode: Boolean = true

        @JvmStatic
        fun getInstance(): MeetCallManager {
            return instance
        }

        /** 当前配置是否走 LiveKit 群会议。 */
        @JvmStatic
        fun isLiveKitEngineEnabled(): Boolean = HttpsBroadConfig.useLiveKitGroupMeeting
    }

    fun muteRemoteAudio(uid: Int, mute: Boolean): Int {
        if (mCallData.useLiveKit) {
            // LiveKit 远端静音由 UI 侧处理；引擎层无按 int uid 的接口
            return 0
        }
        return callEngine?.muteRemoteAudioStream(uid, mute) ?: -1
    }

    /**
     * muted
     * 是否停止发布本地音频流：
     * true：停止发布本地音频流。
     * false：（默认）恢复发布本地音频流。
     * 返回值
     * 0：方法调用成功。
     * < 0：方法调用失败。详见错误码了解详情和解决建议。
     *
     */
    fun muteLocalAudio(mute: Boolean): Int {
        if (mCallData.useLiveKit) {
            MeetLiveKitUtil.setMicrophoneEnabled(!mute && mCallData.canPublish)
            return 0
        }
        return callEngine?.muteLocalAudioStream(mute) ?: -1
    }

    private fun checkMeetPermissions(): Boolean {
        for (permission in getMeetAudioPermissions()) {
            val permissionCheck = ContextCompat.checkSelfPermission(getAppContext(), permission)
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                eLogD(APP_TAG, "被拒绝的权限--》" + permission)
                return false
            }
        }
        return true
    }

    /**
     * 通话参数初始化，拉起会议
     * @param msg CallRespData 通话接口数据
     * @param direct CallDirect? 会议接听/发言方
     */
    fun callMeetAction(
        direct: CallDirect?,
        channelId: String,
        token: String,
        quickDetailMeetBean: QuickDetailMeetBean? = null,
        groupMemberRole: Int = GroupMemberRole.UNKNOWN,
    ) {
        if (checkMeetPermissions()) {
            if (!isIdleState()) {
                eLogD("call", "callEvent 当前有正在进行的通话会议=========>")
                return
            }
            val data = CallData()
            data.callStatus = CallStatus.WAITING
            data.direct = direct
            data.channelId = channelId
            data.quickDetailMeetBean = quickDetailMeetBean
            data.groupMemberRole = groupMemberRole
            data.useLiveKit = isLiveKitEngineEnabled()
                || !quickDetailMeetBean?.sfuUrl.isNullOrBlank()
                || !quickDetailMeetBean?.meetingId.isNullOrBlank()
            data.sfuUrl = quickDetailMeetBean?.sfuUrl.orEmpty()
            data.meetingId = quickDetailMeetBean?.meetingId.orEmpty()
            data.roomName = quickDetailMeetBean?.roomName.orEmpty()
            data.canPublish = quickDetailMeetBean?.canPublish == true
                || direct == CallDirect.CALLER
            //初始化通话参数
            start(data, channelId, token)
        } else {
            // 判断 targetSDKVersion 31 及以上时所需的权限
            // Determine the permissions required when targetSDKVersion is 31 or higher
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ToastBroadUtils.show("会议功能,请开启手机录音权限，以及蓝牙权限")
            } else {
                ToastBroadUtils.show("会议功能,请开启手机录音权限")
            }
            getResumeActivity()?.let {
                ActivityCompat.requestPermissions(it, getMeetAudioPermissions(), PERMISSION_REQ_ID)
            }
        }
    }


    /**
     * 初始化参数和音视频sdk
     * @param data CallData
     */
    private fun start(data: CallData, channelId: String, token: String) {
        if (isIdleState()) {
            eLogD("call", "init===>channelName=$channelId---token=$token liveKit=${data.useLiveKit}")
            val realUserId = MMKVBroadUtils.getRealUserId()
            mCallData.direct = data.direct
            mCallData.callStatus = data.callStatus
            mCallData.channelId = data.channelId
            mCallData.quickDetailMeetBean = data.quickDetailMeetBean
            mCallData.groupMemberRole = data.groupMemberRole
            mCallData.useLiveKit = data.useLiveKit
            mCallData.sfuUrl = data.sfuUrl
            mCallData.meetingId = data.meetingId
            mCallData.roomName = data.roomName
            mCallData.canPublish = data.canPublish
            mCallData.role = if (
                data.quickDetailMeetBean != null
                && (realUserId == data.quickDetailMeetBean?.userId
                        || "host".equals(data.quickDetailMeetBean?.myRole, ignoreCase = true))
            ) {
                MeetRole.OWNER
            } else if (data.groupMemberRole == GroupMemberRole.ADMIN) {
                MeetRole.MANAGER
            } else {
                null
            }
            if (data.useLiveKit) {
                initLiveKitEngine(token)
            } else {
                initRtcEngine(UtilBroadApplication.cxt.applicationContext, channelId, token)
            }
        } else {
            eLogD("call", "rtc 当前有在处理的视频通话===>")
        }
    }

    private fun initLiveKitEngine(token: String) {
        notifyEngineSetup()
        val url = mCallData.sfuUrl.ifBlank { mCallData.quickDetailMeetBean?.sfuUrl.orEmpty() }
        val joinToken = token.ifBlank { mCallData.quickDetailMeetBean?.token.orEmpty() }
        if (url.isBlank() || joinToken.isBlank()) {
            eLogD(APP_TAG, "LiveKit join missing url/token")
            mCallData.callStatus = CallStatus.IDLE
            ToastBroadUtils.show("会议进房参数无效")
            return
        }
        MeetLiveKitUtil.joinRoom(
            url = url,
            token = joinToken,
            canPublish = mCallData.canPublish,
            scope = GlobalScope,
            onEvent = { event -> handleLiveKitEvent(event) },
        )
    }

    private fun handleLiveKitEvent(event: CallEvent) {
        runOnUiThread {
            when (event) {
                is CallEvent.Connected -> {
                    mCallData.callStatus = CallStatus.CALLING
                    startCallService()
                    notifyRtcJoinChannelSuccess()
                }
                is CallEvent.ParticipantConnected -> {
                    notifyRtcUserJoined(hashUid(event.userId))
                }
                is CallEvent.ParticipantDisconnected -> {
                    notifyRtcUserOffline(hashUid(event.userId), 0)
                }
                is CallEvent.ActiveSpeakersChanged -> {
                    // MeetPlugin 依赖 Agora AudioVolumeInfo；LiveKit 侧暂用说话列表驱动本地波形由座位刷新兜底
                }
                is CallEvent.Disconnected -> {
                    notifyOwnerMeetEndedMuteRestore()
                    notifyEngineDestroy(mCallData)
                    resetLiveKitState()
                }
                is CallEvent.FailedToConnect -> {
                    ToastBroadUtils.show(event.errorMessage ?: "会议连接失败")
                    leaveAndRelease()
                }
                else -> Unit
            }
        }
    }

    private fun hashUid(userId: String): Int {
        if (userId.isBlank()) return 0
        return userId.hashCode() and 0x7fffffff
    }

    private fun resetLiveKitState() {
        mCallData.callStatus = CallStatus.IDLE
        mCallData.useLiveKit = false
        mCallData.sfuUrl = ""
        mCallData.meetingId = ""
        mCallData.roomName = ""
        mCallData.canPublish = false
        stopCallService()
    }

    /**
     * LiveKit 换 token 重连（上麦审批后 /speak/leave 返回新 token 时调用）。
     */
    fun reconnectLiveKit(url: String?, token: String?, canPublish: Boolean) {
        if (!mCallData.useLiveKit) return
        if (url.isNullOrBlank() || token.isNullOrBlank()) return
        mCallData.sfuUrl = url
        mCallData.canPublish = canPublish
        mCallData.quickDetailMeetBean?.sfuUrl = url
        mCallData.quickDetailMeetBean?.token = token
        mCallData.quickDetailMeetBean?.canPublish = canPublish
        val oldRole = if (mCallData.direct == CallDirect.CALLER) {
            Constants.CLIENT_ROLE_BROADCASTER
        } else {
            Constants.CLIENT_ROLE_AUDIENCE
        }
        MeetLiveKitUtil.reconnect(
            url = url,
            token = token,
            canPublish = canPublish,
            scope = GlobalScope,
            onEvent = { event -> handleLiveKitEvent(event) },
        )
        mCallData.direct = if (canPublish) CallDirect.CALLER else CallDirect.ANSWER
        val newRole = if (canPublish) Constants.CLIENT_ROLE_BROADCASTER else Constants.CLIENT_ROLE_AUDIENCE
        if (oldRole != newRole) {
            notifyRtcRoleChanged(oldRole, newRole)
        }
    }


    private fun joinChannel(channelName: String, token: String, options: ChannelMediaOptions) {
        if (isIdleState()) {
            eLogD(APP_TAG, "会议未启动===>")
            return
        }
        val userMId = MMKVBroadUtils.getUserMId().toInt()
        eLogD(APP_TAG, "joinChannel---channelName=$channelName---mid=$userMId---token=$token")
        callEngine?.joinChannel(token, channelName, userMId, options)
    }

    private fun initRtcEngine(context: Context, channelId: String, token: String) {
        notifyEngineSetup()
        try {
            // 创建 RtcEngineConfig 对象，并进行配置
            // Create and configure the RtcEngineConfig object
            val config = RtcEngineConfig()
            config.mContext = context
            config.mAppId = PlatformParams.agoraAppId
            config.mEventHandler = mRtcEventHandler
            // 创建并初始化 RtcEngine
            // Create and initialize the RtcEngine
            callEngine = RtcEngine.create(config)
            //调节采集音频信号的音量。原始音量的2倍
            callEngine?.adjustRecordingSignalVolume(200)
            // 启用 onAudioVolumeIndication 回调
            callEngine?.enableAudioVolumeIndication(1000, 3, true);

        } catch (e: Exception) {
            throw RuntimeException("Check the error.")
        }


        // 创建 ChannelMediaOptions 对象，并进行配置
        // Create and configure the ChannelMediaOptions object
        val options = ChannelMediaOptions()


        // 根据场景将用户角色设置为 BROADCASTER (主播) 或 AUDIENCE (观众)
        // Set the user role as BROADCASTER (host) or AUDIENCE (audience) based on the scenario
        if (mCallData.direct == CallDirect.CALLER) {
            //主播端
            options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
        } else {
            //观众端
            options.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
            options.audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY

        }

        // 直播场景下，设置频道场景为 BROADCASTING (直播场景)
        // In the live broadcast scenario, set the channel profile as BROADCASTING
        options.channelProfile = Constants.CHANNEL_PROFILE_LIVE_BROADCASTING
        options.publishMicrophoneTrack = true
        options.autoSubscribeAudio = true

        callEngine?.let {
            eLogD(APP_TAG, "会议join===>")
            joinChannel(channelId, token, options)
        }
    }

    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            super.onJoinChannelSuccess(channel, uid, elapsed)
            eLogD(APP_TAG, "onJoinChannelSuccess---channel=$channel---uid=$uid")
            runOnUiThread(Runnable {
                mCallData.callStatus = CallStatus.CALLING
                startCallService()
                notifyRtcJoinChannelSuccess()
            })
        }

        //        @Override
        //        public void onError(int err) {
        //            super.onError(err);
        //            Log.d(APP_TAG,"声网报错码error=="+err);
        //        }
        override fun onConnectionStateChanged(state: Int, reason: Int) {
            super.onConnectionStateChanged(state, reason)
            eLogD(APP_TAG, "声网连接状态==" + state + "----------reason=" + reason)
            runOnUiThread(Runnable {
                if (state == CONNECTION_STATE_DISCONNECTED && reason == CONNECTION_CHANGED_LEAVE_CHANNEL) {
                    // 正常/异常离会后声网回调：再兜底恢复会前禁言（会前无禁言则解禁）
                    notifyOwnerMeetEndedMuteRestore()
                    notifyEngineDestroy(mCallData)
                } else {
                    onRtcConnectionStateChanged(state, reason)
                }
            })

        }

        // 监听频道内的远端用户，获取用户的 uid 信息
        //Listen to remote users in the channel and retrieve their UID information.
        override fun onUserJoined(uid: Int, elapsed: Int) {
            notifyRtcUserJoined(uid)

        }

        override fun onUserOffline(uid: Int, reason: Int) {
            runOnUiThread(Runnable {
                notifyRtcUserOffline(uid, reason)

            })
        }

        override fun onClientRoleChanged(oldRole: Int, newRole: Int, newRoleOptions: ClientRoleOptions) {
            super.onClientRoleChanged(oldRole, newRole, newRoleOptions)
            eLogD(APP_TAG, "oldRole==" + oldRole + "---newRole==" + newRole + "---audienceLatencyLevel==" + newRoleOptions.getAudienceLatencyLevel())
            if (oldRole != newRole) {
                runOnUiThread(Runnable {
                    if (newRole == Constants.CLIENT_ROLE_BROADCASTER) {
                        mCallData.direct =CallDirect.CALLER
                    } else {
                        mCallData.direct =CallDirect.ANSWER
                    }
                    notifyRtcRoleChanged(oldRole, newRole)
                })
            }
        }

        // 获取瞬时说话音量最高的三个用户（即说话者）的用户 ID、他们的音量及本地用户是否在说话
        override fun onAudioVolumeIndication(speakers: Array<AudioVolumeInfo>?, totalVolume: Int) {
            super.onAudioVolumeIndication(speakers, totalVolume)
            runOnUiThread {
                notifyRtcAudioVolumeChange(speakers, totalVolume)
            }
        }

        //远端用户或主播停止或恢复发送音频流的回调
        override fun onUserMuteAudio(uid: Int, muted: Boolean) {
            eLogD(APP_TAG,"onUserMuteAudio---uid=$uid--muted=$muted")
            super.onUserMuteAudio(uid, muted)
            runOnUiThread {
                notifyRtcUserMuteAudioChange(uid, muted)
            }
        }

        override fun onAudioPublishStateChanged(channel: String?, oldState: Int, newState: Int, elapseSinceLastState: Int) {
//            eLogD(APP_TAG,"onAudioPublishStateChanged---channel=$channel--oldState=$oldState--newState=$newState")
        }

        override fun onAudioSubscribeStateChanged(channel: String?, uid: Int, oldState: Int, newState: Int, elapseSinceLastState: Int) {
//            eLogD(APP_TAG,"onAudioSubscribeStateChanged---channel=$channel--uid=$uid--oldState=$oldState--newState=$newState")
        }

        override fun onRtcStats(stats: RtcStats?) {
            notifyRtcStatsInfo(stats)
        }
    }

    /**
     * 扬声器是否已开启
     * @return Boolean
     */
    fun isSpeakerphoneEnabled(): Boolean {
        if (mCallData.useLiveKit) {
            return MeetLiveKitUtil.isSpeakerphoneEnabled()
        }
        return callEngine?.isSpeakerphoneEnabled() ?: false
    }

    /**
     * 打开扬声器
     * @param enabled Boolean
     */
    fun setEnableSpeakerphone(enabled: Boolean) {
        if (mCallData.useLiveKit) {
            MeetLiveKitUtil.setEnableSpeakerphone(enabled)
            return
        }
        callEngine?.setEnableSpeakerphone(enabled)
    }

    /**
     * 通话计时
     */
    private fun startCallTimer() {}

    private fun cancelCallTimer() {

    }

    private fun onRtcConnectionStateChanged(state: Int, reason: Int) {
        val list = ArrayList<CallEventCallback>(mCallEvents)
        for (callEvent in list) {
            callEvent.onRtcConnectionStateChanged(state, reason)
        }
    }

    /**
     * 通话会议事件
     * @param event CallEventCallback
     */
    fun registerEngineEvent(event: CallEventCallback) {
        if (!mCallEvents.contains(event)) {
            mCallEvents.add(event)
        }
    }

    fun unregisterEngineEvent(event: CallEventCallback) {
        mCallEvents.remove(event)
    }

    private fun notifyEngineSetup() {
        val list = ArrayList<CallEventCallback>(mCallEvents)
        for (callEvent in list) {
            callEvent.onEngineSetup()
        }
    }

    private fun notifyEngineDestroy(data: CallData) {
        val list = ArrayList<CallEventCallback>(mCallEvents)
        for (callEvent in list) {
            callEvent.onEngineDestroy(data)
        }
    }

    private fun notifyRtcUserJoined(uid: Int) {
        val list = ArrayList<CallEventCallback>(mCallEvents)
        for (callEvent in list) {
            callEvent.onRtcUserJoined(uid)
        }
    }

    private fun notifyRtcUserOffline(uid: Int, reason: Int) {
        val list = ArrayList<CallEventCallback>(mCallEvents)
        for (callEvent in list) {
            callEvent.onRtcUserOffline(uid, reason)
        }
    }


    private fun notifyRtcJoinChannelSuccess() {
        val list = ArrayList<CallEventCallback>(mCallEvents)
        for (callEvent in list) {
            callEvent.onRtcJoinChannelSuccess()
        }
    }

    private fun notifyRtcAudioVolumeChange(speakers: Array<AudioVolumeInfo>?, totalVolume: Int) {
        val list = ArrayList<CallEventCallback>(mCallEvents)
        for (callEvent in list) {
            callEvent.onRoomAudioVolumeIndication(speakers, totalVolume)
        }
    }

    private fun notifyRtcUserMuteAudioChange(uid: Int, muted: Boolean) {
        val list = ArrayList<CallEventCallback>(mCallEvents)
        for (callEvent in list) {
            callEvent.onRoomUserMuteAudio(uid, muted)
        }
    }

    /**
     * 会议通话中
     * @return Boolean
     */
    fun isCallingState(): Boolean {
        return mCallData.callStatus == CallStatus.CALLING
    }

    /**
     * 空闲状态
     * @return Boolean
     */
    fun isIdleState(): Boolean {
        return mCallData.callStatus == CallStatus.IDLE
    }

    /**
     * 会议发言方
     * @return Boolean
     */
    fun isCallDirect(): Boolean {
        return mCallData.direct == CallDirect.CALLER
    }

    /**
     * 会议等待状态
     * @return Boolean
     */
    fun isWaitingState(): Boolean {
        return mCallData.callStatus == CallStatus.WAITING
    }

    fun getMyselfMeetRole(): MeetRole? {
        return mCallData.role
    }
    fun isOwner(): Boolean {
        return mCallData.role == MeetRole.OWNER
    }

    /** 当前用户是否为群主或群管理员（用于下麦等群管理操作）。 */
    fun isGroupOwnerOrAdmin(): Boolean {
        return GroupMemberRole.isOwnerOrAdmin(mCallData.groupMemberRole)
    }

    /**
     * 设置是否处于最小化状态
     */
    fun setZoomMode(zoomMode: Boolean) {
    }

    /**
     * 切换会议中自己的身份
     * 返回值
     * 0：方法调用成功。
     * < 0：方法调用失败
     */
    fun setRtcClientRoleDirect(type: CallDirect): Int {
        if (mCallData.useLiveKit) {
            if (mCallData.direct == type) {
                return -1
            }
            val canPublish = type == CallDirect.CALLER
            // LiveKit canPublish 写死在 token；此处先切换本端麦状态。
            // 真正发麦需走 /speak/approve 后 rejoin（见 reconnectLiveKit）。
            mCallData.canPublish = canPublish
            mCallData.direct = type
            MeetLiveKitUtil.setMicrophoneEnabled(canPublish)
            val oldRole = if (type == CallDirect.CALLER) {
                Constants.CLIENT_ROLE_AUDIENCE
            } else {
                Constants.CLIENT_ROLE_BROADCASTER
            }
            val newRole = if (canPublish) {
                Constants.CLIENT_ROLE_BROADCASTER
            } else {
                Constants.CLIENT_ROLE_AUDIENCE
            }
            notifyRtcRoleChanged(oldRole, newRole)
            return 0
        }
        return callEngine?.let {
            if (mCallData.direct == type) {
                -1
            } else if (CallDirect.ANSWER == type) {
                //切换成观众
                val clientRoleOptions = ClientRoleOptions()
                clientRoleOptions.audienceLatencyLevel = Constants.AUDIENCE_LATENCY_LEVEL_LOW_LATENCY
                it.setClientRole(Constants.CLIENT_ROLE_AUDIENCE, clientRoleOptions)
            } else {
                //切换成主播
                it.setClientRole(Constants.CLIENT_ROLE_BROADCASTER)
            }
        } ?: -1
    }

    private fun notifyRtcRoleChanged(oldRole: Int, newRole: Int) {
        val list = ArrayList<CallEventCallback>(mCallEvents)
        list.forEach { callback ->
            callback.onRtcRoleChanged(oldRole, newRole)
        }
    }

    private fun notifyRtcStatsInfo(statsBean: IRtcEngineEventHandler.RtcStats?) {
        val list = ArrayList<CallEventCallback>(mCallEvents)
        list.forEach { callback ->
            callback.onRtcStats(statsBean)
        }
    }
    fun notifyMeetTotalPersonCount(chanelId: String,personCount: Int) {
        val list = ArrayList<CallEventCallback>(mCallEvents)
        list.forEach { callback ->
            callback.onMeetTotalPersonCount(chanelId,personCount)
        }
    }

    fun leaveAndRelease() {
        // 房主主动/意外离会统一在此恢复会前禁言（含 isCalling 已变为 false 的异常路径）
        notifyOwnerMeetEndedMuteRestore()
        if (mCallData.useLiveKit) {
            MeetLiveKitUtil.destroyRoom()
            resetLiveKitState()
            return
        }
        if (callEngine != null) {
            mCallData.callStatus = CallStatus.IDLE
            // 离开频道
            callEngine?.leaveChannel()
            callEngine = null
            // 销毁引擎
            RtcEngine.destroy()
        } else {
            mCallData.callStatus = CallStatus.IDLE
        }
        stopCallService()
    }

    /**
     * 房主离会/会议结束：通知恢复开会前全员禁言态。
     * 会前无禁言则解禁，会前已禁言则保持；无本地快照时 [MeetGroupMuteHelper] 会跳过。
     */
    fun notifyOwnerMeetEndedMuteRestore() {
        if (mCallData.role != MeetRole.OWNER) {
            return
        }
        val bean = mCallData.quickDetailMeetBean
        val imGroupId = bean?.groupIdNo?.takeIf { it.isNotEmpty() }
            ?: bean?.channelId?.takeIf { it.isNotEmpty() }
            ?: mCallData.channelId.takeIf { it.isNotEmpty() }
            ?: return
        eLogD(APP_TAG, "notifyOwnerMeetEndedMuteRestore groupId=$imGroupId")
        meetEndMsgLiveData.postValue(imGroupId)
    }


    /**
     * 获取通话时长。在结束通话方法之前获取,执行结束方法后为0
     * @return Long
     */
    fun getCallDuration(): Long {
        return 0L
    }

    //开启前台服务
    private fun startCallService() {
        if (isCallServiceStart) {
            return
        }
        val ctx = UtilBroadApplication.cxt
        // targetSdk 36：microphone FGS 必须已有 RECORD_AUDIO，且应用处于可启动前台服务的状态
        if (ContextCompat.checkSelfPermission(ctx, android.Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
            eLogD("call", "skip MeetCallService: RECORD_AUDIO not granted")
            return
        }
        try {
            isCallServiceStart = true
            val intent = Intent(ctx, MeetCallService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                ctx.startForegroundService(intent)
            } else {
                ctx.startService(intent)
            }
        } catch (e: Exception) {
            isCallServiceStart = false
            eLogD("call", "startCallService failed: ${e.message}")
        }
    }

    //停止前台服务
    private fun stopCallService() {
        try {
            if (isCallServiceStart) {
                isCallServiceStart = false
                UtilBroadApplication.cxt.stopService(Intent(UtilBroadApplication.cxt, MeetCallService::class.java))
            }
        } catch (ignore: Exception) {
        }
    }
}