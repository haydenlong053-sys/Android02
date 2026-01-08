package com.cboda.intelligent.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.dialog.pop.SaveQrCodeTipBroadPop
import com.cboda.intelligent.bean.ActionEventBroadBean
import com.cboda.intelligent.databinding.ActivityAuthBroadStep2Binding
import com.cboda.intelligent.utils.isSubmitCanGo
import com.cboda.intelligent.utils.pasteToEditText
import com.cboda.intelligent.model.TwoFactorBroadValidate
import com.cboda.intelligent.viewmodel.SafeCenterViewBroadModel
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.getValue


/**
 * 谷歌验证步骤2
 */
class GoogleAuthStep2BroadActivity : BaseSimple2BroadActivity() {
    private val viewModel: SafeCenterViewBroadModel by viewModels()
    private var mBind : ActivityAuthBroadStep2Binding?=null
    override fun getLayoutId(): Int {
        return R.layout.activity_auth_broad_step_2
    }

    override fun getViewBindingClass(): Class<out ViewBinding>? {
        return ActivityAuthBroadStep2Binding::class.java
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBind =binding as? ActivityAuthBroadStep2Binding
        EventBus.getDefault().register(this)
        getToolbarTitle()?.text = getResources().getString(R.string.mine_google_auth)
        initData()
        initListener()
    }

    private fun initListener() {
        mBind?.mTvCopy?.setOnClickListener {
            pasteToEditText(mBind?.mEtCopy!!)
        }
        mBind?.mTvSubmit?.setOnClickListener {
            val code =mBind?.mEtCopy?.text?.trim()?.toString()
            code?.let {
                lifecycleScope.launch {
                    viewModel.goBindTwoFactorValidate(TwoFactorBroadValidate(it)).observe(this@GoogleAuthStep2BroadActivity){
                        val tipPop = SaveQrCodeTipBroadPop(this@GoogleAuthStep2BroadActivity)
                        tipPop.showPopupWindow()
                    }
                }
            }
        }
        mBind?.mTvSubmit?.isSubmitCanGo(false)
        mBind?.mEtCopy?.addTextChangedListener { editable ->
            mBind?.mTvSubmit?.isSubmitCanGo(mBind?.mEtCopy?.text?.trim()?.toString()?.length==6)
        }
    }

    private fun initData() {


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