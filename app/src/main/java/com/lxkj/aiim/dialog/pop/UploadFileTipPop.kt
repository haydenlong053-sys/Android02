package com.lxkj.aiim.dialog.pop

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.animation.Animation
import android.view.animation.OvershootInterpolator
import android.view.animation.ScaleAnimation
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.lifecycle.ViewModelProvider
import com.cboda.baselibrary.utils.setTxt
import com.cboda.intelligent.BuildConfig
import com.cboda.intelligent.R
import com.cboda.intelligent.databinding.ActivityAuthStep1Binding
import com.cboda.intelligent.databinding.DialogPopCommonTipBinding
import com.cboda.intelligent.databinding.DialogPopFileUploadTipBinding
import com.cboda.intelligent.databinding.DialogPopSaveAuthBinding
import com.cboda.intelligent.databinding.DialogPopSureTransferBinding
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
class UploadFileTipPop(val context: Context) :
    BasePopupWindow(context) {
    //    private val scope = MainScope() // 创建主线程的协程作用域
//    private var viewModel: TransferViewModel? = null
    var sureInvoke: () -> Unit = {}

    //添加取消回調
    var cancelInvoke: () -> Unit = {}
    private var binding = DialogPopFileUploadTipBinding.inflate(
        LayoutInflater.from(context)
    )

    init {
        contentView = binding.root
        popupGravity = Gravity.CENTER

    }

    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
        this.setOnDismissListener(onDismissListener)
        binding.mIvClose.setOnClickListener {
            dismiss()
        }
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

    fun setContentTxt(strName: Int): BasePopupWindow {
        binding.mTvDes.setTxt(strName)
        return this
    }

    fun setSureBtn(strName: Int): BasePopupWindow {
        binding.mTvSure.setTxt(strName)
        return this
    }

    fun setCancelBtn(strName: Int): BasePopupWindow {
        binding.mTvCancel.setTxt(strName)
        return this
    }

    private fun initData() {
//        if (context is AppCompatActivity) {
//            val activity = context
//            viewModel =
//                ViewModelProvider(activity).get<TransferViewModel>(TransferViewModel::class.java)
//        }

    }

    var dismissInvoke: () -> Unit = {}
    private val onDismissListener = object : OnDismissListener() {
        override fun onDismiss() {
            dismissInvoke.invoke()
        }
    }

    override fun onDismiss() {
//        scope.cancel()
        super.onDismiss()
    }


    /**
     * 创建显示动画：从小到大的弹性缩放动画
     */
    override fun onCreateShowAnimation(): Animation {
        val scaleAnimation = ScaleAnimation(
            0.3f, 1.0f,  // fromXScale, toXScale
            0.3f, 1.0f,  // fromYScale, toYScale
            Animation.RELATIVE_TO_SELF, 0.5f,  // pivotXType, pivotXValue
            Animation.RELATIVE_TO_SELF, 0.5f   // pivotYType, pivotYValue
        ).apply {
            duration = 400
            interpolator = OvershootInterpolator(1.5f) // 弹性插值器，1.5f 控制弹性强度
        }

        val alphaAnimation = android.view.animation.AlphaAnimation(0.0f, 1.0f).apply {
            duration = 300
        }

        val animationSet = android.view.animation.AnimationSet(true).apply {
            addAnimation(scaleAnimation)
            addAnimation(alphaAnimation)
        }

        return animationSet
    }

    /**
     * 创建关闭动画：从大到小的缩放动画
     */
    override fun onCreateDismissAnimation(): Animation {
        val scaleAnimation = ScaleAnimation(
            1.0f, 0.3f,  // fromXScale, toXScale
            1.0f, 0.3f,  // fromYScale, toYScale
            Animation.RELATIVE_TO_SELF, 0.5f,  // pivotXType, pivotXValue
            Animation.RELATIVE_TO_SELF, 0.5f   // pivotYType, pivotYValue
        ).apply {
            duration = 300
            interpolator = android.view.animation.AccelerateInterpolator()
        }
        val alphaAnimation = android.view.animation.AlphaAnimation(1.0f, 0.0f).apply {
            duration = 250
        }
        val animationSet = android.view.animation.AnimationSet(true).apply {
            addAnimation(scaleAnimation)
            addAnimation(alphaAnimation)
        }
        return animationSet
    }
}