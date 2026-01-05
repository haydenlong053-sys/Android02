package com.lxkj.aiim.holder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.lxkj.aiim.R
import com.lxkj.aiim.utils.KeyboardBroadUtils
import com.lxkj.aiim.utils.PixelBroadUtils
import com.lxkj.aiim.widge.LiXInCenterDialog

class BianJiTextHolder(private val activity: Context?, private val mTitle: String) {
    private var dialog: AlertDialog? = null
    var callBack: OnConfimCallBack? = null
    private var edtBeizhu: EditText? = null

    fun show() {
        activity?.apply {
            if (dialog == null) {
                val view: View = View.inflate(
                    activity,
                    R.layout.bianji_txt_dialog_layout,
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
                    if (edtBeizhu?.text?.trim().isNullOrEmpty()) {
                        return@setOnClickListener
                    }
                    dialog?.dismiss()
                    callBack?.onClickConfim(edtBeizhu?.text?.trim()?.toString() ?: "")
                    KeyboardBroadUtils.closeSoftKeyboard(activity,edtBeizhu)
                }
                view.findViewById<View>(R.id.tvNotAgree)?.setOnClickListener {
                    dialog?.dismiss()
                }
                edtBeizhu = view.findViewById(R.id.edtBeizhu)

                dialog = LiXInCenterDialog(
                    activity,
                    view,
                    isCancle = true,
                    showSoft = true
                ).createAndShowDialog()
            } else {
                dialog?.show()
            }
            edtBeizhu?.setText(mTitle)

        }
    }

    interface OnConfimCallBack {
        fun onClickConfim(txt: String)
    }
}