package com.freddy.chat

import com.freddy.im.MessageType
import com.freddy.im.http.model.im.SessionItem
import com.freddy.im.message.ImMessageManager

enum class ConversationSessionCategory {
    /** 单聊 / 对话 */
    SINGLE,
    /** 群聊（含原私群、公群） */
    GROUP;

    fun uiModeTag(): String = when (this) {
        SINGLE -> "single"
        GROUP -> "group"
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
            @Suppress("UNUSED_PARAMETER") currentUserId: String?,
        ): Boolean {
            val conversation = session.conversation
            return when (category) {
                // 「对话」含单聊 + 我的收藏(conversation=6)
                SINGLE -> conversation == MessageType.SINGLE_CHAT.msgType
                    || MessageType.isMyCollect(conversation)
                GROUP -> conversation == MessageType.GROUP_CHAT.msgType
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
