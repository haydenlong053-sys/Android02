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
            val conversation = session.conversation
            return when (category) {
                SINGLE -> conversation == MessageType.SINGLE_CHAT.msgType
                MY_GROUP -> conversation == MessageType.GROUP_CHAT.msgType
                    && !TextUtils.isEmpty(currentUserId)
                    && TextUtils.equals(currentUserId, session.owerId)
                OTHER_GROUP -> conversation == MessageType.GROUP_CHAT.msgType
                    && (TextUtils.isEmpty(currentUserId)
                    || !TextUtils.equals(currentUserId, session.owerId))
            }
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
