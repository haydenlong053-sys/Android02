package com.freddy.chat

import android.text.TextUtils
import com.freddy.im.MessageType
import com.freddy.im.http.model.im.SessionItem
import com.freddy.im.message.ImMessageManager

enum class ConversationSessionCategory {
    SINGLE,
    MY_GROUP,
    OTHER_GROUP;

    fun uiModeTag(): String = when (this) {
        SINGLE -> "single"
        MY_GROUP -> "myGroup"
        OTHER_GROUP -> "otherGroup"
    }

    companion object {
        fun filter(
            sessions: List<SessionItem>?,
            category: ConversationSessionCategory,
            currentUserId: String?,
        ): List<SessionItem> {
            if (sessions.isNullOrEmpty()) {
                return emptyList()
            }
            return sessions
                .filter {
                    !ImMessageManager.isFileAssistantSession(it.sessionId)
                        && matches(it, category, currentUserId)
                }
                .sortedByDescending { it.lastMessageTime }
        }

        fun matches(
            session: SessionItem,
            category: ConversationSessionCategory,
            currentUserId: String?,
        ): Boolean {
            return when (category) {
                SINGLE -> isSingleSession(session)
                MY_GROUP -> isGroupSession(session)
                    && !TextUtils.isEmpty(currentUserId)
                    && TextUtils.equals(currentUserId?.trim(), session.owerId?.trim())
                OTHER_GROUP -> isGroupSession(session)
                    && (TextUtils.isEmpty(currentUserId)
                    || !TextUtils.equals(currentUserId?.trim(), session.owerId?.trim()))
            }
        }

        /** 明确单聊，或会话类型缺失且 ID 为 uid!uid。 */
        fun isSingleSession(session: SessionItem): Boolean {
            if (MessageType.isSingleChat(session.conversation)) {
                return true
            }
            if (session.conversation > 0) {
                return false
            }
            return session.sessionId?.contains('!') == true
        }

        /**
         * 群会话：conversation=MULTI，或 getList 把类型写成 0 时用群主/人数/非单聊 ID 兜底，
         * 避免群从私群、公群同时消失。
         */
        fun isGroupSession(session: SessionItem): Boolean {
            if (MessageType.isGroupChat(session.conversation)) {
                return true
            }
            if (session.conversation > 0) {
                return false
            }
            if (!session.owerId.isNullOrBlank() || session.memCount > 0) {
                return true
            }
            val sessionId = session.sessionId
            return !sessionId.isNullOrBlank() && !sessionId.contains('!')
        }

        fun sumUnread(sessions: List<SessionItem>?): Int {
            if (sessions.isNullOrEmpty()) {
                return 0
            }
            var total = 0
            for (session in sessions) {
                // 免打扰会话不计入 Tab 未读角标（对齐野火 isSilent）
                if (session.isMuteNotify) {
                    continue
                }
                val unread = session.unreadCount
                if (unread > 0) {
                    total += unread
                }
            }
            return total
        }
    }
}
