package com.freddy.chat

/** 私群（本人创建的群）会话列表 */
class ConversationGroupFragment : BaseConversationSessionFragment() {
    override val sessionCategory: ConversationSessionCategory = ConversationSessionCategory.MY_GROUP
}
