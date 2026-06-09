package com.cboda.intelligent.dialog.pop

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import com.blankj.utilcode.util.SizeUtils
import com.cboda.intelligent.R

class SquarePostReportPopup(
    private val activity: Activity,
    private val anchor: View,
) {
    private var popup: PopupWindow? = null

    fun isShowing(): Boolean = popup?.isShowing == true

    fun dismiss() {
        popup?.dismiss()
    }

    fun show(onReportClick: () -> Unit) {
        dismiss()
        val popView = LayoutInflater.from(activity)
            .inflate(R.layout.square_post_report_popup, null)
        popup = PopupWindow(
            popView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true,
        ).apply {
            isOutsideTouchable = true
            isFocusable = true
            isTouchable = true
            // 空 BitmapDrawable 会导致弹层内点击需多次才生效
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            animationStyle = R.style.pop_animation
            setOnDismissListener { popup = null }
        }
        popView.findViewById<View>(R.id.squareReportRow).setOnClickListener {
            dismiss()
            onReportClick()
        }
        val xOffset = -SizeUtils.dp2px(8f)
        if (anchor.isAttachedToWindow) {
            anchor.post {
                popup?.showAsDropDown(anchor, xOffset, 0, Gravity.END)
            }
        } else {
            popup?.showAsDropDown(anchor, xOffset, 0, Gravity.END)
        }
    }
}
