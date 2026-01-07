package com.cboda.intelligent.fragment

import android.annotation.SuppressLint
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.core.view.isVisible
import com.cboda.baselibrary.utils.OnSingleClickBroadListener
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseBroadFragment
import com.cboda.intelligent.databinding.NewMirrorFragmentBinding
import com.cboda.intelligent.dialog.pop.UserQrCodeTipBroadPop
import com.cboda.intelligent.activity.SignInBroadActivity
import com.cboda.intelligent.activity.UserInfoBroadActivity
import com.cboda.intelligent.utils.click
import com.cboda.intelligent.model.UserInfoBroadModel
import com.cboda.intelligent.viewmodel.NewMirrorViewBroadModel

class NewMirrorBroadFragment : BaseBroadFragment<NewMirrorFragmentBinding, NewMirrorViewBroadModel>() {
    override fun onCreateViewBinding(
        inflater: LayoutInflater,
        parent: ViewGroup?,
        savedInstanceState: Bundle?
    ) = NewMirrorFragmentBinding.inflate(inflater, parent, false)

    private var userInfo: UserInfoBroadModel? = null

    override fun initListener() {
        super.initListener()
        binding.ivMirrorSign.click {
            viewModel.startAct<SignInBroadActivity>()
        }
        binding.qrCodeLayout.click {
            UserQrCodeTipBroadPop(requireContext(), userInfo).showPopupWindow()
        }
        binding.ivMirrorAvatar.setOnClickListener(userInfoClick)
        binding.layoutMirrorCredit.setOnClickListener(userInfoClick)
        binding.idTv.setOnClickListener(userInfoClick)
        binding.vocationalTv.setOnClickListener(userInfoClick)
        binding.daysTv.setOnClickListener(userInfoClick)
        binding.hobbyTv1.setOnClickListener(userInfoClick)
        binding.hobbyTv2.setOnClickListener(userInfoClick)
        binding.hobbyTv3.setOnClickListener(userInfoClick)
        binding.hobbyTv4.setOnClickListener(userInfoClick)
        binding.hobbyTv5.setOnClickListener(userInfoClick)
        binding.hobbyTv6.setOnClickListener(userInfoClick)
    }

    private val userInfoClick = object : OnSingleClickBroadListener() {
        override fun onSingleClick(v: View?) {
            viewModel.startAct<UserInfoBroadActivity>()
        }
    }

    @SuppressLint("SetTextI18n")
    override fun onResume() {
        super.onResume()
        viewModel.getSignInfo().observe(this) {
            binding.ivMirrorSign.setImageResource(if (it) R.drawable.bg_sign_true else R.drawable.bg_sign_false)
        }
        viewModel.getUserInfo().observe(this) {
            it?.let {
                userInfo = it
            }
            binding.ivMirrorAvatar.setImageResource(if (userInfo?.sex == "2") R.drawable.ic_mirror_female else R.drawable.ic_mirror_male)
            binding.tvMirrorCreditScore.text = "${userInfo?.grade}"
            binding.idTv.text = "ID:${if(!TextUtils.isEmpty(userInfo?.idno)) userInfo?.idno else userInfo?.mId}"
            binding.vocationalTv.isGone = TextUtils.isEmpty(userInfo?.vocational)
            binding.vocationalTv.text = userInfo?.vocational
            val registerDay = userInfo?.registerDayBean?.registDay
            binding.daysTv.isGone = registerDay == null
            binding.daysTv.text = "${registerDay}天"
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

}