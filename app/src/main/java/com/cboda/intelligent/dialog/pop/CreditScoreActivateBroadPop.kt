package com.cboda.intelligent.dialog.pop

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import com.cboda.intelligent.databinding.DialogPopCreditScoreActivateBroadBinding
import razerdp.basepopup.BasePopupWindow

/**
 * 「激活信用评分」引导弹窗，布局与交互参考 [UserQrCodeTipBroadPop]、[com.cboda.baselibrary.popup.CommonTipBroadPop]。
 */
class CreditScoreActivateBroadPop(val context: Context) : BasePopupWindow(context) {

    var sureInvoke: () -> Unit = {}
    var cancelInvoke: () -> Unit = {}

    private var binding: DialogPopCreditScoreActivateBroadBinding =
        DialogPopCreditScoreActivateBroadBinding.inflate(LayoutInflater.from(context))

    init {
        contentView = binding.root
        popupGravity = Gravity.CENTER
    }

    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
        binding.mTvCancel.setOnClickListener {
            cancelInvoke.invoke()
            dismiss()
        }
        binding.mTvSure.setOnClickListener {
            sureInvoke.invoke()
            dismiss()
        }
    }

    override fun onDismiss() {
        super.onDismiss()
    }
}
