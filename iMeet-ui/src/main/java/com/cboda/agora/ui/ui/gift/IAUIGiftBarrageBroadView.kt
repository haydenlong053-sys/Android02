package com.cboda.agora.ui.ui.gift

import android.view.View
import com.cboda.agora.ui.ui.gift.impl.dialog.AUiGiftListBroadView

interface IAUIGiftBarrageBroadView {
    fun setDialogActionListener(listener: AUiGiftListBroadView.ActionListener){}

    fun refresh(giftList:List<AUIGiftInfo>?) {}

    fun setEmptyView(view: View?){}
}