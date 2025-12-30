package com.lxkj.aiim.lj

import android.os.Bundle
import androidx.activity.viewModels
import androidx.viewbinding.ViewBinding
import com.lxkj.aiim.R
import com.lxkj.aiim.base.BaseSimple2Activity
import com.lxkj.aiim.databinding.ActivitySecurityAuthBinding
import com.lxkj.aiim.lj.bean.ResultBean
import com.lxkj.aiim.lj.configuration.SafeJumpEnum
import com.lxkj.aiim.lj.event.ActionEventBean
import com.lxkj.aiim.lj.http.BaseCallback
import com.lxkj.aiim.lj.utils.ToastUtil
import com.lxkj.aiim.lj.utils.goSafeCenterAuth
import com.lxkj.aiim.lj.utils.maskWithLastChar
import com.lxkj.aiim.model.SafeCenterBean
import com.lxkj.aiim.viewmodel.SafeCenterViewModel
import okhttp3.Request
import okhttp3.Response
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.getValue

//密保验证查看页面
class SecurityAuthActivity : BaseSimple2Activity() {
    private val viewModel: SafeCenterViewModel by viewModels()
    private var mData: SafeCenterBean? = null

    override fun getLayoutId(): Int {
        return R.layout.activity_security_auth
    }

    override fun getViewBindingClass(): Class<out ViewBinding>? {
        return ActivitySecurityAuthBinding::class.java
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        EventBus.getDefault().register(this)
        mData = intent.getSerializableExtra("data") as SafeCenterBean?
        getToolbarTitle()?.text = getResources().getString(R.string.密保问题)
        initListener()

    }

    private fun initListener() {
        (binding as? ActivitySecurityAuthBinding)?.mIvEdit?.setOnClickListener {
          mData?.let {
              goSafeCenterAuth(this,SafeJumpEnum.QuestionAuth.sourceType,it)
          }
        }
    }

    override fun onResume() {
        super.onResume()
        initData()
    }
    private fun initData() {
        viewModel.userInfo(this@SecurityAuthActivity,object : BaseCallback<ResultBean>() {
            override fun onError(response: Response, code: Int, e: Exception) {}
            override fun onSuccess(response: Response?, t: ResultBean) {
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
    fun onAppEventReceiver(event: ActionEventBean){
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