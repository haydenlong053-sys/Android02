package com.cboda.intelligent.dialog.pop

import android.content.Context
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import com.cboda.intelligent.databinding.DialogPopChangeGuideTipBinding
import razerdp.basepopup.BasePopupWindow

//安全中心引导pop
class ChangeGuideBroadPop(val context: Context, val title: String = "", val des: String = "") :
    BasePopupWindow(context) {
    //    private val scope = MainScope() // 创建主线程的协程作用域
//    private var viewModel: TransferViewBroadModel? = null
    var sureInvoke:()-> Unit ={}
    private var binding = DialogPopChangeGuideTipBinding.inflate(
        LayoutInflater.from(context)
    )

    init {
        contentView = binding.root
        popupGravity = Gravity.CENTER

    }

    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
        binding.mTvCancel.setOnClickListener {
            dismiss()
        }
        binding.mTvSure.setOnClickListener {
            sureInvoke.invoke()
            dismiss()

        }
        initData()

    }

     fun setSureBtn(strName: String ="") :BasePopupWindow {
         binding.mTvSure.text =strName
        return this
    }

    fun setCancelBtn(strName: String ="") :BasePopupWindow {
        binding.mTvCancel.text =strName
        return this
    }
    private fun initData() {
        binding.mTvTitle.text = title
        binding.mTvDes.text = des
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