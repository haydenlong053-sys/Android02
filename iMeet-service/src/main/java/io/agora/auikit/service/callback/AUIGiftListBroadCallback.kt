package io.agora.auikit.service.callback

import io.agora.auikit.model.AUIGiftTabBroadEntity


interface AUIGiftListBroadCallback {

    fun onResult(error: AUIBroadException?, giftList: List<AUIGiftTabBroadEntity>)

}