package com.example.asceneskit.binder

import com.cboda.agora.ui.chatBottomBar.IAUIChatBottomBarBroadView
import com.cboda.agora.ui.chatList.AUIChatBroadInfo
import com.cboda.agora.ui.ui.chatList.IAUIChatListBroadView
import com.cboda.agora.ui.ui.chatList.impl.AUIChatListBroadView
import com.cboda.agora.ui.ui.chatList.listener.AUIChatListItemClickBroadListener
import io.agora.auikit.model.AUIRoomBroadInfo
import io.agora.auikit.service.IAUIIMManagerBroadService
import io.agora.auikit.service.im.AUIChatBroadManager
import io.agora.auikit.utils.ThreadBroadManager

class AUIChatListBinder constructor(
    roomInfo: AUIRoomBroadInfo,
    private val chatList: IAUIChatListBroadView,
    private val chatBottomBar: IAUIChatBottomBarBroadView,
    private val chatManager: AUIChatBroadManager,
    private val imManagerService:IAUIIMManagerBroadService,
): IAUIBroadBindable, AUIChatListItemClickBroadListener, IAUIIMManagerBroadService.AUIIMManagerRespObserver {

    init {
        (chatList as? AUIChatListBroadView)?.setOwnerId(roomInfo.owner?.userId ?: "")
    }

    override fun bind() {
        chatList.setChatListItemClickListener(this)
        imManagerService.registerRespObserver(this)
    }

    override fun unBind() {
        chatList.setChatListItemClickListener(null)
        imManagerService.unRegisterRespObserver(this)
    }

    override fun onChatListViewClickListener() {
        chatBottomBar.hideKeyboard()
    }


    override fun messageDidReceive(
        roomId: String,
        message: IAUIIMManagerBroadService.AgoraChatTextMessage
    ) {
        ThreadBroadManager.getInstance().runOnMainThread{
            chatList.refreshSelectLast(chatManager.getMsgList().map {
                AUIChatBroadInfo(
                    it.user?.userId ?: "", it.user?.userName ?: "",
                    it.content, it.joined
                )
            })
        }
    }

    override fun onUserDidJoinRoom(
        roomId: String,
        message: IAUIIMManagerBroadService.AgoraChatTextMessage
    ) {
        ThreadBroadManager.getInstance().runOnMainThread{
            chatList.refreshSelectLast(chatManager.getMsgList().map {
                AUIChatBroadInfo(
                    it.user?.userId ?: "", it.user?.userName ?: "",
                    it.content, it.joined
                )
            })
        }
    }

}