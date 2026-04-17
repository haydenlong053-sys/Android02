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
import com.cboda.intelligent.model.UserInfoBroadModel
import com.cboda.intelligent.utils.JumpBroadUtils
import com.cboda.intelligent.utils.click
import com.cboda.intelligent.utils.showGuidePasswordTip
import com.cboda.intelligent.viewmodel.NewMirrorViewBroadModel
import kotlinx.coroutines.launch

class NewMirrorBroadFragment : BaseBroadFragment<NewMirrorBroadFragmentBinding, NewMirrorViewBroadModel>() {
    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ) = NewMirrorBroadFragmentBinding.inflate(inflater, parent, false)

    private var userInfo: UserInfoBroadModel? = null

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
            userInfo?.gradeStatus?.let {
                //信用分状态 1、已开通 0:未开通
                if (it == 1) {
                    JumpBroadUtils.startCreditScoreActivity(requireContext())
                } else {
                    CreditScoreActivateBroadPop(requireContext()).apply {
                        sureInvoke = {
                            if (userInfo?.hasPaypwd == "1") {
                                PasswordPricedPop(context = requireContext(), priceStr = userInfo?.gradePrice ?: "", backInvoke = { password ->
                                    lifecycleScope.launch {
                                        viewModel.goOpenCredit(password).observe(viewLifecycleOwner) { bool ->
                                            if (bool) {
                                                ToastBroadUtils.show("激活成功")
                                                onResume()
                                            }
                                        }
                                    }


                                }, isCheckPassword = false).showPopupWindow()
                            } else {
                                showGuidePasswordTip(context)
                            }
                        }
                    }.showPopupWindow()
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

    private fun getUserInfo() {
        viewModel.getUserInfo().observe(this) {
            it?.let {
                userInfo = it
            }
            eLogD("NewMirrorViewBroadModelClass", "userInfo:${toJson2(userInfo)}")
            binding.vipMirrorCredit.isVisible = userInfo?.isvip == "1"
            binding.ivMirrorAvatar.setImageResource(if (userInfo?.sex == "2") R.drawable.ic_mirror_broad_female else R.drawable.ic_mirror_broad_male)
            binding.tvMirrorCreditScore.text = formatWithBigDecimal(userInfo?.grade)
            binding.mViewPremiumNumber.isVisible = (userInfo?.isLiang == "1")
            binding.idTv.isInvisible = binding.mViewPremiumNumber.isVisible
            if (userInfo?.isLiang == "1") {
                binding.layoutMirrorCredit.setBackColorAndStrokeColor(R.color.black, R.color.black_or_white_65)
                binding.tvMirrorCreditScore.setTxtColor(com.cboda.baselibrary.R.color.color_FFCA69)
                binding.tvMirrorCreditScoreValue.setTxtColor(com.cboda.baselibrary.R.color.color_FFCA69)
                binding.tvMirrorCreditLabel.setTxtColor(com.cboda.baselibrary.R.color.color_FFCA69)
                binding.mTvPremiumNumber.text = "ID:${userInfo?.lianghao}"
                if (!binding.mViewAnimateNumb.isAnimating) {
                    binding.mViewAnimateNumb.playAnimation()
                }
            } else {
                val registrationDayScore = anyToInt(userInfo?.registerDayBean?.registrationDayScore ?: "")
                val registrationDay = anyToInt(userInfo?.registerDayBean?.registDay ?: 0)
                if (registrationDay < registrationDayScore) {
                    binding.layoutMirrorCredit.setBackColorAndStrokeColor(com.cboda.baselibrary.R.color.color_FFB789_20, R.color.white)
                    binding.tvMirrorCreditScore.setTxtColor(com.cboda.baselibrary.R.color.color_D5271E)
                    binding.tvMirrorCreditScoreValue.setTxtColor(com.cboda.baselibrary.R.color.color_D5271E)
                    binding.tvMirrorCreditLabel.setTxtColor(com.cboda.baselibrary.R.color.color_D5271E)
                } else {
                    binding.layoutMirrorCredit.setBackColorAndStrokeColor(com.cboda.baselibrary.R.color.color_4F6FF3_20, R.color.white)
                    binding.tvMirrorCreditScore.setTxtColor(com.cboda.baselibrary.R.color.color_031256)
                    binding.tvMirrorCreditScoreValue.setTxtColor(com.cboda.baselibrary.R.color.color_031256)
                    binding.tvMirrorCreditLabel.setTxtColor(com.cboda.baselibrary.R.color.color_031256)
                }
                binding.idTv.text = "ID:${if (!TextUtils.isEmpty(userInfo?.idno)) userInfo?.idno else userInfo?.mId}"
                if (binding.mViewAnimateNumb.isAnimating) {
                    binding.mViewAnimateNumb.pauseAnimation()
                }
            }
            binding.vocationalTv.isGone = TextUtils.isEmpty(userInfo?.vocational)
            binding.vocationalTv.text = userInfo?.vocational
            val registerDay = userInfo?.registerDayBean?.registDay
            binding.daysTv.isGone = registerDay == null
            binding.daysTv.text = "${registerDay}${getString(R.string.days_unit)}"
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