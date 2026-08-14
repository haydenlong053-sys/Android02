package com.freddy.chat

/** 群聊会话列表（合并原私群 + 公群） */
class ConversationGroupFragment : BaseConversationSessionFragment() {
    override val sessionCategory: ConversationSessionCategory = ConversationSessionCategory.GROUP
}
