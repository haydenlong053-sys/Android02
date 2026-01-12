package com.cboda.intelligent.holder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.cboda.intelligent.R
import com.cboda.intelligent.utils.PixelBroadBroadUtils
import com.cboda.intelligent.widge.MyCenterBroadDialog

class GongGaoBroadHolder(
    private val activity: Context?,
    private val content: String
) {
    private var dialog: AlertDialog? = null

    fun show() {
        activity?.apply {

            if (dialog == null) {
                val view: View = View.inflate(
                    activity,
                    R.layout.gong_broad_gao_dialog_layout2,
                    null
                )
                val rootView = view.findViewById<View>(R.id.rootView)
                if (rootView.layoutParams is ViewGroup.MarginLayoutParams) {
                    val mLayoutParams = rootView.layoutParams as ViewGroup.MarginLayoutParams
                    mLayoutParams.marginStart = PixelBroadBroadUtils.dip2px(activity, 39f)
                    mLayoutParams.marginEnd = PixelBroadBroadUtils.dip2px(activity, 39f)
                    mLayoutParams.width =
                        PixelBroadBroadUtils.getScreenWidth(activity) - 2 * PixelBroadBroadUtils.dip2px(
                            activity,
                            39f
                        )
                    rootView.layoutParams = mLayoutParams
                }
                view.findViewById<View>(R.id.tvAgree)?.setOnClickListener {
                    dialog?.dismiss()
                }
                view.findViewById<TextView>(R.id.tvContent).text = content

                dialog = MyCenterBroadDialog(activity, view, isCancle = true).createAndShowDialog()
            } else {
                dialog?.show()
            }

        }
    }

    interface OnConfimCallBack {
        fun onClickConfim(isConfim: Boolean)
    }

}