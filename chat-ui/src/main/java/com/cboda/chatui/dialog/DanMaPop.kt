package com.cboda.chatui.dialog

import android.content.Context
import android.graphics.Color
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import com.cboda.chatui.databinding.DanMaPopularBinding
import razerdp.basepopup.BasePopupWindow

class DanMaPop(context: Context): BasePopupWindow(context) {

    var sureInvoke:()-> Unit ={}
    private var binding = DanMaPopularBinding.inflate(
        LayoutInflater.from(context)
    )

    init {
        contentView = binding.root
        popupGravity = Gravity.TOP
        setBackgroundColor(Color.parseColor("#00ffffff"))
    }

    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
//        binding.mTvCancel.setOnClickListener {
//            dismiss()
//        }
//        binding.mTvSure.setOnClickListener {
//            sureInvoke.invoke()
//            dismiss()
//
//        }
        initData()

    }

    fun setSureBtn(strName: String ="") :BasePopupWindow {
//        binding.mTvSure.text =strName
        return this
    }

    fun setCancelBtn(strName: String ="") :BasePopupWindow {
//        binding.mTvCancel.text =strName
        return this
    }
    private fun initData() {
//        binding.mTvTitle.text = title
//        binding.mTvDes.text = des
//        if (context is AppCompatActivity) {
//            val activity = context
//            viewModel =
//                ViewModelProvider(activity).get<TransferViewBroadModel>(TransferViewBroadModel::class.java)
//        }

    }


    override fun onDismiss() {
//        scope.cancel()
        super.onDismiss()
    }
}