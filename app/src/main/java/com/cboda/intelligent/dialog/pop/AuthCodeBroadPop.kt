package com.cboda.intelligent.dialog.pop

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.ViewModelProvider
import com.cboda.baselibrary.popup.BaseBottomBroadPop
import com.cboda.intelligent.R
import com.cboda.intelligent.configuration.SafeJumpBroadEnum
import com.cboda.intelligent.databinding.DialogPopAuthBroadCodeBinding
import com.cboda.intelligent.utils.isSubmitCanGo
import com.cboda.intelligent.utils.pasteToEditText
import com.cboda.intelligent.viewmodel.SafeCenterViewBroadModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch


//谷歌验证码验证pop
class AuthCodeBroadPop(val context: Context, val jumpSource: Int = SafeJumpBroadEnum.None.sourceType,val isDeepColor : Boolean=false,val account:String?=null,) :
    BaseBottomBroadPop(context) {
    private val scope = MainScope() // 创建主线程的协程作用域
    private var viewModel: SafeCenterViewBroadModel? = null

    var transferInvoke: () -> Unit = {}
    private var binding: DialogPopAuthBroadCodeBinding = DialogPopAuthBroadCodeBinding.inflate(
        LayoutInflater.from(context)
    )

    init {
        contentView = binding.root
        setBackgroundColor(Color.parseColor("#8f000000"))
    }

    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
        initDeepColor()
        initListener()
        initData()
    }

    private fun initDeepColor() {
        if (isDeepColor){
            binding.mViewTopBg.setBackgroundResource(R.drawable.bg_rect_deep_only_top_broad_18dp)
            binding.mIvClose.setImageResource(R.mipmap.ic_auth_broad_close2)
//            binding.mIvGo.imageTintList = ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white))
            binding.mTvTitle.setTextColor(ContextCompat.getColor(context,R.color.white))
            binding.mTvDes2.setTextColor(ContextCompat.getColor(context,R.color.white))
            binding.mLlGoogleAuthCopy.setBackgroundResource(R.drawable.bg_rect_save_deep_edit_broad_16dp)
            binding.mEtCode.setTextColor(ContextCompat.getColor(context,R.color.white))
            binding.mEtCode.setHintTextColor(ContextCompat.getColor(context, com.cboda.baselibrary.R.color.color_629CF8_35))
            binding.mTvCopy.setTextColor(ContextCompat.getColor(context, com.cboda.baselibrary.R.color.color_C83FF5))

        }
    }

    private fun initListener() {
        binding.mIvClose.setOnClickListener {
            dismiss()
        }
        binding.mTvCopy.setOnClickListener {
            pasteToEditText(binding.mEtCode)
        }
        binding.mTvNext.isSubmitCanGo(false)
        binding.mEtCode.addTextChangedListener { editable ->
            binding.mTvNext.isSubmitCanGo(binding.mEtCode.text.trim().toString().length==6)
        }
    }

    private fun initData() {
        binding.mTvNext.setOnClickListener {
            if (context is AppCompatActivity && binding.mEtCode.text.trim().isNotEmpty()) {
                val activity = context
                // 获取该 Activity 作用域的 ViewModel
                viewModel =
                    ViewModelProvider(activity).get<SafeCenterViewBroadModel>(SafeCenterViewBroadModel::class.java)
                // 现在可以使用 viewModel 进行数据操作
                scope.launch {
                    val type = if (jumpSource == SafeJumpBroadEnum.TransferAuthSure.sourceType) {
                        1
                    } else if (jumpSource == SafeJumpBroadEnum.LoginForgetPassword.sourceType)
                        2
                    else {
                        0
                    }
                    viewModel?.goValidateTwoFactor(binding.mEtCode.text.trim().toString(), type,account)
                        ?.observe(activity) {
                            dismiss()
                            when (jumpSource) {
                                SafeJumpBroadEnum.GoogleAuth.sourceType -> {
                                    transferInvoke.invoke()
                                }

                                SafeJumpBroadEnum.PasswordAuth.sourceType -> {
                                    transferInvoke.invoke()

                                }

                                SafeJumpBroadEnum.QuestionAuth.sourceType -> {
                                    transferInvoke.invoke()
                                }

                                SafeJumpBroadEnum.TransferAuthSure.sourceType -> {
                                    transferInvoke.invoke()
                                }
                                SafeJumpBroadEnum.ChangeMachineAuth.sourceType -> {
                                    transferInvoke.invoke()
                                }
                                SafeJumpBroadEnum.LoginNewMachine.sourceType -> {
                                    transferInvoke.invoke()
                                }
                                SafeJumpBroadEnum.LoginForgetPassword.sourceType -> {
                                    transferInvoke.invoke()
                                }
                            }
                        }
                }
            }
        }
    }

    override fun onDismiss() {
        scope.cancel()
        super.onDismiss()
    }
}