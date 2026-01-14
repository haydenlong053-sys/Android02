package com.cboda.agora.ui.ui.gift.listener

import android.view.View
import com.cboda.agora.ui.ui.gift.AUIGiftInfo

interface AUIGiftItemClickBroadListener {
    fun sendGift(view: View, position:Int, gift: AUIGiftInfo)
    fun selectGift(view: View, position:Int, gift: AUIGiftInfo)
}