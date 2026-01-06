package com.lxkj.aiim.lj

import android.os.Bundle
import android.os.CountDownTimer
import androidx.viewbinding.ViewBinding
import com.cboda.intelligent.R
import com.lxkj.aiim.base.BaseSimple2Activity
import com.cboda.intelligent.databinding.ActivityPaySuccessBinding
import com.lxkj.aiim.lj.event.ActionEventBean
import org.greenrobot.eventbus.EventBus

class PaySuccessActivity : BaseSimple2Activity() {
    private var mBind: ActivityPaySuccessBinding? = null
    private var timer: CountDownTimer? = null
    override fun getViewBindingClass(): Class<out ViewBinding>? {
        return ActivityPaySuccessBinding::class.java
    }

    override fun useViewBinding(): Boolean {
        return true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mBind = binding as? ActivityPaySuccessBinding
        mBind?.mTvStateDes?.text = String.format(getString(R.string.mine_3s_back), "3")
        mBind?.mTvOk?.setOnClickListener {
            performAction()
        }
        startTimer()
    }

    private fun startTimer() {
        timer?.cancel() // 取消之前的定时器
        timer = object : CountDownTimer(3000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished / 1000) + 1
                mBind?.mTvStateDes?.text =
                    String.format(getString(R.string.mine_3s_back), seconds.toString())
            }

            override fun onFinish() {
                // 执行你的操作
                performAction()
            }
        }
        timer?.start()
    }

    private fun performAction() {
        // 这里写定时器结束后要执行的操作
        EventBus.getDefault().post(ActionEventBean().apply {
            isFinishAction = true
        })
        finish()
    }


    override fun onDestroy() {
        super.onDestroy()
        timer?.cancel() // 防止内存泄漏
    }
}