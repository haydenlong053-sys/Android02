package com.cboda.chatui.quickmeet.vm

import com.cboda.baselibrary.base.BaseViewModel
import com.cboda.baselibrary.livedata.SingleLiveBroadData
import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.chatui.quickmeet.UrlMeetConstants
import com.cboda.chatui.quickmeet.bean.QuickCreateMeetBean
import com.cboda.chatui.quickmeet.bean.QuickMeetInfoBean
import com.cboda.chatui.quickmeet.dismissLoadingWfc
import com.cboda.chatui.quickmeet.showLoadingWfc

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


open class QuickMeetViewBroadModel: BaseViewModel() {

    /**
     * 直播信息查询
     */
    suspend fun goQuickMeetInfo(groupIdNo: String, liveStatus: String) : SingleLiveBroadData<QuickMeetInfoBean> {
        showLoadingWfc()
        val safeCenterLiveData = SingleLiveBroadData<QuickMeetInfoBean>()
        val params = hashMapOf("groupIdNo" to groupIdNo,"liveStatus" to liveStatus)
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
    suspend fun goCreateQuickMeet(groupIdNo: String, channelName: String,durationConfigId: Int,userTierConfigId: Int) : SingleLiveBroadData<QuickCreateMeetBean> {
        showLoadingWfc()
        val safeCenterLiveData = SingleLiveBroadData<QuickCreateMeetBean>()
        val params = hashMapOf("groupIdNo" to groupIdNo,"channelName" to channelName,"durationConfigId" to durationConfigId,"userTierConfigId" to userTierConfigId)
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2<QuickCreateMeetBean>(
                    UrlMeetConstants.QUICK_LIVE_CREATE,
                    QuickCreateMeetBean::class.java,
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

}