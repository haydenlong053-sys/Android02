package io.agora.auikit.ui.gift

import android.view.View
import io.agora.auikit.ui.gift.impl.dialog.AUiGiftListBroadView

interface IAUIGiftBarrageBroadView {
    fun setDialogActionListener(listener: AUiGiftListBroadView.ActionListener){}

    fun refresh(giftList:List<AUIGiftInfo>?) {}

    fun setEmptyView(view: View?){}
}