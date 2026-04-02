package com.cboda.intelligent.dialog.pop

import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.cboda.baselibrary.popup.BaseBottomBroadPop
import com.cboda.baselibrary.utils.setLeftImgAndTextColor
import com.cboda.intelligent.activity.GoogleAuthStep1BroadActivity
import com.cboda.intelligent.activity.SecurityAuthEditBroadActivity
import com.cboda.intelligent.configuration.SafeJumpBroadEnum
import com.cboda.intelligent.model.SafeCenterBroadBean
import com.cboda.intelligent.viewmodel.SafeCenterViewBroadModel
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import com.cboda.intelligent.R
import com.cboda.intelligent.databinding.DialogPopAuthBroadWayBinding


//认证方式选择
class AuthWayBroadPop(
    val context: Context,
    val jumpSource: Int = SafeJumpBroadEnum.None.sourceType,
    val isDeepColor: Boolean = false,
    val outSafeCenterBean:SafeCenterBroadBean?=null,
    val account:String?=null,
) :
    BaseBottomBroadPop(context) {
    private val scope = MainScope() // 创建主线程的协程作用域
    private var viewModel: SafeCenterViewBroadModel? = null

    @JvmField
    var transferInvoke: () -> Unit = {}
    var safeCenterBean: SafeCenterBroadBean? = null

    private var binding: DialogPopAuthBroadWayBinding = DialogPopAuthBroadWayBinding.inflate(
        LayoutInflater.from(context)
    )

    init {
        contentView = binding.root
        setBackgroundColor(Color.parseColor("#8f000000"))

    }

    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
//        eLogD(APP_TAG,"context===="+context)
        if (outSafeCenterBean!=null){
            safeCenterBean = outSafeCenterBean
            if (jumpSource == SafeJumpBroadEnum.LoginForgetPassword.sourceType) {
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
                ViewModelProvider(activity).get<SafeCenterViewBroadModel>(SafeCenterViewBroadModel::class.java)
            // 现在可以使用 viewModel 进行数据操作
            scope.launch {
                viewModel?.goSecurityCenter()?.observe(activity) {
                    safeCenterBean = it
                    if (jumpSource == SafeJumpBroadEnum.LoginNewMachine.sourceType) {
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
                        if (jumpSource == SafeJumpBroadEnum.ChangeMachineAuth.sourceType) {
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
            binding.mViewTopBg.setBackgroundResource(R.drawable.bg_rect_deep_only_top_broad_18dp)
            binding.mIvClose.setImageResource(R.mipmap.ic_auth_broad_close2)
            binding.mIvGo.imageTintList =
                ColorStateList.valueOf(ContextCompat.getColor(context, R.color.white))
            binding.mTvTitle.setTextColor(ContextCompat.getColor(context, R.color.white))
            binding.mLlGoogleAuth.setBackgroundResource(R.drawable.bg_rect_save_deep_broad_16dp)
            binding.mLlPassWord.setBackgroundResource(R.drawable.bg_rect_save_deep_broad_16dp)
            binding.mLlSecurity.setBackgroundResource(R.drawable.bg_rect_save_deep_broad_16dp)

            binding.mTvGoogleText.setLeftImgAndTextColor(R.mipmap.ic_google_broad_auth2, R.color.white)
            binding.mTvPasswordText.setLeftImgAndTextColor(
                R.mipmap.ic_pay_password_broad_auth2,
                R.color.white
            )
            binding.mTvSecurityText.setLeftImgAndTextColor(
                R.mipmap.ic_security_broad_auth2,
                R.color.white
            )
        }
    }

    private fun initData() {
//        if (jumpSource ==SafeJumpBroadEnum.TransferAuthSure.sourceType){
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
                        val authQuestionPop = AuthQuestionBroadPop(
                            context,
                            SafeJumpBroadEnum.GoogleAuth.sourceType
                        )
                        authQuestionPop.backInvoke = {
                            val mPop = AuthPasswordBroadPop(
                                context,
                                SafeJumpBroadEnum.GoogleAuth.sourceType
                            )
                            mPop.backInvoke = {
                                context.startActivity(
                                    Intent(context, GoogleAuthStep1BroadActivity::class.java)
                                )
                            }
                            mPop.showPopupWindow()
                        }
                        authQuestionPop.showPopupWindow()

                    } else {
                        val passwordPop =
                            AuthPasswordBroadPop(context, SafeJumpBroadEnum.GoogleAuth.sourceType)
                        passwordPop.backInvoke = {
                            val intent = Intent(context, GoogleAuthStep1BroadActivity::class.java)
                            context.startActivity(intent)
                        }
                        passwordPop.showPopupWindow()
                    }
                }

            } else {
                val tipPop = AuthCodeBroadPop(
                    context,
                    jumpSource,
                    jumpSource == SafeJumpBroadEnum.LoginNewMachine.sourceType || jumpSource == SafeJumpBroadEnum.LoginForgetPassword.sourceType,
                    account =account
                )
                if (jumpSource == SafeJumpBroadEnum.TransferAuthSure.sourceType || jumpSource == SafeJumpBroadEnum.ChangeMachineAuth.sourceType || jumpSource == SafeJumpBroadEnum.LoginNewMachine.sourceType || jumpSource == SafeJumpBroadEnum.LoginForgetPassword.sourceType
                    || jumpSource == SafeJumpBroadEnum.ChangeLoginPassword.sourceType) {
                    tipPop.transferInvoke = transferInvoke
                }
                tipPop.showPopupWindow()
            }

        }

        binding.mLlPassWord.setOnClickListener {
            dismiss()
            if (binding.mViewAddPassword.isGone) {
                val tipPop = AuthPasswordBroadPop(
                    context,
                    jumpSource,
                    isDeepColor = (jumpSource == SafeJumpBroadEnum.LoginNewMachine.sourceType || jumpSource == SafeJumpBroadEnum.LoginForgetPassword.sourceType),
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
                        val codePop = AuthCodeBroadPop(
                            context,
                            SafeJumpBroadEnum.QuestionAuth.sourceType
                        )
                        codePop.transferInvoke = {
                            val mPop = AuthPasswordBroadPop(
                                context,
                                SafeJumpBroadEnum.QuestionAuth.sourceType
                            )
                            mPop.backInvoke = {
                                context.startActivity(
                                    Intent(
                                        context,
                                        SecurityAuthEditBroadActivity::class.java
                                    )
                                )
                            }
                            mPop.showPopupWindow()

                        }
                        codePop.showPopupWindow()

                    } else {
                        val mPop = AuthPasswordBroadPop(
                            context,
                            SafeJumpBroadEnum.QuestionAuth.sourceType
                        )
                        mPop.backInvoke = {
                            context.startActivity(
                                Intent(
                                    context,
                                    SecurityAuthEditBroadActivity::class.java
                                )
                            )
                        }
                        mPop.showPopupWindow()
                    }
                }
            } else {
                val tipPop = AuthQuestionBroadPop(
                    context,
                    jumpSource,
                    jumpSource == SafeJumpBroadEnum.LoginNewMachine.sourceType || jumpSource == SafeJumpBroadEnum.LoginForgetPassword.sourceType,
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