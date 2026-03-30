package com.cboda.chatui.quickmeet.vm

import com.cboda.baselibrary.base.BaseViewModel
import com.cboda.baselibrary.bean.GlobalChatGroupBean
import com.cboda.baselibrary.livedata.SingleLiveBroadData
import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.chatui.quickmeet.UrlMeetConstants
import com.cboda.chatui.quickmeet.bean.QuickDetailMeetBean
import com.cboda.chatui.quickmeet.bean.QuickMeetInfoBean
import com.cboda.chatui.quickmeet.bean.SeatMicListBean
import com.cboda.chatui.quickmeet.dismissLoadingWfc
import com.cboda.chatui.quickmeet.showLoadingWfc

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//极速直播
open class QuickMeetViewBroadModel : BaseViewModel() {

    /**
     * 直播信息查询
     */
    suspend fun goQuickMeetInfo(groupIdNo: String, liveStatus: String): SingleLiveBroadData<QuickMeetInfoBean> {
        showLoadingWfc()
        val safeCenterLiveData = SingleLiveBroadData<QuickMeetInfoBean>()
        val params = hashMapOf("groupIdNo" to groupIdNo, "liveStatus" to liveStatus)
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2<QuickMeetInfoBean>(
                    UrlMeetConstants.QUICK_LIVE_INFO,
                    QuickMeetInfoBean::class.java,
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
     * 创建直播
     */
    suspend fun goCreateQuickMeet(groupIdNo: String, channelName: String, durationConfigId: Int, userTierConfigId: Int): SingleLiveBroadData<QuickDetailMeetBean> {
        showLoadingWfc()
        val safeCenterLiveData = SingleLiveBroadData<QuickDetailMeetBean>()
        val params = hashMapOf("groupIdNo" to groupIdNo, "channelId" to groupIdNo, "channelName" to channelName, "durationConfigId" to durationConfigId, "userTierConfigId" to userTierConfigId)
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
     * 销毁直播频道
     */
    suspend fun goDissolveQuickMeet(groupIdNo: String): SingleLiveBroadData<Boolean> {
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

    /**
     * 延长直播时间
     */
    suspend fun goExtendQuickMeet(groupIdNo: String, durationConfigId: Int = 1): SingleLiveBroadData<QuickDetailMeetBean> {
        showLoadingWfc()
        val safeCenterLiveData = SingleLiveBroadData<QuickDetailMeetBean>()
        val params = hashMapOf("groupIdNo" to groupIdNo, "durationConfigId" to durationConfigId)
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
        showLoadingWfc()
        val safeCenterLiveData = SingleLiveBroadData<SeatMicListBean>()
        val params = hashMapOf("groupIdNo" to groupIdNo,)
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


    /**
     * 下麦
     */
    suspend fun goNetDownMic(applicantUserId: String?,groupIdNo: String?) :SingleLiveBroadData<Boolean>{
        showLoadingWfc()
        val mLiveData = SingleLiveBroadData<Boolean>()
        val params = hashMapOf("applicantUserId" to applicantUserId,"groupIdNo" to groupIdNo,)
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2(
                    UrlMeetConstants.QUICK_DOWN_MIC,
                    Any::class.java,
                    params
                ).onSuccess { userInfo ->
                    dismissLoadingWfc()
                    mLiveData.postValue(true)

                }.onFailure { code, message->
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


}