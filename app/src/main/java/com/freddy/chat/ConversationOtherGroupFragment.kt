package com.freddy.chat

/** 公群（非本人创建的群）会话列表 */
class ConversationOtherGroupFragment : BaseConversationSessionFragment() {
    override val sessionCategory: ConversationSessionCategory = ConversationSessionCategory.OTHER_GROUP
}
