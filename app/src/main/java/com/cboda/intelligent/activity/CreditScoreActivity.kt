package com.cboda.intelligent.activity

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.SizeUtils
import com.cboda.baselibrary.popup.CommonTipBroadPop
import com.cboda.baselibrary.utils.HttpsBroadConfig
import com.cboda.baselibrary.utils.OnSingleClickBroadListener
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.databinding.CreditScoreActivityBinding
import com.cboda.baselibrary.utils.Vp2FragmentBroadAdapter
import com.cboda.baselibrary.utils.anyToFloat
import com.cboda.baselibrary.utils.getAppColor
import com.cboda.baselibrary.utils.getAppContext
import com.cboda.baselibrary.utils.loadImg
import com.cboda.baselibrary.view.magicindicator.CommonBroadNavigator
import com.cboda.baselibrary.view.magicindicator.bindIndicatorAndViewPager
import com.cboda.intelligent.fragment.CreditScoreComposeFragment
import com.cboda.intelligent.viewmodel.NewMirrorViewBroadModel

import com.cboda.intelligent.widge.TextIndicatorAdapter
import kotlinx.coroutines.launch
import kotlin.getValue

class CreditScoreActivity : BaseSimple2BroadActivity() {
    private val viewModel: NewMirrorViewBroadModel by viewModels()
    private var mBind: CreditScoreActivityBinding? = null
    override fun getViewBindingClass() = CreditScoreActivityBinding::class.java
    override fun showTransparentTopTitleLayout() = false
    override fun useViewBinding() = true
    override fun showOrHideTitleLayout() = true
    override fun useStatusBarDarkFont() = true

    @SuppressLint("SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBind = (binding as? CreditScoreActivityBinding)
        mIvBgTop.setImageResource(R.mipmap.credit_score_back)
        mIvBgTop.layoutParams.height = SizeUtils.dp2px(278f)
        getToolbarTitle()?.text = getString(R.string.credit_score_title)
        initData()

        initViewPagerAndIndicator()
    }

    @SuppressLint("SetTextI18n")
    private fun initData() {
        lifecycleScope.launch {
            viewModel.getCreditDetails().observe(this@CreditScoreActivity) {
                initListener(it.isVip, it.isLianghao)
                mBind?.userImg?.loadImg(it.icon)
                mBind?.userName?.text = "HI,${it.nickname}"
                val isVip = it.isVip
                mBind?.userMemberImg?.setImageResource(if (isVip) R.mipmap.credit_score8 else R.mipmap.credit_score10)
                mBind?.userMemberTv?.text = if (isVip) "会员权益" else "会员未开通"
                mBind?.userMemberTv?.setTextColor(getAppColor(this@CreditScoreActivity, if (isVip) com.cboda.baselibrary.R.color.color_B25900 else com.cboda.baselibrary.R.color.color_717171))

                val isNiceNum = it.isLianghao
                mBind?.userNiceNumImg?.setImageResource(if (isNiceNum) R.mipmap.credit_score9 else R.mipmap.credit_score11)
                mBind?.userNiceNumTv?.text = if (isNiceNum) "靓号权益" else "靓号未开通"
                mBind?.userNiceNumTv?.setTextColor(getAppColor(this@CreditScoreActivity, if (isNiceNum) com.cboda.baselibrary.R.color.color_B25900 else com.cboda.baselibrary.R.color.color_717171))

                val score = it.creditScore ?: "0"
                val scoreFloat = anyToFloat(score)
                mBind?.userHint?.text = if (scoreFloat < 1000f) {
                    "信用很差，请努力提高！"
                } else if (scoreFloat > 5000f) {
                    "信用极好，请继续保持！"
                } else if (scoreFloat > 3000f) {
                    "信用不错，再接再厉！"
                } else {
                    "信用一般，要加油哦！"
                }
                mBind?.userScore?.text = score
                val tempValue = if (scoreFloat < 0) {
                    0f
                } else {
                    scoreFloat
                }
//                mBind?.circleProgress?.progress = anyToInt(tempValue / 100)
                //进度条规则待定，展示用4/3进度
                mBind?.circleProgress?.progress = 75

            }

        }
    }

    private fun initListener(vip: Boolean, lianghao: Boolean) {
        mBind?.layerMember?.setOnClickListener(object : OnSingleClickBroadListener() {
            override fun onSingleClick(v: View?) {
                if (!vip) {
                    showMemberTip(this@CreditScoreActivity) {
                        if (!HttpsBroadConfig.isReviewEnv) {
                            finish()
                            // 跳转到会员中心（原本 llHyzx 点击）
                            startActivity(Intent(this@CreditScoreActivity, HyzxBroadActivity::class.java))
                        }
                    }
                }
            }

        })
        mBind?.layerPremiumNumb?.setOnClickListener(object : OnSingleClickBroadListener() {
            override fun onSingleClick(v: View?) {
                if (!lianghao) {
                    showPremiumNumbTip(this@CreditScoreActivity){

                    }
                }
            }

        })
    }

    private fun showMemberTip(context: Context? = null, block: () -> Unit = {}) {
        val mContext = getAppContext(context)
        val commonTipPop = CommonTipBroadPop(
            mContext,
            mContext.getString(com.cboda.baselibrary.R.string.credit_score_open_member),
            mContext.getString(com.cboda.baselibrary.R.string.credit_score_des1)
        )
        commonTipPop.setCancelBtn(mContext.getString(com.cboda.baselibrary.R.string.bs_quxiao))
        commonTipPop.setSureBtn(mContext.getString(com.cboda.baselibrary.R.string.credit_score_go))
        commonTipPop.sureInvoke = {
            block.invoke()
        }
        commonTipPop.showPopupWindow()
    }

    private fun showPremiumNumbTip(context: Context? = null, block: () -> Unit = {}) {
        val mContext = getAppContext(context)
        val commonTipPop = CommonTipBroadPop(
            mContext,
            mContext.getString(com.cboda.baselibrary.R.string.credit_score_open_premium_number),
            mContext.getString(com.cboda.baselibrary.R.string.credit_score_des2)
        )
        commonTipPop.setCancelBtn(mContext.getString(com.cboda.baselibrary.R.string.bs_quxiao))
        commonTipPop.setSureBtn(mContext.getString(com.cboda.baselibrary.R.string.credit_score_go))
        commonTipPop.sureInvoke = {
            block.invoke()
        }
        commonTipPop.showPopupWindow()
    }


    private fun initViewPagerAndIndicator() {
        val vp2FragmentAdapter = Vp2FragmentBroadAdapter(this)
        vp2FragmentAdapter.addFragment(CreditScoreComposeFragment.newInstance(0))
        vp2FragmentAdapter.addFragment(CreditScoreComposeFragment.newInstance(1))

        mBind?.viewPager?.adapter = vp2FragmentAdapter
        mBind?.viewPager?.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        mBind?.viewPager?.isUserInputEnabled = true
        mBind?.viewPager?.offscreenPageLimit = vp2FragmentAdapter.getData().size
        mBind?.viewPager?.isSaveEnabled = false

        val titleResIds = intArrayOf(
            R.string.credit_score_tab_compose,
            R.string.credit_score_tab_record
        )
        val commonNavigator = CommonBroadNavigator(this)
        commonNavigator.isAdjustMode = true
        commonNavigator.isSkimOver = true
        val indicatorAdapter = TextIndicatorAdapter(
            this,
            titleResIds,
            com.cboda.baselibrary.R.dimen.dp_15
        )
        commonNavigator.adapter = indicatorAdapter
        mBind?.magicIndicator?.navigator = commonNavigator
        mBind?.apply {
            bindIndicatorAndViewPager(magicIndicator, viewPager, indicatorAdapter, magicIndicatorNeedOnPageScrolled = true)
        }

    }


}