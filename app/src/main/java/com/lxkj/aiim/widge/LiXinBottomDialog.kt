package com.lxkj.aiim.widge

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import com.cboda.intelligent.R

/**
 * 简述：底部dialog
 */
class LiXinBottomDialog(private val activity: Context, private val view: View) :
    Dialog(activity, R.style.transparentFrameWindowStyle) {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(
            view, ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

    }

    override fun onStart() {
        super.onStart()
        setCanceledOnTouchOutside(true)
        setCancelable(true)
        val window: Window? = window
        // 设置显示动画
        window?.setWindowAnimations(R.style.main_menu_animstyle)
        val wl: WindowManager.LayoutParams? = window?.attributes
        wl?.x = 0
        wl?.y = window?.windowManager?.defaultDisplay?.height?:0
        // 以下这两句是为了保证按钮可以水平满屏
        wl?.width = ViewGroup.LayoutParams.MATCH_PARENT
        wl?.height = ViewGroup.LayoutParams.WRAP_CONTENT

        // 设置显示位置
        onWindowAttributesChanged(wl)
        // 设置点击外围解散
        setCanceledOnTouchOutside(true)
    }
}