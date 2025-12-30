package com.lxkj.aiim.dialog.pop

import android.content.Context
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.lifecycle.LifecycleCoroutineScope
import com.lxkj.aiim.databinding.ActivityAuthStep1Binding
import com.lxkj.aiim.databinding.DialogPopSaveAuthBinding
import com.lxkj.aiim.lj.event.ActionEventBean
import com.lxkj.aiim.lj.utils.copyToClipboard
import com.lxkj.aiim.lj.utils.showBase64QrCodeIv
import com.lxkj.aiim.model.TwoStepAuthBean
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import razerdp.basepopup.BasePopupWindow

//保存提示pop
class SaveQrCodeTipPop(val context: Context) : BasePopupWindow(context) {
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
    fun onAppEventReceiver(event: TwoStepAuthBean?){
        event?.let {
            binding.mTvCopyDes.text = it.randomSecretKey
            showBase64QrCodeIv(context,binding.mIvQrCodeAuth,it.qrCodeImageBase64)

        }

    }



    override fun onDismiss() {
        EventBus.getDefault().post(ActionEventBean().apply {
            isFinishAction =true
        })
        EventBus.getDefault().unregister(this)
        super.onDismiss()
    }
}