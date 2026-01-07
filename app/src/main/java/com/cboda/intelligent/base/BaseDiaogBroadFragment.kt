package com.cboda.intelligent.base

import android.os.Bundle
import android.view.Gravity
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import com.cboda.intelligent.R
import com.cboda.intelligent.utils.AppBroadUtils

open class BaseDiaogBroadFragment : DialogFragment() {
    /**
     * 自定义背景透明度
     */
    val selfDimAmount = 0.7f

    /**
     * 自定义显示位置
     */
    var selfGravity = Gravity.BOTTOM

    var isWrap = true

    var isBottomAni = true


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.transparentFrameWindowStyle)
        AppBroadUtils.debug("当前页面", this.javaClass.simpleName ?: "")
    }


    override fun onStart() {
        super.onStart()
        dialog?.window?.apply {
            val mLayoutParams = attributes
            mLayoutParams.gravity = selfGravity
            mLayoutParams.dimAmount = selfDimAmount
            mLayoutParams.width = ViewGroup.LayoutParams.MATCH_PARENT
            mLayoutParams.height = if (isWrap)ViewGroup.LayoutParams.WRAP_CONTENT else ViewGroup.LayoutParams.MATCH_PARENT
            attributes = mLayoutParams
            if (isBottomAni){
                setWindowAnimations(R.style.main_menu_animstyle)
            }
        }
    }
}