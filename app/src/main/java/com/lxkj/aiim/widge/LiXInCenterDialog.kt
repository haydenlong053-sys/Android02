package com.lxkj.aiim.widge

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.cboda.intelligent.R


class LiXInCenterDialog(
    private val activity: Context,
    private val view: View,
    private val showSoft: Boolean = false,
    private val isCancle:Boolean = true,
    private val isFull: Boolean = false,

    ) {
    fun createAndShowDialog(): AlertDialog? {
        val builder = AlertDialog.Builder(activity, R.style.transparentFrameWindowStyle)
        val dialog = builder.create()
        if (showSoft) {
            dialog.setView(EditText(activity))
        }
        dialog.show()
        dialog.setContentView(view)
        dialog.setCanceledOnTouchOutside(isCancle)
        dialog.setCancelable(isCancle)

        if (isFull){
            val wl: WindowManager.LayoutParams? = dialog.window?.attributes
            wl?.width = ViewGroup.LayoutParams.MATCH_PARENT
            wl?.height = ViewGroup.LayoutParams.WRAP_CONTENT
            dialog.onWindowAttributesChanged(wl)
        }

        return dialog
    }
}