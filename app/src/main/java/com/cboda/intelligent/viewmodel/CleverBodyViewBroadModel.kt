package com.cboda.intelligent.viewmodel

import androidx.lifecycle.viewModelScope
import com.cboda.baselibrary.livedata.SingleLiveBroadData
import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.repository.NetworkListBroadResult
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.cboda.intelligent.base.BaseViewBroadModel
import com.cboda.intelligent.bean.SquarePostBean
import com.cboda.intelligent.configuration.CommonBroadConfig
import com.cboda.intelligent.model.SignInfoViewBroadModel
import com.cboda.intelligent.utils.UrlBroadBroadConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CleverBodyViewBroadModel : BaseViewBroadModel() {

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

//    fun getUserInfo(): SingleLiveBroadData<UserInfoBroadModel?> {
//        val liveData = SingleLiveBroadData<UserInfoBroadModel?>()
//        viewModelScope.launch {
//            withContext(Dispatchers.IO) {
//                try {
//                    val result = NetworkBroadRepository.postEncrypted(
//                        UrlBroadBroadConstants.GET_USER_INFO,
//                        UserInfoBroadModel::class.java, HashMap<String, Any>()
//                    )
//                    result.onSuccess { data ->
//                        data?.let {
//                            val bean = GlobalFriendBroadBean()
//                            bean.userId = data.id
//                            bean.icon = data.icon
//                            bean.nickName = data.name
//                            bean.grade = data.grade
//                            bean.registerDay = data.registerDayBean?.registDay
//                            bean.isVip = data.isvip == "1"
//                            bean.isNiceNum = !TextUtils.isEmpty(data.lianghao)
//                            MMKVBroadUtils.saveUserData(bean)
//                        }
//                        liveData.postValue(data)
//                    }.onFailure { _, _ ->
//                        liveData.postValue(null)
//                    }.onError { _ ->
//                        liveData.postValue(null)
//                    }
//                } catch (_: Exception) {
//                    liveData.postValue(null)
//                }
//            }
//        }
//        return liveData
//    }

    /**
     * 广场帖子列表（分页）
     * tabType: 0=世界 1=热门 2=我的关注
     */
    fun getSquarePostList(
        tabType: Int,
        pageNo: Int,
        pageSize: Int = CommonBroadConfig.pageSize_20,
    ): SingleLiveBroadData<NetworkListBroadResult<SquarePostBean>> {
        val liveData = SingleLiveBroadData<NetworkListBroadResult<SquarePostBean>>()
        val params = hashMapOf<String, Any>(
            "listType" to tabType.toString(),
            "pageNo" to pageNo.toString(),
            "pageSize" to pageSize.toString(),
        )
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    NetworkBroadRepository.postEncryptedList2(
                        UrlBroadBroadConstants.SQUARE_POST_LIST,
                        SquarePostBean::class.java,
                        params,
                    ).onSuccess { result ->
                        liveData.postValue(
                            result ?: NetworkListBroadResult(emptyList(), 0, 1),
                        )
                    }.onFailure { _, _ ->
//                        liveData.postValue(buildMockSquarePostList(tabType, pageNo, pageSize))
                    }.onError {
//                        liveData.postValue(buildMockSquarePostList(tabType, pageNo, pageSize))
                    }
                } catch (_: Exception) {
//                    liveData.postValue(buildMockSquarePostList(tabType, pageNo, pageSize))
                }
            }
        }
        return liveData
    }

    /**
     * 朋友圈列表（分页）
     * tabType: 0=世界 1=热门 2=我的关
     */
    fun getFriendPostList(
        pageNo: Int,
        pageSize: Int = CommonBroadConfig.pageSize_20,
    ): SingleLiveBroadData<NetworkListBroadResult<SquarePostBean>> {
        val liveData = SingleLiveBroadData<NetworkListBroadResult<SquarePostBean>>()
        val params = hashMapOf<String, Any>(
            "pageNo" to pageNo.toString(),
            "pageSize" to pageSize.toString(),
        )
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                try {
                    NetworkBroadRepository.postEncryptedList2(
                        UrlBroadBroadConstants.FRIEND_POST_LIST,
                        SquarePostBean::class.java,
                        params,
                    ).onSuccess { result ->
                        liveData.postValue(
                            result ?: NetworkListBroadResult(emptyList(), 0, 1),
                        )
                    }.onFailure { _, _ ->
//                        liveData.postValue(buildMockSquarePostList(tabType, pageNo, pageSize))
                    }.onError {
//                        liveData.postValue(buildMockSquarePostList(tabType, pageNo, pageSize))
                    }
                } catch (_: Exception) {
//                    liveData.postValue(buildMockSquarePostList(tabType, pageNo, pageSize))
                }
            }
        }
        return liveData
    }
}