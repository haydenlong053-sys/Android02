package com.cboda.intelligent.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.databinding.ActivitySafeCenterBinding
import com.cboda.intelligent.configuration.SafeJumpBroadEnum
import com.cboda.intelligent.bean.ActionRefreshBroadEvent
import com.cboda.intelligent.utils.goSafeCenterAuth
import com.cboda.intelligent.model.SafeCenterBroadBean
import com.cboda.intelligent.viewmodel.SafeCenterViewBroadModel
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.Int
import kotlin.getValue

class SafeCenterBroadActivity : BaseSimple2BroadActivity() {
    private val viewModel: SafeCenterViewBroadModel by viewModels()
    private var safeBean :SafeCenterBroadBean?=null
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
                    val intent =   Intent(this, IdCardAuthBroadActivity::class.java).apply {
                        this.putExtra("data", safeBean)
                    }
                    startActivity(intent)
                }else{
                    goSafeCenterAuth(this,SafeJumpBroadEnum.GoogleAuth.sourceType,it)
                }

            }


        }

        (binding as? ActivitySafeCenterBinding)?.mLlPassWord?.setOnClickListener {
            safeBean?.let {
                goSafeCenterAuth(this,SafeJumpBroadEnum.PasswordAuth.sourceType,it)
            }

        }

        (binding as? ActivitySafeCenterBinding)?.mLlSecurity?.setOnClickListener {
            safeBean?.let {
                if (it.securityId?.isNotEmpty() == true) {
                    val intent =   Intent(this, SecurityAuthBroadActivity::class.java).apply {
                        this.putExtra("data", safeBean)
                    }
                    startActivity(intent)
                } else {
                    goSafeCenterAuth(this,SafeJumpBroadEnum.QuestionAuth.sourceType,it)
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
            viewModel.goSecurityCenter().observe(this@SafeCenterBroadActivity) {
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
    fun onAppEventReceiver(event: ActionRefreshBroadEvent) {
        initData()

    }

    override fun onDestroy() {
        EventBus.getDefault().removeAllStickyEvents()
        EventBus.getDefault().unregister(this)
        super.onDestroy()

    }

}