package com.lxkj.aiim.lj

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isGone
import androidx.fragment.app.Fragment
import com.cboda.baselibrary.event.AppBroadEvent
import com.cboda.baselibrary.event.AppEventBroadCode
import com.cboda.baselibrary.network.config.NetworkBroadConfig.updateAppToken
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.baselibrary.utils.OnSingleClickBroadListener
import com.cboda.baselibrary.utils.Vp2FragmentBroadAdapter
import com.lxkj.aiim.base.BaseSimple2Activity
import com.lxkj.aiim.databinding.ActivityNewEquipmentBinding
import com.lxkj.aiim.dialog.pop.AuthWayPop
import com.lxkj.aiim.lj.bean.ResultBean
import com.lxkj.aiim.lj.configuration.AppConsts
import com.lxkj.aiim.lj.configuration.SafeJumpEnum
import com.lxkj.aiim.lj.utils.SharePrefBroadUtil
import com.lxkj.aiim.lj.http.Url
import com.lxkj.aiim.lj.utils.click
import com.lxkj.aiim.utils.AppUtil.Companion.imLogin
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import com.lxkj.aiim.utils.JumpUtils.Companion.startH5Activity
import com.lxkj.aiim.R

class NewEquipmentActivity : BaseSimple2Activity() {
    private val loginIndex = 1
    private val selectLoginIndex = 0
    private val forgetPwdIndex = 2
    private val updatePwdIndex = 3
    private val registeredIndex = 4
    private var isSelectLoginFragment = false
    private var loginAccount = ""
    private var updatePwdAccount = ""
    private val forgetPwdFragment = ForgetPwdFragment()
    private val updatePwdFragment = UpdatePwdFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        mBinding = (binding as? ActivityNewEquipmentBinding)
        mIvBgTop.setImageResource(com.cboda.baselibrary.R.color.color_111B40)
        mIvBgTop.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
        mIvBgTop.requestLayout() // 或直接调用 setLayoutParams 会自动触发
        setLottiePath("login_back1.json")
        val adapter = Vp2FragmentBroadAdapter(this)
        val fragments = arrayListOf<Fragment>()
        fragments.add(SelectLoginFragment())
        fragments.add(LoginFragment())
        fragments.add(forgetPwdFragment)
        fragments.add(updatePwdFragment)
        fragments.add(RegisteredFragment())
        adapter.setData(fragments)
        mBinding?.loginViewPager?.adapter = adapter
        mBinding?.loginViewPager?.isUserInputEnabled = false
        val language = MMKVBroadUtils.getYuYan()
        mBinding?.languageTv?.text = if (TextUtils.isEmpty(language)) getString(R.string.simplified_chinese) else language
        mBinding?.ivBlack?.click {
            checkIsFinish()
        }
        mBinding?.languageIv1?.setOnClickListener(switchLanguageClick)
        mBinding?.languageTv?.setOnClickListener(switchLanguageClick)
        mBinding?.languageIv2?.setOnClickListener(switchLanguageClick)
        mBinding?.bottomTv2?.click {
            startH5Activity(this, Url.YHXY, getString(R.string.用户协议))
        }
        mBinding?.bottomTv5?.click {
            startH5Activity(this, Url.YSXY, getString(R.string.隐私协议))
        }

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
                if (isSelectLoginFragment) toSelectLogin(false) else toLogin(
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
            startActivity(Intent(this@NewEquipmentActivity, LanguageActivity::class.java))
        }
    }

    private fun setBackIsShow(boolean: Boolean) {
        mBinding?.ivBlack?.isGone = !boolean
    }

    private fun setLanguageIsShow(boolean: Boolean) {
        mBinding?.languageGroup?.isGone = !boolean
    }

    fun toSelectLogin(smoothScroll: Boolean = true) {
        mBinding?.loginViewPager?.setCurrentItem(selectLoginIndex, smoothScroll)
        setBackIsShow(false)
        setBackLottieIsShow(true)
        setLanguageIsShow(true)
    }

    fun toLogin(smoothScroll: Boolean = true) {
        mBinding?.loginViewPager?.setCurrentItem(loginIndex, smoothScroll)
        setBackIsShow(true)
        setBackLottieIsShow(true)
        setLanguageIsShow(true)
    }


    fun toForgetPwd(needUpdateAccount: Boolean = false) {
        mBinding?.loginViewPager?.setCurrentItem(forgetPwdIndex, true)
        setBackIsShow(true)
        setBackLottieIsShow(true)
        setLanguageIsShow(true)
        if (needUpdateAccount) {
            forgetPwdFragment.updateAccount(loginAccount)
        }
    }

    fun toUpdatePwd() {
        mBinding?.loginViewPager?.setCurrentItem(updatePwdIndex, true)
        setBackIsShow(true)
        setBackLottieIsShow(false)
        setLanguageIsShow(false)
        updatePwdFragment.updateAccount(updatePwdAccount)
    }

    fun toRegistered(isSelectLoginFragment: Boolean = false) {
        this.isSelectLoginFragment = isSelectLoginFragment
        mBinding?.loginViewPager?.setCurrentItem(registeredIndex, false)
        setBackIsShow(true)
        setBackLottieIsShow(false)
        setLanguageIsShow(false)
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun receiveEventMsg(eventBusBean: AppBroadEvent<Any?>) {
        if (eventBusBean.eventCode == AppEventBroadCode.EVENT_NEW_MACHINE_CHECK && eventBusBean.data != null) {
            val resultBean = eventBusBean.data as ResultBean?
            SharePrefBroadUtil.saveString(this, AppConsts.APPTOKEN, resultBean!!.apptoken)
            updateAppToken(resultBean.apptoken)
            val authWayPop = AuthWayPop(this, SafeJumpEnum.LoginNewMachine.sourceType, true)
            authWayPop.transferInvoke = {
                imLogin(
                    this@NewEquipmentActivity,
                    resultBean.apptoken,
                    resultBean.userSign,
                    resultBean.userId,
                    resultBean.imtoken
                )
            }
            authWayPop.showPopupWindow()
        }
    }


    private var mBinding: ActivityNewEquipmentBinding? = null

    //    private val viewModel: LoginViewModel by viewModels()
    override fun getViewBindingClass() = ActivityNewEquipmentBinding::class.java
    override fun showTransparentTopTitleLayout() = false
    override fun useViewBinding() = true
    override fun showOrHideTitleLayout() = false
    override fun useStatusBarDarkFont() = false
    override fun showBgTopTitleLayout() = false

    override fun onDestroy() {
        super.onDestroy()
        EventBus.getDefault().unregister(this)
    }
}