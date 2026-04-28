package com.cboda.intelligent.viewmodel

import android.text.TextUtils
import com.blankj.utilcode.util.ThreadUtils
import com.cboda.baselibrary.base.BaseViewModel
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
import com.cboda.intelligent.model.SocialBroadCircle
import com.cboda.intelligent.model.SocialContact
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


    suspend fun fetchFriendShipList(): SingleLiveBroadData<Pair<List<GlobalFriendBroadBean>, List<FriendBean>>> {
        val liveData = SingleLiveBroadData<Pair<List<GlobalFriendBroadBean>, List<FriendBean>>>()
        val body = HashMap<String, String>()
        withContext(Dispatchers.IO) {
            val list = arrayListOf<GlobalFriendBroadBean>()
            val list2 = arrayListOf<FriendBean>()
            val result = NetworkBroadRepository.postEncryptedList2(
                UrlBroadBroadConstants.FRIEND_CONTACT_LIST,
                SocialContact::class.java, body
            )
            result.onSuccess { data ->
                data?.list?.forEach { item ->
                    val friendBean = FriendBean()
                    val glBean = GlobalFriendBroadBean()
                    glBean.userId = item.userId
                    glBean.registerDay = item.registerDay?.let { anyToInt(it) }
                    glBean.grade = item.grade?.let { anyToDouble(it) }
                    glBean.icon = item.icon
                    glBean.nickName = item.nickName
                    glBean.remarkName = item.remarkName
                    glBean.isVip = "1" == item.isvip
                    glBean.isNiceNum = !TextUtils.isEmpty(item.lianghao)

                    friendBean.id = item.userId
                    friendBean.grade = item.grade
                    friendBean.remarkName = item.remarkName
                    friendBean.isVip = "1" == item.isvip
                    friendBean.isNiceNum = !TextUtils.isEmpty(item.lianghao)
                    friendBean.registerDay = item.registerDay
                    friendBean.sex = item.sex
                    friendBean.nickName = item.nickName
                    friendBean.userImg = item.icon
                    list2.add(friendBean)
                    list.add(glBean)

                }
                liveData.postValue(Pair(list, list2))
            }.onFailure { _, _ ->
                liveData.postValue(Pair(list, list2))
            }.onError { _ ->
                liveData.postValue(Pair(list, list2))
            }
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