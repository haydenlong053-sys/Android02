package io.agora.auikit.service.imp

import androidx.annotation.StringRes
import io.agora.auikit.model.AUIGiftBroadEntity
import io.agora.auikit.model.AUIGiftTabBroadEntity
import io.agora.auikit.model.AUIRoomBroadContext
import io.agora.auikit.service.IAUIGiftsBroadService
import io.agora.auikit.service.R
import io.agora.auikit.service.callback.AUIBroadCallback
import io.agora.auikit.service.callback.AUIBroadException
import io.agora.auikit.service.callback.AUIGiftListBroadCallback
import io.agora.auikit.service.im.AUIChatBroadManager
import io.agora.auikit.service.rtm.AUIRtmBroadManager
import io.agora.auikit.service.rtm.AUIRtmMessageRespObserver
import io.agora.auikit.utils.GsonBroadTools
import io.agora.auikit.utils.ObservableBroadHelper
import org.json.JSONObject

private const val giftKey = "AUIChatRoomGift"
class AUIGiftServiceBroadImpl constructor(
    private val channelName: String,
    private val rtmManager: AUIRtmBroadManager,
    private val chatManager:AUIChatBroadManager
) : IAUIGiftsBroadService, AUIRtmMessageRespObserver {

    private val observableHelper =
        ObservableBroadHelper<IAUIGiftsBroadService.AUIGiftRespObserver>()
    private var roomContext: AUIRoomBroadContext

    init {
        rtmManager.subscribeMessage(this)
        this.roomContext = AUIRoomBroadContext.shared()
    }

    private fun getString(@StringRes id: Int) : String {
        return roomContext.mCommonConfig?.context?.getString(id) ?: ""
    }

    override fun getGiftsFromService(callback: AUIGiftListBroadCallback?) {
        callback?.onResult(null, listOf(
            AUIGiftTabBroadEntity(
                1,
                getString(R.string.aui_gift_group_name),
                listOf(
                    AUIGiftBroadEntity(
                        "2665752a-e273-427c-ac5a-4b2a9c82b255",
                        getString(R.string.aui_gift_sweet_heart),
                        "1",
                        "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pictures/gift/AUIKitGift1.png"
                    ),
                    AUIGiftBroadEntity(
                        "ff3bbb9e-ef18-430f-aa61-5bddf75eb722",
                        getString(R.string.aui_gift_flower),
                        "1",
                        "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pictures/gift/AUIKitGift2.png"
                    ),
                    AUIGiftBroadEntity(
                        "94f296fa-86d9-4552-84db-025b05ed9f8d",
                        getString(R.string.aui_gift_sweet_heart),
                        "1",
                        "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pictures/gift/AUIKitGift3.png"
                    ),
                    AUIGiftBroadEntity(
                        "d4cd0526-d8db-4e00-8fc0-d5228907a517",
                        getString(R.string.aui_gift_super_agora),
                        "1",
                        "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pictures/gift/AUIKitGift4.png"
                    ),
                    AUIGiftBroadEntity(
                        "c1997f02-d927-46f5-adda-e6af6714bd75",
                        getString(R.string.aui_gift_star),
                        "1",
                        "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pictures/gift/AUIKitGift5.png"
                    ),
                    AUIGiftBroadEntity(
                        "0c62b402-376f-4fbb-b584-769a8249189e",
                        getString(R.string.aui_gift_lollipop),
                        "1",
                        "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pictures/gift/AUIKitGift6.png"
                    ), AUIGiftBroadEntity(
                        "ce3f8bc3-74d7-43be-a040-c397d5c49f6d",
                        getString(R.string.aui_gift_diamond),
                        "1",
                        "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pictures/gift/AUIKitGift7.png"
                    ),
                    AUIGiftBroadEntity(
                        "948b1a3b-b2c6-41fc-99b7-a5b9457cd159",
                        getString(R.string.aui_gift_crown),
                        "1",
                        "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pictures/gift/AUIKitGift8.png"
                    ),
                    AUIGiftBroadEntity(
                        "f1e12397-feb7-4c01-b834-f11faf321dbf",
                        getString(R.string.aui_gift_mic),
                        "1",
                        "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pictures/gift/AUIKitGift9.png"
                    ),
                    AUIGiftBroadEntity(
                        "e915438c-7fbd-4e03-840f-0036ec97c824",
                        getString(R.string.aui_gift_balloon),
                        "1",
                        "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pictures/gift/AUIKitGift10.png",
                        giftEffect = "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pag/ballon.pag",
                        effectMD5 = "141761700268c0290852af8f6a501c10"
                    ),
                    AUIGiftBroadEntity(
                        "0c832b52-8f2e-4202-958b-9410db2d9438",
                        getString(R.string.aui_gift_plant),
                        "1",
                        "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pictures/gift/AUIKitGift11.png",
                        giftEffect = "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pag/planet.pag",
                        effectMD5 = "41f3eeff249be268004d82a1d1eaf481"
                    ),
                    AUIGiftBroadEntity(
                        "beada6a3-eae6-450e-869c-743d02fa95e7",
                        getString(R.string.aui_gift_rocket),
                        "1",
                        "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pictures/gift/AUIKitGift12.png",
                        giftEffect = "https://fullapp.oss-cn-beijing.aliyuncs.com/uikit/pag/rocket.pag",
                        effectMD5 = "de5094b30eebeadf8b8f5d8357a19578"
                    )
                )
            )
        ))
    }

    override fun sendGift(gift: AUIGiftBroadEntity, callback: AUIBroadCallback) {
        val giftJson = JSONObject()
        giftJson.put("messageType", giftKey)
        giftJson.put("messageInfo", GsonBroadTools.beanToString(gift))
        rtmManager.publish(channelName, "", giftJson.toString()){ error ->
            if(error == null ){
                callback.onResult(null)
            }else{
                callback.onResult(AUIBroadException(AUIBroadException.ERROR_CODE_RTM, "error: $error"))
            }
        }
    }

    override fun registerRespObserver(observer: IAUIGiftsBroadService.AUIGiftRespObserver?) {
        observableHelper.subscribeEvent(observer)
    }

    override fun unRegisterRespObserver(observer: IAUIGiftsBroadService.AUIGiftRespObserver?) {
        observableHelper.subscribeEvent(observer)
    }

    override fun getRoomContext() = roomContext

    override fun getChannelName() = channelName

    override fun onMessageReceive(channelName: String, publisherId: String, message: String) {
        if(this@AUIGiftServiceBroadImpl.channelName != channelName){
            return
        }
        val json = JSONObject(message)
        if (json.getString("messageType") != giftKey) {
            return
        }
        GsonBroadTools.toBean(json["messageInfo"].toString(), AUIGiftBroadEntity::class.java)?.let { it ->
            chatManager.addGiftList(it)
            this.observableHelper.notifyEventHandlers { it1 ->
                it1.onReceiveGiftMsg(it)
            }
        }
    }
}