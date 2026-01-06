package com.cboda.agora.service.service.im

import android.text.TextUtils
import android.util.Log
import com.orhanobut.logger.Logger
import io.agora.CallBack
import io.agora.ChatRoomChangeListener
import io.agora.MessageListener
import io.agora.ValueCallBack
import com.cboda.agora.service.model.AUIChatBroadEntity
import com.cboda.agora.service.model.AUICustomMsgBroadType
import com.cboda.agora.service.model.AUIGiftBroadEntity
import com.cboda.agora.service.model.AUIRoomBroadContext
import com.cboda.agora.service.model.AUIUserThumbnailBroadInfo
import com.cboda.agora.service.model.AgoraChatBroadMessage
import com.cboda.agora.service.service.callback.AUIChatMsgBroadCallback
import com.cboda.agora.service.service.callback.AUIBroadException
import com.cboda.agora.service.utils.GsonBroadTools
import com.cboda.agora.service.utils.ThreadBroadManager
import io.agora.chat.ChatClient
import io.agora.chat.ChatMessage
import io.agora.chat.ChatRoom
import io.agora.chat.CustomMessageBody
import io.agora.chat.TextMessageBody
import io.agora.chat.adapter.EMAChatRoomManagerListener
import org.json.JSONObject

class AUIChatBroadManager(
    channelName: String,
    roomContext: AUIRoomBroadContext,
): MessageListener, ChatRoomChangeListener {
    private val chatEvnetHandlers = mutableListOf<AUIChatEventBroadHandler>()
    private var chatRoomId:String? = ""
    private var channelId:String? = ""
    private var appKey:String? =""

    var userName:String?=""
    var userId:String?=""
    var accessToken:String?=""

    private var roomContext:AUIRoomBroadContext? = null

    private val currentMsgList: ArrayList<AUIChatBroadEntity> = ArrayList<AUIChatBroadEntity>()
    private val currentGiftList: ArrayList<AUIGiftBroadEntity> = ArrayList<AUIGiftBroadEntity>()

    init {
        this.channelId = channelName
        this.roomContext = roomContext
    }

    fun subscribeChatMsg(delegate: AUIChatEventBroadHandler) {
        chatEvnetHandlers.add(delegate)
    }

    fun unsubscribeChatMsg(delegate: AUIChatEventBroadHandler?) {
        chatEvnetHandlers.remove(delegate)
    }

    fun initManager() {
        Logger.t("AUIChatBroadManager").d("initManager")
        ChatClient.getInstance().chatManager().addMessageListener(this)
        ChatClient.getInstance().chatroomManager().addChatRoomChangeListener(this)
    }

    fun setChatRoom(chatRoomId: String?){
        this.chatRoomId = chatRoomId
        Log.e("apex","setChatRoomID $chatRoomId")
    }

    fun getCurrentRoom():String?{
        return chatRoomId
    }

    fun getCurrentRoomOwnerId():String?{
        return roomContext?.getRoomOwner(channelId)
    }

    fun isOwner():Boolean{
        return roomContext?.isRoomOwner(channelId) == true
    }

    fun getAppKey():String?{
        return appKey
    }

    fun clear(){
        chatRoomId = ""
        channelId = ""
        userId = ""
        userName = ""
        accessToken = ""
        currentMsgList.clear()
        currentGiftList.clear()
        if (ChatClient.getInstance().isSdkInited) {
            ChatClient.getInstance().chatManager().removeMessageListener(this)
            ChatClient.getInstance().chatroomManager().removeChatRoomListener(this)
        }
    }

    fun loginChat(userName:String,userToken:String,callback:CallBack){
        Logger.t("AUIChatBroadManager").d("loginChat")
        ChatClient.getInstance().loginWithToken(userName,userToken,object : CallBack{
            override fun onSuccess() {
                callback.onSuccess()
            }

            override fun onError(code: Int, error: String?) {
                callback.onError(code,error)
            }
        })
    }

    fun loginChat(callback:CallBack){
        Logger.t("AUIChatBroadManager").d("loginChat")
        ChatClient.getInstance().loginWithToken(userName,accessToken,object : CallBack{
            override fun onSuccess() {
                ThreadBroadManager.getInstance().runOnMainThread{
                    callback.onSuccess()
                }
                Log.d("apex","loginChat suc")
            }

            override fun onError(code: Int, error: String?) {
                ThreadBroadManager.getInstance().runOnMainThread{
                    callback.onError(code,error)
                }
                Log.e("apex","loginChat error $code  $error")
            }
        })
    }

    fun logoutChat(callback: CallBack){
        Logger.t("AUIChatBroadManager").d("logoutChat")
        ChatClient.getInstance().logout(false,object:CallBack{
            override fun onSuccess() {
                callback.onSuccess()
            }

            override fun onError(code: Int, error: String?) {
                callback.onError(code, error)
            }
        })
    }

    fun logoutChat() {
        if (ChatClient.getInstance().isSdkInited) {
            ChatClient.getInstance().logout(false)
        }
    }

    /**
     * 加入房间
     */
    fun joinRoom(roomId: String?,callback: AUIChatMsgBroadCallback){
        ChatClient.getInstance().chatroomManager().joinChatRoom(roomId,object:
            ValueCallBack<ChatRoom> {
            override fun onSuccess(value: ChatRoom?) {
                Log.e("apex","joinRoom onSuccess $roomId")
                //加入成功后 返回成员加入消息
                sendJoinMsg(roomId,roomContext?.currentUserInfo,object : AUIChatMsgBroadCallback{
                    override fun onOriginalResult(error: AUIBroadException?, message: ChatMessage?) {
                        if (error == null){
                            ThreadBroadManager.getInstance().runOnMainThreadDelay({callback.onOriginalResult(null,message)},300)
                        }
                    }
                })
            }

            override fun onError(code: Int, errorMsg: String?) {
                Log.e("apex","joinRoom onError $roomId $code $errorMsg")
                ThreadBroadManager.getInstance().runOnMainThread { callback.onOriginalResult(AUIBroadException(code,errorMsg),null) }
            }
        })
    }

    /**
     * 离开房间
     */
    fun leaveChatRoom() {
        if(ChatClient.getInstance().isSdkInited){
            ChatClient.getInstance().chatroomManager().leaveChatRoom(chatRoomId)
        }
    }

    /**
     * 销毁房间
     */
    fun asyncDestroyChatRoom(callBack: CallBack) {

        ChatClient.getInstance().chatroomManager()?.asyncDestroyChatRoom(chatRoomId, object : CallBack {
                override fun onSuccess() {
                    ThreadBroadManager.getInstance().runOnMainThread(Runnable { callBack.onSuccess() })
                }

                override fun onError(code: Int, error: String) {
                    ThreadBroadManager.getInstance()
                        .runOnMainThread(Runnable { callBack.onError(code, error) })
                }
            })
    }


    fun isLoggedIn():Boolean{
        return ChatClient.getInstance().isSdkInited && ChatClient.getInstance().isLoggedIn
    }

    override fun onMessageReceived(messages: MutableList<ChatMessage>?) {
        messages?.forEach {
            if (it.type == ChatMessage.Type.TXT) {
                parseMsgChatEntity(it)
                try {
                    for (listener in chatEvnetHandlers) {
                        listener.onReceiveTextMsg(channelId,parseChatMessage(it))
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
            // 先判断是否自定义消息
            if (it.type == ChatMessage.Type.CUSTOM) {
                val body = it.body as CustomMessageBody
                val event = body.event()
                val msgType: AUICustomMsgBroadType? = getCustomMsgType(event)

                // 再排除单聊
                if (it.chatType != ChatMessage.ChatType.Chat){
                    val username: String = it.to
                    // 判断是否同一个聊天室或者群组 并且 event不为空
                    if (TextUtils.equals(username,chatRoomId) && !TextUtils.isEmpty(event)) {
                        when (msgType) {
                            AUICustomMsgBroadType.AUIChatRoomJoinedMember -> {
                                parseMsgChatEntity(it)
                                try {
                                    for (listener in chatEvnetHandlers) {
                                        listener.onReceiveMemberJoinedMsg(channelId,parseChatMessage(it))
                                    }
                                } catch (e: Exception) {
                                    e.printStackTrace()
                                }
                            }
                            else -> {}
                        }
                    }
                }
            }
        }
    }

    /**
     *  解析消息 AgoraChatBroadMessage（原消息）
     */
    private fun parseChatMessage(chatMessage: ChatMessage): AgoraChatBroadMessage {
        var chatId:String? = ""
        var messageId:String? = ""
        var content:String? = ""
        var user:AUIUserThumbnailBroadInfo? = AUIUserThumbnailBroadInfo()

        val attr = chatMessage.getStringAttribute("user","")
        if (!attr.isNullOrEmpty()){
            GsonBroadTools.toBean(attr, AgoraChatBroadMessage::class.java)?.let { it ->
                user = it.user
            }
        }
        chatId = chatMessage.conversationId()
        messageId = chatMessage.msgId
        if (chatMessage.body is TextMessageBody) {
            content = (chatMessage.body as TextMessageBody).message
        }
        return AgoraChatBroadMessage(
            chatId,
            messageId,
            content,
            user
        )
    }

    /**
     * 解析消息 AUIChatBroadEntity （ui渲染消息）
     */
    fun parseMsgChatEntity(chatMessage: ChatMessage){
        var content:String?=""
        var joined = false
        val user = AUIUserThumbnailBroadInfo()
        if (chatMessage.body is TextMessageBody) {
            content = (chatMessage.body as TextMessageBody).message
            joined = false
            val attr = chatMessage.getStringAttribute("user","")
            if (!attr.isNullOrEmpty()){
                val json = JSONObject(attr)
                user.userId = json.get("userId") as String
                user.userName = json.get("userName") as String
                user.userAvatar = json.get("userAvatar") as String
            }
        } else if (chatMessage.body is CustomMessageBody) {
            joined = true
            val params = (chatMessage.body as CustomMessageBody).params
            val attr = params["user"]
            if (!attr.isNullOrEmpty()){
                val json = JSONObject(attr)
                user.userId = json.get("userId") as String
                user.userName = json.get("userName") as String
                user.userAvatar = json.get("userAvatar") as String
            }
        }
        currentMsgList.add(AUIChatBroadEntity(user,content,joined))
    }


    fun getGiftList(): ArrayList<AUIGiftBroadEntity>{
        return currentGiftList
    }

    fun addGiftList(gift:AUIGiftBroadEntity){
        currentGiftList.clear()
        currentGiftList.add(gift)
    }

    fun getMsgList(): ArrayList<AUIChatBroadEntity>{
        return currentMsgList
    }

    /**
     * 发送文本消息
     * @param content
     * @param callBack
     */
    fun sendTxtMsg(roomId: String?, content: String?, userInfo: AUIUserThumbnailBroadInfo?, callBack: AUIChatMsgBroadCallback) {
        if (!isLoggedIn()) {
            return
        }
        val message = ChatMessage.createTextSendMessage(content, roomId)
        message?.let {
            it.setAttribute("user",GsonBroadTools.beanToString(userInfo))
            it.chatType = ChatMessage.ChatType.ChatRoom
            it.setMessageStatusCallback(object : CallBack {
                override fun onSuccess() {
                    parseMsgChatEntity(it)
                    callBack.onResult(null,parseChatMessage(it))
                }

                override fun onError(i: Int, s: String) {
                    callBack.onResult(AUIBroadException(i, s),null)
                }

                override fun onProgress(i: Int, s: String) {

                }
            })
            ChatClient.getInstance().chatManager().sendMessage(it)
        }
    }

    fun sendJoinMsg(roomId: String?,userInfo: AUIUserThumbnailBroadInfo?,callBack: AUIChatMsgBroadCallback){
        if (!isLoggedIn()) {
            return
        }
        val messages = ChatMessage.createSendMessage(ChatMessage.Type.CUSTOM)
        messages.to = roomId
        val customBody = CustomMessageBody( AUICustomMsgBroadType.AUIChatRoomJoinedMember.name)
        val ext = mutableMapOf<String,String>()
        ext["user"] = GsonBroadTools.beanToString(userInfo).toString()
        customBody.params = ext
        messages.let {
            it.body = customBody
            it?.chatType = ChatMessage.ChatType.ChatRoom
            it?.setMessageStatusCallback(object : CallBack {
                override fun onSuccess() {
                    parseMsgChatEntity(it)
                    callBack.onOriginalResult(null,it)
                }

                override fun onError(i: Int, s: String) {
                    callBack.onOriginalResult(AUIBroadException(i, s),null)
                }

                override fun onProgress(i: Int, s: String) {

                }
            })
            ChatClient.getInstance().chatManager().sendMessage(it)
        }
    }

    /**
     * 插入欢迎消息
     * @param content
     */
    fun saveWelcomeMsg(content: String?) {
        currentMsgList.clear()
        val auiChatEntity = AUIChatBroadEntity(
            roomContext?.currentUserInfo,
            content,false
        )
        currentMsgList.add(auiChatEntity)
    }

    /**
     * 获取自定义消息类型
     * @param event
     * @return
     */
    private fun getCustomMsgType(event: String?): AUICustomMsgBroadType? {
        return if (TextUtils.isEmpty(event)) {
            null
        } else AUICustomMsgBroadType.fromName(event)
    }


    /**
     * 获取成员非主动退出房间原因
     * @param reason
     * @return
     */
    private fun getKickReason(reason: Int): VoiceRoomServiceKickedReason? {
        return when (reason) {
            EMAChatRoomManagerListener.BE_KICKED -> VoiceRoomServiceKickedReason.removed
            EMAChatRoomManagerListener.DESTROYED -> VoiceRoomServiceKickedReason.destroyed
            EMAChatRoomManagerListener.BE_KICKED_FOR_OFFLINE -> VoiceRoomServiceKickedReason.offLined
            else -> null
        }
    }

    override fun onAnnouncementChanged(chatRoomId: String?, announcement: String?) {
    }

    override fun onRemovedFromChatRoom(
        reason: Int,
        chatRoomId: String?,
        roomName: String?,
        participant: String?
    ) {
        try {
            for (listener in chatEvnetHandlers) {
                listener.onUserBeKicked(chatRoomId,getKickReason(reason))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onChatRoomDestroyed(chatRoomId: String?, roomName: String?) {
        try {
            for (listener in chatEvnetHandlers) {
                listener.onRoomDestroyed(chatRoomId)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onMemberJoined(roomId: String?, participant: String?) {}

    override fun onMemberExited(roomId: String?, roomName: String?, participant: String?) {}

    override fun onMuteListAdded(
        chatRoomId: String?,
        mutes: MutableList<String>?,
        expireTime: Long
    ) {}

    override fun onMuteListRemoved(chatRoomId: String?, mutes: MutableList<String>?) {}

    override fun onWhiteListAdded(chatRoomId: String?, whitelist: MutableList<String>?) {}

    override fun onWhiteListRemoved(chatRoomId: String?, whitelist: MutableList<String>?) {}

    override fun onAllMemberMuteStateChanged(chatRoomId: String?, isMuted: Boolean) {}

    override fun onAdminAdded(chatRoomId: String?, admin: String?) {}

    override fun onAdminRemoved(chatRoomId: String?, admin: String?) {}

    override fun onOwnerChanged(chatRoomId: String?, newOwner: String?, oldOwner: String?) {}

}