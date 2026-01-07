package com.cboda.intelligent.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.databinding.ActivityAuthStep1Binding
import com.cboda.intelligent.bean.ActionEventBroadBean
import com.cboda.intelligent.utils.copyToClipboard
import com.cboda.intelligent.utils.showBase64QrCodeIv
import com.cboda.intelligent.viewmodel.SafeCenterViewBroadModel
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.getValue

/**
 * 谷歌验证步骤1
 */
class GoogleAuthStep1BroadActivity : BaseSimple2BroadActivity() {
    private val viewModel: SafeCenterViewBroadModel by viewModels()

    override fun getLayoutId(): Int {
        return R.layout.activity_auth_step_1
    }

    override fun getViewBindingClass(): Class<out ViewBinding>? {
        return ActivityAuthStep1Binding::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        getToolbarTitle()?.text = getResources().getString(R.string.mine_google_auth)
        initListener()
        initData()
    }

    private fun initListener() {
        (binding as? ActivityAuthStep1Binding)?.mTvNext?.setOnClickListener {
            val intent = Intent(this, GoogleAuthStep2BroadActivity::class.java)
            startActivity(intent)
        }
        (binding as? ActivityAuthStep1Binding)?.mIvCopy?.setOnClickListener {
            copyToClipboard(this,(binding as? ActivityAuthStep1Binding)?.mTvRandomSecretKey?.text.toString())

        }
    }

    private fun initData() {
        lifecycleScope.launch {
            viewModel.goTwoStepAuth().observe(this@GoogleAuthStep1BroadActivity) {
                EventBus.getDefault().postSticky(it)
                (binding as? ActivityAuthStep1Binding)?.mTvRandomSecretKey?.text = it.randomSecretKey

                if (it.qrCodeImageBase64?.isNotEmpty() == true && it.qrCodeImageBase64!!.contains("base64,")) {
                    showBase64QrCodeIv(this@GoogleAuthStep1BroadActivity,(binding as? ActivityAuthStep1Binding)?.mIvQrCodeAuth!!,it.qrCodeImageBase64)

                }
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
    fun onAppEventReceiver(event: ActionEventBroadBean){
        if (event.isFinishAction){
            finish()
        }

    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }


}