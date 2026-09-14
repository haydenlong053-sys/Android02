package com.cboda.chatui.quickmeet.vm

import com.cboda.baselibrary.base.BaseViewModel
import com.cboda.baselibrary.livedata.SingleLiveBroadData
import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.baselibrary.utils.HttpsBroadConfig
import com.cboda.chatui.quickmeet.MeetCallManager
import com.cboda.chatui.quickmeet.UrlMeetConstants
import com.cboda.chatui.quickmeet.bean.SeatApplyItem
import com.cboda.chatui.quickmeet.bean.SeatApplyListBean
import com.cboda.chatui.quickmeet.dismissLoadingWfc
import com.cboda.chatui.quickmeet.showLoadingWfc
import com.freddy.im.http.ImMeetingHttpExample
import com.freddy.im.listener.OnHttpImListener
import com.freddy.im.meeting.GroupMeetSessionStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume

//极速直播--申请列表
class QuickMeetMicViewModel : BaseViewModel() {

    /**
     * 申请上麦人员列表
     */
    suspend fun getApplyMicList(groupIdNo: String?): SingleLiveBroadData<SeatApplyListBean> {
        if (HttpsBroadConfig.useLiveKitGroupMeeting || MeetCallManager.getInstance().mCallData.useLiveKit) {
            return getLiveKitApplyMicList(groupIdNo)
        }
        showLoadingWfc()
        val safeCenterLiveData = SingleLiveBroadData<SeatApplyListBean>()
        val params = hashMapOf("groupIdNo" to groupIdNo)
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2<SeatApplyListBean>(
                    UrlMeetConstants.QUICK_APPLY_MIC_LIST,
                    SeatApplyListBean::class.java,
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

    private suspend fun getLiveKitApplyMicList(groupIdNo: String?): SingleLiveBroadData<SeatApplyListBean> {
        showLoadingWfc()
        val liveData = SingleLiveBroadData<SeatApplyListBean>()
        val meetingId = resolveMeetingId(groupIdNo)
        val roomName = resolveRoomName(groupIdNo)
        val session = withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { cont ->
                ImMeetingHttpExample.requestInfo(
                    meetingId,
                    roomName,
                    OnHttpImListener { s -> if (cont.isActive) cont.resume(s) },
                    Runnable { if (cont.isActive) cont.resume(null) },
                )
            }
        }
        dismissLoadingWfc()
        // pending 为申请上麦的 userId 字符串列表
        val bean = SeatApplyListBean(
            applyList = session?.pending?.map { userId ->
                SeatApplyItem(
                    nickname = "",
                    role = "",
                    icon = "",
                    idNo = "",
                    userId = userId.orEmpty(),
                )
            },
            groupIdNo = groupIdNo,
        )
        liveData.postValue(bean)
        return liveData
    }

    /**
     * 同意上麦
     */
    suspend fun goAgreeUpMic(applicantUserId: String?, groupIdNo: String?): SingleLiveBroadData<Boolean> {
        if (HttpsBroadConfig.useLiveKitGroupMeeting || MeetCallManager.getInstance().mCallData.useLiveKit) {
            return goLiveKitAgreeUpMic(applicantUserId, groupIdNo)
        }
        showLoadingWfc()
        val mLiveData = SingleLiveBroadData<Boolean>()
        val params = hashMapOf("applicantUserId" to applicantUserId, "groupIdNo" to groupIdNo)
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2(
                    UrlMeetConstants.QUICK_AGREE_MIC,
                    Any::class.java,
                    params
                ).onSuccess {
                    dismissLoadingWfc()
                    mLiveData.postValue(true)

                }.onFailure { _, message ->
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

    private suspend fun goLiveKitAgreeUpMic(applicantUserId: String?, groupIdNo: String?): SingleLiveBroadData<Boolean> {
        showLoadingWfc()
        val liveData = SingleLiveBroadData<Boolean>()
        if (applicantUserId.isNullOrBlank()) {
            dismissLoadingWfc()
            liveData.postValue(false)
            return liveData
        }
        val meetingId = resolveMeetingId(groupIdNo)
        val roomName = resolveRoomName(groupIdNo)
        val ok = withContext(Dispatchers.IO) {
            suspendCancellableCoroutine { cont ->
                ImMeetingHttpExample.requestSpeakApprove(
                    meetingId,
                    roomName,
                    applicantUserId,
                    OnHttpImListener { if (cont.isActive) cont.resume(true) },
                    Runnable { if (cont.isActive) cont.resume(false) },
                )
            }
        }
        dismissLoadingWfc()
        liveData.postValue(ok)
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
}
