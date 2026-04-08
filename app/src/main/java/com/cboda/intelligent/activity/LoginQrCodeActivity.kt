package com.cboda.intelligent.activity

import android.content.res.ColorStateList
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.viewpager2.widget.ViewPager2
import com.blankj.utilcode.util.ActivityUtils
import com.cboda.baselibrary.utils.Vp2FragmentBroadAdapter
import com.cboda.baselibrary.utils.getAppColor
import com.cboda.baselibrary.utils.updatePayPwdDataLiveData
import com.cboda.baselibrary.view.magicindicator.CommonBroadNavigator
import com.cboda.baselibrary.view.magicindicator.MagicBroadIndicator
import com.cboda.baselibrary.view.magicindicator.bindIndicatorAndViewPager
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.databinding.LoginQrCodeActivityBinding
import com.cboda.intelligent.fragment.AccountDetailFragment
import com.cboda.intelligent.fragment.LoginQrCodeFragment
import com.cboda.intelligent.utils.JumpBroadUtils.Companion.startMainActivity
import com.cboda.intelligent.viewmodel.LoginViewBroadModel
import com.cboda.intelligent.widge.TextBackIndicatorAdapter

class LoginQrCodeActivity : BaseSimple2BroadActivity() {

    private val loginQrCodeFragment = LoginQrCodeFragment()
    private val accountDetailFragment = AccountDetailFragment()
    private var backCallback: OnBackPressedCallback? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBinding = (binding as? LoginQrCodeActivityBinding)
        setRootBgRes(R.drawable.bg_blue_white)
        backIcon.imageTintList = ColorStateList.valueOf(getAppColor(this@LoginQrCodeActivity, R.color.white))
        addCenterLayout(R.layout.login_qr_code_center_title_layout)
        val vp2FragmentAdapter = Vp2FragmentBroadAdapter(this)
        vp2FragmentAdapter.addFragment(loginQrCodeFragment)
        vp2FragmentAdapter.addFragment(accountDetailFragment)
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
        getData()
        updatePayPwdDataLiveData.observe(this) {
            getData()
        }


        // 创建回调（true = 拦截返回事件）
        backCallback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {


                val list = ActivityUtils.getActivityList()
                var haveMainAct = false
                list.forEach {
                    val actName = it.javaClass.simpleName
                    if (actName == "MainBroadActivity") {
                        haveMainAct = true
                    }
                }
                if (!haveMainAct) {
                    startMainActivity(this@LoginQrCodeActivity, "LoginQrCodeActivity")
                } else {
                    isEnabled = false
                    onBackPressedDispatcher.onBackPressed()
                    isEnabled = true
                }
            }
        }

        backCallback?.let { backCallback ->
            // 注册回调（自动绑定 lifecycle）
            onBackPressedDispatcher.addCallback(this, backCallback)
        }

    }


    private fun getData() {
        viewModel.getUserInfo().observe(this) {
            loginQrCodeFragment.setData(it)
            accountDetailFragment.setData(it)
        }
    }

//    override fun finish() {
////        val haveMainActivity = ActivityUtils.getActivityList().any { it is MainBroadActivity }
////        if (!haveMainActivity) {
////            startMainActivity(this)
////        }
//        val list = ActivityUtils.getActivityList()
//        var haveMainAct = false
//        list.forEach {
//            val actName = it.javaClass.simpleName
//            if (actName == "MainBroadActivity") {
//                haveMainAct = true
//            }
//        }
//        if (!haveMainAct) {
//            startMainActivity(this@LoginQrCodeActivity, "LoginQrCodeActivity")
//        }
//        super.finish()
//
//    }

    override fun finish() {
        super.finish()
        backCallback?.remove()
    }

    override fun onDestroy() {
        super.onDestroy()
        backCallback?.remove()
    }

    private var mBinding: LoginQrCodeActivityBinding? = null
    private val viewModel: LoginViewBroadModel by viewModels()
    override fun getViewBindingClass() = LoginQrCodeActivityBinding::class.java
    override fun showTransparentTopTitleLayout() = true
    override fun useViewBinding() = true

    override fun useStatusBarDarkFont() = false
    override fun showBgTopTitleLayout() = false
    override fun showOrHideTitleLayout() = true

}