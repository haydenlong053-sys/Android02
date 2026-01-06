package io.agora.auikit.service.imp

import io.agora.CallBack
import io.agora.auikit.model.AUIRoomBroadContext
import io.agora.auikit.model.AgoraChatBroadMessage
import io.agora.auikit.service.IAUIIMManagerBroadService
import io.agora.auikit.service.callback.AUIBroadCallback
import io.agora.auikit.service.callback.AUIChatMsgBroadCallback
import io.agora.auikit.service.callback.AUIBroadException
import io.agora.auikit.service.collection.AUIAttributesModel
import io.agora.auikit.service.collection.AUIMapBroadCollection
import io.agora.auikit.service.http.CommonBroadResp
import io.agora.auikit.service.http.HttpBroadManager
import io.agora.auikit.service.http.chat.CHATROOM_CREATE_TYPE_USER
import io.agora.auikit.service.http.chat.CHATROOM_CREATE_TYPE_USER_ROOM
import io.agora.auikit.service.http.chat.ChatIMConfig
import io.agora.auikit.service.http.chat.ChatBroadInterface
import io.agora.auikit.service.http.chat.ChatRoomConfig
import io.agora.auikit.service.http.chat.ChatUser
import io.agora.auikit.service.http.chat.CreateChatRoomResp
import io.agora.auikit.service.im.AUIChatEventBroadHandler
import io.agora.auikit.service.im.AUIChatBroadManager
import io.agora.auikit.service.rtm.AUIRtmBroadManager
import io.agora.auikit.utils.AUIBroadLogger
import io.agora.auikit.utils.AgoraEngineBroadCreator
import io.agora.auikit.utils.ObservableBroadHelper
import io.agora.chat.ChatMessage
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

const val kChatAttrKey = "chatRoom"
private const val kChatIdKey = "chatRoomId"

class AUIIMManagerServiceBroadImpl constructor(
    private val channelName: String,
    private val rtmManager: AUIRtmBroadManager,
    private val chatManager: AUIChatBroadManager
) : IAUIIMManagerBroadService, AUIChatEventBroadHandler {
    private val roomContext = AUIRoomBroadContext.shared()

    private val observableHelper = ObservableBroadHelper<IAUIIMManagerBroadService.AUIIMManagerRespObserver>()

    private val mapCollection = AUIMapBroadCollection(channelName, kChatAttrKey, rtmManager)

    private var chatRoomId = ""
    private var chatUserId = ""
    private var chatUserToken = ""

    init {
        mapCollection.subscribeAttributesDidChanged(this::onAttributeChanged)
    }

    override fun serviceDidLoad() {
        super.serviceDidLoad()
        initChatRoom()
    }

    override fun deInitService(completion: AUIBroadCallback?) {
        super.deInitService(completion)

        mapCollection.cleanMetaData {
            completion?.onResult(
                if (it == null) null else AUIBroadException(
                    AUIBroadException.ERROR_CODE_RTM_COLLECTION,
                    " $it"
                )
            )
        }
        mapCollection.release()
    }

    private fun initChatRoom() {
        configIM { configError ->
            if (configError != null) {
                AUIBroadLogger.logger().e(message = "IM initialize failed! -- $configError")
                return@configIM
            }

            loginAndJoinChatRoom()


        }
    }

    private fun loginAndJoinChatRoom() {
        if (chatRoomId.isEmpty()) {
            AUIBroadLogger.logger().d(message = "loginAndJoinChatRoom >> chatRoomId is empty")
            return
        }
        chatManager.setChatRoom(chatRoomId)
        login { loginError ->
            if (loginError != null) {
                AUIBroadLogger.logger()
                    .e(message = "loginAndJoinChatRoom >> IM login failed! -- $loginError")
                return@login
            }
            val userName = chatManager.userName
            if (userName == null) {
                AUIBroadLogger.logger().e(message = "loginAndJoinChatRoom >> IM login userName is null!")
                return@login
            }

            joinChatRoom(chatRoomId) { message, error ->
                AUIBroadLogger.logger()
                    .d(message = "loginAndJoinChatRoom >> joinChatRoom result: message=$message, error=$error")
            }
        }
    }

    private fun joinChatRoom(
        roomId: String?,
        callback: (message: IAUIIMManagerBroadService.AgoraChatTextMessage?, error: AUIBroadException?) -> Unit
    ) {
        chatManager.initManager()
        chatManager.subscribeChatMsg(this)
        chatManager.joinRoom(roomId, object : AUIChatMsgBroadCallback {
            override fun onOriginalResult(
                error: AUIBroadException?,
                message: ChatMessage?
            ) {
                super.onOriginalResult(error, message)
                if (error != null) {
                    AUIBroadLogger.logger()
                        .e(message = "IM join chat room failed! -- $error")
                    callback.invoke(
                        null,
                        AUIBroadException(-1, "joinChatRoom >> IM join chat room failed! -- $error")
                    )
                    return
                }

                val textMsg = IAUIIMManagerBroadService.AgoraChatTextMessage(
                    message?.msgId, message?.body?.toString(), null
                )
                observableHelper.notifyEventHandlers {
                    it.onUserDidJoinRoom(roomId!!, textMsg)
                }
                callback.invoke(textMsg, null)
            }
        })
    }


    override fun registerRespObserver(observer: IAUIIMManagerBroadService.AUIIMManagerRespObserver?) {
        observableHelper.subscribeEvent(observer)
    }

    override fun unRegisterRespObserver(observer: IAUIIMManagerBroadService.AUIIMManagerRespObserver?) {
        observableHelper.unSubscribeEvent(observer)
    }

    override fun getRoomContext(): AUIRoomBroadContext = roomContext

    override fun getChannelName() = channelName

    override fun sendMessage(
        roomId: String,
        text: String,
        completion: (IAUIIMManagerBroadService.AgoraChatTextMessage?, AUIBroadException?) -> Unit
    ) {
        chatManager.sendTxtMsg(
            chatRoomId,
            text,
            roomContext.currentUserInfo,
            object : AUIChatMsgBroadCallback {
                override fun onResult(error: AUIBroadException?, message: AgoraChatBroadMessage?) {
                    super.onResult(error, message)
                    if (error != null) {
                        completion.invoke(null, error)
                        return
                    }

                    completion.invoke(
                        IAUIIMManagerBroadService.AgoraChatTextMessage(
                            message?.messageId,
                            message?.content, roomContext.currentUserInfo
                        ), null
                    )
                }
            })
    }

    override fun userQuitRoom(completion: ((error: AUIBroadException?) -> Unit)?) {
        mapCollection.subscribeAttributesDidChanged(null)
        chatManager.leaveChatRoom()
        chatManager.logoutChat()
        chatManager.unsubscribeChatMsg(this)
        chatManager.clear()
        chatRoomId = ""
        chatUserToken = ""
        chatUserId = ""
        completion?.invoke(null)
    }

    override fun userDestroyedChatroom() {
        mapCollection.subscribeAttributesDidChanged(null)
        chatManager.asyncDestroyChatRoom(object : CallBack {
            override fun onSuccess() {

            }

            override fun onError(code: Int, error: String?) {
                AUIBroadLogger.logger()
                    .e(message = "userDestroyedChatroom error -- code=$code, message=$error")
            }
        })
        chatManager.logoutChat()
        chatManager.unsubscribeChatMsg(this)
        chatManager.clear()
        chatRoomId = ""
        chatUserId = ""
        chatUserToken = ""
    }

    private fun configIM(completion: (AUIBroadException?) -> Unit) {
        // request restful api to create user and room
        val isRoomOwner = roomContext.isRoomOwner(channelName)
        val commonConfig = roomContext.mCommonConfig ?: return
        HttpBroadManager.getService(ChatBroadInterface::class.java).createChatRoom(
            io.agora.auikit.service.http.chat.CreateChatRoomReq(
                appId = commonConfig.appId,
                type = if (isRoomOwner) CHATROOM_CREATE_TYPE_USER_ROOM else CHATROOM_CREATE_TYPE_USER,
                chatRoomConfig = if (isRoomOwner) ChatRoomConfig(channelName) else null,
                imConfig = ChatIMConfig(
                    commonConfig.imAppKey.split("#").getOrNull(0),
                    commonConfig.imAppKey.split("#").getOrNull(1),
                    commonConfig.imClientId,
                    commonConfig.imClientSecret
                ),
                user = ChatUser(roomContext.currentUserInfo.userId)
            )
        ).enqueue(object : Callback<CommonBroadResp<CreateChatRoomResp>> {
            override fun onResponse(
                call: Call<CommonBroadResp<CreateChatRoomResp>>,
                response: Response<CommonBroadResp<CreateChatRoomResp>>
            ) {
                val resp = response.body()?.data
                if (resp == null) {
                    completion.invoke(AUIBroadException(-1, "configIM >> createChatRoom resp null."))
                    return
                }
                resp.chatId?.let {
                    if (it.isNotEmpty()) {
                        mapCollection.addMetaData(null, mapOf(Pair(kChatIdKey, it)), null) {}
                        chatRoomId = it
                    }
                }

                chatUserId = roomContext.currentUserInfo.userId ?: ""
                chatUserToken = resp.userToken ?: ""

                AgoraEngineBroadCreator.createChatClient(
                    roomContext.requireCommonConfig().context,
                    resp.appKey
                )

                completion.invoke(null)
            }

            override fun onFailure(call: Call<CommonBroadResp<CreateChatRoomResp>>, t: Throwable) {
                completion.invoke(AUIBroadException(-1, t.message))
            }
        })
    }

    private fun login(completion: (AUIBroadException?) -> Unit) {
        if (chatUserId.isEmpty() || chatUserToken.isEmpty()) {
            AUIBroadLogger.logger()
                .d(message = "login >> parameters are empty. chatUserId=$chatUserId, chatUserToken=$chatUserToken")
            return
        }
        chatManager.userName = chatUserId
        chatManager.accessToken = chatUserToken
        chatManager.loginChat(object : CallBack {
            override fun onSuccess() {
                completion.invoke(null)
            }

            override fun onError(code: Int, error: String?) {
                completion.invoke(AUIBroadException(code, error))
            }
        })
    }

    private fun onAttributeChanged(channelName: String, key: String, value: AUIAttributesModel) {
        // 解析数据 获取环信聊天室id
        if (key == kChatAttrKey) {
            val attributes = value.getMap()
            chatRoomId = attributes?.get(kChatIdKey) as? String ?: ""
            if (!roomContext.isRoomOwner(channelName)) {
                loginAndJoinChatRoom()
            }
        }
    }

    override fun onReceiveMemberJoinedMsg(roomId: String?, message: AgoraChatBroadMessage?) {
        super.onReceiveMemberJoinedMsg(roomId, message)
        message ?: return
        roomId ?: return
        observableHelper.notifyEventHandlers {
            it.onUserDidJoinRoom(
                roomId, IAUIIMManagerBroadService.AgoraChatTextMessage(
                    message.messageId,
                    message.content,
                    message.user
                )
            )
        }
    }

    override fun onReceiveTextMsg(roomId: String?, message: AgoraChatBroadMessage?) {
        super.onReceiveTextMsg(roomId, message)
        message ?: return
        roomId ?: return
        observableHelper.notifyEventHandlers {
            it.messageDidReceive(
                roomId,
                IAUIIMManagerBroadService.AgoraChatTextMessage(
                    message.messageId,
                    message.content,
                    message.user
                )
            )
        }
    }
}