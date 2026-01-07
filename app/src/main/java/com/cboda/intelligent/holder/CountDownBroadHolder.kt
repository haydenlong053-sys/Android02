package com.cboda.intelligent.holder

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.CountDownTimer
import android.widget.TextView
import androidx.lifecycle.*


/**
 * 简述：倒计时组件 normalColor为正常颜色 color为倒计时的字体颜色
 */
class CountDownBroadHolder(
    private val textView: TextView?,
    private val normalColor: String = "#9B4EDC",
    private val color: String = "#9F9E9E"
) : LifecycleEventObserver {
    private var totalTime = 60
    private var mCountDownTimer: CountDownTimer? = null

    /**
     * 开始倒计时
     */
    fun startCountDown() {
        if (mCountDownTimer == null) {
            mCountDownTimer = object : CountDownTimer(totalTime * 1000L, 1000) {
                @SuppressLint("SetTextI18n")
                override fun onTick(millisUntilFinished: Long) {
                    totalTime -= 1
                    if (totalTime == 0) {
                        return
                    }
                    textView?.text = "$totalTime S"
                    textView?.setTextColor(Color.parseColor(color))
                    textView?.isEnabled = false
                    textView?.isClickable = false
                }

                override fun onFinish() {
                    textView?.text = "获取验证码"
                    textView?.setTextColor(Color.parseColor(normalColor))
                    textView?.isEnabled = true
                    textView?.isClickable = true
                    totalTime = 60
                }
            }
        }

        mCountDownTimer?.start()
    }


    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        when (event.name) {
            "ON_DESTROY" -> {
                mCountDownTimer?.cancel()
                mCountDownTimer = null
            }
        }
    }
}