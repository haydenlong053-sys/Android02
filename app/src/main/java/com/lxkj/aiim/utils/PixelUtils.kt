package com.lxkj.aiim.utils

import android.content.Context
import android.util.DisplayMetrics
import android.util.TypedValue
import android.view.WindowManager


class PixelUtils {
    companion object {
        fun dip2px(context: Context?, dpValue: Float): Int {
            val scale: Float = context?.resources?.displayMetrics?.density ?: 0F
            return (dpValue * scale + 0.5f).toInt()
        }

        fun sp2px(context: Context?, sp: Float): Float {
            return TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_SP,
                sp,
                context?.resources?.displayMetrics
            )
        }

        /**
         * 获取屏幕的宽度
         *
         * @return
         */
        fun getScreenWidth(context: Context?): Int {
            val manager =
                context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val outMetrics = DisplayMetrics()
            manager.defaultDisplay.getMetrics(outMetrics)
            return outMetrics.widthPixels
        }

        /**
         * 获取屏幕的高度
         *
         * @return
         */
        fun getScreenHeight(context: Context?): Int {
            val manager =
                context?.getSystemService(Context.WINDOW_SERVICE) as WindowManager
            val outMetrics = DisplayMetrics()
            manager.defaultDisplay.getMetrics(outMetrics)
            return outMetrics.heightPixels
        }
    }
}