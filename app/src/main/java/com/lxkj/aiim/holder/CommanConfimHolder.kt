package com.lxkj.aiim.holder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import com.lxkj.aiim.R
import com.lxkj.aiim.utils.PixelUtils
import com.lxkj.aiim.widge.LiXInCenterDialog

/**
 * 简述： <功能描述>
 * Created by ysh on 2023/7/15.
 */
class CommanConfimHolder (private val activity: Context?,private val mTitle:String? = "温馨提示",private val content:String,private val comfimName:String = "确定",val canNmae:String = "取消") {
    private var dialog: AlertDialog? = null
    var callBack: OnConfimCallBack? = null

    fun show() {
        activity?.apply {
            if (dialog == null) {
                val view: View = View.inflate(
                    activity,
                    R.layout.ke_hu_confim_dialog_layout,
                    null
                )
                val rootView = view.findViewById<View>(R.id.rootView)
                if (rootView.layoutParams is ViewGroup.MarginLayoutParams) {
                    val mLayoutParams = rootView.layoutParams as ViewGroup.MarginLayoutParams
                    mLayoutParams.marginStart = PixelUtils.dip2px(activity, 39f)
                    mLayoutParams.marginEnd = PixelUtils.dip2px(activity, 39f)
                    mLayoutParams.width =
                        PixelUtils.getScreenWidth(activity) - 2 * PixelUtils.dip2px(
                            activity,
                            39f
                        )
                    rootView.layoutParams = mLayoutParams
                }
                view.findViewById<View>(R.id.tvAgree)?.setOnClickListener {
                    dialog?.dismiss()
                    callBack?.onClickConfim(true)
                }
                view.findViewById<View>(R.id.tvNotAgree)?.setOnClickListener {
                    dialog?.dismiss()
                    callBack?.onClickConfim(false)
                }
                view.findViewById<TextView>(R.id.tvAgree)?.text=comfimName
                view.findViewById<TextView>(R.id.tvNotAgree)?.text=canNmae

                view.findViewById<TextView>(R.id.tvTitle).text = mTitle
                view.findViewById<TextView>(R.id.tvContent).text = content

                dialog = LiXInCenterDialog(activity, view,isCancle = true).createAndShowDialog()
            } else {
                dialog?.show()
            }

        }
    }

    interface OnConfimCallBack {
        fun onClickConfim(isConfim:Boolean)
    }
}