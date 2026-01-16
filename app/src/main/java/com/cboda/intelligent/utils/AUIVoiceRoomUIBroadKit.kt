//package com.cboda.intelligent.utils
//
//import com.cboda.agora.AUIAPIBroadConfig
//import com.cboda.intelligent.service.MeetingFloatBroadService
//import com.cboda.agora.AUIVoiceRoomBroadObserver
//import com.cboda.agora.AUIVoiceRoomBroadService
//import com.cboda.agora.AUIVoiceRoomBroadView
//import com.cboda.agora.service.service.callback.AUIBroadException
//import com.cboda.agora.service.service.callback.AUIRoomBroadCallback
//import com.cboda.agora.service.service.http.CommonBroadResp
//import com.cboda.agora.service.service.http.HttpBroadManager
//import com.cboda.agora.service.service.http.BroadUtils
//import com.cboda.agora.service.service.http.room.RoomBroadInterface
//import com.cboda.agora.service.service.http.room.UpdateRoomReq
//import com.cboda.agora.service.service.http.token.TokenGenerateBroadReq
//import com.cboda.agora.service.service.http.token.TokenGenerateResp
//import com.cboda.agora.service.service.http.token.TokenBroadInterface
//import com.cboda.agora.service.service.room.AUIRoomBroadManager
//import com.cboda.agora.service.model.AUICommonBroadConfig
//import com.cboda.agora.service.model.AUIRoomBroadConfig
//import com.cboda.agora.service.model.AUIRoomBroadContext
//import com.cboda.agora.service.model.AUIRoomBroadInfo
//import com.cboda.agora.service.utils.AUIBroadLogger
//import org.greenrobot.eventbus.EventBus
//import retrofit2.Call
//import retrofit2.Callback
//import retrofit2.Response
//
//object AUIVoiceRoomUIBroadKit {
//    private val mSceneId = "VoiceRoomUIKit"
//    private var mAPIConfig: AUIAPIBroadConfig? = null
//    private val mRoomManager by lazy { AUIRoomBroadManager(sceneId = mSceneId) }
//    private val mServices = mutableMapOf<String, AUIVoiceRoomBroadService>()
//    //用来存放本地禁言状态
//    private val mRoomMuteState = mutableMapOf<String, Boolean>()
//
//
//    private val roomInterface by lazy {
//        HttpBroadManager.getService(RoomBroadInterface::class.java)
//    }
//
//    /**
//     * 初始化。
//     * 对于rtmClient、rtcEngineEx、ktvApi：
//     *      当外部没传时内部会自行创建，并在release方法调用时销毁；
//     *      当外部传入时在release时不会销毁
//     */
//    fun init(
//        config: AUICommonBroadConfig,
//        apiConfig: AUIAPIBroadConfig
//    ) {
//        mAPIConfig = apiConfig
//        AUIRoomBroadContext.shared().setCommonConfig(config)
//        HttpBroadManager.setBaseURL(config.host)
//        AUIBroadLogger.initLogger(AUIBroadLogger.BroadConfig(config.context, "Voice"))
//
//    }
//
//    /**
//     * 释放资源
//     */
//    fun release() {
//        mAPIConfig = null
//    }
//
//    fun getServiceByRoomId(roomId:String): AUIVoiceRoomBroadService? {
//       return mServices[roomId]
//    }
//
//    /**
//     * 获取房间列表
//     */
//    fun getRoomList(
//        startTime: Long?,
//        pageSize: Int,
//        success: (List<AUIRoomBroadInfo>) -> Unit,
//        failure: (AUIBroadException) -> Unit
//    ) {
//        checkSetupAndCommonConfig()
//        mRoomManager.getRoomInfoList(
//            startTime, pageSize
//        ) { error, roomList ->
//            if (error == null) {
//                success.invoke(roomList ?: emptyList())
//            } else {
//                failure.invoke(error)
//            }
//        }
//    }
//
//
//    fun updateRoomInfo(roomInfo: AUIRoomBroadInfo,
//                       callback: AUIRoomBroadCallback?){
//        roomInterface.updateRoomInfo(UpdateRoomReq(AUIRoomBroadContext.shared().mCommonConfig?.appId?:"", mSceneId, roomInfo.roomId, roomInfo))
//            .enqueue(object : retrofit2.Callback<CommonBroadResp<String>> {
//                override fun onResponse(
//                    call: Call<CommonBroadResp<String>>,
//                    response: Response<CommonBroadResp<String>>
//                ) {
//                    val rsp = response.body()?.data
//                    if (response.body()?.code == 0 && rsp != null) {
//                        // success
//                        callback?.onResult(null, roomInfo)
//                    } else {
//                        callback?.onResult(BroadUtils.errorFromResponse(response), null)
//                    }
//                }
//
//                override fun onFailure(call: Call<CommonBroadResp<String>>, t: Throwable) {
//                    callback?.onResult(AUIBroadException(-1, t.message), null)
//                }
//            })
//
//
//    }
//
//    /**
//     * 创建房间
//     */
//    fun createRoom(
//        roomInfo: AUIRoomBroadInfo,
//        roomConfig: AUIRoomBroadConfig,
//        roomView: AUIVoiceRoomBroadView,
//        completion: (AUIBroadException?, AUIRoomBroadInfo?) -> Unit
//    ) {
//        checkSetupAndCommonConfig()
//        if (mServices[roomInfo.roomId] != null) {
//            completion.invoke(AUIBroadException(AUIBroadException.ERROR_CODE_ROOM_EXITED, ""), null)
//            return
//        }
//        mRoomManager.createRoom(
//            roomInfo
//        ) { error, _ ->
//            AUIBroadLogger.logger().d(
//                message = "Create room >> error=$error, roomInfo=$roomInfo"
//            )
//        }
//        val roomService = AUIVoiceRoomBroadService(
//            mAPIConfig!!,
//            roomConfig
//        )
//        mServices[roomInfo.roomId] = roomService
//        // 加入房间
//        roomService.create(roomInfo) { error ->
//            if (error == null) {
//                // success
//                AUIBroadLogger.logger().d(tag = "KaraokeUiKit", message = "Enter room successfully")
//                completion.invoke(null, roomInfo)
//                EventBus.getDefault().post(MeetingFloatBroadService.MeetingEvent(MeetingFloatBroadService.EVENT_SUBSCRIBE_MSG,true))
//            } else {
//                AUIBroadLogger.logger()
//                    .d(tag = "KaraokeUiKit", message = "Enter room failed : ${error.code}  msg:${error.message}")
//                completion.invoke(error, null)
//            }
//        }
//        roomView.bindService(roomService)
//    }
//
//
//
//    /**
//     * 拉起并跳转的房间页面
//     */
//    fun launchRoom(
//        roomInfo: AUIRoomBroadInfo,
//        roomConfig: AUIRoomBroadConfig,
//        voiceRoom: AUIVoiceRoomBroadView,
//        completion: (AUIBroadException?, AUIRoomBroadInfo?) -> Unit
//    ) {
//        checkSetupAndCommonConfig()
//        if (mServices[roomInfo.roomId] != null) {
//            completion.invoke(AUIBroadException(AUIBroadException.ERROR_CODE_ROOM_EXITED, ""), null)
//            return
//        }
//        val roomService = AUIVoiceRoomBroadService(
//            mAPIConfig!!,
//            roomConfig
//        )
//        mServices[roomInfo.roomId] = roomService
//        roomService.observableHelper.subscribeEvent(voiceRoom)
//        // 加入房间
//        roomService.enter { error ->
//            if (error == null) {
//                // success
//                voiceRoom.bindService(roomService)
//                AUIBroadLogger.logger().d(tag = "AUIVoiceRoomUiKit", message = "Enter room successfully")
//                completion.invoke(null, roomInfo)
//                EventBus.getDefault().post(MeetingFloatBroadService.MeetingEvent(MeetingFloatBroadService.EVENT_SUBSCRIBE_MSG,true))
//            } else {
//                AUIBroadLogger.logger()
//                    .d(tag = "KaraokeUiKit", message = "Enter room failed : ${error.code}")
//                completion.invoke(error, null)
//            }
//        }
//    }
//
//    fun destroyRoom(roomId: String) {
//        if (AUIRoomBroadContext.shared().isRoomOwner(roomId)) {
//            mRoomManager.destroyRoom(
//                roomId
//            ) {}
//            mServices[roomId]?.destroy()
//        } else if (mServices[roomId]?.exit() == true) {
//            mRoomManager.destroyRoom(
//                roomId
//            ) {}
//        }
//        mServices.remove(roomId)
//        mRoomMuteState.remove(roomId)
//
//    }
//
//    fun registerRespObserver(roomId: String, observer: AUIVoiceRoomBroadObserver) {
//        mServices[roomId]?.observableHelper?.subscribeEvent(observer)
//    }
//
//    fun unRegisterRespObserver(roomId: String, observer: AUIVoiceRoomBroadObserver) {
//        mServices[roomId]?.observableHelper?.unSubscribeEvent(observer)
//    }
//
//    fun renewToken(roomId: String, roomConfig: AUIRoomBroadConfig) {
//        mServices[roomId]?.renew(roomConfig)
//    }
//
//    fun generateToken(
//        roomId: String,
//        onSuccess: (AUIRoomBroadConfig) -> Unit,
//        onFailure: (AUIBroadException) -> Unit
//    ) {
//        val config = AUIRoomBroadConfig(roomId)
//        var response = 2
//        var isFailure = false
//        val trySuccess = {
//            response -= 1
//            if (response == 0 && !isFailure) {
//                onSuccess.invoke(config)
//            }
//        }
//        val failure = { ex: AUIBroadException ->
//            if (!isFailure) {
//                isFailure = true
//                onFailure.invoke(ex)
//            }
//        }
//
//        val userId = AUIRoomBroadContext.shared().currentUserInfo.userId
//        HttpBroadManager
//            .getService(TokenBroadInterface::class.java)
//            .tokenGenerate(
//                TokenGenerateBroadReq(
//                    AUIRoomBroadContext.shared().requireCommonConfig().appId,
//                    AUIRoomBroadContext.shared().requireCommonConfig().appCert,
//                    config.channelName,
//                    userId
//                )
//            )
//            .enqueue(object : Callback<CommonBroadResp<TokenGenerateResp>> {
//                override fun onResponse(
//                    call: Call<CommonBroadResp<TokenGenerateResp>>,
//                    response: Response<CommonBroadResp<TokenGenerateResp>>
//                ) {
//                    val rspObj = response.body()?.data
//                    if (rspObj != null) {
//                        config.rtcToken = rspObj.rtcToken
//                        config.rtmToken = rspObj.rtmToken
//                        AUIRoomBroadContext.shared().requireCommonConfig().appId = rspObj.appId
//                    }
//                    trySuccess.invoke()
//                }
//
//                override fun onFailure(
//                    call: Call<CommonBroadResp<TokenGenerateResp>>,
//                    t: Throwable
//                ) {
//                    failure.invoke(AUIBroadException(-1, t.message))
//                }
//            })
//        HttpBroadManager
//            .getService(TokenBroadInterface::class.java)
//            .tokenGenerate(
//                TokenGenerateBroadReq(
//                    AUIRoomBroadContext.shared().requireCommonConfig().appId,
//                    AUIRoomBroadContext.shared().requireCommonConfig().appCert,
//                    config.rtcChorusChannelName,
//                    userId
//                )
//            )
//            .enqueue(object : Callback<CommonBroadResp<TokenGenerateResp>> {
//                override fun onResponse(
//                    call: Call<CommonBroadResp<TokenGenerateResp>>,
//                    response: Response<CommonBroadResp<TokenGenerateResp>>
//                ) {
//                    val rspObj = response.body()?.data
//                    if (rspObj != null) {
//                        // rtcChorusRtcToken
//                        config.rtcChorusRtcToken = rspObj.rtcToken
//                    }
//                    trySuccess.invoke()
//                }
//
//                override fun onFailure(
//                    call: Call<CommonBroadResp<TokenGenerateResp>>,
//                    t: Throwable
//                ) {
//                    failure.invoke(AUIBroadException(-3, t.message))
//                }
//            })
//    }
//
//
//    private fun checkSetupAndCommonConfig() {
//        if (AUIRoomBroadContext.shared().mCommonConfig == null) {
//            throw RuntimeException("make sure invoke setup first")
//        }
//    }
//
//
//    /**
//     * 本地存储
//     */
//    fun setRoomMuteState(roomId: String,muted: Boolean){
//         mRoomMuteState[roomId]= muted
//
//    }
//
//    /**
//     * 获取是否是状态
//     */
//    fun isRoomMuted(roomId: String): Boolean = mRoomMuteState[roomId]?:false
//
//}