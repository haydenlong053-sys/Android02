package com.cboda.intelligent.viewmodel

import android.text.TextUtils
import com.cboda.baselibrary.base.BaseViewModel
import com.cboda.baselibrary.bean.GlobalFriendBroadBean
import com.cboda.baselibrary.bean.GlobalSmallGroupBroadBean
import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.cboda.baselibrary.livedata.SingleLiveBroadData
import com.cboda.intelligent.utils.UrlBroadBroadConstants
import com.cboda.intelligent.model.SocialBroadCircle
import com.cboda.intelligent.model.SocialContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

open class FriendViewBroadModel : BaseViewModel() {

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


    suspend fun fetchFriendShipList(): SingleLiveBroadData<List<GlobalFriendBroadBean>> {
        val liveData = SingleLiveBroadData<List<GlobalFriendBroadBean>>()
        val body = HashMap<String, String>()
        withContext(Dispatchers.IO) {
            val list = arrayListOf<GlobalFriendBroadBean>()
            val result = NetworkBroadRepository.postEncryptedList2<SocialContact>(
                UrlBroadBroadConstants.FRIEND_CONTACT_LIST,
                SocialContact::class.java, body
            )
            result.onSuccess { data ->
                data?.list?.forEach { item ->
                    val glBean = GlobalFriendBroadBean()
                    glBean.userId = item.userId
                    glBean.registerDay = item.registerDay
                    glBean.grade = item.grade
                    glBean.icon = item.icon
                    glBean.nickName = item.nickName
                    glBean.remarkName = item.remarkName
                    glBean.isVip = "1" == item.isvip
                    glBean.isNiceNum = !TextUtils.isEmpty(item.lianghao)
                    list.add(glBean)
                }
                liveData.postValue(list)
            }.onFailure { code, message ->
                liveData.postValue(list)
            }.onError { e ->
                liveData.postValue(list)
            }
            list
        }
        return liveData
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