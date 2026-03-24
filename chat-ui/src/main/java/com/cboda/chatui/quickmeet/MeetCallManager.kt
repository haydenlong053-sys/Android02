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
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.getAppContext
import com.cboda.baselibrary.utils.getResumeActivity
import com.cboda.chatui.quickmeet.bean.CallData
import com.cboda.chatui.quickmeet.bean.CallDirect
import com.cboda.chatui.quickmeet.bean.CallStatus
import com.cboda.chatui.quickmeet.bean.MeetRole
import com.cboda.chatui.quickmeet.bean.QuickDetailMeetBean
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.ClientRoleOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.Constants.CONNECTION_CHANGED_LEAVE_CHANNEL
import io.agora.rtc2.Constants.CONNECTION_STATE_DISCONNECTED
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.IRtcEngineEventHandler.AudioVolumeInfo
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig

/**
 * 会议管理类
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
    }

    fun muteRemoteAudio(uid: Int, mute: Boolean): Int {
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
    fun callMeetAction(direct: CallDirect?, channelId: String, token: String, quickDetailMeetBean: QuickDetailMeetBean? = null) {
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
            eLogD("call", "init===>channelName=$channelId---token=$token")
            val realUserId = MMKVBroadUtils.getRealUserId()
            mCallData.direct = data.direct
            mCallData.callStatus = data.callStatus
            mCallData.channelId = data.channelId
            mCallData.quickDetailMeetBean = data.quickDetailMeetBean
            mCallData.role = if (data.quickDetailMeetBean != null && realUserId == data.quickDetailMeetBean?.userId) {
                MeetRole.OWNER
            } else {
                null
            }
            //初始化 音视频sdk
            initRtcEngine(UtilBroadApplication.cxt.applicationContext, channelId, token)
        } else {
            eLogD("call", "rtc 当前有在处理的视频通话===>")
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
                    //正常退出会议频道
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
                    notifyRtcRoleChanged(oldRole, newRole)
//                if (newRole == Constants.CLIENT_ROLE_AUDIENCE) {
//                    LOOK_TYPE = 1
//                } else {
//                    LOOK_TYPE = 0
//                }
//                if (LOOK_TYPE == 0) {
//                    mTvLook.setText("当前是主播，可发言可收听,点击切换为观众端")
//                } else {
//                    mTvLook.setText("当前是观众，仅收听,点击切换为主播端")
//                }
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
            super.onUserMuteAudio(uid, muted)
            runOnUiThread {
                notifyRtcUserMuteAudioChange(uid, muted)
            }
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
        return callEngine?.isSpeakerphoneEnabled() ?: false
    }

    /**
     * 打开扬声器
     * @param enabled Boolean
     */
    fun setEnableSpeakerphone(enabled: Boolean) {
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
    fun setRtcClientRoleDirect(type: CallDirect): Int = callEngine?.let {
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

    fun leaveAndRelease() {
        if (callEngine != null) {
            mCallData.callStatus = CallStatus.IDLE
            // 离开频道
            callEngine?.leaveChannel()
            callEngine = null
            // 销毁引擎
            RtcEngine.destroy()
        }
        stopCallService()
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
        if (!isCallServiceStart) {
            isCallServiceStart = true
            try {
                val intent = Intent(UtilBroadApplication.cxt, MeetCallService::class.java)
                UtilBroadApplication.cxt.startForegroundService(intent)
            } catch (ignore: Exception) {
            }
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