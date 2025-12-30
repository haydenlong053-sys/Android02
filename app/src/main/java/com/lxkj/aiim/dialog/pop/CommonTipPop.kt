package com.lxkj.aiim.dialog.pop

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.ViewModelProvider
import com.lxkj.aiim.BuildConfig
import com.lxkj.aiim.R
import com.lxkj.aiim.databinding.ActivityAuthStep1Binding
import com.lxkj.aiim.databinding.DialogPopCommonTipBinding
import com.lxkj.aiim.databinding.DialogPopSaveAuthBinding
import com.lxkj.aiim.databinding.DialogPopSureTransferBinding
import com.lxkj.aiim.lj.GoogleAuthStep1Activity
import com.lxkj.aiim.lj.SecurityAuthEditActivity
import com.lxkj.aiim.lj.bean.ResultBean
import com.lxkj.aiim.lj.configuration.SafeJumpEnum
import com.lxkj.aiim.lj.event.ActionEventBean
import com.lxkj.aiim.lj.event.TransferEventBean
import com.lxkj.aiim.lj.http.BaseCallback
import com.lxkj.aiim.lj.utils.GlideUtil
import com.lxkj.aiim.lj.utils.copyToClipboard
import com.lxkj.aiim.lj.utils.showBase64QrCodeIv
import com.lxkj.aiim.model.PersonInfoBean
import com.lxkj.aiim.model.TwoStepAuthBean
import com.lxkj.aiim.viewmodel.SafeCenterViewModel
import com.lxkj.aiim.viewmodel.TransferViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import okhttp3.Request
import okhttp3.Response
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import razerdp.basepopup.BasePopupWindow

//简单基础提示pop
class CommonTipPop(val context: Context, val title: String = "", val des: String = "") :
    BasePopupWindow(context) {
    //    private val scope = MainScope() // 创建主线程的协程作用域
//    private var viewModel: TransferViewModel? = null
    var sureInvoke:()-> Unit ={}
    //添加取消回調
    var cancelInvoke:()-> Unit ={}
    private var binding = DialogPopCommonTipBinding.inflate(
        LayoutInflater.from(context)
    )

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
//                ViewModelProvider(activity).get<TransferViewModel>(TransferViewModel::class.java)
//        }

    }


    override fun onDismiss() {
//        scope.cancel()
        super.onDismiss()
    }
}