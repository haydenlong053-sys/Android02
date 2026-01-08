package com.cboda.intelligent.holder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
import com.cboda.intelligent.R
import com.cboda.intelligent.utils.PixelBroadBroadUtils
import com.cboda.intelligent.widge.LiXInCenterBroadDialog

class ZhuceHuanyingyuBroadHolder(
    private val activity: Context?,
) {
    private var dialog: AlertDialog? = null

    fun show(regInfo:String) {
        activity?.apply {

            if (dialog == null) {
                val view: View = View.inflate(
                    activity,
                    R.layout.zhucehuanyingyu_dialog_broad_layout,
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
                    onConfimCallBack?.onClickConfim(true)
                    dialog?.dismiss()
                }
                view.findViewById<WebView>(R.id.tvContent)?.loadUrl(
                    regInfo
                )

                dialog = LiXInCenterBroadDialog(activity, view, isCancle = false).createAndShowDialog()
            } else {
                dialog?.show()
            }

        }
    }

    var onConfimCallBack:OnConfimCallBack? = null

    interface OnConfimCallBack {
        fun onClickConfim(isConfim: Boolean)
    }

}