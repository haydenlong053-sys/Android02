package com.cboda.intelligent.activity

import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.cboda.baselibrary.utils.Vp2FragmentBroadAdapter
import com.cboda.baselibrary.utils.getAppColor
import com.cboda.baselibrary.view.magicindicator.CommonBroadNavigator
import com.cboda.baselibrary.view.magicindicator.bindIndicatorAndViewPager
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.databinding.LoginQrCodeActivityBinding
import com.cboda.intelligent.fragment.AiRecordFragment
import com.cboda.intelligent.fragment.LoginQrCodeFragment
import com.cboda.intelligent.viewmodel.LoginViewBroadModel
import com.cboda.intelligent.widge.ImgIndicatorBroadAdapter
import com.cboda.intelligent.widge.TextBackIndicatorAdapter
import kotlin.getValue

class LoginQrCodeActivity : BaseSimple2BroadActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = (binding as? LoginQrCodeActivityBinding)
        setRootBgRes(R.drawable.bg_blue_white)
//        backIcon.imageTintList = ColorStateList.valueOf(getAppColor(this@LoginQrCodeActivity, R.color.white))

        val vp2FragmentAdapter = Vp2FragmentBroadAdapter(this)
        vp2FragmentAdapter.addFragment(LoginQrCodeFragment())
        vp2FragmentAdapter.addFragment(LoginQrCodeFragment())
        mBinding?.viewPager?.adapter = vp2FragmentAdapter
        mBinding?.viewPager?.orientation = ViewPager2.ORIENTATION_HORIZONTAL
        mBinding?.viewPager?.isUserInputEnabled = true
        mBinding?.viewPager?.offscreenPageLimit = vp2FragmentAdapter.getData().size
        mBinding?.viewPager?.isSaveEnabled = false
        val mResIds =
            intArrayOf(R.string.ai_document, R.string.ai_img)
//        mBinding?.magicIndicator?.setBackgroundColor(Color.TRANSPARENT)
        val commonNavigator = CommonBroadNavigator(this)
        commonNavigator.isAdjustMode = true
        commonNavigator.isSkimOver = true
        val indicatorAdapt = TextBackIndicatorAdapter(this, mResIds)
        commonNavigator.adapter = indicatorAdapt
        mBinding?.magicIndicator?.navigator = commonNavigator
        mBinding?.magicIndicator?.also { magicIndicator ->
            mBinding?.viewPager?.also { viewPager ->
                bindIndicatorAndViewPager(magicIndicator, viewPager, indicatorAdapt)
            }
        }

    }

    private var mBinding: LoginQrCodeActivityBinding? = null

    private val viewModel: LoginViewBroadModel by viewModels()
    override fun getViewBindingClass() = LoginQrCodeActivityBinding::class.java
    override fun showTransparentTopTitleLayout() = true
    override fun useViewBinding() = true

    override fun useStatusBarDarkFont() = false
    override fun showBgTopTitleLayout() = false
    override fun showOrHideTitleLayout() = false
}