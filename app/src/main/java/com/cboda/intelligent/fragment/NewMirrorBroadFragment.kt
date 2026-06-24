package com.cboda.intelligent.fragment

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.lifecycle.lifecycleScope
import com.cboda.baselibrary.bean.PayLsOrderBean
import com.cboda.baselibrary.bean.UserInfoBean
import com.cboda.baselibrary.toast.ToastBroadUtils
import com.cboda.baselibrary.utils.OnSingleClickBroadListener
import com.cboda.baselibrary.utils.anyToInt
import com.cboda.baselibrary.utils.clipViewOutlineRound
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.formatWithBigDecimal
import com.cboda.baselibrary.utils.setBackColor
import com.cboda.baselibrary.utils.setBackColorAndStrokeColor
import com.cboda.baselibrary.utils.setTxtColor
import com.cboda.baselibrary.utils.getAppContext
import com.cboda.baselibrary.utils.setBackColor2
import com.cboda.baselibrary.utils.toJson2
import com.cboda.baselibrary.utils.updatePayPwdDataLiveData
import com.cboda.intelligent.R
import com.cboda.intelligent.activity.QianbaoBroadActivity
import com.cboda.intelligent.activity.SignInBroadActivity
import com.cboda.intelligent.activity.UserInfoBroadActivity
import com.cboda.intelligent.base.BaseBroadFragment
import com.cboda.intelligent.databinding.NewMirrorBroadFragmentBinding
import com.cboda.intelligent.dialog.pop.CreditScoreActivateBroadPop
import com.cboda.intelligent.dialog.pop.PasswordPricedPop
import com.cboda.intelligent.dialog.pop.UserQrCodeTipBroadPop
import com.cboda.baselibrary.bean.UserInfoBroadModel
import com.cboda.baselibrary.utils.HttpsBroadConfig
import com.cboda.baselibrary.utils.callImTokenViaDeepLink
import com.cboda.baselibrary.utils.callMetaMaskWithEIP681
import com.cboda.baselibrary.utils.openH5InImToken
import com.cboda.baselibrary.utils.openH5InMetaMask
import com.cboda.baselibrary.utils.openH5InTokenPocket
import com.cboda.intelligent.bean.PaymentMethodBroadBean
import com.cboda.intelligent.bean.PaymentMethodDefaults.IM_TOKEN
import com.cboda.intelligent.bean.PaymentMethodDefaults.META_MASK
import com.cboda.intelligent.bean.PaymentMethodDefaults.TP
import com.cboda.intelligent.dialog.pop.PaymentMethodPopup
import com.cboda.intelligent.utils.JumpBroadUtils
import com.cboda.intelligent.utils.click
import com.cboda.intelligent.utils.showGuidePasswordTip
import com.cboda.intelligent.utils.showToastMain
import com.cboda.intelligent.viewmodel.NewMirrorViewBroadModel
import kotlinx.coroutines.launch

class NewMirrorBroadFragment : BaseBroadFragment<NewMirrorBroadFragmentBinding, NewMirrorViewBroadModel>() {
    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ) = NewMirrorBroadFragmentBinding.inflate(inflater, parent, false)

    private var userInfo: UserInfoBean? = null
    private var payLsOrderBean: PayLsOrderBean? = null
    override fun initListener() {
        super.initListener()
        clipViewOutlineRound(binding.mViewPremiumNumber, 20)
        binding.ivMirrorSign.click {
            viewModel.startAct<SignInBroadActivity>()
        }
        binding.qrCodeLayout.click {
            UserQrCodeTipBroadPop(requireContext(), userInfo).showPopupWindow()
        }
        binding.ivMirrorAvatar.setOnClickListener(userInfoClick)
        binding.layoutMirrorCredit.click {
            if (!HttpsBroadConfig.isReviewEnv) {
                //信用分状态 1、已开通 0:未开通
                if (anyToInt(userInfo?.gradeStatus) == 1) {
                    JumpBroadUtils.startCreditScoreActivity(requireContext())
                } else {
//                    CreditScoreActivateBroadPop(requireContext()).apply {
//                        sureInvoke = {
//                            if (anyToInt(userInfo?.hasPayPwd) == 1) {
//                                PasswordPricedPop(context = requireContext(), priceStr = userInfo?.gradePrice ?: "", backInvoke = { password ->
//                                    lifecycleScope.launch {
//                                        viewModel.goOpenCredit(password).observe(viewLifecycleOwner) { bool ->
//                                            if (bool) {
//                                                showToastMain(com.cboda.baselibrary.R.string.activation_successful)
//                                                onResume()
//                                            }
//                                        }
//                                    }
//
//
//                                }, isCheckPassword = false).showPopupWindow()
//                            } else {
//                                showGuidePasswordTip(context)
//                            }
//                        }
//                    }.showPopupWindow()
                    if (payLsOrderBean == null) {
                        viewModel.createPayOrder("credit_activate").observe(viewLifecycleOwner) { data ->
                            eLogD("payOrderData", toJson2(data))
                            payLsOrderBean = data
                            val payPopup = PaymentMethodPopup(requireContext())
                            payPopup.setOnSelected {
                                openData(data, it)
                                payPopup.dismiss()
                            }
                            payPopup.showPopupWindow()
                        }
                    } else {
                        val payPopup = PaymentMethodPopup(requireContext())
                        payPopup.setOnSelected {
                            openData(payLsOrderBean, it)
                            payPopup.dismiss()
                        }
                        payPopup.showPopupWindow()
                    }
                }
            }
        }
        binding.idTv.setOnClickListener(userInfoClick)
        binding.vocationalTv.setOnClickListener(userInfoClick)
        binding.daysTv.setOnClickListener(userInfoClick)
        binding.hobbyTv1.setOnClickListener(userInfoClick)
        binding.hobbyTv2.setOnClickListener(userInfoClick)
        binding.hobbyTv3.setOnClickListener(userInfoClick)
        binding.hobbyTv4.setOnClickListener(userInfoClick)
        binding.hobbyTv5.setOnClickListener(userInfoClick)
        binding.hobbyTv6.setOnClickListener(userInfoClick)
        updatePayPwdDataLiveData.observe(this) {
            getUserInfo()
        }
    }

    private fun openData(data: PayLsOrderBean?, paymentMethod: PaymentMethodBroadBean) {
        if (data == null) {
            ToastBroadUtils.show("订单数据为空")
            return
        }
//        val tokenSymbol = data.tokenSymbol.ifBlank { "ODIC" }
        val chainId = anyToInt(data.chainId)
        val h5Url = "https://pay.web4x.io/pay?amountDisplay=${data.amountDisplay}" +
                "&chainId=$chainId" +
                "&orderId=${data.orderId}" +
                "&tokenSymbol=ODIC&paymentType=2"
        eLogD("payOrderData_h5Url", h5Url)
        when (paymentMethod.id) {
            IM_TOKEN -> openH5InImToken(h5Url, requireContext())
            TP -> openH5InTokenPocket(h5Url, requireContext(), chainId)
            META_MASK -> openH5InMetaMask(h5Url, requireContext())
        }
    }

    private val userInfoClick = object : OnSingleClickBroadListener() {
        override fun onSingleClick(v: View?) {
            viewModel.startAct<UserInfoBroadActivity>()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        if (binding.mViewPremiumNumber.isVisible && !binding.mViewAnimateNumb.isAnimating) {
            binding.mViewAnimateNumb.playAnimation()
        }
        viewModel.getSignInfo().observe(this) {
            binding.mTvMirrorSign.text = if (it) getString(R.string.已签到) else getString(R.string.签到)
        }
        getUserInfo()
    }

    @SuppressLint("SetTextI18n")
    private fun getUserInfo() {
        viewModel.getUserInfo().observe(this) {
            it?.let {
                userInfo = it
            }
//            eLogD("NewMirrorViewBroadModelClass", "userInfo:${toJson2(userInfo)}")
            binding.ivMirrorAvatar.setImageResource(if (userInfo?.sex == "2") R.drawable.ic_mirror_broad_female else R.drawable.ic_mirror_broad_male)
            binding.tvMirrorCreditScore.text = formatWithBigDecimal(userInfo?.grade)
            binding.mViewPremiumNumber.isVisible = (userInfo?.isLiang == "1")
            binding.idTv.isInvisible = binding.mViewPremiumNumber.isVisible
            binding.vipMirrorCredit.isVisible = userInfo?.isVip == "1"
            val registrationDay = anyToInt(userInfo?.registerDayBean?.registrationDays ?: 0)
            if (userInfo?.isLiang == "1") {
                binding.layoutMirrorCredit.setBackColor(com.cboda.baselibrary.R.color.black, com.cboda.baselibrary.R.color.color_31231A, com.cboda.baselibrary.R.color.black)
                binding.tvMirrorCreditScore.setTxtColor(com.cboda.baselibrary.R.color.color_FFCA69)
                binding.tvMirrorCreditScoreValue.setTxtColor(com.cboda.baselibrary.R.color.color_FFCA69)
                binding.tvMirrorCreditLabel.setTxtColor(com.cboda.baselibrary.R.color.color_FFCA69)
            } else {
                val registrationDayScore = anyToInt(userInfo?.registerDayBean?.registrationDayScore ?: "")
                if (registrationDay < registrationDayScore) {
                    binding.layoutMirrorCredit.setBackColor(com.cboda.baselibrary.R.color.color_2DFF0000)
                    binding.tvMirrorCreditScore.setTxtColor(com.cboda.baselibrary.R.color.color_D5271E)
                    binding.tvMirrorCreditScoreValue.setTxtColor(com.cboda.baselibrary.R.color.color_D5271E)
                    binding.tvMirrorCreditLabel.setTxtColor(com.cboda.baselibrary.R.color.color_D5271E)
                } else {
                    binding.layoutMirrorCredit.setBackColor(com.cboda.baselibrary.R.color.color_4F6FF3_20)
                    binding.tvMirrorCreditScore.setTxtColor(com.cboda.baselibrary.R.color.color_031256)
                    binding.tvMirrorCreditScoreValue.setTxtColor(com.cboda.baselibrary.R.color.color_031256)
                    binding.tvMirrorCreditLabel.setTxtColor(com.cboda.baselibrary.R.color.color_031256)
                }
            }
            if (userInfo?.isLiang == "1") {
                binding.mTvPremiumNumber.text = "ID:${userInfo?.niceNumberId}"
                if (!binding.mViewAnimateNumb.isAnimating) {
                    binding.mViewAnimateNumb.playAnimation()
                }
            } else {
                binding.idTv.text = "ID:${if (!TextUtils.isEmpty(userInfo?.showId)) userInfo?.showId else userInfo?.realityId}"
                if (binding.mViewAnimateNumb.isAnimating) {
                    binding.mViewAnimateNumb.pauseAnimation()
                }
            }
            binding.vocationalTv.isGone = TextUtils.isEmpty(userInfo?.vocational)
            binding.vocationalTv.text = userInfo?.vocational
            val registerDay = userInfo?.registerDayBean?.registrationDays
            binding.daysTv.isGone = registerDay == null
            binding.daysTv.text = "${registrationDay}${getString(R.string.days_unit)}"
            val memberInterestList = userInfo?.memberInterestList ?: arrayOf()
            binding.hobbyTv1.isVisible = memberInterestList.isNotEmpty()
            binding.hobbyTv2.isVisible = memberInterestList.size > 1
            binding.hobbyTv3.isVisible = memberInterestList.size > 2
            binding.hobbyTv4.isVisible = memberInterestList.size > 3
            binding.hobbyTv5.isVisible = memberInterestList.size > 4
            binding.hobbyTv6.isVisible = memberInterestList.size > 5
            memberInterestList.forEachIndexed { index, hobby ->
                when (index) {
                    0 -> binding.hobbyTv1.text = hobby
                    1 -> binding.hobbyTv2.text = hobby
                    2 -> binding.hobbyTv3.text = hobby
                    3 -> binding.hobbyTv4.text = hobby
                    4 -> binding.hobbyTv5.text = hobby
                    5 -> binding.hobbyTv6.text = hobby
                }
            }

        }
    }

    override fun onPause() {
        super.onPause()
        if (binding.mViewAnimateNumb.isAnimating) {
            binding.mViewAnimateNumb.pauseAnimation()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        binding.mViewAnimateNumb.cancelAnimation()
    }
}