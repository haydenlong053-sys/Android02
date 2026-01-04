package com.lxkj.aiim.viewmodel

import androidx.lifecycle.ViewModel
import com.cboda.baselibrary.bean.GlobalFriendBroadBean
import com.cboda.baselibrary.bean.GlobalSmallGroupBroadBean
import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.lxkj.aiim.livedata.SingleLiveData
import com.lxkj.aiim.lj.http.UrlBroadConstants
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
                NetworkBroadRepository.postListEncrypted(
                    UrlBroadConstants.FRIEND_CHARACTER,
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


    suspend fun fetchFriendShipList(): SingleLiveData<List<GlobalFriendBroadBean>> {
        val liveData = SingleLiveData<List<GlobalFriendBroadBean>>()
        val body = HashMap<String, String>()
        withContext(Dispatchers.IO) {
            var list: List<GlobalFriendBroadBean> = emptyList()
            val result = NetworkBroadRepository.postEncryptedList2<GlobalFriendBroadBean>(
                UrlBroadConstants.FRIEND_CONTACT_LIST,
                GlobalFriendBroadBean::class.java, body
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

    suspend fun fetchGroupList(): SingleLiveData<List<GlobalSmallGroupBroadBean>> {
        val liveData = SingleLiveData<List<GlobalSmallGroupBroadBean>>()
        val body = mapOf(
            "pageNo" to "1",
            "pageSize" to "100"
        )
        withContext(Dispatchers.IO) {
            var list: List<GlobalSmallGroupBroadBean> = emptyList()
            val result = NetworkBroadRepository.postEncryptedList2(
                UrlBroadConstants.GROUP_LIST,
                GlobalSmallGroupBroadBean::class.java, body
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