package com.cboda.intelligent.viewmodel

import android.text.TextUtils
import com.cboda.baselibrary.bean.FriendBean
import com.cboda.baselibrary.bean.GlobalFriendBroadBean
import com.cboda.baselibrary.bean.GlobalSmallGroupBroadBean
import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.cboda.baselibrary.livedata.SingleLiveBroadData
import com.cboda.baselibrary.utils.anyToDouble
import com.cboda.baselibrary.utils.anyToInt
import com.cboda.intelligent.utils.UrlBroadBroadConstants
import com.cboda.baselibrary.bean.SocialBroadCircle
import com.cboda.baselibrary.bean.SocialContact
import com.cboda.baselibrary.utils.RealmUtil
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

open class FriendViewBroadModel : LoginViewBroadModel() {

    /**
     * 安全中心
     */
    suspend fun getFriendshipCharacter(): SingleLiveBroadData<List<SocialBroadCircle>> {
        val liveData = SingleLiveBroadData<List<SocialBroadCircle>>()
        withContext(Dispatchers.IO) {
            try {
                NetworkBroadRepository.postListEncrypted(
                    UrlBroadBroadConstants.FRIEND_CHARACTER,
                    SocialBroadCircle::class.java,
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


    suspend fun fetchFriendShipList() {
        val body = HashMap<String, String>()
        withContext(Dispatchers.IO) {
            val result = NetworkBroadRepository.postEncryptedList2(
                UrlBroadBroadConstants.FRIEND_CONTACT_LIST,
                SocialContact::class.java, body
            )
            result.onSuccess { data ->
                RealmUtil.addUserData(data?.list)
            }.onFailure { _, _ ->

            }.onError { _ ->
            }
        }
    }

    suspend fun fetchGroupList(): SingleLiveBroadData<List<GlobalSmallGroupBroadBean>> {
        val liveData = SingleLiveBroadData<List<GlobalSmallGroupBroadBean>>()
        val body = mapOf(
            "pageNo" to "1",
            "pageSize" to "100"
        )
        withContext(Dispatchers.IO) {
            var list: List<GlobalSmallGroupBroadBean> = emptyList()
            val result = NetworkBroadRepository.postEncryptedList2(
                UrlBroadBroadConstants.GROUP_LIST,
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