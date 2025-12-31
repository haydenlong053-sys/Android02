package com.lxkj.aiim.repository

import android.util.Log
import cn.wildfirechat.remote.ChatManager
import com.cboda.baselibrary.network.repository.NetworkRepository
import com.cboda.baselibrary.network.result.NetworkResult
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.lxkj.aiim.lj.http.UrlConstants
import com.lxkj.aiim.model.SocialCircle
import com.lxkj.aiim.model.SocialContact
import com.lxkj.aiim.model.SocialFriendRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

class SocialRepository {


    suspend fun fetchFriendshipCharacter(): List<SocialCircle> {
        // 若服务端需要查询参数或表单体，请在此构造
        val body = HashMap<String, String>()
        return withContext(Dispatchers.IO) {
            var list: List<SocialCircle> = emptyList()
            val result = NetworkRepository.postListEncrypted<SocialCircle>(
                UrlConstants.FRIEND_CHARACTER,
                SocialCircle::class.java, body
            )
            result.onSuccess { circles ->
                list = circles ?: emptyList()

            }.onFailure { code, message ->
                list = emptyList()
            }
                .onError { e ->
                    list = emptyList()
                }

            list
        }
    }

    suspend fun fetchCircles(): List<SocialCircle> {
        delay(300) // 模拟网络延迟
        return listOf(
            SocialCircle(
                id = 1,
                characterTags = "公共圈",
                characterExplanation = "陌生人,临时联系",
                createTime = "2025-11-20 09:39:48",
                updateTime = "2025-11-20 09:39:50"
            ),
            SocialCircle(
                id = 2,
                characterTags = "亲密圈",
                characterExplanation = "好友,家人",
                createTime = "2025-11-20 09:39:48",
                updateTime = "2025-11-20 09:39:50"
            ),
            SocialCircle(
                id = 3,
                characterTags = "工作圈",
                characterExplanation = "同事,商务联系",
                createTime = "2025-11-20 09:39:48",
                updateTime = "2025-11-20 09:39:50"
            ),
            SocialCircle(
                id = 4,
                characterTags = "学习圈",
                characterExplanation = "同学,培训",
                createTime = "2025-11-20 09:39:48",
                updateTime = "2025-11-20 09:39:50"
            )
        )
    }

    suspend fun fetchFriendShipList(): List<SocialContact> {
        val body = HashMap<String, String>()
        return withContext(Dispatchers.IO) {
            var list: List<SocialContact> = emptyList()
            val result = NetworkRepository.postEncryptedList2<SocialContact>(
                UrlConstants.FRIEND_CONTACT_LIST,
                SocialContact::class.java, body
            )
            result.onSuccess { data ->
                list = data?.list ?: emptyList()

            }.onFailure { code, message ->
                list = emptyList()
            }
                .onError { e ->
                    list = emptyList()
                }

            list
        }
    }


    // 角标数量（示例：实际应调用后端或SDK）
    suspend fun fetchNewFriendCount(): Int {
        val params = HashMap<String, String>()
        return withContext(Dispatchers.IO) {
            var applyCount = 0
            val result = NetworkRepository.postEncrypted(
                UrlConstants.FRIEND_APPLY_COUNT,
                SocialFriendRequest::class.java, params
            )
            result.onSuccess {
                applyCount = it?.count ?: 0
            }.onFailure { code, message ->
                applyCount = 0
            }.onError {
                applyCount = 0
            }
            applyCount

        }
    }

    suspend fun fetchNewGroupInviteCount(): Int {
//        delay(100)
        return ChatManager.Instance().qunshenqing.toInt()
    }
}