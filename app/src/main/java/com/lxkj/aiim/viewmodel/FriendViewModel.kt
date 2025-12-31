package com.lxkj.aiim.viewmodel

import androidx.lifecycle.ViewModel
import com.cboda.baselibrary.bean.GlobalFriendBean
import com.cboda.baselibrary.bean.GlobalSmallGroupBean
import com.cboda.baselibrary.network.repository.NetworkRepository
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.lxkj.aiim.livedata.SingleLiveData
import com.lxkj.aiim.lj.http.UrlConstants
import com.lxkj.aiim.model.SocialCircle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

open class FriendViewModel : ViewModel() {

    /**
     * 安全中心
     */
    suspend fun getFriendshipCharacter(): SingleLiveData<List<SocialCircle>> {
        val liveData = SingleLiveData<List<SocialCircle>>()
        withContext(Dispatchers.IO) {
            try {
                NetworkRepository.postListEncrypted(
                    UrlConstants.FRIEND_CHARACTER,
                    SocialCircle::class.java,
                    HashMap<String, String>()
                ).onSuccess { circles ->
                    liveData.postValue(circles ?: emptyList())
                }.onFailure { _, _ ->
                }.onError {
                }

            } catch (e: Exception) {
            }
        }
        return liveData

    }


    suspend fun fetchFriendShipList(): SingleLiveData<List<GlobalFriendBean>> {
        val liveData = SingleLiveData<List<GlobalFriendBean>>()
        val body = HashMap<String, String>()
        withContext(Dispatchers.IO) {
            var list: List<GlobalFriendBean> = emptyList()
            val result = NetworkRepository.postEncryptedList2<GlobalFriendBean>(
                UrlConstants.FRIEND_CONTACT_LIST,
                GlobalFriendBean::class.java, body
            )
            result.onSuccess { data ->
                list = data?.list ?: emptyList()
                liveData.postValue(list)

            }.onFailure { code, message ->
                list = emptyList()
                liveData.postValue(list)
            }.onError { e ->
                list = emptyList()
                liveData.postValue(list)
            }
            list
        }
        return liveData
    }

    suspend fun fetchGroupList(): SingleLiveData<List<GlobalSmallGroupBean>> {
        val liveData = SingleLiveData<List<GlobalSmallGroupBean>>()
        val body = mapOf(
            "pageNo" to "1",
            "pageSize" to "100"
        )
        withContext(Dispatchers.IO) {
            var list: List<GlobalSmallGroupBean> = emptyList()
            val result = NetworkRepository.postEncryptedList2(
                UrlConstants.GROUP_LIST,
                GlobalSmallGroupBean::class.java, body
            )
            result.onSuccess { data ->
                list = data?.list ?: emptyList()
                liveData.postValue(list)

            }.onFailure { code, message ->
                list = emptyList()
                liveData.postValue(list)
            }.onError { e ->
                list = emptyList()
                liveData.postValue(list)
            }
            list
        }
        return liveData
    }
}