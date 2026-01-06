package com.cboda.agora.service.service.callback

import com.cboda.agora.service.model.AUIGiftTabBroadEntity


interface AUIGiftListBroadCallback {

    fun onResult(error: AUIBroadException?, giftList: List<AUIGiftTabBroadEntity>)

}