package com.freddy.chat.utils

import android.os.Handler
import android.os.Looper
import android.text.TextUtils
import com.cboda.baselibrary.bean.FriendBean
import com.cboda.baselibrary.utils.RealmUtil
import com.freddy.im.MessageType
import com.freddy.im.db.ImLocalStore
import com.freddy.im.http.ImMessageHttpExample
import com.freddy.im.http.model.im.BaseMsgResponse
import com.freddy.im.http.model.im.SessionItem
import com.freddy.im.http.model.im.UserInfoItem
import com.freddy.im.http.model.im.UserInfoListRes
import com.freddy.im.listener.OnHttpImListener
import java.util.Collections
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.atomic.AtomicInteger

/**
 * 单聊会话展示信息补全：野火用户表 → IM getUserList。
 */
object SessionDisplayEnricher {

    private val userCache = ConcurrentHashMap<String, UserInfoItem>()
    private val mainHandler = Handler(Looper.getMainLooper())

    fun needsEnrich(session: SessionItem?): Boolean {
        if (session == null || session.conversation != MessageType.SINGLE_CHAT.msgType) {
            return false
        }
        return TextUtils.isEmpty(session.sessionName) || TextUtils.isEmpty(session.icon)
    }

    fun enrichSingleChatSessions(
        sessions: List<SessionItem>?,
        currentUserId: String?,
        onComplete: (List<SessionItem>) -> Unit,
    ) {
        val source = sessions ?: emptyList()
        if (source.isEmpty()) {
            mainHandler.post { onComplete(emptyList()) }
            return
        }
        val pendingUserIds = LinkedHashSet<String>()
        for (session in source) {
            if (!needsEnrich(session)) {
                continue
            }
            resolvePeerUserId(session, currentUserId)?.let { pendingUserIds.add(it) }
        }
        if (pendingUserIds.isEmpty()) {
            mainHandler.post { onComplete(source) }
            return
        }

        val apiQueue = Collections.synchronizedList(mutableListOf<String>())
        val pending = AtomicInteger(pendingUserIds.size)
        for (userId in pendingUserIds) {
            if (userCache.containsKey(userId)) {
                if (pending.decrementAndGet() == 0) {
                    finish(source, apiQueue, currentUserId, onComplete)
                }
                continue
            }
            RealmUtil.findUserData(userId) { friend ->
                if (friend != null) {
                    cacheFriend(userId, friend)
                } else {
                    apiQueue.add(userId)
                }
                if (pending.decrementAndGet() == 0) {
                    finish(source, apiQueue, currentUserId, onComplete)
                }
            }
        }
    }

    private fun finish(
        source: List<SessionItem>,
        apiQueue: List<String>,
        currentUserId: String?,
        onComplete: (List<SessionItem>) -> Unit,
    ) {
        val distinctApiIds = apiQueue.distinct().filter { !userCache.containsKey(it) }
        if (distinctApiIds.isEmpty()) {
            publish(source, currentUserId, onComplete)
            return
        }
        fetchUsersFromApi(distinctApiIds, 0, source, currentUserId, onComplete)
    }

    private fun fetchUsersFromApi(
        userIds: List<String>,
        index: Int,
        source: List<SessionItem>,
        currentUserId: String?,
        onComplete: (List<SessionItem>) -> Unit,
    ) {
        if (index >= userIds.size) {
            publish(source, currentUserId, onComplete)
            return
        }
        val userId = userIds[index]
        ImMessageHttpExample.requestGetUserList(
            userId,
            1,
            10,
            object : OnHttpImListener<BaseMsgResponse<UserInfoListRes>> {
                override fun onHttpImSuccess(result: BaseMsgResponse<UserInfoListRes>) {
                    val list = result.content?.userList.orEmpty()
                    val matched = list.firstOrNull { TextUtils.equals(it.userId, userId) }
                        ?: list.firstOrNull()
                    matched?.let { userCache[it.userId] = it }
                    fetchUsersFromApi(userIds, index + 1, source, currentUserId, onComplete)
                }
            },
            { fetchUsersFromApi(userIds, index + 1, source, currentUserId, onComplete) },
        )
    }

    private fun publish(
        source: List<SessionItem>,
        currentUserId: String?,
        onComplete: (List<SessionItem>) -> Unit,
    ) {
        val updatedSessions = mutableListOf<SessionItem>()
        val displayList = source.map { session ->
            if (!needsEnrich(session)) {
                return@map session
            }
            val peerUserId = resolvePeerUserId(session, currentUserId) ?: return@map session
            val user = userCache[peerUserId] ?: return@map session
            val copy = cloneSession(session)
            applyUserInfo(copy, user)
            if (hasDisplayInfo(copy)) {
                updatedSessions.add(copy)
            }
            copy
        }
        if (updatedSessions.isNotEmpty()) {
            ImLocalStore.saveSessions(updatedSessions)
        }
        mainHandler.post { onComplete(displayList) }
    }

    fun resolvePeerUserId(session: SessionItem?, currentUserId: String?): String? {
        if (session == null) {
            return null
        }
        if (!TextUtils.isEmpty(session.oppositeUserId)) {
            return session.oppositeUserId.trim()
        }
        val sessionId = session.sessionId ?: return null
        val parts = sessionId.split("!")
        if (parts.size != 2) {
            return null
        }
        val myId = currentUserId?.trim().orEmpty()
        return when {
            parts[0] == myId -> parts[1]
            parts[1] == myId -> parts[0]
            else -> parts.maxByOrNull { it.length }
        }
    }

    private fun cacheFriend(userId: String, friend: FriendBean) {
        val item = UserInfoItem()
        item.userId = userId
        item.userName = friend.remarkName?.takeIf { it.isNotBlank() }
            ?: friend.nickName?.takeIf { it.isNotBlank() }
        item.icon = friend.userImg?.takeIf { it.isNotBlank() }
        userCache[userId] = item
    }

    private fun applyUserInfo(session: SessionItem, user: UserInfoItem) {
        if (TextUtils.isEmpty(session.oppositeUserId) && !TextUtils.isEmpty(user.userId)) {
            session.oppositeUserId = user.userId
        }
        if (TextUtils.isEmpty(session.sessionName) && !TextUtils.isEmpty(user.userName)) {
            session.sessionName = user.userName.trim()
        }
        if (TextUtils.isEmpty(session.username) && !TextUtils.isEmpty(user.userName)) {
            session.username = user.userName.trim()
        }
        val avatar = WeChatUiHelper.resolveAvatarUrl(user)
        if (TextUtils.isEmpty(session.icon) && !TextUtils.isEmpty(avatar)) {
            session.icon = avatar
        }
    }

    private fun hasDisplayInfo(session: SessionItem): Boolean {
        return !TextUtils.isEmpty(session.sessionName) || !TextUtils.isEmpty(session.icon)
    }

    private fun cloneSession(source: SessionItem): SessionItem {
        val copy = SessionItem()
        copy.sessionId = source.sessionId
        copy.sessionName = source.sessionName
        copy.username = source.username
        copy.conversation = source.conversation
        copy.createTime = source.createTime
        copy.icon = source.icon
        copy.memCount = source.memCount
        copy.unreadCount = source.unreadCount
        copy.unPullCount = source.unPullCount
        copy.internalFlag = source.internalFlag
        copy.topNum = source.topNum
        copy.isMuteNotify = source.isMuteNotify
        copy.readedPosition = source.readedPosition
        copy.selfReadPosition = source.selfReadPosition
        copy.busi = source.busi
        copy.tags = source.tags
        copy.permission = source.permission
        copy.oppositeUserId = source.oppositeUserId
        copy.owerId = source.owerId
        copy.oppositeCompanyId = source.oppositeCompanyId
        copy.extAttrs = source.extAttrs
        copy.userExtAttrs = source.userExtAttrs
        copy.lastMessageId = source.lastMessageId
        copy.lastMessageContent = source.lastMessageContent
        copy.lastContentType = source.lastContentType
        copy.lastCustomInfo = source.lastCustomInfo
        copy.lastMsgType = source.lastMsgType
        copy.lastMessageTime = source.lastMessageTime
        return copy
    }
}
