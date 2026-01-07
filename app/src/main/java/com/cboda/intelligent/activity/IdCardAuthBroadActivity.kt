package com.cboda.intelligent.activity

import android.os.Bundle
import androidx.viewbinding.ViewBinding
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.databinding.ActivityIdcardAuthBinding
import com.cboda.intelligent.configuration.SafeJumpBroadEnum
import com.cboda.intelligent.bean.ActionEventBroadBean
import com.cboda.intelligent.utils.goSafeCenterAuth
import com.cboda.intelligent.model.SafeCenterBroadBean
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class IdCardAuthBroadActivity : BaseSimple2BroadActivity() {
    private var mData: SafeCenterBroadBean? = null
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
        mData = intent.getSerializableExtra("data") as SafeCenterBroadBean?
        initData()
    }

    private fun initData() {
        (binding as? ActivityIdcardAuthBinding)?.mTvAuthTime?.text =
            String.format(getString(R.string.mine_idcard_auth_add), mData?.twoFaceTime ?: "")
        (binding as? ActivityIdcardAuthBinding)?.mLlIdCardAuth?.setOnClickListener {
            mData?.let {
                goSafeCenterAuth(this,SafeJumpBroadEnum.GoogleAuth.sourceType,it)
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