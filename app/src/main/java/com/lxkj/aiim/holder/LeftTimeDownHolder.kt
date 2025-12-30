package com.lxkj.aiim.holder

import android.os.Handler
import android.os.Looper
import com.lxkj.aiim.utils.TimeUtils

/**
 * 简述： 活动倒计时
 * Created by ysh on 2023/3/20.
 */
class LeftTimeDownHolder {
    private var hour = 0
    private var minute = 0
    private var second = 0
    private var mLeftTime = 0L
    var leftTimeListener: LeftTimeListener? = null
    private var mHandler = object : Handler(Looper.getMainLooper()) {}

    private var downRunnable = object : Runnable {
        override fun run() {
            mLeftTime -= TimeUtils.SECOND
            getLeftTime(mLeftTime)
            if (mLeftTime > 0) {
                mHandler.postDelayed(this, TimeUtils.SECOND)
            } else {
                release()
            }
        }

    }

    fun startDown(leftTime: Long) {
        getLeftTime(leftTime)
        mLeftTime = leftTime
        mHandler.postDelayed(downRunnable, TimeUtils.SECOND)
    }


    private fun getLeftTime(time: Long) {
//        AppUtil.debug("倒计时","---> $time")
        val mins = time / TimeUtils.MINUTE
        val sec = (time  - mins*TimeUtils.MINUTE)/ TimeUtils.SECOND
        leftTimeListener?.onLeftTime(
            if (mins < 10) "0$mins" else mins.toString(),
            if (sec < 10) "0$sec" else sec.toString(),
            )

    }

    fun release() {
        mLeftTime = 0
        mHandler.removeCallbacks(downRunnable)
    }

    interface LeftTimeListener {
        fun onLeftTime( minute: String, second: String)
    }
}