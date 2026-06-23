package com.freddy.chat.search

import androidx.fragment.app.Fragment
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.intelligent.utils.AppBroadUtils
import com.cboda.intelligent.utils.JumpBroadUtils
import com.freddy.im.http.model.im.SessionItem
import com.freddy.im.signal.ImSignalProtos
import com.freddy.im.util.ImUtils

/** 自研 IM 搜索页跳转实现。 */
object ImSearchChatLaunchDelegate : SearchChatLaunchDelegate {

    override fun openSingleChat(fragment: Fragment, userId: String, displayName: String?) {
        AppBroadUtils.goChatP2p(fragment.requireContext(),userId,displayName)
        fragment.activity?.finish()
    }

    override fun openGroupChat(fragment: Fragment, sessionItem: SessionItem) {
        if (sessionItem.sessionId.isNullOrEmpty()) return
        JumpBroadUtils.startChatMsgActivity(sessionItem, fragment.activity)
        fragment.activity?.finish()
    }

    override fun openChatAtMessage(
        fragment: Fragment,
        sessionItem: SessionItem,
        focusMsgId: String
    ) {
        if (sessionItem.sessionId.isNullOrEmpty() || focusMsgId.isEmpty()) return
        JumpBroadUtils.startChatMsgActivity(sessionItem, focusMsgId, fragment.activity)
        fragment.activity?.finish()
    }
}
