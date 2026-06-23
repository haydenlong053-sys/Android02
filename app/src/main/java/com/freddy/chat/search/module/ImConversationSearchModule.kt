package com.freddy.chat.search.module

import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cboda.chatui.R
import com.cboda.chatui.search.SearchableBroadModule
import com.freddy.chat.search.ImSearchChatLaunchDelegate
import com.freddy.chat.search.viewholder.ImConversationSearchViewHolder
import com.freddy.im.bean.AppMessage
import com.freddy.im.bean.ImConversationSearchResult
import com.freddy.im.db.ImLocalStore

/**
 * 自研聊天记录搜索：Realm 文本消息（TEXT=100）content 模糊匹配，每会话展示最近一条命中。
 */
class ImConversationSearchModule :
    SearchableBroadModule<ImConversationSearchResult, ImConversationSearchViewHolder>() {

    override fun onCreateViewHolder(
        fragment: Fragment,
        parent: ViewGroup,
        type: Int
    ): ImConversationSearchViewHolder {
        val itemView = LayoutInflater.from(fragment.activity)
            .inflate(R.layout.search_item_broad_conversation2, parent, false)
        return ImConversationSearchViewHolder(fragment, itemView)
    }

    override fun onBind(
        fragment: Fragment,
        holder: ImConversationSearchViewHolder,
        item: ImConversationSearchResult
    ) {
        holder.onBind(keyword, item)
    }

    override fun onBind2(
        fragment: Fragment,
        holder: ImConversationSearchViewHolder,
        item: ImConversationSearchResult,
        currentPosition: Int,
        startPosition: Int,
        endPosition: Int
    ) {
        holder.onBind2(keyword, item, currentPosition, startPosition, endPosition)
    }

    override fun onClick(
        fragment: Fragment,
        holder: ImConversationSearchViewHolder,
        view: View,
        item: ImConversationSearchResult
    ) {
        val session = item.sessionItem ?: return
        val message: AppMessage? = item.latestMatchedMessage
        val focusMsgId = resolveFocusMsgId(message)
        if (!session.sessionId.isNullOrEmpty() && !focusMsgId.isNullOrEmpty()) {
            ImSearchChatLaunchDelegate.openChatAtMessage(fragment, session, focusMsgId)
        } else if (!session.sessionId.isNullOrEmpty()) {
            ImSearchChatLaunchDelegate.openGroupChat(fragment, session)
        }
    }

    override fun getViewType(item: ImConversationSearchResult): Int =
        R.layout.search_item_broad_conversation2

    override fun priority(): Int = 80

    override fun category(): String = "聊天记录"

    override fun category2(view: View): String = view.resources.getString(R.string.msg_chat_bibi)

    override fun search(keyword: String): List<ImConversationSearchResult> {
        return ImLocalStore.searchConversationsByMessageContent(keyword)
    }

    private fun resolveFocusMsgId(message: AppMessage?): String? {
        if (message == null) return null
        if (!TextUtils.isEmpty(message.msgId)) {
            return message.msgId
        }
        return message.clientMsgId
    }
}
