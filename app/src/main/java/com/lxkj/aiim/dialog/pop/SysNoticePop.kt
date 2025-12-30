package com.lxkj.aiim.dialog.pop

import android.content.Context
import android.content.Intent
import android.text.TextUtils
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.ViewModelProvider
import com.lxkj.aiim.R
import com.lxkj.aiim.databinding.DialogPopSysNoticeBinding
import com.lxkj.aiim.lj.NoticeDetailActivity
import com.lxkj.aiim.lj.utils.GlideUtil
import com.lxkj.aiim.model.NoticeInfoBean
import com.lxkj.aiim.viewmodel.NoticeViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import razerdp.basepopup.BasePopupWindow

//公告提示pop
class SysNoticePop(val context: Context, val bean: NoticeInfoBean) :
    BasePopupWindow(context) {
    private val scope = MainScope() // 创建主线程的协程作用域
    private var viewModel: NoticeViewModel? = null
//    var sureInvoke: () -> Unit = {}
    private var binding = DialogPopSysNoticeBinding.inflate(
        LayoutInflater.from(context)
    )

    init {
        contentView = binding.root
        popupGravity = Gravity.CENTER

    }

    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
        binding.mIvNoticeIcon.isVisible = (bean.imageUrl?.isNotEmpty() == true)
        if (bean.imageUrl?.isNotEmpty() == true) {
            binding.mTvNoticeDes.setLines(2)
            binding.mTvNoticeDes.ellipsize = TextUtils.TruncateAt.END
            GlideUtil.setImag(context, bean.imageUrl, binding.mIvNoticeIcon)
        }else{
            binding.mTvNoticeDes.setLines(6)
            binding.mTvNoticeDes.ellipsize = TextUtils.TruncateAt.END
        }
        binding.mTvNoticeTitle.text =bean.noticeTitle
        binding.mTvNoticeDes.text =bean.previewContent

        binding.mTvSure.setOnClickListener {
//            sureInvoke.invoke()
            dismiss()
            context.startActivity(Intent(
                context,
                NoticeDetailActivity::class.java
            ).apply {
                putExtra("data", bean)
            })


        }
        binding.mViewClose.setOnClickListener {
            dismiss()
        }
        initData()

    }

    private fun initData() {
        if (context is AppCompatActivity) {
            val activity = context
            viewModel =
                ViewModelProvider(activity).get<NoticeViewModel>(NoticeViewModel::class.java)
            scope.launch {
                viewModel?.goNoticeRead(bean.id)
            }
        }

    }


    override fun onDismiss() {
        scope.cancel()
        super.onDismiss()
    }
}