package com.lxkj.aiim.lj

import android.os.Bundle
import android.util.Log
import androidx.viewbinding.ViewBinding
import com.lxkj.aiim.R
import com.lxkj.aiim.base.BaseSimple2Activity
import com.lxkj.aiim.databinding.ActivityIdcardAuthBinding
import com.lxkj.aiim.lj.configuration.SafeJumpEnum
import com.lxkj.aiim.lj.event.ActionEventBean
import com.lxkj.aiim.lj.utils.goSafeCenterAuth
import com.lxkj.aiim.model.SafeCenterBean
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class IdCardAuthActivity : BaseSimple2Activity() {
    private var mData: SafeCenterBean? = null
    override fun getLayoutId(): Int {
        return R.layout.activity_idcard_auth
    }

    override fun getViewBindingClass(): Class<out ViewBinding>? {
        return ActivityIdcardAuthBinding::class.java
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        getToolbarTitle()?.text = getResources().getString(R.string.mine_idcard_auth_des_1)
        mData = intent.getSerializableExtra("data") as SafeCenterBean?
        initData()
    }

    private fun initData() {
        (binding as? ActivityIdcardAuthBinding)?.mTvAuthTime?.text =
            String.format(getString(R.string.mine_idcard_auth_add), mData?.twoFaceTime ?: "")
        (binding as? ActivityIdcardAuthBinding)?.mLlIdCardAuth?.setOnClickListener {
            mData?.let {
                goSafeCenterAuth(this,SafeJumpEnum.GoogleAuth.sourceType,it)
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