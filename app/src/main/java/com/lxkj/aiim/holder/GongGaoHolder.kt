package com.lxkj.aiim.holder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.cboda.intelligent.R
import com.lxkj.aiim.utils.PixelBroadUtils
import com.lxkj.aiim.widge.LiXInCenterDialog

class GongGaoHolder(
    private val activity: Context?,
    private val content: String
) {
    private var dialog: AlertDialog? = null

    fun show() {
        activity?.apply {

            if (dialog == null) {
                val view: View = View.inflate(
                    activity,
                    R.layout.gonggao_dialog_layout2,
                    null
                )
                val rootView = view.findViewById<View>(R.id.rootView)
                if (rootView.layoutParams is ViewGroup.MarginLayoutParams) {
                    val mLayoutParams = rootView.layoutParams as ViewGroup.MarginLayoutParams
                    mLayoutParams.marginStart = PixelBroadUtils.dip2px(activity, 39f)
                    mLayoutParams.marginEnd = PixelBroadUtils.dip2px(activity, 39f)
                    mLayoutParams.width =
                        PixelBroadUtils.getScreenWidth(activity) - 2 * PixelBroadUtils.dip2px(
                            activity,
                            39f
                        )
                    rootView.layoutParams = mLayoutParams
                }
                view.findViewById<View>(R.id.tvAgree)?.setOnClickListener {
                    dialog?.dismiss()
                }
                view.findViewById<TextView>(R.id.tvContent).text = content

                dialog = LiXInCenterDialog(activity, view, isCancle = true).createAndShowDialog()
            } else {
                dialog?.show()
            }

        }
    }

    interface OnConfimCallBack {
        fun onClickConfim(isConfim: Boolean)
    }

}