package com.lxkj.aiim.lj

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.lxkj.aiim.R
import com.lxkj.aiim.base.BaseSimple2Activity
import com.lxkj.aiim.databinding.ActivitySafeCenterBinding
import com.lxkj.aiim.lj.configuration.SafeJumpEnum
import com.lxkj.aiim.lj.event.ActionRefreshEvent
import com.lxkj.aiim.lj.utils.goSafeCenterAuth
import com.lxkj.aiim.model.SafeCenterBean
import com.lxkj.aiim.viewmodel.SafeCenterViewModel
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.Int
import kotlin.getValue

class SafeCenterActivity : BaseSimple2Activity() {
    private val viewModel: SafeCenterViewModel by viewModels()
    private var safeBean :SafeCenterBean?=null
    override fun getLayoutId(): Int {
        return R.layout.activity_safe_center
    }

    override fun getViewBindingClass(): Class<out ViewBinding>? {
        return ActivitySafeCenterBinding::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        getToolbarTitle()?.text = getResources().getString(R.string.mine_safe_center)
        initListener()
    }

    private fun initListener() {

        (binding as? ActivitySafeCenterBinding)?.mLlGoogleAuth?.setOnClickListener {
            safeBean?.let {
                if (it.twoFactorCode?.isNotEmpty() == true){
                    val intent =   Intent(this, IdCardAuthActivity::class.java).apply {
                        this.putExtra("data", safeBean)
                    }
                    startActivity(intent)
                }else{
                    goSafeCenterAuth(this,SafeJumpEnum.GoogleAuth.sourceType,it)
                }

            }


        }

        (binding as? ActivitySafeCenterBinding)?.mLlPassWord?.setOnClickListener {
            safeBean?.let {
                goSafeCenterAuth(this,SafeJumpEnum.PasswordAuth.sourceType,it)
            }

        }

        (binding as? ActivitySafeCenterBinding)?.mLlSecurity?.setOnClickListener {
            safeBean?.let {
                if (it.securityId?.isNotEmpty() == true) {
                    val intent =   Intent(this, SecurityAuthActivity::class.java).apply {
                        this.putExtra("data", safeBean)
                    }
                    startActivity(intent)
                } else {
                    goSafeCenterAuth(this,SafeJumpEnum.QuestionAuth.sourceType,it)
                }

            }


        }
    }


    override fun onResume() {
        super.onResume()
        initData()
    }

    private fun initData() {

        lifecycleScope.launch {
            viewModel.goSecurityCenter().observe(this@SafeCenterActivity) {
                safeBean = it
                (binding as? ActivitySafeCenterBinding)?.mLlGoogleAuth?.isSelected =
                    !it.twoFactorCode.isNullOrEmpty()
                (binding as? ActivitySafeCenterBinding)?.mLlPassWord?.isSelected =
                    !it.payPassword.isNullOrEmpty()
                (binding as? ActivitySafeCenterBinding)?.mLlSecurity?.isSelected =
                    !it.securityId.isNullOrEmpty()
            }
        }

    }

    override fun showBgTopTitleLayout(): Boolean {
        return true
    }

    override fun useViewBinding(): Boolean {
        return true
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onAppEventReceiver(event: ActionRefreshEvent) {
        initData()

    }

    override fun onDestroy() {
        EventBus.getDefault().removeAllStickyEvents()
        EventBus.getDefault().unregister(this)
        super.onDestroy()

    }

}