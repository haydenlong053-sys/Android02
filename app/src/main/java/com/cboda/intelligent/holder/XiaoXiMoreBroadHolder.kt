package com.cboda.intelligent.holder

import android.app.Activity
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import com.cboda.intelligent.R

class XiaoXiMoreBroadHolder(
    private val context: Context?,
    private val view: View?,
    private val marginEnd: Float = 0F,
    private val gravity: Int = Gravity.END,
    private val bgAlpha: Float = 1F

) {

    private var pop: PopupWindow? = null
    var itemListener: OnItemClickListener? = null
    var disMissListener: DisMissListener? = null

    fun show() {
        context?.apply {
            if (pop == null) {
                val popView = View.inflate(context, R.layout.xiaoxi_more_pop_layout, null)
                pop = PopupWindow(
                    popView, ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, true
                )
                pop?.isTouchable = true
                pop?.isOutsideTouchable = true
                pop?.animationStyle = R.style.pop_animation
                pop?.setBackgroundDrawable(BitmapDrawable())
                popView?.findViewById<View>(R.id.faqiqun)?.setOnClickListener {
                    pop?.dismiss()
                    itemListener?.onItemClick("faqiqun")
                }

                popView?.findViewById<View>(R.id.tianjaipengyou)?.setOnClickListener {
                    pop?.dismiss()
                    itemListener?.onItemClick("tianjia")
                }

                popView?.findViewById<View>(R.id.saoyisao)?.setOnClickListener {
                    pop?.dismiss()
                    itemListener?.onItemClick("saoyisao")
                }
            }

            pop?.apply {
                view?.apply {
                    showAsDropDown(view, 0, 0, gravity)
                }
            }
            backgroundAlpha(context, bgAlpha)
            pop?.setOnDismissListener {
                backgroundAlpha(context, 1F)
                disMissListener?.ondisMiss()
            }
        }
    }


    interface OnItemClickListener {
        fun onItemClick(content: String)
    }

    interface DisMissListener {
        fun ondisMiss()
    }

    private fun backgroundAlpha(context: Context?, bgAlpha: Float) {
        if (context is Activity) {
            val lp: WindowManager.LayoutParams? = context.window?.attributes
            lp?.alpha = bgAlpha //0.0-1.0
            context.window.attributes = lp
        }
    }
}