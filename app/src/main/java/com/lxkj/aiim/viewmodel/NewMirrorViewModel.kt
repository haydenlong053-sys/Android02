package com.lxkj.aiim.viewmodel

import androidx.lifecycle.viewModelScope
import com.cboda.baselibrary.bean.GlobalFriendBroadBean
import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.lxkj.aiim.base.BaseViewModel
import com.lxkj.aiim.livedata.SingleLiveData
import com.lxkj.aiim.lj.http.UrlBroadConstants
import com.lxkj.aiim.model.SignInfoViewModel
import com.lxkj.aiim.model.UserInfoModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NewMirrorViewModel : BaseViewModel() {

    fun getSignInfo(): SingleLiveData<Boolean> {
        val liveData = SingleLiveData<Boolean>()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val result = NetworkBroadRepository.postEncrypted(
                        UrlBroadConstants.SIGN_INFO,
                        SignInfoViewModel::class.java, HashMap<String, Any>()
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

    fun getUserInfo(): SingleLiveData<UserInfoModel?> {
        val liveData = SingleLiveData<UserInfoModel?>()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val result = NetworkBroadRepository.postEncrypted(
                        UrlBroadConstants.GET_USER_INFO,
                        UserInfoModel::class.java, HashMap<String, Any>()
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
                        liveData.postValue(data)
                    }.onFailure { _, _ ->
                        liveData.postValue(null)
                    }.onError { _ ->
                        liveData.postValue(null)
                    }
                } catch (_: Exception) {
                    liveData.postValue(null)
                }
            }
        }
        return liveData
    }
}