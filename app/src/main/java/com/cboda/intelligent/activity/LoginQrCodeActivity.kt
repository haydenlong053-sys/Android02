package com.cboda.intelligent.activity

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.viewpager2.widget.ViewPager2
import com.cboda.baselibrary.utils.Vp2FragmentBroadAdapter
import com.cboda.baselibrary.utils.getAppColor
import com.cboda.baselibrary.view.magicindicator.CommonBroadNavigator
import com.cboda.baselibrary.view.magicindicator.MagicBroadIndicator
import com.cboda.baselibrary.view.magicindicator.bindIndicatorAndViewPager
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.databinding.LoginQrCodeActivityBinding
import com.cboda.intelligent.fragment.AccountDetailFragment
import com.cboda.intelligent.fragment.LoginQrCodeFragment
import com.cboda.intelligent.widge.TextBackIndicatorAdapter

class LoginQrCodeActivity : BaseSimple2BroadActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = (binding as? LoginQrCodeActivityBinding)
        setRootBgRes(R.drawable.bg_blue_white)
        backIcon.imageTintList = ColorStateList.valueOf(getAppColor(this@LoginQrCodeActivity, R.color.white))
        addCenterLayout(R.layout.login_qr_code_center_title_layout)
        val vp2FragmentAdapter = Vp2FragmentBroadAdapter(this)
        vp2FragmentAdapter.addFragment(LoginQrCodeFragment())
        vp2FragmentAdapter.addFragment(AccountDetailFragment())
        mBinding?.viewPager?.adapter = vp2FragmentAdapter
        mBinding?.viewPager?.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        mBinding?.viewPager?.isUserInputEnabled = true
        mBinding?.viewPager?.offscreenPageLimit = vp2FragmentAdapter.getData().size
        mBinding?.viewPager?.isSaveEnabled = false
        val mResIds =
            intArrayOf(com.cboda.baselibrary.R.string.login_qr_code, com.cboda.baselibrary.R.string.account_detail)
        val commonNavigator = CommonBroadNavigator(this)
        commonNavigator.isAdjustMode = true
        commonNavigator.isSkimOver = true
        val indicatorAdapt = TextBackIndicatorAdapter(this, mResIds)
        commonNavigator.adapter = indicatorAdapt
        val magicIndicator = mTitleLayout.findViewById<MagicBroadIndicator>(R.id.magicIndicator)
        magicIndicator?.navigator = commonNavigator
        magicIndicator?.also { magicIndicator ->
            mBinding?.viewPager?.also { viewPager ->
                bindIndicatorAndViewPager(magicIndicator, viewPager, indicatorAdapt, magicIndicatorNeedOnPageScrolled = true)
            }
        }

    }

    private var mBinding: LoginQrCodeActivityBinding? = null

//    private val viewModel: LoginViewBroadModel by viewModels()
    override fun getViewBindingClass() = LoginQrCodeActivityBinding::class.java
    override fun showTransparentTopTitleLayout() = true
    override fun useViewBinding() = true

    override fun useStatusBarDarkFont() = false
    override fun showBgTopTitleLayout() = false
    override fun showOrHideTitleLayout() = true

}