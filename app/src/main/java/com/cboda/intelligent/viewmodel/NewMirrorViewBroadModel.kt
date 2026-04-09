package com.cboda.intelligent.viewmodel

import androidx.lifecycle.viewModelScope
import com.cboda.baselibrary.bean.GlobalFriendBroadBean
import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.intelligent.base.BaseViewBroadModel
import com.cboda.baselibrary.livedata.SingleLiveBroadData
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.toJson2
import com.cboda.intelligent.utils.UrlBroadBroadConstants
import com.cboda.intelligent.model.SignInfoViewBroadModel
import com.cboda.intelligent.model.UserInfoBroadModel
import com.cboda.intelligent.utils.dismissLoading
import com.cboda.intelligent.utils.showLoading
import com.cboda.intelligent.utils.showToastMain
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewMirrorViewBroadModel : BaseViewBroadModel() {
    /**
     * 开通信用分
     */
    suspend fun goOpenCredit(paypwd: String?) :SingleLiveBroadData<Boolean>{
        showLoading()
        val mLiveData = SingleLiveBroadData<Boolean>()
        val params = hashMapOf<String, Any?>("paypwd" to paypwd)

        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postEncrypted2(
                    UrlBroadBroadConstants.CREDIT_ACTIVATECREDIT,
                    Any::class.java,
                    params
                ).onSuccess { userInfo ->
                    dismissLoading()
                    mLiveData.postValue(true)

                }.onFailure { code, message->
                    dismissLoading()
                    showToastMain(message)

                }.onError {
                    dismissLoading()
                }

            } catch (e: Exception) {
                dismissLoading()
            }
        }
        return mLiveData
    }



    fun getSignInfo(): SingleLiveBroadData<Boolean> {
        val liveData = SingleLiveBroadData<Boolean>()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val result = NetworkBroadRepository.postEncrypted(
                        UrlBroadBroadConstants.SIGN_INFO,
                        SignInfoViewBroadModel::class.java, HashMap<String, Any>()
                    )
                    result.onSuccess { data ->
                        liveData.postValue(1 == data?.isSign)
                    }.onFailure { _, _ ->
                        liveData.postValue(false)
                    }.onError { _ ->
                        liveData.postValue(false)
                    }
                } catch (_: Exception) {
                    liveData.postValue(false)
                }
            }
        }
        return liveData
    }

    fun getUserInfo(): SingleLiveBroadData<UserInfoBroadModel?> {
        val liveData = SingleLiveBroadData<UserInfoBroadModel?>()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val result = NetworkBroadRepository.postEncrypted(
                        UrlBroadBroadConstants.GET_USER_INFO,
                        UserInfoBroadModel::class.java, HashMap<String, Any>()
                    )
                    result.onSuccess { data ->
                        data?.let {
                            val bean = GlobalFriendBroadBean()
                            bean.userId = data.id
                            bean.icon = data.icon
                            bean.nickName = data.name
                            bean.grade = data.grade
                            bean.registerDay = data.registerDayBean?.registDay
                            MMKVBroadUtils.saveUserData(bean)
                        }
                        eLogD("NewMirrorViewBroadModelClass","data:${toJson2(data)}")
                        liveData.postValue(data)
                    }.onFailure { _, _ ->
                        eLogD("NewMirrorViewBroadModelClass","getUserInfo onFailure")
                        liveData.postValue(null)
                    }.onError { _ ->
                        eLogD("NewMirrorViewBroadModelClass","getUserInfo onError")
                        liveData.postValue(null)
                    }
                } catch (_: Exception) {
                    eLogD("NewMirrorViewBroadModelClass","getUserInfo Exception")
                    liveData.postValue(null)
                }
            }
        }
        return liveData
    }
}