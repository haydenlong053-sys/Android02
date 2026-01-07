package com.cboda.intelligent.dialog.pop

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import com.cboda.intelligent.databinding.DialogPopSaveAuthBinding
import com.cboda.intelligent.bean.ActionEventBroadBean
import com.cboda.intelligent.utils.copyToClipboard
import com.cboda.intelligent.utils.showBase64QrCodeIv
import com.cboda.intelligent.model.TwoStepAuthBroadBean
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import razerdp.basepopup.BasePopupWindow

//保存提示pop
class SaveQrCodeTipBroadPop(val context: Context) : BasePopupWindow(context) {
    private var  binding: DialogPopSaveAuthBinding = DialogPopSaveAuthBinding.inflate(
        LayoutInflater.from(context))
    init {
        contentView = binding.root
        popupGravity = Gravity.CENTER

    }

    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
        EventBus.getDefault().register(this)
        binding.mLlGoogleAuthCopy.setOnClickListener {
            copyToClipboard(context,binding.mTvCopyDes.text.toString())
        }
        binding.mTvOk.setOnClickListener {
            dismiss()
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onAppEventReceiver(event: TwoStepAuthBroadBean?){
        event?.let {
            binding.mTvCopyDes.text = it.randomSecretKey
            showBase64QrCodeIv(context,binding.mIvQrCodeAuth,it.qrCodeImageBase64)

        }

    }



    override fun onDismiss() {
        EventBus.getDefault().post(ActionEventBroadBean().apply {
            isFinishAction =true
        })
        EventBus.getDefault().unregister(this)
        super.onDismiss()
    }
}