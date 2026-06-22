package com.freddy.chat.search.module

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.cboda.chatui.R
import com.cboda.chatui.search.SearchableBroadModule
import com.freddy.chat.search.ImSearchChatLaunchDelegate
import com.freddy.chat.search.viewholder.ImGroupSearchViewHolder
import com.freddy.im.db.ImLocalStore
import com.freddy.im.http.model.im.SessionItem

/**
 * 自研群搜索：Realm 会话表，群聊且群名包含关键字。
 */
class ImGroupSearchModule : SearchableBroadModule<SessionItem, ImGroupSearchViewHolder>() {

    override fun onCreateViewHolder(
        fragment: Fragment,
        parent: ViewGroup,
        type: Int
    ): ImGroupSearchViewHolder {
        val itemView = LayoutInflater.from(fragment.activity)
            .inflate(R.layout.search_item_broad_group2, parent, false)
        return ImGroupSearchViewHolder(fragment, itemView)
    }

    override fun onBind(fragment: Fragment, holder: ImGroupSearchViewHolder, item: SessionItem) {
        holder.onBind(keyword, item)
    }

    override fun onBind2(
        fragment: Fragment,
        holder: ImGroupSearchViewHolder,
        item: SessionItem,
        currentPosition: Int,
        startPosition: Int,
        endPosition: Int
    ) {
        holder.onBind2(keyword, item, currentPosition, startPosition, endPosition)
    }

    override fun onClick(
        fragment: Fragment,
        holder: ImGroupSearchViewHolder,
        view: View,
        item: SessionItem
    ) {
        if (!item.sessionId.isNullOrEmpty()) {
            ImSearchChatLaunchDelegate.openGroupChat(fragment, item)
        }
    }

    override fun getViewType(item: SessionItem): Int = R.layout.search_item_broad_group2

    override fun priority(): Int = 90

    override fun category(): String = "群组"

    override fun category2(view: View): String = view.resources.getString(R.string.qunliao)

    override fun search(keyword: String): List<SessionItem> {
        return ImLocalStore.searchGroupSessionsByName(keyword)
    }
}
