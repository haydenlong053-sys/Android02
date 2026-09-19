package com.cboda.intelligent.activity

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import com.cboda.baselibrary.bean.LoginBean
import com.cboda.baselibrary.event.AppBroadEvent
import com.cboda.baselibrary.event.AppEventBroadCode
import com.cboda.baselibrary.network.config.NetworkBroadConfig.updateAppToken
import com.cboda.baselibrary.utils.DeviceBroadUtil
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.OnSingleClickBroadListener
import com.cboda.baselibrary.utils.Vp2FragmentBroadAdapter
import com.cboda.baselibrary.utils.eLogD
import com.cboda.baselibrary.utils.fromJson2
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.bean.ResultBroadBean
import com.cboda.intelligent.configuration.AppBroadConsts
import com.cboda.intelligent.configuration.SafeJumpBroadEnum
import com.cboda.intelligent.configuration.ScanJumpBroadEnum
import com.cboda.intelligent.databinding.ActivityNewBroadEquipmentBinding
import com.cboda.intelligent.dialog.pop.AuthWayBroadPop
import com.cboda.intelligent.dialog.pop.SecurityVerificationPopup
import com.cboda.intelligent.net.BroadUrl
import com.cboda.intelligent.utils.AppBroadUtils.Companion.imLogin
import com.cboda.intelligent.utils.JumpBroadUtils.Companion.startH5Activity
import com.cboda.intelligent.utils.JumpBroadUtils.Companion.toScanActivity
import com.cboda.intelligent.utils.SharePrefBroadBroadUtil
import com.cboda.intelligent.utils.click
import com.cboda.select.picture.utils.LanguageUtil
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class NewEquipmentBroadActivity : BaseSimple2BroadActivity() {
    private val loginIndex = 0

    //    private val selectLoginIndex = 0
    private val forgetPwdIndex = 3
    private val updatePwdIndex = 2
    private val registeredIndex = 1
    private var isSelectLoginFragment = false
    private var loginAccount = ""
    private var updatePwdAccount = ""
    private val forgetPwdFragment = ForgetPwdBroadFragment()
    private val updatePwdFragment = UpdatePwdBroadFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        mBinding = (binding as? ActivityNewBroadEquipmentBinding)
        mIvBgTop.setImageResource(R.drawable.ic_login_big_bg)
        mIvBgTop.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        mIvBgTop.requestLayout() // 或直接调用 setLayoutParams 会自动触发
        setLottiePath("login_back1.json")
        val adapter = Vp2FragmentBroadAdapter(this)
        val fragments = arrayListOf<Fragment>()
//        fragments.add(SelectLoginBroadFragment())
        fragments.add(LoginBroadFragment())
//        fragments.add(forgetPwdFragment)
//        fragments.add(updatePwdFragment)
        fragments.add(RegisteredBroadFragment())
        adapter.setData(fragments)
        mBinding?.loginViewPager?.adapter = adapter
        mBinding?.loginViewPager?.isUserInputEnabled = false
        mBinding?.languageTv?.text = LanguageUtil.getAppLanguageName("", this)
        mBinding?.ivBlack?.click {
            checkIsFinish()
        }
        mBinding?.languageIv1?.setOnClickListener(switchLanguageClick)
        mBinding?.languageTv?.setOnClickListener(switchLanguageClick)
        mBinding?.languageIv2?.setOnClickListener(switchLanguageClick)
        mBinding?.bottomTv2?.click {
            startH5Activity(this, BroadUrl.getUserAgreementUrl(), getString(R.string.用户协议))
        }
        mBinding?.bottomTv5?.click {
            startH5Activity(this, BroadUrl.getPrivacyPolicyUrl(), getString(R.string.隐私协议))
        }
        toLogin()
    }

    private fun checkIsFinish(): Boolean {
        return when (mBinding?.loginViewPager?.currentItem) {
            loginIndex -> {
                toSelectLogin()
                true
            }

            forgetPwdIndex -> {
                toLogin()
                true
            }

            updatePwdIndex -> {
                toForgetPwd()
                true
            }

            registeredIndex -> {
                toLogin(
                    false
                )
                true
            }

            else -> false
        }
    }

//    @Deprecated("This method has been deprecated in favor of using the\n      {@link OnBackPressedDispatcher} via {@link #getOnBackPressedDispatcher()}.\n      The OnBackPressedDispatcher controls how back button events are dispatched\n      to one or more {@link OnBackPressedCallback} objects.")
//    override fun onBackPressed() {
//        eLogD("NewEquipmentActivityClass","onBackPressed")
//        if (!checkIsFinish()) {
//            super.onBackPressed()
//        }
//    }
//    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
//        val action= event?.action
//        eLogD("NewEquipmentActivityClass","action:$action ,keyCode:$keyCode")
//        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            // 处理返回键
//            if (checkIsFinish()) {
//                return true // 消费事件，不再传递
//            }
//        }
//        eLogD("NewEquipmentActivityClass","2action:$action ,keyCode:$keyCode")
//        return super.onKeyDown(keyCode, event)
//    }

    fun updateLoginAccount(account: String) {
        loginAccount = account
    }

    fun updatePwdAccount(account: String) {
        updatePwdAccount = account
    }


    private val switchLanguageClick = object : OnSingleClickBroadListener() {
        override fun onSingleClick(v: View?) {
            startActivity(Intent(this@NewEquipmentBroadActivity, LanguageBroadActivity::class.java))
        }
    }

    private fun setBackIsShow(boolean: Boolean) {
        mBinding?.ivBlack?.isGone = !boolean
    }

    private fun setLanguageIsShow(boolean: Boolean) {
        mBinding?.languageGroup?.isGone = !boolean
    }

    /** 登录/注册白底页：隐藏暗色 Lottie，返回键与协议文案改深色。 */
    private fun applyAuthLightChrome(light: Boolean) {
        if (light) {
//            mIvBgTop.setImageResource(android.R.color.white)
            setBackLottieIsShow(false)
            mBinding?.ivBlack?.imageTintList = ColorStateList.valueOf(Color.BLACK)
            mBinding?.languageTv?.setTextColor(ContextCompat.getColor(this, R.color.black_65))
            mBinding?.bottomTv1?.setTextColor(0x99000000.toInt())
            mBinding?.bottomTv2?.setTextColor(ContextCompat.getColor(this, R.color.color_3B47DD))
            mBinding?.bottomTv3?.setTextColor(0x99000000.toInt())
            mBinding?.bottomTv5?.setTextColor(ContextCompat.getColor(this, R.color.color_3B47DD))
        } else {
//            mIvBgTop.setImageResource(com.cboda.baselibrary.R.color.color_111B40)
            setBackLottieIsShow(true)
            mBinding?.ivBlack?.imageTintList = ColorStateList.valueOf(Color.WHITE)
            mBinding?.languageTv?.setTextColor(ContextCompat.getColor(this, R.color.white))
            mBinding?.bottomTv1?.setTextColor(0x59FFFFFF)
            mBinding?.bottomTv2?.setTextColor(0xA6FFFFFF.toInt())
            mBinding?.bottomTv3?.setTextColor(0x59FFFFFF)
            mBinding?.bottomTv5?.setTextColor(0xA6FFFFFF.toInt())
        }
    }

    fun toSelectLogin(smoothScroll: Boolean = true) {
//        mBinding?.loginViewPager?.setCurrentItem(selectLoginIndex, smoothScroll)
        setBackIsShow(false)
        setLanguageIsShow(true)
        applyAuthLightChrome(false)
    }

    fun toLogin(smoothScroll: Boolean = true) {
        mBinding?.loginViewPager?.setCurrentItem(loginIndex, smoothScroll)
        setBackIsShow(false)
        setLanguageIsShow(false)
        applyAuthLightChrome(true)
    }


    fun toForgetPwd(needUpdateAccount: Boolean = false) {
        mBinding?.loginViewPager?.setCurrentItem(forgetPwdIndex, true)
        setBackIsShow(true)
        setLanguageIsShow(true)
        applyAuthLightChrome(false)
        if (needUpdateAccount) {
            forgetPwdFragment.updateAccount(loginAccount)
        }
    }

    fun toUpdatePwd() {
        mBinding?.loginViewPager?.setCurrentItem(updatePwdIndex, true)
        setBackIsShow(true)
        setLanguageIsShow(false)
        applyAuthLightChrome(false)
        updatePwdFragment.updateAccount(updatePwdAccount)
    }

    fun toScan() {
        toScanActivity(ScanJumpBroadEnum.Login.sourceType, this@NewEquipmentBroadActivity, startForResult)
    }


    fun openSecurityDialog(loginBean: LoginBean) {
        val securityDialog = SecurityVerificationPopup(this@NewEquipmentBroadActivity, loginBean)
        securityDialog.showPopupWindow()
    }

    private val startForResult =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == RESULT_OK) {
                val loginBeanStr = it.data?.getStringExtra("loginBean")
                if (!TextUtils.isEmpty(loginBeanStr)) {
                    val loginBean = fromJson2(loginBeanStr, LoginBean::class.java)
                    openSecurityDialog(loginBean)
                }
            }

        }

    fun toRegistered(isSelectLoginFragment: Boolean = false) {
        this.isSelectLoginFragment = isSelectLoginFragment
        mBinding?.loginViewPager?.setCurrentItem(registeredIndex, false)
        setBackIsShow(true)
        setLanguageIsShow(false)
        applyAuthLightChrome(true)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun receiveEventMsg(eventBusBean: AppBroadEvent<Any?>) {
//        if (eventBusBean.eventCode == AppEventBroadCode.EVENT_NEW_MACHINE_CHECK && eventBusBean.data != null) {
//            val resultBean = eventBusBean.data as ResultBroadBean?
////            SharePrefBroadBroadUtil.saveString(this, AppBroadConsts.APPTOKEN, resultBean!!.apptoken)
////            updateAppToken(resultBean.apptoken)
//            val appToken = resultBean?.apptoken ?: ""
//            MMKVBroadUtils.saveUserAppToken(appToken)
//            updateAppToken(appToken)
//            val authWayPop = AuthWayBroadPop(this, SafeJumpBroadEnum.LoginNewMachine.sourceType, true)
//            authWayPop.transferInvoke = {
//                imLogin(
//                    this@NewEquipmentBroadActivity,
//                    appToken,
//                    resultBean?.userId,
//                    resultBean?.imtoken, "NewEquipmentBroadActivity"
//                )
//            }
//            authWayPop.showPopupWindow()
//        }
    }


    private var mBinding: ActivityNewBroadEquipmentBinding? = null

    //    private val viewModel: LoginViewBroadModel by viewModels()
    override fun getViewBindingClass() = ActivityNewBroadEquipmentBinding::class.java
    override fun showTransparentTopTitleLayout() = false
    override fun useViewBinding() = true
    override fun showOrHideTitleLayout() = false
    override fun useStatusBarDarkFont() = true
    override fun showBgTopTitleLayout() = false

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}