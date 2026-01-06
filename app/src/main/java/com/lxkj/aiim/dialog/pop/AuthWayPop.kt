package com.lxkj.aiim.dialog.pop

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.cboda.baselibrary.popup.BaseBottomBroadPop
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.setLeftImgAndTextColor
import com.cboda.intelligent.databinding.DialogPopAuthWayBinding
import com.lxkj.aiim.lj.GoogleAuthStep1Activity
import com.lxkj.aiim.lj.SecurityAuthEditActivity
import com.lxkj.aiim.lj.configuration.SafeJumpEnum
import com.lxkj.aiim.model.SafeCenterBean
import com.lxkj.aiim.viewmodel.SafeCenterViewModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import com.cboda.intelligent.R


//认证方式选择
class AuthWayPop(
    val context: Context,
    val jumpSource: Int = SafeJumpEnum.None.sourceType,
    val isDeepColor: Boolean = false,
    val outSafeCenterBean:SafeCenterBean?=null,
    val account:String?=null,
) :
    BaseBottomBroadPop(context) {
    private val scope = MainScope() // 创建主线程的协程作用域
    private var viewModel: SafeCenterViewModel? = null

    @JvmField
    var transferInvoke: () -> Unit = {}
    var safeCenterBean: SafeCenterBean? = null

    private var binding: DialogPopAuthWayBinding = DialogPopAuthWayBinding.inflate(
        LayoutInflater.from(context)
    )

    init {
        contentView = binding.root
        setBackgroundColor(Color.parseColor("#8f000000"))

    }

    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
//        eLogD("zhanglong","context===="+context)
        if (outSafeCenterBean!=null){
            safeCenterBean = outSafeCenterBean
            if (jumpSource == SafeJumpEnum.LoginForgetPassword.sourceType) {
                safeCenterBean?.let {
                    binding.mLlGoogleAuth.isVisible = !it.twoFactorCode.isNullOrEmpty()
                    binding.mLlPassWord.isVisible = !it.payPassword.isNullOrEmpty()
                    binding.mLlSecurity.isVisible = !it.securityId.isNullOrEmpty()

                    binding.mViewAddGoogle.isVisible = it.twoFactorCode.isNullOrEmpty()
                    binding.mViewGoogle.isVisible = !it.twoFactorCode.isNullOrEmpty()
                    binding.mViewAddPassword.isVisible = it.payPassword.isNullOrEmpty()
                    binding.mViewPassword.isVisible = !it.payPassword.isNullOrEmpty()
                    binding.mViewAddSecurity.isVisible = it.securityId.isNullOrEmpty()
                    binding.mViewSecurity.isVisible = !it.securityId.isNullOrEmpty()
                }
            }

        }else if (context is FragmentActivity) {
            val activity = context
            // 获取该 Activity 作用域的 ViewModel
            viewModel =
                ViewModelProvider(activity).get<SafeCenterViewModel>(SafeCenterViewModel::class.java)
            // 现在可以使用 viewModel 进行数据操作
            scope.launch {
                viewModel?.goSecurityCenter()?.observe(activity) {
                    safeCenterBean = it
                    if (jumpSource == SafeJumpEnum.LoginNewMachine.sourceType) {
                        //如果调用来源是新设备且安全中心相关保护什么都没设置，直接放行
                        if (it.twoFactorCode.isNullOrEmpty() && it.payPassword.isNullOrEmpty() && it.securityId.isNullOrEmpty()) {
                            dismiss(false)
                            transferInvoke.invoke()
                        } else {
                            binding.mLlGoogleAuth.isVisible = !it.twoFactorCode.isNullOrEmpty()
                            binding.mLlPassWord.isVisible = !it.payPassword.isNullOrEmpty()
                            binding.mLlSecurity.isVisible = !it.securityId.isNullOrEmpty()

                            binding.mViewAddGoogle.isVisible = it.twoFactorCode.isNullOrEmpty()
                            binding.mViewGoogle.isVisible = !it.twoFactorCode.isNullOrEmpty()
                            binding.mViewAddPassword.isVisible = it.payPassword.isNullOrEmpty()
                            binding.mViewPassword.isVisible = !it.payPassword.isNullOrEmpty()
                            binding.mViewAddSecurity.isVisible = it.securityId.isNullOrEmpty()
                            binding.mViewSecurity.isVisible = !it.securityId.isNullOrEmpty()
                        }

                    } else {
                        binding.mViewAddGoogle.isVisible = it.twoFactorCode.isNullOrEmpty()
                        binding.mViewGoogle.isVisible = !it.twoFactorCode.isNullOrEmpty()
                        if (jumpSource == SafeJumpEnum.ChangeMachineAuth.sourceType) {
                            binding.mLlPassWord.isVisible = true
                            binding.mViewAddPassword.isVisible = it.payPassword.isNullOrEmpty()
                            binding.mViewPassword.isVisible = !it.payPassword.isNullOrEmpty()
                        }
                        binding.mViewAddSecurity.isVisible = it.securityId.isNullOrEmpty()
                        binding.mViewSecurity.isVisible = !it.securityId.isNullOrEmpty()
                    }
                }
            }

        }
        binding.mIvClose.setOnClickListener {
            dismiss()
        }
        initDeepColor()
        initData()
        initListener()
    }

    private fun initDeepColor() {
        if (isDeepColor) {
            binding.mViewTopBg.setBackgroundResource(R.drawable.bg_rect_deep_only_top_18dp)
            binding.mIvClose.setImageResource(R.mipmap.ic_auth_close2)
            binding.mIvGo.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white))
            binding.mTvTitle.setTextColor(ContextCompat.getColor(context, R.color.white))
            binding.mLlGoogleAuth.setBackgroundResource(R.drawable.bg_rect_save_deep_16dp)
            binding.mLlPassWord.setBackgroundResource(R.drawable.bg_rect_save_deep_16dp)
            binding.mLlSecurity.setBackgroundResource(R.drawable.bg_rect_save_deep_16dp)

            binding.mTvGoogleText.setLeftImgAndTextColor(R.mipmap.ic_google_auth2, R.color.white)
            binding.mTvPasswordText.setLeftImgAndTextColor(
                R.mipmap.ic_pay_password_auth2,
                R.color.white
            )
            binding.mTvSecurityText.setLeftImgAndTextColor(
                R.mipmap.ic_security_auth2,
                R.color.white
            )
        }
    }

    private fun initData() {
//        if (jumpSource ==SafeJumpEnum.TransferAuthSure.sourceType){
//            binding.mLlPassWord.isVisible=false
//        }

    }

    private fun initListener() {
        binding.mLlGoogleAuth.setOnClickListener {
            dismiss()
            if (binding.mViewAddGoogle.isVisible) {
                //去添加
                safeCenterBean?.let {
                    if (it.securityId?.isNotEmpty() == true) {
                        val authQuestionPop = AuthQuestionPop(
                            context,
                            SafeJumpEnum.GoogleAuth.sourceType
                        )
                        authQuestionPop.backInvoke = {
                            val mPop = AuthPasswordPop(
                                context,
                                SafeJumpEnum.GoogleAuth.sourceType
                            )
                            mPop.backInvoke = {
                                context.startActivity(
                                    Intent(context, GoogleAuthStep1Activity::class.java)
                                )
                            }
                            mPop.showPopupWindow()
                        }
                        authQuestionPop.showPopupWindow()

                    } else {
                        val passwordPop =
                            AuthPasswordPop(context, SafeJumpEnum.GoogleAuth.sourceType)
                        passwordPop.backInvoke = {
                            val intent = Intent(context, GoogleAuthStep1Activity::class.java)
                            context.startActivity(intent)
                        }
                        passwordPop.showPopupWindow()
                    }
                }

            } else {
                val tipPop = AuthCodePop(
                    context,
                    jumpSource,
                    jumpSource == SafeJumpEnum.LoginNewMachine.sourceType || jumpSource == SafeJumpEnum.LoginForgetPassword.sourceType,
                    account =account
                )
                if (jumpSource == SafeJumpEnum.TransferAuthSure.sourceType || jumpSource == SafeJumpEnum.ChangeMachineAuth.sourceType || jumpSource == SafeJumpEnum.LoginNewMachine.sourceType || jumpSource == SafeJumpEnum.LoginForgetPassword.sourceType) {
                    tipPop.transferInvoke = transferInvoke
                }
                tipPop.showPopupWindow()
            }

        }

        binding.mLlPassWord.setOnClickListener {
            dismiss()
            if (binding.mViewAddPassword.isGone) {
                val tipPop = AuthPasswordPop(
                    context,
                    jumpSource,
                    isDeepColor = (jumpSource == SafeJumpEnum.LoginNewMachine.sourceType || jumpSource == SafeJumpEnum.LoginForgetPassword.sourceType),
                    account = account
                )
                tipPop.backInvoke = transferInvoke
                tipPop.showPopupWindow()
            }

        }
        binding.mLlSecurity.setOnClickListener {
            dismiss()
            if (binding.mViewAddSecurity.isVisible) {
                safeCenterBean?.let {
                    if (it.twoFactorCode?.isNotEmpty() == true) {
                        val codePop = AuthCodePop(
                            context,
                            SafeJumpEnum.QuestionAuth.sourceType
                        )
                        codePop.transferInvoke = {
                            val mPop = AuthPasswordPop(
                                context,
                                SafeJumpEnum.QuestionAuth.sourceType
                            )
                            mPop.backInvoke = {
                                context.startActivity(
                                    Intent(
                                        context,
                                        SecurityAuthEditActivity::class.java
                                    )
                                )
                            }
                            mPop.showPopupWindow()

                        }
                        codePop.showPopupWindow()

                    } else {
                        val mPop = AuthPasswordPop(
                            context,
                            SafeJumpEnum.QuestionAuth.sourceType
                        )
                        mPop.backInvoke = {
                            context.startActivity(
                                Intent(
                                    context,
                                    SecurityAuthEditActivity::class.java
                                )
                            )
                        }
                        mPop.showPopupWindow()
                    }
                }
            } else {
                val tipPop = AuthQuestionPop(
                    context,
                    jumpSource,
                    jumpSource == SafeJumpEnum.LoginNewMachine.sourceType || jumpSource == SafeJumpEnum.LoginForgetPassword.sourceType,
                    account = account
                )
                tipPop.backInvoke = transferInvoke
                tipPop.showPopupWindow()
            }

        }
    }

    override fun onDismiss() {
        scope.cancel()
        super.onDismiss()
    }

}