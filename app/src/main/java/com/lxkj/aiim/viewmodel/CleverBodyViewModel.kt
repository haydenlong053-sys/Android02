package com.lxkj.aiim.viewmodel

import androidx.lifecycle.viewModelScope
import com.base.commonlibrary.bean.GlobalFriendBean
import com.base.commonlibrary.network.repository.NetworkRepository
import com.base.commonlibrary.network.result.onError
import com.base.commonlibrary.network.result.onFailure
import com.base.commonlibrary.network.result.onSuccess
import com.base.commonlibrary.utils.MMKVUtils
import com.lxkj.aiim.base.BaseViewModel
import com.lxkj.aiim.livedata.SingleLiveData
import com.lxkj.aiim.lj.http.UrlConstants
import com.lxkj.aiim.model.SignInfoViewModel
import com.lxkj.aiim.model.UserInfoModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CleverBodyViewModel : BaseViewModel() {

    fun getSignInfo(): SingleLiveData<Boolean> {
        val liveData = SingleLiveData<Boolean>()
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    val result = NetworkRepository.postEncrypted(
                        UrlConstants.SIGN_INFO,
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
                    val result = NetworkRepository.postEncrypted(
                        UrlConstants.GET_USER_INFO,
                        UserInfoModel::class.java, HashMap<String, Any>()
                    )
                    result.onSuccess { data ->
                        data?.let {
                            val bean = GlobalFriendBean()
                            bean.userId = data.id
                            bean.icon = data.icon
                            bean.nickName = data.name
                            bean.grade = data.grade
                            bean.registerDay = data.registerDayBean?.registDay
                            MMKVUtils.saveUserData(bean)
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