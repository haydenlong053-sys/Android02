package com.freddy.chat.search

import androidx.fragment.app.Fragment
import com.cboda.chatui.search.SearchBroadActivity2
import com.cboda.chatui.search.SearchableBroadModule
import com.cboda.intelligent.utils.JumpBroadUtils
import com.freddy.chat.search.module.ImConversationSearchModule
import com.freddy.chat.search.module.ImGroupSearchModule

/**
 * 自研 IM 聊天搜索入口：联系人 GET im/user/search/{keyword}（非加密）+ Realm 本地群名/聊天记录。
 */
class ImSearchPortalBroadActivity : SearchBroadActivity2() {

    override fun initSearchModule(modules: MutableList<SearchableBroadModule<*, *>>) {
        modules.add(ImGroupSearchModule())
        modules.add(ImConversationSearchModule())
    }

    override fun onSearchFriendItemClick(
        fragment: Fragment,
        userId: String,
        displayName: String?
    ): Boolean {
        // 与 SearchUserBroadActivity 一致：搜索结果进好友主页
        JumpBroadUtils.startHaoYouAddActivity(this, userId, "2")
        finish()
        return true
    }
}
