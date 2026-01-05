package com.lxkj.aiim.utils

import com.example.asceneskit.AUIAPIBroadConfig
import com.lxkj.aiim.service.MeetingFloatService
import com.example.asceneskit.AUIVoiceRoomBroadObserver
import com.example.asceneskit.AUIVoiceRoomBroadService
import com.example.asceneskit.AUIVoiceRoomBroadView
import io.agora.auikit.service.callback.AUIException
import io.agora.auikit.service.callback.AUIRoomCallback
import io.agora.auikit.service.http.CommonResp
import io.agora.auikit.service.http.HttpManager
import io.agora.auikit.service.http.Utils
import io.agora.auikit.service.http.room.RoomInterface
import io.agora.auikit.service.http.room.UpdateRoomReq
import io.agora.auikit.service.http.token.TokenGenerateReq
import io.agora.auikit.service.http.token.TokenGenerateResp
import io.agora.auikit.service.http.token.TokenInterface
import io.agora.auikit.service.room.AUIRoomManager
import io.agora.auikit.model.AUICommonConfig
import io.agora.auikit.model.AUIRoomConfig
import io.agora.auikit.model.AUIRoomContext
import io.agora.auikit.model.AUIRoomInfo
import io.agora.auikit.utils.AUILogger
import org.greenrobot.eventbus.EventBus
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object AUIVoiceRoomUIKit {
    private val mSceneId = "VoiceRoomUIKit"
    private var mAPIConfig: AUIAPIBroadConfig? = null
    private val mRoomManager by lazy { AUIRoomManager(sceneId = mSceneId) }
    private val mServices = mutableMapOf<String, AUIVoiceRoomBroadService>()
    //用来存放本地禁言状态
    private val mRoomMuteState = mutableMapOf<String, Boolean>()


    private val roomInterface by lazy {
        HttpManager.getService(RoomInterface::class.java)
    }

    /**
     * 初始化。
     * 对于rtmClient、rtcEngineEx、ktvApi：
     *      当外部没传时内部会自行创建，并在release方法调用时销毁；
     *      当外部传入时在release时不会销毁
     */
    fun init(
        config: AUICommonConfig,
        apiConfig: AUIAPIBroadConfig
    ) {
        mAPIConfig = apiConfig
        AUIRoomContext.shared().setCommonConfig(config)
        HttpManager.setBaseURL(config.host)
        AUILogger.initLogger(AUILogger.BroadConfig(config.context, "Voice"))

    }

    /**
     * 释放资源
     */
    fun release() {
        mAPIConfig = null
    }

    fun getServiceByRoomId(roomId:String): AUIVoiceRoomBroadService? {
       return mServices[roomId]
    }

    /**
     * 获取房间列表
     */
    fun getRoomList(
        startTime: Long?,
        pageSize: Int,
        success: (List<AUIRoomInfo>) -> Unit,
        failure: (AUIException) -> Unit
    ) {
        checkSetupAndCommonConfig()
        mRoomManager.getRoomInfoList(
            startTime, pageSize
        ) { error, roomList ->
            if (error == null) {
                success.invoke(roomList ?: emptyList())
            } else {
                failure.invoke(error)
            }
        }
    }


    fun updateRoomInfo(roomInfo: AUIRoomInfo,
                       callback: AUIRoomCallback?){
        roomInterface.updateRoomInfo(UpdateRoomReq(AUIRoomContext.shared().mCommonConfig?.appId?:"", mSceneId, roomInfo.roomId, roomInfo))
            .enqueue(object : retrofit2.Callback<CommonResp<String>> {
                override fun onResponse(
                    call: Call<CommonResp<String>>,
                    response: Response<CommonResp<String>>
                ) {
                    val rsp = response.body()?.data
                    if (response.body()?.code == 0 && rsp != null) {
                        // success
                        callback?.onResult(null, roomInfo)
                    } else {
                        callback?.onResult(Utils.errorFromResponse(response), null)
                    }
                }

                override fun onFailure(call: Call<CommonResp<String>>, t: Throwable) {
                    callback?.onResult(AUIException(-1, t.message), null)
                }
            })


    }

    /**
     * 创建房间
     */
    fun createRoom(
        roomInfo: AUIRoomInfo,
        roomConfig: AUIRoomConfig,
        roomView: AUIVoiceRoomBroadView,
        completion: (AUIException?, AUIRoomInfo?) -> Unit
    ) {
        checkSetupAndCommonConfig()
        if (mServices[roomInfo.roomId] != null) {
            completion.invoke(AUIException(AUIException.ERROR_CODE_ROOM_EXITED, ""), null)
            return
        }
        mRoomManager.createRoom(
            roomInfo
        ) { error, _ ->
            AUILogger.logger().d(
                message = "Create room >> error=$error, roomInfo=$roomInfo"
            )
        }
        val roomService = AUIVoiceRoomBroadService(
            mAPIConfig!!,
            roomConfig
        )
        mServices[roomInfo.roomId] = roomService
        // 加入房间
        roomService.create(roomInfo) { error ->
            if (error == null) {
                // success
                AUILogger.logger().d(tag = "KaraokeUiKit", message = "Enter room successfully")
                completion.invoke(null, roomInfo)
                EventBus.getDefault().post(MeetingFloatService.MeetingEvent(MeetingFloatService.EVENT_SUBSCRIBE_MSG,true))
            } else {
                AUILogger.logger()
                    .d(tag = "KaraokeUiKit", message = "Enter room failed : ${error.code}  msg:${error.message}")
                completion.invoke(error, null)
            }
        }
        roomView.bindService(roomService)
    }



    /**
     * 拉起并跳转的房间页面
     */
    fun launchRoom(
        roomInfo: AUIRoomInfo,
        roomConfig: AUIRoomConfig,
        voiceRoom: AUIVoiceRoomBroadView,
        completion: (AUIException?, AUIRoomInfo?) -> Unit
    ) {
        checkSetupAndCommonConfig()
        if (mServices[roomInfo.roomId] != null) {
            completion.invoke(AUIException(AUIException.ERROR_CODE_ROOM_EXITED, ""), null)
            return
        }
        val roomService = AUIVoiceRoomBroadService(
            mAPIConfig!!,
            roomConfig
        )
        mServices[roomInfo.roomId] = roomService
        roomService.observableHelper.subscribeEvent(voiceRoom)
        // 加入房间
        roomService.enter { error ->
            if (error == null) {
                // success
                voiceRoom.bindService(roomService)
                AUILogger.logger().d(tag = "AUIVoiceRoomUiKit", message = "Enter room successfully")
                completion.invoke(null, roomInfo)
                EventBus.getDefault().post(MeetingFloatService.MeetingEvent(MeetingFloatService.EVENT_SUBSCRIBE_MSG,true))
            } else {
                AUILogger.logger()
                    .d(tag = "KaraokeUiKit", message = "Enter room failed : ${error.code}")
                completion.invoke(error, null)
            }
        }
    }

    fun destroyRoom(roomId: String) {
        if (AUIRoomContext.shared().isRoomOwner(roomId)) {
            mRoomManager.destroyRoom(
                roomId
            ) {}
            mServices[roomId]?.destroy()
        } else if (mServices[roomId]?.exit() == true) {
            mRoomManager.destroyRoom(
                roomId
            ) {}
        }
        mServices.remove(roomId)
        mRoomMuteState.remove(roomId)

    }

    fun registerRespObserver(roomId: String, observer: AUIVoiceRoomBroadObserver) {
        mServices[roomId]?.observableHelper?.subscribeEvent(observer)
    }

    fun unRegisterRespObserver(roomId: String, observer: AUIVoiceRoomBroadObserver) {
        mServices[roomId]?.observableHelper?.unSubscribeEvent(observer)
    }

    fun renewToken(roomId: String, roomConfig: AUIRoomConfig) {
        mServices[roomId]?.renew(roomConfig)
    }

    fun generateToken(
        roomId: String,
        onSuccess: (AUIRoomConfig) -> Unit,
        onFailure: (AUIException) -> Unit
    ) {
        val config = AUIRoomConfig(roomId)
        var response = 2
        var isFailure = false
        val trySuccess = {
            response -= 1
            if (response == 0 && !isFailure) {
                onSuccess.invoke(config)
            }
        }
        val failure = { ex: AUIException ->
            if (!isFailure) {
                isFailure = true
                onFailure.invoke(ex)
            }
        }

        val userId = AUIRoomContext.shared().currentUserInfo.userId
        HttpManager
            .getService(TokenInterface::class.java)
            .tokenGenerate(
                TokenGenerateReq(
                    AUIRoomContext.shared().requireCommonConfig().appId,
                    AUIRoomContext.shared().requireCommonConfig().appCert,
                    config.channelName,
                    userId
                )
            )
            .enqueue(object : Callback<CommonResp<TokenGenerateResp>> {
                override fun onResponse(
                    call: Call<CommonResp<TokenGenerateResp>>,
                    response: Response<CommonResp<TokenGenerateResp>>
                ) {
                    val rspObj = response.body()?.data
                    if (rspObj != null) {
                        config.rtcToken = rspObj.rtcToken
                        config.rtmToken = rspObj.rtmToken
                        AUIRoomContext.shared().requireCommonConfig().appId = rspObj.appId
                    }
                    trySuccess.invoke()
                }

                override fun onFailure(
                    call: Call<CommonResp<TokenGenerateResp>>,
                    t: Throwable
                ) {
                    failure.invoke(AUIException(-1, t.message))
                }
            })
        HttpManager
            .getService(TokenInterface::class.java)
            .tokenGenerate(
                TokenGenerateReq(
                    AUIRoomContext.shared().requireCommonConfig().appId,
                    AUIRoomContext.shared().requireCommonConfig().appCert,
                    config.rtcChorusChannelName,
                    userId
                )
            )
            .enqueue(object : Callback<CommonResp<TokenGenerateResp>> {
                override fun onResponse(
                    call: Call<CommonResp<TokenGenerateResp>>,
                    response: Response<CommonResp<TokenGenerateResp>>
                ) {
                    val rspObj = response.body()?.data
                    if (rspObj != null) {
                        // rtcChorusRtcToken
                        config.rtcChorusRtcToken = rspObj.rtcToken
                    }
                    trySuccess.invoke()
                }

                override fun onFailure(
                    call: Call<CommonResp<TokenGenerateResp>>,
                    t: Throwable
                ) {
                    failure.invoke(AUIException(-3, t.message))
                }
            })
    }


    private fun checkSetupAndCommonConfig() {
        if (AUIRoomContext.shared().mCommonConfig == null) {
            throw RuntimeException("make sure invoke setup first")
        }
    }


    /**
     * 本地存储
     */
    fun setRoomMuteState(roomId: String,muted: Boolean){
         mRoomMuteState[roomId]= muted

    }

    /**
     * 获取是否是状态
     */
    fun isRoomMuted(roomId: String): Boolean = mRoomMuteState[roomId]?:false

}