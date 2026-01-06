package com.lxkj.aiim.lj

import android.os.Bundle
import androidx.activity.viewModels
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.lifecycleScope
import androidx.viewbinding.ViewBinding
import com.cboda.intelligent.R
import com.lxkj.aiim.base.BaseSimple2Activity
import com.cboda.intelligent.databinding.ActivityAuthStep2Binding
import com.lxkj.aiim.dialog.pop.SaveQrCodeTipPop
import com.lxkj.aiim.lj.event.ActionEventBean
import com.lxkj.aiim.lj.utils.isSubmitCanGo
import com.lxkj.aiim.lj.utils.pasteToEditText
import com.lxkj.aiim.lj.utils.showBase64QrCodeIv
import com.lxkj.aiim.model.TwoFactorValidate
import com.lxkj.aiim.model.TwoStepAuthBean
import com.lxkj.aiim.viewmodel.SafeCenterViewModel
import kotlinx.coroutines.launch
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.getValue


/**
 * 谷歌验证步骤2
 */
class GoogleAuthStep2Activity : BaseSimple2Activity() {
    private val viewModel: SafeCenterViewModel by viewModels()
    private var mBind :ActivityAuthStep2Binding?=null
    override fun getLayoutId(): Int {
        return R.layout.activity_auth_step_2
    }

    override fun getViewBindingClass(): Class<out ViewBinding>? {
        return ActivityAuthStep2Binding::class.java
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBind =binding as? ActivityAuthStep2Binding
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
                    viewModel.goBindTwoFactorValidate(TwoFactorValidate(it)).observe(this@GoogleAuthStep2Activity){
                        val tipPop = SaveQrCodeTipPop(this@GoogleAuthStep2Activity)
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
    fun onAppEventReceiver(event: ActionEventBean){
        if (event.isFinishAction){
            finish()
        }

    }

    override fun onDestroy() {
        EventBus.getDefault().unregister(this)
        super.onDestroy()
    }

}