package com.cboda.intelligent.activity

import android.os.Bundle
import androidx.activity.viewModels
import androidx.viewbinding.ViewBinding
import com.cboda.intelligent.R
import com.cboda.intelligent.base.BaseSimple2BroadActivity
import com.cboda.intelligent.databinding.ActivitySecurityAuthBinding
import com.cboda.intelligent.bean.ResultBroadBean
import com.cboda.intelligent.configuration.SafeJumpBroadEnum
import com.cboda.intelligent.bean.ActionEventBroadBean
import com.cboda.intelligent.utils.BaseBroadCallback
import com.cboda.intelligent.utils.goSafeCenterAuth
import com.cboda.intelligent.utils.maskWithLastChar
import com.cboda.intelligent.model.SafeCenterBroadBean
import com.cboda.intelligent.viewmodel.SafeCenterViewBroadModel
import okhttp3.Request
import okhttp3.Response
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.getValue

//密保验证查看页面
class SecurityAuthBroadActivity : BaseSimple2BroadActivity() {
    private val viewModel: SafeCenterViewBroadModel by viewModels()
    private var mData: SafeCenterBroadBean? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_security_auth
    }

    override fun getViewBindingClass(): Class<out ViewBinding>? {
        return ActivitySecurityAuthBinding::class.java
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        mData = intent.getSerializableExtra("data") as SafeCenterBroadBean?
        getToolbarTitle()?.text = getResources().getString(R.string.密保问题)
        initListener()

    }

    private fun initListener() {
        (binding as? ActivitySecurityAuthBinding)?.mIvEdit?.setOnClickListener {
          mData?.let {
              goSafeCenterAuth(this,SafeJumpBroadEnum.QuestionAuth.sourceType,it)
          }
        }
    }

    override fun onResume() {
        super.onResume()
        initData()
    }
    private fun initData() {
        viewModel.userInfo(this@SecurityAuthBroadActivity,object : BaseBroadCallback<ResultBroadBean>() {
            override fun onError(response: Response, code: Int, e: Exception) {}
            override fun onSuccess(response: Response?, t: ResultBroadBean) {
                (binding as? ActivitySecurityAuthBinding)?.mTvSecurityName?.text=  t.data.mbName
                if (t.data.mbAnswer?.isNotEmpty() == true){
                    (binding as? ActivitySecurityAuthBinding)?.mTvAnswer?.text = t.data.mbAnswer?.maskWithLastChar()
                }


            }

            override fun onBeforeRequest(request: Request?) {
            }

            override fun onFailure(request: Request?, e: java.lang.Exception?) {
            }

            override fun onResponse(response: Response?) {
            }
        })

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

    override fun showBgTopTitleLayout(): Boolean {
        return true
    }

    override fun useViewBinding(): Boolean {
        return true
    }


}