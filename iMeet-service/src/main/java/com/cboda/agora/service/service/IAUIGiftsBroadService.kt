package com.cboda.agora.service.service

import com.cboda.agora.service.model.AUIGiftBroadEntity
import com.cboda.agora.service.service.callback.AUIBroadCallback
import com.cboda.agora.service.service.callback.AUIGiftListBroadCallback


interface IAUIGiftsBroadService : IAUICommonBroadService<IAUIGiftsBroadService.AUIGiftRespObserver> {

    /**
     * - roomId: 房间id
     * - callback: 结果回调(包含礼物数组)
     */
    fun getGiftsFromService(callback: AUIGiftListBroadCallback?)


    /**
     * - gift：礼物信息
     * - callback: 结果回调
     */
    fun sendGift(gift: AUIGiftBroadEntity, callback: AUIBroadCallback)

    interface AUIGiftRespObserver{
        fun onReceiveGiftMsg(giftEntity: AUIGiftBroadEntity?){}
    }
}