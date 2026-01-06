package io.agora.auikit.service.imp

import io.agora.auikit.model.AUIRoomBroadContext
import io.agora.auikit.model.AUIUserBroadInfo
import io.agora.auikit.service.IAUIUserBroadService
import io.agora.auikit.service.callback.AUIBroadCallback
import io.agora.auikit.service.callback.AUIBroadException
import io.agora.auikit.service.callback.AUIUserListBroadCallback
import io.agora.auikit.service.http.CommonBroadResp
import io.agora.auikit.service.http.HttpBroadManager
import io.agora.auikit.service.http.BroadUtils
import io.agora.auikit.service.http.user.UserBroadInterface
import io.agora.auikit.service.http.user.UserKickOutBroadReq
import io.agora.auikit.service.http.user.UserKickOutResp
import io.agora.auikit.service.rtm.AUIRtmBroadManager
import io.agora.auikit.service.rtm.AUIRtmUserRespObserver
import io.agora.auikit.utils.AUIBroadLogger
import io.agora.auikit.utils.GsonBroadTools
import io.agora.auikit.utils.ObservableBroadHelper
import io.agora.rtc2.Constants
import io.agora.rtc2.IRtcEngineEventHandler
import io.agora.rtc2.RtcEngine
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AUIUserServiceBroadImpl constructor(
    private val channelName: String,
    private val rtmManager: AUIRtmBroadManager,
    // TODO rtm踢人在2.1.9版本尚不完成，暂时使用rtcEngine代替，如果不传将无法收到被踢回调
    private val rtcEngine: RtcEngine? = null,
) : IAUIUserBroadService, AUIRtmUserRespObserver {

    private val TAG = "AUiUserServiceImpl"

    private var mUserList = mutableListOf<AUIUserBroadInfo>()

    private val observableHelper =
        ObservableBroadHelper<IAUIUserBroadService.AUIUserRespObserver>()

    private var isLocalUserKickedOut = false

    private val rtcEventHandler = object: IRtcEngineEventHandler(){
        override fun onConnectionStateChanged(state: Int, reason: Int) {
            super.onConnectionStateChanged(state, reason)
//            Log.e("被踢原因","resaon: $reason. state:$state")
            if (state == Constants.CONNECTION_STATE_FAILED && reason == Constants.CONNECTION_CHANGED_BANNED_BY_SERVER) {
                isLocalUserKickedOut = true

                observableHelper.notifyEventHandlers {
                    it.onLocalUserKickedOut(channelName)
                }
            }
        }

        override fun onNetworkQuality(uid: Int, txQuality: Int, rxQuality: Int) {
            super.onNetworkQuality(uid, txQuality, rxQuality)
//            Log.d("onNetworkQuality", "发送网络质量事件: userId=$uid, txQuality=$txQuality, rxQuality=$rxQuality")
             observableHelper.notifyEventHandlers {
                   it.onNetworkQuality(uid.toString(),txQuality,rxQuality)
             }
        }
    }

    init {
        rtcEngine?.addHandler(rtcEventHandler)
        rtmManager.subscribeUser(this)
    }

    override fun registerRespObserver(observer: IAUIUserBroadService.AUIUserRespObserver?) {
        observableHelper.subscribeEvent(observer)
        if (mUserList.isNotEmpty()) {
            this.observableHelper.notifyEventHandlers {
                it.onRoomUserSnapshot(channelName, mUserList)
            }
        }
        if (isLocalUserKickedOut) {
            observableHelper.notifyEventHandlers {
                it.onLocalUserKickedOut(channelName)
            }
        }
    }

    override fun unRegisterRespObserver(observer: IAUIUserBroadService.AUIUserRespObserver?) {
        observableHelper.unSubscribeEvent(observer)
    }

    override fun getUserInfoList(
        roomId: String,
        userIdList: MutableList<String>?,
        callback: AUIUserListBroadCallback?
    ) {
        rtmManager.whoNow(roomId) { error, userList ->
            if (error != null) {
                callback?.onResult(
                    AUIBroadException(
                        error.code,
                        error.message
                    ), null)
                return@whoNow
            }
            if (userList == null) {
                callback?.onResult(
                    AUIBroadException(
                        -1,
                        ""
                    ), null)
                return@whoNow
            }
            val users = mutableListOf<AUIUserBroadInfo>()
            userList.forEach { userMap ->
                GsonBroadTools.toBean(GsonBroadTools.beanToString(userMap), AUIUserBroadInfo::class.java)?.let {
                    users.add(it)
                }
            }
            callback?.onResult(null, users)
        }
    }

    override fun muteUserAudio(isMute: Boolean, callback: AUIBroadCallback?) {
        val currentUserId = roomContext.currentUserInfo.userId
        val user = mUserList.first { it.userId == currentUserId }
        user.muteAudio = if (isMute) 1 else 0
        val map = GsonBroadTools.beanToMap(user)
        rtmManager.setPresenceState(channelName, attr = map.mapValues { it.value.toString() }) { error ->
            if (error != null) {
                callback?.onResult(
                    AUIBroadException(
                        error.code,
                        error.message
                    )
                )
            } else {
                this.observableHelper.notifyEventHandlers {
                    it.onUserAudioMute(currentUserId, isMute)
                }
                callback?.onResult(null)
            }
        }
    }

    override fun muteUserVideo(isMute: Boolean, callback: AUIBroadCallback?) {
        val currentUserId = roomContext.currentUserInfo.userId
        val user = mUserList.firstOrNull { it.userId == currentUserId }
        if (user == null) {
            callback?.onResult(
                AUIBroadException(
                    -1,
                    "can't find current user from users"
                )
            )
            return
        }
        user.muteVideo = if (isMute) 1 else 0
        val map = GsonBroadTools.beanToMap(user) as Map<String, String>
        rtmManager.setPresenceState(channelName, attr = map) { error ->
            if (error != null) {
                callback?.onResult(
                    AUIBroadException(
                        error.code,
                        error.message
                    )
                )
            } else {
                callback?.onResult(null)
            }
        }
    }

    override fun kickUser(userId: String, callback: AUIBroadCallback?) {
        // 调用 http 接口踢人
        HttpBroadManager.getService(UserBroadInterface::class.java).kickOut(
            UserKickOutBroadReq(
                roomContext.mCommonConfig?.appId ?: "",
                roomContext.mCommonConfig?.basicAuth ?: "",
                channelName,
                userId.toLong()
            )
        ).enqueue(object: Callback<CommonBroadResp<UserKickOutResp>>{
            override fun onResponse(
                call: Call<CommonBroadResp<UserKickOutResp>>,
                response: Response<CommonBroadResp<UserKickOutResp>>
            ) {
                val rsp = response.body()?.data
                if (response.body()?.code == 0 && rsp != null) {
                    // success
                    callback?.onResult(null)
                } else {
                    callback?.onResult(BroadUtils.errorFromResponse(response))
                }
            }

            override fun onFailure(call: Call<CommonBroadResp<UserKickOutResp>>, t: Throwable) {
                callback?.onResult(AUIBroadException(-1, t.message))
            }
        })
    }

    override fun getUserInfo(userId: String): AUIUserBroadInfo? {
        return mUserList.firstOrNull { it.userId == userId }
    }

    override fun getRoomContext(): AUIRoomBroadContext { return AUIRoomBroadContext.shared() }

    override fun getChannelName() = channelName

    /** AUiRtmUserProxyDelegate */
    override fun onUserSnapshotRecv(
        channelName: String,
        userId: String,
        userList: List<Map<String, Any>>
    ) {
        val users = mutableListOf<AUIUserBroadInfo>()
        userList.forEach { userMap ->
            GsonBroadTools.toBean(GsonBroadTools.beanToString(userMap), AUIUserBroadInfo::class.java)?.let {
                users.add(it)
            }
        }
        mUserList = users
        this.observableHelper.notifyEventHandlers {
            it.onRoomUserSnapshot(channelName, mUserList)
        }
        setupUserAttr(channelName)
    }

    override fun onUserDidJoined(
        channelName: String,
        userId: String,
        userInfo: Map<String, Any>
    ) {
        GsonBroadTools.toBean(GsonBroadTools.beanToString(userInfo), AUIUserBroadInfo::class.java)?.let { info ->
            info.userId = userId
            mUserList.add(info)
            this.observableHelper.notifyEventHandlers {
                it.onRoomUserEnter(channelName, info)
            }
        }
    }

    override fun onUserDidLeaved(
        channelName: String,
        userId: String,
        userInfo: Map<String, Any>
    ) {
        val index = mUserList.indexOfFirst{ it.userId == userId }
        val info = mUserList.removeAt(index)
        this.observableHelper.notifyEventHandlers {
            it.onRoomUserLeave(channelName, info)
        }
    }

    override fun onUserDidUpdated(
        channelName: String,
        userId: String,
        userInfo: Map<String, Any>
    ) {
        if (userInfo.isEmpty()) {
            return
        }
        GsonBroadTools.toBean(GsonBroadTools.beanToString(userInfo), AUIUserBroadInfo::class.java)?.let { info ->
            val index = mUserList.indexOfFirst{ it.userId == info.userId }
            if (index == -1) { // 不存在该用户
                mUserList.add(info)
            } else {
                val oldInfo = mUserList[index]
                mUserList[index] = info
                // 单独更新语音被禁用回调
                if (oldInfo.muteAudio != info.muteAudio) {
                    this.observableHelper.notifyEventHandlers {
                        it.onUserAudioMute(info.userId, (info.muteAudio == 1))
                    }
                }
            }
            this.observableHelper.notifyEventHandlers {
                it.onRoomUserUpdate(channelName, info)
            }
        }
    }

    private fun setupUserAttr(roomId: String){
        val userId = AUIRoomBroadContext.shared().currentUserInfo.userId
        val userInfo = mUserList.firstOrNull { it.userId == userId } ?: AUIUserBroadInfo()
        userInfo.userId = AUIRoomBroadContext.shared().currentUserInfo.userId
        userInfo.userName = AUIRoomBroadContext.shared().currentUserInfo.userName
        userInfo.userAvatar = AUIRoomBroadContext.shared().currentUserInfo.userAvatar

        val userAttr = GsonBroadTools.beanToMap(userInfo)
        AUIBroadLogger.logger().d(TAG, "setupUserAttr: $roomId : $userAttr")
        rtmManager.setPresenceState(roomId, attr = userAttr) { error ->
            if(error != null){
                AUIBroadLogger.logger().d(TAG, "setupUserAttr: $roomId fail: ${error.reason}")
            }else{
                //rtm不会返回自己更新的数据，需要手动处理
                onUserDidUpdated(roomId, userId, userAttr)
            }
        }
    }
}