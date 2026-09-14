package com.cboda.chatui.quickmeet.vm

import android.text.TextUtils
import androidx.lifecycle.viewModelScope
import com.cboda.baselibrary.base.BaseViewModel
import com.cboda.baselibrary.bean.CreateGroupData
import com.cboda.baselibrary.bean.GlobalChatGroupBean
import com.cboda.baselibrary.bean.PayLsOrderBean
import com.cboda.baselibrary.livedata.SingleLiveBroadData
import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.baselibrary.utils.APP_TAG
import com.cboda.baselibrary.utils.HttpsBroadConfig
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.toJson2
import com.cboda.baselibrary.utils.toLog
import com.cboda.chatui.quickmeet.LiveKitMeetMapper
import com.cboda.chatui.quickmeet.MeetCallManager
import com.cboda.chatui.quickmeet.UrlMeetConstants
import com.cboda.chatui.quickmeet.bean.CheckMeetPayBean
import com.cboda.chatui.quickmeet.bean.QuickDetailMeetBean
import com.cboda.chatui.quickmeet.bean.QuickMeetInfoBean
import com.cboda.chatui.quickmeet.bean.SeatMicListBean
import com.cboda.chatui.quickmeet.dismissLoadingWfc
import com.cboda.chatui.quickmeet.showLoadingWfc
import com.freddy.im.http.ImMeetingHttpExample
import com.freddy.im.http.ImNotifyHttpExample
import com.freddy.im.http.model.im.MeetingSessionVO
import com.freddy.im.listener.OnHttpImListener
import com.freddy.im.meeting.GroupMeetSessionStore
import com.freddy.im.signal.OthBusiSigCodes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.json.JSONObject
import kotlin.coroutines.resume
import kotlin.jvm.java

//极速直播
open class QuickMeetViewBroadModel : BaseViewModel() {

    /**
     * 直播信息查询（声网）/ LiveKit 用 meetingId join 换 token。
     */
    suspend fun goQuickMeetInfo(groupIdNo: String, liveStatus: String): SingleLiveBroadData<QuickMeetInfoBean> {
        if (HttpsBroadConfig.useLiveKitGroupMeeting) {
            return goLiveKitMeetInfo(groupIdNo)
        }
//        showLoadingWfc()
        val safeCenterLiveData = SingleLiveBroadData<QuickMeetInfoBean>()
        val params = hashMapOf("groupIdNo" to groupIdNo, "liveStatus" to liveStatus)
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2<QuickMeetInfoBean>(
                    UrlMeetConstants.QUICK_LIVE_INFO,
                    QuickMeetInfoBean::class.java,
                    params
                ).onSuccess { userInfo ->
//                    dismissLoadingWfc()
                    userInfo?.let {
                        safeCenterLiveData.postValue(it)
                    }

                }.onFailure { code, message ->
//                    dismissLoadingWfc()
                    ToastBroadUtils.show(message)
                }.onError {
//                    dismissLoadingWfc()
                }

            } catch (e: Exception) {
                e.printStackTrace()
//                dismissLoadingWfc()
            }
        }
        return safeCenterLiveData
    }

    /** LiveKit：按群缓存的 meetingId 调 /join，组装成旧 QuickMeetInfoBean。 */
    private suspend fun goLiveKitMeetInfo(groupIdNo: String): SingleLiveBroadData<QuickMeetInfoBean> {
        val liveData = SingleLiveBroadData<QuickMeetInfoBean>()
        val session = GroupMeetSessionStore.get(groupIdNo)
        if (session == null) {
            ToastBroadUtils.show("会议不存在或已结束~")
            return liveData
        }
        val joined = awaitMeetingSession { cb, fail ->
            ImMeetingHttpExample.requestJoin(session.meetingId, session.roomName, null, cb, fail)
        } ?: return liveData
        val bean = LiveKitMeetMapper.toQuickDetail(joined, groupIdNo, session.title)
        GroupMeetSessionStore.put(groupIdNo, joined.meetingId, joined.roomName, joined.title)
        liveData.postValue(
            QuickMeetInfoBean(
                list = listOf(bean),
                liveStatus = "active",
                token = joined.token,
            )
        )
        return liveData
    }


    /**
     * 创建直播 / LiveKit 群会议室
     */
    suspend fun goCreateQuickMeet(groupIdNo: String, channelName: String, durationConfigId: Int, userTierConfigId: Int, orderId: String?): SingleLiveBroadData<QuickDetailMeetBean> {
        if (HttpsBroadConfig.useLiveKitGroupMeeting) {
            return goCreateLiveKitMeet(groupIdNo, channelName)
        }
        showLoadingWfc()
        val safeCenterLiveData = SingleLiveBroadData<QuickDetailMeetBean>()
        val params = hashMapOf(
            "groupIdNo" to groupIdNo,
            "channelId" to groupIdNo,
            "channelName" to channelName,
            "durationConfigId" to durationConfigId,
            "userTierConfigId" to userTierConfigId
        )
        if (!TextUtils.isEmpty(orderId)) {
            params["payType"] = "chain"
            params["orderId"] = orderId ?: ""
        }
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2<QuickDetailMeetBean>(
                    UrlMeetConstants.QUICK_LIVE_CREATE,
                    QuickDetailMeetBean::class.java,
                    params
                ).onSuccess { userInfo ->
                    dismissLoadingWfc()
                    userInfo?.let {
                        safeCenterLiveData.postValue(it)
                    }

                }.onFailure { code, message ->
                    dismissLoadingWfc()
//                    safeCenterLiveData.postValue(QuickDetailMeetBean())
                    ToastBroadUtils.show(message)
                }.onError {
                    dismissLoadingWfc()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                dismissLoadingWfc()
            }
        }
        return safeCenterLiveData
    }

    /**
     * LiveKit：POST /im/meeting/create（mode=live, mediaType=audio），并推送 MEET_START。
     */
    suspend fun goCreateLiveKitMeet(groupIdNo: String, channelName: String): SingleLiveBroadData<QuickDetailMeetBean> {
        showLoadingWfc()
        val liveData = SingleLiveBroadData<QuickDetailMeetBean>()
        val session = awaitMeetingSession { cb, fail ->
            ImMeetingHttpExample.requestCreateLiveAudio(channelName, groupIdNo, 4, cb, fail)
        }
        dismissLoadingWfc()
        if (session == null) {
            return liveData
        }
        val bean = LiveKitMeetMapper.toQuickDetail(session, groupIdNo, channelName)
        GroupMeetSessionStore.put(groupIdNo, session.meetingId, session.roomName, session.title)
        // 通知群成员可加入（对齐声网 MEET_START）
        pushMeetStartSignal(groupIdNo, session)
        liveData.postValue(bean)
        return liveData
    }

    private fun pushMeetStartSignal(groupIdNo: String, session: MeetingSessionVO) {
        try {
            val data = JSONObject()
                .put("groupId", groupIdNo)
                .put("meetingId", session.meetingId)
                .put("roomName", session.roomName)
                .put("title", session.title ?: "")
                .toString()
            ImNotifyHttpExample.requestBusiPush(
                groupIdNo,
                OthBusiSigCodes.MEET_START,
                data,
                { },
                null,
            )
        } catch (e: Exception) {
            e.toLog(APP_TAG)
        }
    }


    /**
     * 销毁直播频道 / LiveKit 散会
     */
    suspend fun goDissolveQuickMeet(groupIdNo: String): SingleLiveBroadData<Boolean> {
        if (HttpsBroadConfig.useLiveKitGroupMeeting || MeetCallManager.getInstance().mCallData.useLiveKit) {
            return goEndLiveKitMeet(groupIdNo)
        }
        val mLiveData = SingleLiveBroadData<Boolean>()
        val params = hashMapOf("groupIdNo" to groupIdNo)
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2(
                    UrlMeetConstants.QUICK_LIVE_DISSOLVE,
                    Any::class.java,
                    params
                ).onSuccess { userInfo ->
                    mLiveData.postValue(true)

                }.onFailure { code, message ->
//                    ToastBroadUtils.show(message)
                }.onError {

                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return mLiveData
    }

    private suspend fun goEndLiveKitMeet(groupIdNo: String): SingleLiveBroadData<Boolean> {
        val liveData = SingleLiveBroadData<Boolean>()
        val callData = MeetCallManager.getInstance().mCallData
        val meetingId = callData.meetingId.ifBlank {
            callData.quickDetailMeetBean?.meetingId
                ?: GroupMeetSessionStore.get(groupIdNo)?.meetingId
        }
        val roomName = callData.roomName.ifBlank {
            callData.quickDetailMeetBean?.roomName
                ?: GroupMeetSessionStore.get(groupIdNo)?.roomName
        }
        val ok = awaitBoolean { cb, fail ->
            ImMeetingHttpExample.requestEnd(meetingId, roomName, {
                cb.onHttpImSuccess(true)
            }, fail)
        }
        if (ok) {
            GroupMeetSessionStore.remove(groupIdNo)
            pushMeetEndSignal(groupIdNo)
        }
        liveData.postValue(ok)
        return liveData
    }

    private fun pushMeetEndSignal(groupIdNo: String) {
        try {
            val data = JSONObject().put("groupId", groupIdNo).toString()
            ImNotifyHttpExample.requestBusiPush(
                groupIdNo,
                OthBusiSigCodes.MEET_END,
                data,
                { },
                null,
            )
        } catch (e: Exception) {
            e.toLog(APP_TAG)
        }
    }

    /**
     * 延长直播时间
     */
    suspend fun goExtendQuickMeet(groupIdNo: String, orderId: String? = "", durationConfigId: Int = 1): SingleLiveBroadData<QuickDetailMeetBean> {
        showLoadingWfc()
        val safeCenterLiveData = SingleLiveBroadData<QuickDetailMeetBean>()
        val params = hashMapOf("groupIdNo" to groupIdNo, "durationConfigId" to durationConfigId)
        if (!TextUtils.isEmpty(orderId)) {
            params["payType"] = "chain"
            params["orderId"] = orderId ?: ""
        }
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2<QuickDetailMeetBean>(
                    UrlMeetConstants.QUICK_LIVE_EXTEND,
                    QuickDetailMeetBean::class.java,
                    params
                ).onSuccess { userInfo ->
                    dismissLoadingWfc()
                    userInfo?.let {
                        safeCenterLiveData.postValue(it)
                    }

                }.onFailure { code, message ->
                    dismissLoadingWfc()
                    ToastBroadUtils.show(message)
                }.onError {
                    dismissLoadingWfc()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                dismissLoadingWfc()
            }
        }
        return safeCenterLiveData
    }


    /**
     * 查询群信息
     */
    @Deprecated("废弃")
    suspend fun queryGroupInfo(groupId: String, failInvoke: (code: Int, msg: String) -> Unit = { _, _ -> }): SingleLiveBroadData<GlobalChatGroupBean> {
//        showLoadingWfc()
        val safeCenterLiveData = SingleLiveBroadData<GlobalChatGroupBean>()
        val params = hashMapOf("id" to groupId)
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2<GlobalChatGroupBean>(
                    UrlMeetConstants.QUICK_GROUP_INFO,
                    GlobalChatGroupBean::class.java,
                    params
                ).onSuccess { userInfo ->
//                    dismissLoadingWfc()
                    userInfo?.let {
                        safeCenterLiveData.postValue(it)
                    }

                }.onFailure { code, message ->
//                    dismissLoadingWfc()
                    failInvoke.invoke(code, message)
                    ToastBroadUtils.show(message)
                }.onError {
//                    dismissLoadingWfc()
                }

            } catch (e: Exception) {
                e.printStackTrace()
//                dismissLoadingWfc()
            }
        }
        return safeCenterLiveData
    }

    /**
     * 已经在麦位的人员列表
     */
    suspend fun goMeetMicSeatPersonList(groupIdNo: String?): SingleLiveBroadData<SeatMicListBean> {
        if (HttpsBroadConfig.useLiveKitGroupMeeting || MeetCallManager.getInstance().mCallData.useLiveKit) {
            return goLiveKitMicSeatList(groupIdNo)
        }
        showLoadingWfc()
        val safeCenterLiveData = SingleLiveBroadData<SeatMicListBean>()
        val params = hashMapOf("groupIdNo" to groupIdNo)
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2<SeatMicListBean>(
                    UrlMeetConstants.QUICK_MIC_SEAT_LIST,
                    SeatMicListBean::class.java,
                    params
                ).onSuccess { userInfo ->
                    dismissLoadingWfc()
                    userInfo?.let {
                        safeCenterLiveData.postValue(it)
                    }

                }.onFailure { code, message ->
                    dismissLoadingWfc()
                    ToastBroadUtils.show(message)
                }.onError {
                    dismissLoadingWfc()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                dismissLoadingWfc()
            }
        }
        return safeCenterLiveData
    }

    private suspend fun goLiveKitMicSeatList(groupIdNo: String?): SingleLiveBroadData<SeatMicListBean> {
        showLoadingWfc()
        val liveData = SingleLiveBroadData<SeatMicListBean>()
        val meetingId = resolveMeetingId(groupIdNo)
        val roomName = resolveRoomName(groupIdNo)
        val session = awaitMeetingSession { cb, fail ->
            ImMeetingHttpExample.requestInfo(meetingId, roomName, cb, fail)
        }
        dismissLoadingWfc()
        // speakers 为 userId 字符串列表
        val speaking = session?.speakers?.mapIndexed { index, userId ->
            com.cboda.baselibrary.bean.MeetUser(
                userId = userId,
                seatNumbIndex = index,
            )
        }?.toMutableList()
        liveData.postValue(
            SeatMicListBean(
                adminUserId = session?.hostId,
                speakingList = speaking,
                groupIdNo = groupIdNo,
            )
        )
        return liveData
    }


    /**
     * 下麦（声网 quick/live；LiveKit speak/leave 或 speak/kick）
     */
    suspend fun goNetDownMic(applicantUserId: String?, groupIdNo: String?): SingleLiveBroadData<Boolean> {
        if (HttpsBroadConfig.useLiveKitGroupMeeting || MeetCallManager.getInstance().mCallData.useLiveKit) {
            return goLiveKitDownMic(applicantUserId, groupIdNo)
        }
        showLoadingWfc()
        val mLiveData = SingleLiveBroadData<Boolean>()
        val params = hashMapOf("targetUserId" to applicantUserId, "groupIdNo" to groupIdNo)
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2(
                    UrlMeetConstants.QUICK_DOWN_MIC,
                    Any::class.java,
                    params
                ).onSuccess { userInfo ->
                    dismissLoadingWfc()
                    mLiveData.postValue(true)

                }.onFailure { code, message ->
                    dismissLoadingWfc()
                    ToastBroadUtils.show(message)

                }.onError {
                    dismissLoadingWfc()
                }

            } catch (e: Exception) {
                e.printStackTrace()
                dismissLoadingWfc()
            }
        }
        return mLiveData
    }

    private suspend fun goLiveKitDownMic(applicantUserId: String?, groupIdNo: String?): SingleLiveBroadData<Boolean> {
        showLoadingWfc()
        val liveData = SingleLiveBroadData<Boolean>()
        val callData = MeetCallManager.getInstance().mCallData
        val meetingId = callData.meetingId.ifBlank {
            callData.quickDetailMeetBean?.meetingId
                ?: GroupMeetSessionStore.get(groupIdNo.orEmpty())?.meetingId
        }
        val roomName = callData.roomName.ifBlank {
            callData.quickDetailMeetBean?.roomName
                ?: GroupMeetSessionStore.get(groupIdNo.orEmpty())?.roomName
        }
        val myId = com.cboda.baselibrary.utils.MMKVBroadUtils.getRealUserId()
        val session = if (!applicantUserId.isNullOrBlank() && applicantUserId != myId) {
            awaitMeetingSession { cb, fail ->
                ImMeetingHttpExample.requestSpeakKick(meetingId, roomName, applicantUserId, cb, fail)
            }
        } else {
            awaitMeetingSession { cb, fail ->
                ImMeetingHttpExample.requestSpeakLeave(meetingId, roomName, cb, fail)
            }
        }
        dismissLoadingWfc()
        if (session != null) {
            // speak/leave 直接返回观众 token，需重连
            if (!session.token.isNullOrBlank() && !session.url.isNullOrBlank()) {
                MeetCallManager.getInstance().reconnectLiveKit(
                    session.url,
                    session.token,
                    session.isCanPublish,
                )
            }
            liveData.postValue(true)
        } else {
            liveData.postValue(false)
        }
        return liveData
    }

    /** LiveKit：观众申请上麦。 */
    suspend fun goLiveKitSpeakApply(groupIdNo: String?): SingleLiveBroadData<Boolean> {
        val liveData = SingleLiveBroadData<Boolean>()
        val meetingId = resolveMeetingId(groupIdNo)
        val roomName = resolveRoomName(groupIdNo)
        val session = awaitMeetingSession { cb, fail ->
            ImMeetingHttpExample.requestSpeakApply(meetingId, roomName, cb, fail)
        }
        liveData.postValue(session != null)
        return liveData
    }

    /** LiveKit：主持人同意上麦（对方需自行 rejoin）。 */
    suspend fun goLiveKitSpeakApprove(groupIdNo: String?, targetUserId: String): SingleLiveBroadData<Boolean> {
        val liveData = SingleLiveBroadData<Boolean>()
        val meetingId = resolveMeetingId(groupIdNo)
        val roomName = resolveRoomName(groupIdNo)
        val session = awaitMeetingSession { cb, fail ->
            ImMeetingHttpExample.requestSpeakApprove(meetingId, roomName, targetUserId, cb, fail)
        }
        liveData.postValue(session != null)
        return liveData
    }

    private fun resolveMeetingId(groupIdNo: String?): String? {
        val callData = MeetCallManager.getInstance().mCallData
        return callData.meetingId.ifBlank {
            callData.quickDetailMeetBean?.meetingId
                ?: GroupMeetSessionStore.get(groupIdNo.orEmpty())?.meetingId
        }
    }

    private fun resolveRoomName(groupIdNo: String?): String? {
        val callData = MeetCallManager.getInstance().mCallData
        return callData.roomName.ifBlank {
            callData.quickDetailMeetBean?.roomName
                ?: GroupMeetSessionStore.get(groupIdNo.orEmpty())?.roomName
        }
    }

    private suspend fun awaitMeetingSession(
        block: (OnHttpImListener<MeetingSessionVO>, Runnable) -> Unit,
    ): MeetingSessionVO? = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { cont ->
            block(
                OnHttpImListener { session ->
                    if (cont.isActive) cont.resume(session)
                },
                Runnable {
                    if (cont.isActive) cont.resume(null)
                },
            )
        }
    }

    private suspend fun awaitBoolean(
        block: (OnHttpImListener<Boolean>, Runnable) -> Unit,
    ): Boolean = withContext(Dispatchers.IO) {
        suspendCancellableCoroutine { cont ->
            block(
                OnHttpImListener { ok ->
                    if (cont.isActive) cont.resume(ok == true)
                },
                Runnable {
                    if (cont.isActive) cont.resume(false)
                },
            )
        }
    }

    /**
     * 查询会议总人数
     */
    suspend fun queryMeetTotalPersonCount(groupId: String?): SingleLiveBroadData<String> {
//        showLoadingWfc()
        val safeCenterLiveData = SingleLiveBroadData<String>()
        val params = hashMapOf("groupIdNo" to groupId, "channelName" to groupId)
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.getEncrypted2<String>(UrlMeetConstants.QUICK_MEET_TOTAL_COUNT, params)
                    .onSuccess { userInfo ->
//                    dismissLoadingWfc()
//                        eLogD(APP_TAG, "queryMeetTotalPersonCount=>${userInfo}")

                        userInfo?.let {
                            safeCenterLiveData.postValue(it)
                        }

                    }.onFailure { code, message ->
//                    dismissLoadingWfc()
                        ToastBroadUtils.show(message)
                    }.onError {
//                    dismissLoadingWfc()
                    }

            } catch (e: Exception) {
                e.printStackTrace()
//                dismissLoadingWfc()
            }
        }
        return safeCenterLiveData
    }

    /**
     * 进入会议上报给服务器记录次数
     */
    suspend fun goAttendanceCheckIn(groupIdNo: String): SingleLiveBroadData<Boolean> {
        val mLiveData = SingleLiveBroadData<Boolean>()
        val params = hashMapOf("groupIdNo" to groupIdNo)
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2(
                    UrlMeetConstants.QUICK_ATTENDANCE_CHECK_IN,
                    Any::class.java,
                    params
                ).onSuccess { userInfo ->
                    mLiveData.postValue(true)

                }.onFailure { code, message ->
//                    ToastBroadUtils.show(message)
                }.onError {

                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        return mLiveData
    }


    fun checkPayMeet(groupIdNo: String? = null, callBack: (CheckMeetPayBean) -> Unit = {}): SingleLiveBroadData<CheckMeetPayBean> {
        if (HttpsBroadConfig.useLiveKitGroupMeeting) {
            // LiveKit 文档路径不走 quick/live 付费订单
            val empty = SingleLiveBroadData<CheckMeetPayBean>()
            empty.postValue(CheckMeetPayBean())
            return empty
        }
        val liveData = SingleLiveBroadData<CheckMeetPayBean>()
        val params = hashMapOf<String, Any?>()
        if (!TextUtils.isEmpty(groupIdNo)) {
            params["groupIdNo"] = groupIdNo
        }
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val result = NetworkBroadRepository.postEncrypted(
                        UrlMeetConstants.PENDING_MEETING,
                        CheckMeetPayBean::class.java, params
                    )
                    result.onSuccess { data ->
                        eLogD("checkPayMeet", toJson2(data))
                        data?.let {
                            liveData.postValue(it)
                            callBack.invoke(it)
                        } ?: eLogD("checkPayMeet", "onSuccess_data=null")
                    }.onFailure { _, message ->
                        eLogD("checkPayMeet", "onFailure_$message")
                    }.onError { error ->
                        eLogD("checkPayMeet", "onError_${error?.message}")
                    }
                } catch (e: Exception) {
                    e.toLog("checkPayMeet_onException")
                }
            }
        }
        return liveData
    }

}