package com.cboda.intelligent.dialog.pop

import android.content.Context
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import androidx.core.view.isGone
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.ViewModelProvider
import com.cboda.baselibrary.R
import com.cboda.baselibrary.bean.LoginBean
import com.cboda.baselibrary.bean.LoginIdBean
import com.cboda.baselibrary.popup.BaseBottomBroadPop
import com.cboda.baselibrary.utils.DeviceBroadUtil
import com.cboda.baselibrary.utils.HttpsBroadConfig
import com.cboda.baselibrary.utils.anyToInt
import com.cboda.baselibrary.utils.click
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.getAppColor
import com.cboda.baselibrary.utils.setTxt
import com.cboda.intelligent.bean.ResultBroadBean
import com.cboda.intelligent.configuration.SafeJumpBroadEnum
import com.cboda.intelligent.databinding.SecurityVerificationPopupBinding
import com.cboda.intelligent.utils.AppBroadUtils
import com.cboda.intelligent.utils.OkHttpBroadHelper
import com.cboda.intelligent.utils.SpotsCallBroadBack
import com.cboda.intelligent.utils.showToastMain
import com.cboda.intelligent.viewmodel.LoginViewBroadModel
import okhttp3.Response
import java.lang.Exception

class SecurityVerificationPopup(val context: Context, val loginBean: LoginBean) : BaseBottomBroadPop(context) {
    private val tAG = "SecurityVerificationPopupClass"
    private val binding = SecurityVerificationPopupBinding.inflate(LayoutInflater.from(context))
    private var viewModel: LoginViewBroadModel? = null

    private val loginIdBean = LoginIdBean()

    init {
        contentView = binding.root
        setBackgroundColor(getAppColor(context, R.color.transparent))
    }

    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
        binding.securityVerificationTitle.setTxt(if (loginBean.secondVerifyType == 2) R.string.choose_yz else R.string.choose_all_yz)
        binding.group1.isGone = !loginBean.twoFactorCode
        binding.group2.isGone = !loginBean.existPayPassword
        binding.group3.isGone = !loginBean.existMbName
        loginIdBean.linkId = loginBean.linkId
        binding.securityVerificationClose.click {
            dismiss()
        }
        binding.view1.click {
            val tipPop = AuthCodeBroadPop(
                context,
                SafeJumpBroadEnum.OnlyReturn.sourceType,
                isDeepColor = true
            )
            tipPop.backInvokeStr = { googleCode ->
//                eLogD(tAG, "googleCode:$googleCode")
                tipPop.dismiss()
                postIdLogin(googleCode = googleCode)
            }
            tipPop.showPopupWindow()
        }
        binding.view2.click {
            val tipPop = AuthPasswordBroadPop(
                context,
                SafeJumpBroadEnum.OnlyReturn.sourceType,
                isDeepColor = true
            )
            tipPop.backPwdInvoke = { payPwd ->
//                eLogD(tAG, "payPwd:$payPwd")
                tipPop.dismiss()
                postIdLogin(payPwd = payPwd)
            }
            tipPop.showPopupWindow()
        }
        binding.view3.click {
            val tipPop = AuthQuestionBroadPop(context, SafeJumpBroadEnum.OnlyReturn.sourceType, true, securityQuestion = loginBean.securityQuestion)
            tipPop.backInvokeStr = { securityQuestion ->
//                eLogD(tAG, "securityQuestion:$securityQuestion")
                tipPop.dismiss()
                postIdLogin(securityQuestion = securityQuestion)
            }
            tipPop.showPopupWindow()
        }
    }


    private fun postIdLogin(googleCode: String? = null, payPwd: String? = null, securityQuestion: String? = null) {
        if (!TextUtils.isEmpty(loginBean.linkId)) {
            val params = hashMapOf("id" to loginBean.linkId, "type" to 2)
            params["clientId"] = DeviceBroadUtil.getAndroidId(context)
            if (!TextUtils.isEmpty(googleCode)) {
                params["googleCode"] = googleCode ?: ""
            }
//        if (!TextUtils.isEmpty(loginIdBean.oldDevCode)) {
//            params["oldDeviceCode"] = loginIdBean.oldDevCode
//        }
            if (!TextUtils.isEmpty(payPwd)) {
                params["paywd"] = payPwd ?: ""
            }
            if (!TextUtils.isEmpty(securityQuestion)) {
                params["mbda"] = securityQuestion ?: ""
            }
            OkHttpBroadHelper.getInstance().post_json(context, HttpsBroadConfig.appBaseUrl + "/idLogin", params.toMap(), object : SpotsCallBroadBack<ResultBroadBean>(context) {
                override fun onSuccess(response: Response?, resultBean: ResultBroadBean?) {
//                    eLogD(tAG, "resultBean:${toJson2(resultBean)}")
                    checkResultBroadBean(googleCode, payPwd, securityQuestion, resultBean)
                }

                override fun onError(response: Response?, code: Int, e: Exception?) {
                    eLogD(tAG, "onError:$code")
                }
            })
        } else {
            if (null == viewModel && context is FragmentActivity) {
                viewModel = ViewModelProvider(context)[LoginViewBroadModel::class.java]
            }
            viewModel?.pwdLogin2(loginBean.loginAccount, loginBean.loginPwd, "", payPwd, googleCode, securityQuestion) {
                checkResultBroadBean(googleCode, payPwd, securityQuestion, it)
            }
        }

    }


    private fun checkResultBroadBean(googleCode: String? = null, payPwd: String? = null, securityQuestion: String? = null, resultBean: ResultBroadBean?) {
        if (null != resultBean && !TextUtils.isEmpty(resultBean.apptoken) && ((!HttpsBroadConfig.useSelfImOnly && !TextUtils.isEmpty(resultBean.imtoken)) || HttpsBroadConfig.useSelfImOnly) && !TextUtils.isEmpty(resultBean.userId)) {
            AppBroadUtils.imLogin(
                context,
                resultBean.apptoken,
                resultBean.userId,
                resultBean.imtoken,
                "LoginVerificationActivity"
            )
        } else if (null != resultBean && resultBean.success && anyToInt(resultBean.code) == 200) {
            if (!TextUtils.isEmpty(googleCode)) {
                loginIdBean.googleCode = googleCode ?: ""
                binding.ivRight1.isVisible = true
            } else if (!TextUtils.isEmpty(payPwd)) {
                loginIdBean.payPwd = payPwd ?: ""
                binding.ivRight2.isVisible = true
            } else if (!TextUtils.isEmpty(securityQuestion)) {
                loginIdBean.securityQuestion = securityQuestion ?: ""
                binding.ivRight3.isVisible = true
            }
        } else if (null != resultBean && !TextUtils.isEmpty(resultBean.msg)) {
            showToastMain(resultBean.msg)
        } else {
            showToastMain(R.string.server_error)
        }
    }
}