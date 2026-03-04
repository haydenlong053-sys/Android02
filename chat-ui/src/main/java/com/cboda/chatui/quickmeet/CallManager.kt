package com.cboda.chatui.quickmeet

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.blankj.utilcode.util.ThreadUtils.runOnUiThread
import com.cboda.baselibrary.app.UtilBroadApplication
import com.cboda.baselibrary.common.PlatformParams
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.getAppContext
import com.cboda.baselibrary.utils.getResumeActivity
import com.cboda.chatui.quickmeet.bean.CallData
import com.cboda.chatui.quickmeet.bean.CallDirect
import com.cboda.chatui.quickmeet.bean.CallStatus
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.ClientRoleOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.Constants.CONNECTION_CHANGED_LEAVE_CHANNEL
import io.agora.rtc2.Constants.CONNECTION_STATE_DISCONNECTED
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import io.agora.rtc2.RtcEngineConfig

class CallManager private constructor() {
    private val PERMISSION_REQ_ID: Int = 22
    private val mCallEvents = ArrayList<CallEventCallback>(1)

    val mCallData: CallData = CallData()

    private var callEngine: RtcEngine? = null


    companion object {
        const val KEY_CALL_TIMER = "call_timer"

        private var instance = CallManager()

        /**
         * 是否启用最小化功能
         */
        var isEnabledZoomMode: Boolean = true

        @JvmStatic
        fun getInstance(): CallManager {
            return instance
        }
    }

    fun muteRemoteAudio(streamId: String, mute: Boolean): Int {
//        return callEngine?.muteRemoteAudio(streamId, mute) ?: -1
        return -1
    }


    fun muteLocalAudio(mute: Boolean): Int {
//        return callEngine?.muteLocalAudio(mute) ?: -1
        return -1
    }

    private fun checkMeetPermissions(): Boolean {
        for (permission in getRequiredPermissions()) {
            val permissionCheck = ContextCompat.checkSelfPermission(getAppContext(), permission)
            if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                eLogD("zhanglong", "被拒绝的权限--》" + permission)
                return false
            }
        }
        return true
    }

    // 获取体验实时音视频互动所需的录音、摄像头等权限
    // Get the permissions required for real-time audio and video interaction
    private fun getRequiredPermissions(): Array<String> {
        // 判断 targetSDKVersion 31 及以上时所需的权限
        // Determine the permissions required when targetSDKVersion is 31 or higher
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            return arrayOf<String>(
                Manifest.permission.RECORD_AUDIO,  // 录音权限 // Recording permission
//                Manifest.permission.CAMERA,  // 摄像头权限 // Camera permission
//                Manifest.permission.READ_PHONE_STATE,  // 读取电话状态权限 // Read phone state permission
                Manifest.permission.BLUETOOTH_CONNECT // 蓝牙连接权限 // Bluetooth connection permission
            )
        } else {
            return arrayOf<String>(
                Manifest.permission.RECORD_AUDIO,
//                Manifest.permission.CAMERA
            )
        }
    }


    /**
     * 通话参数初始化，拉起会议
     * @param msg CallRespData 通话接口数据
     * @param direct CallDirect? 会议接听/发言方
     */
    fun callMeetAction(direct: CallDirect?, channelName: String, token: String) {
        if (checkMeetPermissions()) {
            if (!isIdleState()) {
                eLogD("call", "callEvent 当前有正在进行的通话会议=========>")
                return
            }
            val data = CallData()
            data.callStatus = CallStatus.WAITING
            data.direct = direct
            //初始化通话参数
            start(data, channelName, token)
        } else {
            // 判断 targetSDKVersion 31 及以上时所需的权限
            // Determine the permissions required when targetSDKVersion is 31 or higher
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                ToastBroadUtils.show("请开启手机录音权限，以及蓝牙权限")
            } else {
                ToastBroadUtils.show("请开启手机录音权限")
            }
            getResumeActivity()?.let {
                ActivityCompat.requestPermissions(it, getRequiredPermissions(), PERMISSION_REQ_ID)
            }
        }
    }


    /**
     * 初始化参数和音视频sdk
     * @param data CallData
     */
    private fun start(data: CallData, channelName: String, token: String) {
        if (isIdleState()) {
            eLogD("call", "init===>")
            mCallData.direct = data.direct
            mCallData.callStatus = data.callStatus
//            mCallData.chatRoomId = data.chatRoomId

            //初始化 音视频sdk
            initRtcEngine(UtilBroadApplication.cxt.applicationContext, channelName, token)
        } else {
            eLogD("call", "rtc 当前有在处理的视频通话===>")
        }
    }


    private fun joinChannel(channelName: String, token: String, options: ChannelMediaOptions) {
        if (isIdleState()) {
            eLogD("zhanglong", "会议未启动===>")
            return
        }
        callEngine?.joinChannel(token, channelName, 0, options)
    }

    private fun initRtcEngine(context: Context, channelName: String, token: String) {

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
            joinChannel(channelName, token, options)
        }
    }

    private val mRtcEventHandler: IRtcEngineEventHandler = object : IRtcEngineEventHandler() {
        override fun onJoinChannelSuccess(channel: String?, uid: Int, elapsed: Int) {
            super.onJoinChannelSuccess(channel, uid, elapsed)
            runOnUiThread(Runnable {
                mCallData.callStatus = CallStatus.CALLING
                notifyRtcJoinChannelSuccess()
            })
        }

        //        @Override
        //        public void onError(int err) {
        //            super.onError(err);
        //            Log.d("zhanglong","声网报错码error=="+err);
        //        }
        override fun onConnectionStateChanged(state: Int, reason: Int) {
            super.onConnectionStateChanged(state, reason)
            Log.d("zhanglong", "声网连接状态==" + state + "----------reason=" + reason)
            if (state == CONNECTION_STATE_DISCONNECTED && reason == CONNECTION_CHANGED_LEAVE_CHANNEL) {
                //正常退出会议频道
                runOnUiThread(Runnable {
                    notifyEngineDestroy(mCallData)
                })

            }
        }

        // 监听频道内的远端用户，获取用户的 uid 信息
        //Listen to remote users in the channel and retrieve their UID information.
        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread(Runnable {
                notifyRtcUserJoined(uid)
            })

        }

        override fun onUserOffline(uid: Int, reason: Int) {
            super.onUserOffline(uid, reason)
            runOnUiThread(Runnable {
                Toast.makeText(UtilBroadApplication.cxt.applicationContext, "User offline: " + uid, Toast.LENGTH_SHORT).show()
            })
        }

        override fun onClientRoleChanged(oldRole: Int, newRole: Int, newRoleOptions: ClientRoleOptions) {
            super.onClientRoleChanged(oldRole, newRole, newRoleOptions)
            Log.d("zhanglong", "oldRole==" + oldRole + "---newRole==" + newRole + "---audienceLatencyLevel==" + newRoleOptions.getAudienceLatencyLevel())
//            runOnUiThread(Runnable {
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
//            })
        }
    }


    fun parseErrorStatus(s: String?, errorMsg: String? = null) {

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

    fun rtcConnectFail(tips: Boolean = false) {

    }

    fun tokenError() {
        val list = ArrayList<CallEventCallback>(mCallEvents)
        for (callEvent in list) {
            callEvent.onRtcTokenExpired()
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

    fun notifyEngineSetup() {
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

    fun notifyRtcUserJoined(uid: Int) {
        val list = ArrayList<CallEventCallback>(mCallEvents)
        for (callEvent in list) {
            callEvent.onRtcUserJoined(uid)
        }
    }

    fun notifyRtcJoinChannelSuccess() {
        val list = ArrayList<CallEventCallback>(mCallEvents)
        for (callEvent in list) {
            callEvent.onRtcJoinChannelSuccess()
        }
    }


    /**
     * 通话中
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
     * 通话拨打方
     * @return Boolean
     */
    fun isCallDirect(): Boolean {
        return mCallData.direct == CallDirect.CALLER
    }

    /**
     * 拨打等待状态
     * @return Boolean
     */
    fun isWaitingState(): Boolean {
        return mCallData.callStatus == CallStatus.WAITING
    }


    /**
     * 设置是否处于最小化状态
     */
    fun setZoomMode(zoomMode: Boolean) {
    }


    private fun notifyCountdownTimer(time: Long) {
        val list = ArrayList<CallEventCallback>(mCallEvents)
        list.forEach { callback ->
            callback.onHangupCountdown(time)
        }
    }

    private fun notifyCancelCountdownTimer() {
        val list = ArrayList<CallEventCallback>(mCallEvents)
        list.forEach { callback ->
            callback.onCancelHangupCountdown()
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
    }


    /**
     * 获取通话时长。在结束通话方法之前获取,执行结束方法后为0
     * @return Long
     */
    fun getCallDuration(): Long {
        return 0L
    }
}