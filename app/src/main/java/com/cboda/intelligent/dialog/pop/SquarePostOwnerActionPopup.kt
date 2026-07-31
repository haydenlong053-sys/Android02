package com.cboda.intelligent.dialog.pop

import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.TextView
import com.blankj.utilcode.util.SizeUtils
import com.cboda.intelligent.R

class SquarePostOwnerActionPopup(
    private val activity: Activity,
    private val anchor: View,
) {
    private var popup: PopupWindow? = null

    fun isShowing(): Boolean = popup?.isShowing == true

    fun dismiss() {
        popup?.dismiss()
    }

    fun show(
        isTop: Int,
        onDeleteClick: () -> Unit,
        onTopClick: () -> Unit,
    ) {
        dismiss()
        val popView = LayoutInflater.from(activity)
            .inflate(R.layout.square_post_owner_action_popup, null)
        popView.findViewById<TextView>(R.id.squareOwnerTopTitle).setText(
            if (isTop == 1) {
                com.cboda.baselibrary.R.string.square_post_unpin
            } else {
                R.string.zhiding
            },
        )
        popup = PopupWindow(
            popView,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true,
        ).apply {
            isOutsideTouchable = true
            isFocusable = true
            isTouchable = true
            setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            animationStyle = R.style.pop_animation
            setOnDismissListener { popup = null }
        }
        popView.findViewById<View>(R.id.squareOwnerDeleteRow).setOnClickListener {
            dismiss()
            onDeleteClick()
        }
        popView.findViewById<View>(R.id.squareOwnerTopRow).setOnClickListener {
            dismiss()
            onTopClick()
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
