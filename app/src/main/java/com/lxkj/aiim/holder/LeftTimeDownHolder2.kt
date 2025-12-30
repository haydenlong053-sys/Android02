package com.lxkj.aiim.holder

import android.os.Handler
import android.os.Looper
import com.lxkj.aiim.utils.TimeUtils

/**
 * 简述： <功能描述>
 * Created by ysh on 2024/3/21.
 */
class LeftTimeDownHolder2 {
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
        val day = time / TimeUtils.DAY
        val hour = (time - day * TimeUtils.DAY) / TimeUtils.HOUR
        val mins = (time - day * TimeUtils.DAY - hour * TimeUtils.HOUR) / TimeUtils.MINUTE
        val sec =
            (time - day * TimeUtils.DAY - hour * TimeUtils.HOUR - mins * TimeUtils.MINUTE) / TimeUtils.SECOND
//        AppUtil.debug("倒计时","---> $time $hour $mins $sec")

        leftTimeListener?.onLeftTime(
            if (day < 10) "0$day" else day.toString(),
            if (hour < 10) "0$hour" else hour.toString(),
            if (mins < 10) "0$mins" else mins.toString(),
            if (sec < 10) "0$sec" else sec.toString(),
            mLeftTime
        )

    }

    fun release() {
        mLeftTime = 0
        mHandler.removeCallbacks(downRunnable)
    }

    interface LeftTimeListener {
        fun onLeftTime(day: String, hour: String, minute: String, second: String,mLeftTime:Long)
    }
}