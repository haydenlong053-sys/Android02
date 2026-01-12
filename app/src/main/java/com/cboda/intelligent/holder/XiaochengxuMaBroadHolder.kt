package com.cboda.intelligent.holder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.cboda.intelligent.R
import com.cboda.intelligent.utils.PixelBroadBroadUtils
import com.cboda.intelligent.widge.MyCenterBroadDialog

class XiaochengxuMaBroadHolder(private val activity: Context?) {
    private var dialog: AlertDialog? = null
    var callBack: OnConfimCallBack? = null
    private var edtBeizhu: EditText? = null

    fun show() {
        activity?.apply {
            if (dialog == null) {
                val view: View = View.inflate(
                    activity,
                    R.layout.xiaochengxuma_dialog_broad_layout,
                    null
                )
                edtBeizhu = view.findViewById(R.id.edtBeizhu)
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
                    if (edtBeizhu?.text.isNullOrEmpty()) {
                        return@setOnClickListener
                    }
                    dialog?.dismiss()
                    callBack?.onClickConfim(edtBeizhu?.text?.trim()?.toString() ?: "")
                }
                view.findViewById<View>(R.id.tvNotAgree)?.setOnClickListener {
                    dialog?.dismiss()
                }

                dialog = MyCenterBroadDialog(
                    activity,
                    view,
                    isCancle = true,
                    showSoft = true
                ).createAndShowDialog()
            } else {
                dialog?.show()
            }

        }
    }

    interface OnConfimCallBack {
        fun onClickConfim(content: String)
    }
}