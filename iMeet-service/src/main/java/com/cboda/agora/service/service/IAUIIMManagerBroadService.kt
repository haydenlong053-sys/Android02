package com.cboda.agora.service.service

import com.cboda.agora.service.model.AUIUserThumbnailBroadInfo
import com.cboda.agora.service.service.callback.AUIBroadException


interface IAUIIMManagerBroadService: IAUICommonBroadService<IAUIIMManagerBroadService.AUIIMManagerRespObserver>{


    /**
     *  发送聊天室消息
     *  - roomId: 聊天室id
     *  - text: 文本内容
     *  - userInfo: 用户信息
     */
    fun sendMessage(roomId: String, text: String, completion: (AgoraChatTextMessage?, AUIBroadException?) -> Unit)

    /**
     * 退出聊天室
     */
    fun userQuitRoom(completion: ((error: AUIBroadException?)->Unit)? = null)

    /**
     * 销毁聊天室
     */
    fun userDestroyedChatroom()

    data class AgoraChatTextMessage(
        val messageId: String?,
        val content: String?,
        val user: AUIUserThumbnailBroadInfo?
    )

    interface AUIIMManagerRespObserver{

        /**
         * 接收到消息
         */
        fun messageDidReceive(roomId: String, message: AgoraChatTextMessage)

        /**
         * 用户加入聊天室
         */
        fun onUserDidJoinRoom(roomId: String, message: AgoraChatTextMessage)

    }


}