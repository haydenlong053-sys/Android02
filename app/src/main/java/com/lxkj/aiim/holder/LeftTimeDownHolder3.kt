package com.lxkj.aiim.holder

import android.os.Handler
import android.os.Looper
import com.lxkj.aiim.utils.TimeBroadBroadUtils

/**
 * 简述： <功能描述>
 * Created by ysh on 2024/1/30.
 */
class LeftTimeDownHolder3 {
    private var mLeftTime = 0
    var leftTimeListener: LeftTimeListener? = null
    private var mHandler = object : Handler(Looper.getMainLooper()) {}

    private var downRunnable = object : Runnable {
        override fun run() {
            mLeftTime -= 1
            getLeftTime(mLeftTime)
            if (mLeftTime > 0) {
                mHandler.postDelayed(this, TimeBroadBroadUtils.SECOND)
            } else {
                release()
            }
        }

    }

    fun startDown(leftTime: Int) {
        getLeftTime(leftTime)
        mLeftTime = leftTime
        mHandler.postDelayed(downRunnable, TimeBroadBroadUtils.SECOND)
    }


    private fun getLeftTime(time: Int) {
//        AppUtil.debug("倒计时","---> $time")
        leftTimeListener?.onLeftTime(
            time.toString()
        )

    }

    fun release() {
        mLeftTime = 0
        mHandler.removeCallbacks(downRunnable)
    }

    interface LeftTimeListener {
        fun onLeftTime(second: String)
    }
}