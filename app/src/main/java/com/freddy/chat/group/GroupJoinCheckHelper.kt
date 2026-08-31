package com.freddy.chat.group

import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.cboda.intelligent.bean.CheckGroupJoiBean
import com.cboda.intelligent.utils.UrlBroadBroadConstants
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 邀请好友进群前置校验：通过 [api/checkGroupJoin] 判断好友是否在群黑名单。
 */
object GroupJoinCheckHelper {

    /**
     * @param userIds 待校验好友 userId
     * @return 在群黑名单中的 userId 集合
     */
    suspend fun fetchBlacklistedUserIds(
        groupId: String,
        userIds: Collection<String>,
    ): Set<String> {
        if (groupId.isEmpty() || userIds.isEmpty()) return emptySet()
        val distinct = userIds.map { it.trim() }.filter { it.isNotEmpty() }.distinct()
        if (distinct.isEmpty()) return emptySet()
        return withContext(Dispatchers.IO) {
            val blackIds = linkedSetOf<String>()
                val params = hashMapOf<String, Any?>(
                    "id" to groupId,
                    "uids" to userIds.joinToString("|"),
                )
                NetworkBroadRepository.postEncryptedFlatRaw2(
                    UrlBroadBroadConstants.CHECK_GROUP_JOIN,
                    CheckGroupJoiBean::class.java,
                    params,
                ).onSuccess { data ->
                    if (data != null) {
                        blackIds.addAll(data.collectBlacklistedUserIds(userIds))
                    }
                }.onFailure { _, _ ->
                    // ignore
                }.onError {
                    // ignore
                }

            blackIds
        }
    }
}
