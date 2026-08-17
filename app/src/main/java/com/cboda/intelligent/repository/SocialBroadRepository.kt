package com.cboda.intelligent.repository

import com.cboda.baselibrary.bean.SocialBroadCircle
import com.cboda.baselibrary.bean.SocialContact
import com.cboda.baselibrary.bean.SocialFriendRequest
import com.cboda.baselibrary.event.UnreadCountBroadEvent
import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.RealmUtil
import com.cboda.intelligent.utils.UrlBroadBroadConstants
import com.freddy.im.http.ImGroupHttpExample
import com.freddy.im.http.model.im.GroupApplyInfoRes
import com.freddy.im.listener.OnHttpImListener
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import kotlin.coroutines.resume

class SocialBroadRepository {


    suspend fun fetchFriendshipCharacter(): List<SocialBroadCircle> {
        // 若服务端需要查询参数或表单体，请在此构造
        val body = HashMap<String, String>()
        return withContext(Dispatchers.IO) {
            var list: List<SocialBroadCircle> = emptyList()
            val result = NetworkBroadRepository.postListEncrypted<SocialBroadCircle>(
                UrlBroadBroadConstants.FRIEND_CHARACTER,
                SocialBroadCircle::class.java, body
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

    suspend fun fetchCircles(): List<SocialBroadCircle> {
        delay(300) // 模拟网络延迟
        return listOf(
            SocialBroadCircle(
                id = 1,
                characterTags = "公共圈",
                characterExplanation = "陌生人,临时联系",
                createTime = "2025-11-20 09:39:48",
                updateTime = "2025-11-20 09:39:50"
            ),
            SocialBroadCircle(
                id = 2,
                characterTags = "亲密圈",
                characterExplanation = "好友,家人",
                createTime = "2025-11-20 09:39:48",
                updateTime = "2025-11-20 09:39:50"
            ),
            SocialBroadCircle(
                id = 3,
                characterTags = "工作圈",
                characterExplanation = "同事,商务联系",
                createTime = "2025-11-20 09:39:48",
                updateTime = "2025-11-20 09:39:50"
            ),
            SocialBroadCircle(
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
            val result = NetworkBroadRepository.postEncryptedList2<SocialContact>(
                UrlBroadBroadConstants.FRIEND_CONTACT_LIST,
                SocialContact::class.java, body
            )
            result.onSuccess { data ->
                RealmUtil.addUserData(data?.list)
                //eLogD(APP_TAG,"好友总数："+data?.totalCount)
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
            val result = NetworkBroadRepository.postEncrypted(
                UrlBroadBroadConstants.FRIEND_APPLY_COUNT,
                SocialFriendRequest::class.java, params
            )
            result.onSuccess {
                applyCount = it?.count ?: 0
                MMKVBroadUtils.saveNewFriendApplyCount(applyCount.toLong())
                notifyContactBadgeChanged()
            }.onFailure { code, message ->
                applyCount = 0
            }.onError {
                applyCount = 0
            }
            applyCount

        }
    }

    /**
     * 新群邀请角标：对齐好友申请，以 [ImGroupHttpExample.requestSearchGroupAsst] 待审数为准。
     * （im/user/getUser 通常不带 gros，不能再当唯一数据源。）
     */
    suspend fun fetchNewGroupInviteCount(): Int = withContext(Dispatchers.IO) {
        val count = suspendCancellableCoroutine { cont ->
            ImGroupHttpExample.requestSearchGroupAsst(
                object : OnHttpImListener<List<GroupApplyInfoRes>> {
                    override fun onHttpImSuccess(result: List<GroupApplyInfoRes>) {
                        var pending = 0
                        for (item in result) {
                            if (isPendingGroupInvite(item)) {
                                pending++
                            }
                        }
                        if (cont.isActive) {
                            cont.resume(pending)
                        }
                    }
                },
                {
                    if (cont.isActive) {
                        cont.resume(MMKVBroadUtils.getNewGroupInviteCount().toInt())
                    }
                },
            )
        }
        MMKVBroadUtils.saveNewGroupInviteCount(count.toLong())
        notifyContactBadgeChanged()
        count
    }

    private fun isPendingGroupInvite(item: GroupApplyInfoRes): Boolean {
        // status：0 待审；type：approve=待我审批（新群邀请入口）
        val status = item.status?.trim().orEmpty()
        if (status != "0") {
            return false
        }
        val type = item.type?.trim().orEmpty()
        return type.isEmpty() || type.equals("approve", ignoreCase = true)
    }

    private fun notifyContactBadgeChanged() {
        EventBus.getDefault().post(
            UnreadCountBroadEvent(
                null, null, null, null, null, null,
                MMKVBroadUtils.getContactBadgeTotal().toString(),
            ),
        )
    }
}
