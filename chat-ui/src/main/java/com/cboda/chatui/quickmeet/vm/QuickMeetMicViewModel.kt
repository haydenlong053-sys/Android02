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
import com.cboda.chatui.quickmeet.bean.SeatApplyListBean
import com.cboda.chatui.quickmeet.bean.SeatApplyItem
import com.cboda.chatui.quickmeet.dismissLoadingWfc
import com.cboda.chatui.quickmeet.showLoadingWfc

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

//极速直播--申请列表
class QuickMeetMicViewModel : BaseViewModel() {

    /**
     * 申请上麦人员列表
     */
    suspend fun getApplyMicList(groupIdNo: String?): SingleLiveBroadData<SeatApplyListBean> {
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

    /**
     * 同意上麦
     */
    suspend fun goAgreeUpMic(applicantUserId: String?,groupIdNo: String?) :SingleLiveBroadData<Boolean>{
        showLoadingWfc()
        val mLiveData = SingleLiveBroadData<Boolean>()
        val params = hashMapOf("applicantUserId" to applicantUserId,"groupIdNo" to groupIdNo,)
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2(
                    UrlMeetConstants.QUICK_AGREE_MIC,
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