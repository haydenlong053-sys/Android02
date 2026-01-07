package com.cboda.intelligent.holder

import android.os.Handler
import android.os.Looper
import com.cboda.intelligent.utils.TimeBroadUtils

/**
 * 简述： <功能描述>
 * Created by ysh on 2024/3/21.
 */
class LeftTimeDownBroadHolder2 {
    private var mLeftTime = 0L
    var leftTimeListener: LeftTimeListener? = null
    private var mHandler = object : Handler(Looper.getMainLooper()) {}

    private var downRunnable = object : Runnable {
        override fun run() {
            mLeftTime -= TimeBroadUtils.SECOND
            getLeftTime(mLeftTime)
            if (mLeftTime > 0) {
                mHandler.postDelayed(this, TimeBroadUtils.SECOND)
            } else {
                release()
            }
        }

    }

    fun startDown(leftTime: Long) {
        getLeftTime(leftTime)
        mLeftTime = leftTime
        mHandler.postDelayed(downRunnable, TimeBroadUtils.SECOND)
    }


    private fun getLeftTime(time: Long) {
        val day = time / TimeBroadUtils.DAY
        val hour = (time - day * TimeBroadUtils.DAY) / TimeBroadUtils.HOUR
        val mins = (time - day * TimeBroadUtils.DAY - hour * TimeBroadUtils.HOUR) / TimeBroadUtils.MINUTE
        val sec =
            (time - day * TimeBroadUtils.DAY - hour * TimeBroadUtils.HOUR - mins * TimeBroadUtils.MINUTE) / TimeBroadUtils.SECOND
//        AppBroadUtil.debug("倒计时","---> $time $hour $mins $sec")

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