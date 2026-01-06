package com.example.asceneskit

import android.util.Log
import com.cboda.baselibrary.event.AppBroadEvent
import com.cboda.baselibrary.event.AppEventBroadCode
import com.cboda.baselibrary.event.rtc.NetworkQualityEventBroadData
import com.cboda.agora.service.model.AUIRoomBroadConfig
import com.cboda.agora.service.model.AUIRoomBroadContext
import com.cboda.agora.service.model.AUIRoomBroadInfo
import com.cboda.agora.service.model.AUIUserBroadInfo
import com.cboda.agora.service.service.IAUIIMManagerBroadService
import com.cboda.agora.service.service.IAUIInvitationBroadService
import com.cboda.agora.service.service.IAUIMicSeatBroadService
import com.cboda.agora.service.service.IAUIUserBroadService
import com.cboda.agora.service.service.arbiter.AUIBroadArbiter
import com.cboda.agora.service.service.callback.AUIBroadException
import com.cboda.agora.service.service.im.AUIChatBroadManager
import com.cboda.agora.service.service.imp.AUIIMManagerServiceBroadImpl
import com.cboda.agora.service.service.imp.AUIInvitationServiceImpl
import com.cboda.agora.service.service.imp.AUIMicSeatServiceImpl
import com.cboda.agora.service.service.imp.AUIUserServiceBroadImpl
import com.cboda.agora.service.service.rtm.AUIRtmErrorRespBroadObserver
import com.cboda.agora.service.service.rtm.AUIRtmLockRespObserver
import com.cboda.agora.service.service.rtm.AUIRtmBroadManager
import com.cboda.agora.service.utils.AUIBroadLogger
import com.cboda.agora.service.utils.AgoraEngineBroadCreator
import com.cboda.agora.service.utils.GsonBroadTools
import com.cboda.agora.service.utils.ObservableBroadHelper
import com.cboda.agora.service.utils.ThreadBroadManager
import io.agora.rtc2.ChannelMediaOptions
import io.agora.rtc2.Constants
import io.agora.rtc2.RtcEngine
import io.agora.rtm.LockEvent
import io.agora.rtm.RtmClient
import io.agora.rtm.RtmConfig
import io.agora.rtm.RtmConstants
import io.agora.rtm.RtmEventListener
import org.greenrobot.eventbus.EventBus


val kRoomInfoAttrKey = "basic"

class AUIVoiceRoomBroadService constructor(
    apiConfig: AUIAPIBroadConfig,
    private var roomConfig: AUIRoomBroadConfig,
) {

    private val TAG = "AUIVoiceRoomService"

    private val rtcEngineCreateByService = apiConfig.rtcEngineEx == null
    private val rtmCreateCreateByService = apiConfig.rtmClient == null

    private var isRoomDestroyed = false

    private val userRespObserver = object : IAUIUserBroadService.AUIUserRespObserver {

        override fun onRoomUserSnapshot(roomId: String, userList: MutableList<AUIUserBroadInfo>?) {
            userSnapshotList = userList
            userList?.firstOrNull { it.userId == AUIRoomBroadContext.shared().currentUserInfo.userId }
                ?.let { user ->
                    onUserAudioMute(user.userId, (user.muteAudio == 1))
//                    onUserVideoMute(user.userId, (user.muteVideo == 1))
                }
        }

        override fun onRoomUserLeave(roomId: String, userInfo: AUIUserBroadInfo) {
            super.onRoomUserLeave(roomId, userInfo)
            if (AUIRoomBroadContext.shared().getRoomOwner(channelName) == userInfo.userId) {
                cleanRoomInfo(roomId)
            } else {
                cleanUserInfo(roomId, userInfo.userId)
            }
        }

        override fun onUserAudioMute(userId: String, mute: Boolean) {
            if (userId != AUIRoomBroadContext.shared().currentUserInfo.userId) {
                return
            }
            rtcEngine.adjustRecordingSignalVolume(if (mute) 0 else 100)
        }

        override fun onUserVideoMute(userId: String, mute: Boolean) {
            if (userId != AUIRoomBroadContext.shared().currentUserInfo.userId) {
                return
            }
            rtcEngine.enableLocalVideo(!mute)
            val option = ChannelMediaOptions()
            option.publishCameraTrack = !mute
            rtcEngine.updateChannelMediaOptions(option)
        }

        override fun onLocalUserKickedOut(roomId: String) {
            super.onLocalUserKickedOut(roomId)
            exit()
        }

        override fun onNetworkQuality(userId: String?, txQuality: Int, rxQuality: Int) {
            super.onNetworkQuality(userId, txQuality, rxQuality)
            //发送通知给ui
            val eventData = NetworkQualityEventBroadData(userId?:"",txQuality,rxQuality)
//            Log.d("AUIVoiceRoomService", "发送网络质量事件: userId=$userId, txQuality=$txQuality, rxQuality=$rxQuality")
//            Log.d("AUIVoiceRoomService", "事件数据: uid=${eventData.uid}, txQuality=${eventData.txQuality}, rxQuality=${eventData.rxQuality}")
            EventBus.getDefault().post(AppBroadEvent(AppEventBroadCode.EVENT_QUALITY_CHANGE, eventData))

        }

    }



    private val rtmErrorRespObserver = object : AUIRtmErrorRespBroadObserver {
        override fun onTokenPrivilegeWillExpire(channelName: String?) {
            channelName ?: return
            observableHelper.notifyEventHandlers {
                it.onTokenPrivilegeWillExpire(channelName)
            }
        }

        override fun onMsgReceiveEmpty(channelName: String) {
            super.onMsgReceiveEmpty(channelName)
            if (this@AUIVoiceRoomBroadService.channelName != channelName) {
                return
            }
            isRoomDestroyed = true
            observableHelper.notifyEventHandlers {
                it.onRoomDestroy(channelName)
            }
        }

        override fun onConnectionStateChanged(channelName: String?, state: Int, reason: Int) {
            super.onConnectionStateChanged(channelName, state, reason)
            if (reason == RtmConstants.RtmConnectionChangeReason.getValue(RtmConstants.RtmConnectionChangeReason.REJOIN_SUCCESS)) {
                AUIRoomBroadContext.shared().getArbiter(this@AUIVoiceRoomBroadService.channelName)?.acquire()
            }
        }
    }

    private val rtmLockRespObserver = object : AUIRtmLockRespObserver {
        override fun onReceiveLock(channelName: String, lockName: String, lockOwner: String) {
            lockRetrived = true
        }

        override fun onReleaseLock(channelName: String, lockName: String, lockOwner: String) {

        }
    }

    private var enterRoomCompletion: ((AUIRoomBroadInfo?) -> Unit)? = null

    private var subscribeSuccess = false
        set(value) {
            if (field != value) {
                field = value
                checkRoomValid()
            }
        }

    private var lockRetrived = false
        set(value) {
            if (field != value) {
                field = value
                checkRoomValid()
            }
        }

    private var userSnapshotList: List<AUIUserBroadInfo>? = null
        set(value) {
            if (field != value) {
                field = value
                checkRoomValid()
            }
        }

    val channelName: String
        get() {
            return roomConfig.channelName
        }

    val rtcEngine: RtcEngine = apiConfig.rtcEngineEx ?: AgoraEngineBroadCreator.createRtcEngine(
        AUIRoomBroadContext.shared().requireCommonConfig().context,
        AUIRoomBroadContext.shared().requireCommonConfig().appId,
    )

    val rtmClient = apiConfig.rtmClient ?: RtmClient.create(
        RtmConfig.Builder(
            AUIRoomBroadContext.shared().requireCommonConfig().appId,
            AUIRoomBroadContext.shared().currentUserInfo.userId
        ).presenceTimeout(60).eventListener(object : RtmEventListener {
            override fun onLockEvent(event: LockEvent?) {
                super.onLockEvent(event)
                Log.d(TAG, "onLockEvent event: $event")
            }

            override fun onConnectionStateChanged(
                channelName: String?,
                state: RtmConstants.RtmConnectionState?,
                reason: RtmConstants.RtmConnectionChangeReason?
            ) {
                super.onConnectionStateChanged(channelName, state, reason)
                Log.d(TAG, "RtmClient.create >> onConnectionStateChanged channelName=$channelName, state=$state, reason=$reason")
            }
        }).build()
    )

    val rtmManager: AUIRtmBroadManager = AUIRtmBroadManager(
        AUIRoomBroadContext.shared().requireCommonConfig().context,
        rtmClient,
        apiConfig.rtmClient != null
    )

    val observableHelper = ObservableBroadHelper<AUIVoiceRoomBroadObserver>()

    val chatManager = AUIChatBroadManager(channelName, AUIRoomBroadContext.shared())

    val userService: IAUIUserBroadService = AUIUserServiceBroadImpl(channelName, rtmManager, rtcEngine).apply {
        registerRespObserver(userRespObserver)
    }

    val imManagerService: IAUIIMManagerBroadService = AUIIMManagerServiceBroadImpl(
        channelName,
        rtmManager,
        chatManager
    )

    val micSeatService: IAUIMicSeatBroadService = AUIMicSeatServiceImpl(
        channelName,
        rtmManager
    )

    val invitationService: IAUIInvitationBroadService = AUIInvitationServiceImpl(
        channelName,
        rtmManager
    )

//    val giftService: IAUIGiftsBroadService = AUIGiftServiceBroadImpl(
//        channelName,
//        rtmManager,
//        chatManager
//    )

    var roomInfo: AUIRoomBroadInfo? = null
        set(value) {
            if (field != value) {
                field = value
                AUIRoomBroadContext.shared().insertRoomInfo(value)
                checkRoomValid()
            }
        }

    init {
        AUIRoomBroadContext.shared().roomConfigMap[channelName] = roomConfig
        AUIRoomBroadContext.shared().roomArbiterMap[channelName] = AUIBroadArbiter(
            channelName,
            rtmManager,
            AUIRoomBroadContext.shared().currentUserInfo.userId
        )
        AUIRoomBroadContext.shared().setNtpTimeSource {
            return@setNtpTimeSource rtcEngine.ntpWallTimeInMs
        }
    }

    fun create(
        roomInfo: AUIRoomBroadInfo,
        completion: (AUIBroadException?) -> Unit
    ) {
        Log.d(TAG,"开始创建房间${roomInfo.roomId}")
        this.roomInfo = roomInfo

        rtmManager.login(
            roomConfig.rtmToken
        ) { error ->
            if (error != null) {
                completion.invoke(AUIBroadException(AUIBroadException.ERROR_CODE_RTM, "error: $error"))
                return@login
            }
            Log.d(TAG,"rtm登录成功 开始initRoom")
            AUIRoomBroadContext.shared().getArbiter(channelName)?.create()
            initRoom { initError ->
                if (initError != null) {
                    Log.d(TAG,"initRoom错误${initError?.code}")
                    completion.invoke(initError)
                } else {
                    Log.d(TAG,"创建房间成功${roomInfo.roomId} 开始加入房间")
                    enter(completion)
                }
            }
        }
    }

    fun enter(completion: (AUIBroadException?) -> Unit) {
        val roomId = channelName
        rtmManager.login(
            roomConfig.rtmToken
        ) { error ->
            if (error != null) {
                ThreadBroadManager.getInstance().runOnMainThread {
                    completion.invoke(AUIBroadException(AUIBroadException.ERROR_CODE_RTM, "error: $error"))
                }
                return@login
            }



            enterRoomCompletion = {
                ThreadBroadManager.getInstance().runOnMainThread {
                    completion.invoke(null)
                }
            }

            if (roomInfo == null) {
                rtmManager.getMetadata(roomId) { _, metadata ->
                    val voiceRoomInfo = GsonBroadTools.toBean(
                        metadata?.get(kRoomInfoAttrKey),
                        AUIVoiceRoomInfo::class.java
                    ) ?: return@getMetadata
                    roomInfo = AUIRoomBroadInfo().apply {
                        this.roomId = voiceRoomInfo.roomId
                        roomName = voiceRoomInfo.payload.roomName
                        owner = voiceRoomInfo.payload.owner
                        micSeatCount = voiceRoomInfo.micSeatCount
                        micSeatStyle = voiceRoomInfo.micSeatStyle.toString()
                    }





                }
            }else{

            }


            AUIRoomBroadContext.shared().getArbiter(channelName)?.acquire(){ lockError ->
                if(lockError != null && enterRoomCompletion != null){
                    enterRoomCompletion = null
                    isRoomDestroyed = true
                    ThreadBroadManager.getInstance().runOnMainThread {
                        completion.invoke(AUIBroadException(AUIBroadException.ERROR_CODE_RTM, "error: $lockError"))
                    }
                    return@acquire
                }
            }
            rtmManager.subscribeError(rtmErrorRespObserver)
            rtmManager.subscribeLock(channelName, observer = rtmLockRespObserver)
            rtmManager.subscribe(channelName) { subscribeError ->
                if (subscribeError != null) {
                    completion.invoke(
                        AUIBroadException(
                            AUIBroadException.ERROR_CODE_RTM,
                            "error : $subscribeError"
                        )
                    )
                    return@subscribe
                }
                subscribeSuccess = true
            }

            joinRtcChannel { joinError ->
                ThreadBroadManager.getInstance().runOnMainThread {
                    completion.invoke(AUIBroadException(AUIBroadException.ERROR_CODE_RTM, "error: $joinError"))
                }
            }
        }
    }

    fun setupLocalStreamOn(isOn: Boolean) {
        Log.d("rtc_publish_state", "isOn: $isOn")
        if (isOn) {
            val mainChannelMediaOption = ChannelMediaOptions()
            mainChannelMediaOption.publishMicrophoneTrack = true
            mainChannelMediaOption.enableAudioRecordingOrPlayout = true
            mainChannelMediaOption.autoSubscribeVideo = true
            mainChannelMediaOption.autoSubscribeAudio = true
            mainChannelMediaOption.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
            rtcEngine.updateChannelMediaOptions(mainChannelMediaOption)
        } else {
            val mainChannelMediaOption = ChannelMediaOptions()
            mainChannelMediaOption.publishMicrophoneTrack = false
            mainChannelMediaOption.enableAudioRecordingOrPlayout = true
            mainChannelMediaOption.autoSubscribeVideo = true
            mainChannelMediaOption.autoSubscribeAudio = true
            mainChannelMediaOption.clientRoleType = Constants.CLIENT_ROLE_AUDIENCE
            rtcEngine.updateChannelMediaOptions(mainChannelMediaOption)
        }
    }

    fun setupLocalAudioMute(isMute: Boolean) {
        if (isMute) {
            rtcEngine.adjustRecordingSignalVolume(0)
        } else {
            rtcEngine.adjustRecordingSignalVolume(100)
        }
    }

    fun setupRemoteAudioMute(userId: String, isMute: Boolean) {
        rtcEngine.muteRemoteAudioStream(userId.toInt(), isMute)
    }


    fun setupAllRemoteAudioMute(isMute: Boolean){
        rtcEngine.muteAllRemoteAudioStreams(isMute)
    }

    fun setLocalAudioMute(isMute: Boolean){
        rtcEngine.muteLocalAudioStream(isMute)
    }




    fun joinRtcChannel(failure: (AUIBroadException) -> Unit) {
        AUIBroadLogger.logger().d(TAG, "joinRtcRoom start ...")
        rtcEngine.setChannelProfile(Constants.CHANNEL_PROFILE_LIVE_BROADCASTING)
        rtcEngine.disableVideo()
        rtcEngine.enableLocalVideo(false)
        rtcEngine.enableAudio()
        rtcEngine.setAudioProfile(
            Constants.AUDIO_PROFILE_MUSIC_HIGH_QUALITY,
            Constants.AUDIO_SCENARIO_GAME_STREAMING
        )
        rtcEngine.enableAudioVolumeIndication(350, 2, true)
        rtcEngine.setClientRole(
            if (AUIRoomBroadContext.shared()
                    .isRoomOwner(channelName)
            ) Constants.CLIENT_ROLE_BROADCASTER else Constants.CLIENT_ROLE_AUDIENCE
        )

//        AUIBroadLogger.logger().d(
//            "RtcEngineEx",
//            "joinChannel uid:${AUIRoomBroadContext.shared().currentUserInfo.userId.toInt()}  rtcChannelName=${roomConfig.channelName}  rtcRtcToken=${roomConfig.rtcToken}"
//        )
        val ret: Int = rtcEngine.joinChannel(
            roomConfig.rtcToken,
            roomConfig.channelName,
            null,
            AUIRoomBroadContext.shared().currentUserInfo.userId.toInt()
        )

        if (ret == Constants.ERR_OK) {
            AUIBroadLogger.logger().d(TAG, "join rtc room success")
        } else {
            AUIBroadLogger.logger().e(TAG, "join rtc room failed $ret")
            failure.invoke(AUIBroadException(ret, "join rtc room failed"))
        }
        AUIBroadLogger.logger().d(TAG, "joinRtcRoom end ...")
    }


    //token过期之后调用该方法更新所有token
    fun renew(config: AUIRoomBroadConfig) {
        AUIRoomBroadContext.shared().roomConfigMap[config.channelName] = config

        //rtm renew
        rtmManager.renew(config.rtmToken)
        rtmManager.renewStreamChannelToken(config.channelName, config.rtcToken)

        //rtc renew
        rtcEngine.renewToken(config.rtcToken)
        AUIBroadLogger.logger().d(TAG, "renew token ...")
    }

    fun destroy() {
        cleanRoomInfo(channelName)
        cleanRoom()
    }

    fun exit(): Boolean {
        cleanUserInfo(channelName, AUIRoomBroadContext.shared().currentUserInfo.userId)
        cleanRoom()
        AUIRoomBroadContext.shared().getArbiter(channelName)?.release()
        return isRoomDestroyed
    }

    private fun initRoom(completion: (AUIBroadException?) -> Unit) {
        val roomInfo = roomInfo?:return
        val basicInfo = AUIVoiceRoomInfo(
            roomInfo.micSeatCount,
            roomInfo.micSeatStyle.toInt(),
            roomInfo.roomId,
            roomInfo
        )

        val basicInfoStr = GsonBroadTools.beanToString(basicInfo)
        if (basicInfoStr == null) {
            completion.invoke(
                AUIBroadException(
                    AUIBroadException.ERROR_CODE_NETWORK_PARSE,
                    "initRoom >> bean to string failed!"
                )
            )
            return
        }
        rtmManager.setBatchMetadata(
            channelName,
            lockName = "",
            metadata = mapOf(Pair(kRoomInfoAttrKey, basicInfoStr)),
            fetchImmediately = true
        ) { err ->
            if (err == null) {
                completion.invoke(null)
            } else {
                completion.invoke(
                    AUIBroadException(
                        AUIBroadException.ERROR_CODE_RTM,
                        "initRoom >> setBatchMetadata failed : $err"
                    )
                )
            }
        }
    }

    private fun checkRoomValid() {
        if (subscribeSuccess && roomInfo != null && lockRetrived && userSnapshotList != null) {

            micSeatService.initService{}

            if (enterRoomCompletion != null) {
                enterRoomCompletion?.invoke(roomInfo)
                enterRoomCompletion = null
                observableHelper.notifyEventHandlers {
                    it.onRoomInfoChange(channelName, roomInfo!!)
                }
            }

            // room owner not found, clean room
            val snapShotOwnerId = userSnapshotList?.find {
                it.userId == AUIRoomBroadContext.shared().getRoomOwner(channelName)
            }
            if (snapShotOwnerId == null) {
                cleanRoomInfo(channelName)
            }

            imManagerService.serviceDidLoad()
        }
    }

    private fun cleanRoomInfo(roomId: String) {
        if (AUIRoomBroadContext.shared().getArbiter(roomId)?.isArbiter() != true) {
            return
        }
        micSeatService.deInitService { }
        imManagerService.deInitService { }
        invitationService.deInitService { }
        rtmManager.cleanBatchMetadata(
            channelName,
            remoteKeys = listOf(kRoomInfoAttrKey),
            fetchImmediately = true
        ) {}
        AUIRoomBroadContext.shared().getArbiter(channelName)?.destroy()
    }

    private fun cleanRoom() {
        logoutRtm()
        leaveRtcChannel()
        AUIRoomBroadContext.shared().cleanRoom(channelName)
    }

    private fun logoutRtm() {
        rtmManager.deInit()
        rtmManager.unSubscribe(channelName)
        rtmManager.unSubscribeError(rtmErrorRespObserver)
        rtmManager.unsubscribeLock(rtmLockRespObserver)
        rtmManager.logout()
        if (rtmCreateCreateByService) {
            RtmClient.release()
        }
    }

    private fun leaveRtcChannel() {
        rtcEngine.leaveChannel()
        if (rtcEngineCreateByService) {
            RtcEngine.destroy()
        }
    }

    private fun cleanUserInfo(roomId: String, userId: String) {
        if (AUIRoomBroadContext.shared().getArbiter(roomId)?.isArbiter() != true) {
            return
        }
        val index = micSeatService.getMicSeatIndex(userId)
        if (index >= 0) {
            micSeatService.kickSeat(index) {}
        }
    }

    data class AUIVoiceRoomInfo(
        val micSeatCount: Int,
        val micSeatStyle: Int,
        val roomId: String,
        val payload: AUIRoomBroadInfo
    )

}