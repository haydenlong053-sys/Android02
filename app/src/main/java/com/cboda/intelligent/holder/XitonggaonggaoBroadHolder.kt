package com.cboda.intelligent.holder

import android.content.Context
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import androidx.appcompat.app.AlertDialog
import com.cboda.intelligent.R
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.cboda.intelligent.utils.PixelBroadBroadUtils
import com.cboda.intelligent.widge.LiXInCenterBroadDialog

/**
 * 简述： <功能描述>
 * Created by ysh on 2023/7/15.
 */
@Deprecated("已废弃")
class XitonggaonggaoBroadHolder(
    private val activity: Context?,
    private val dataList: MutableList<CommonDataListBroadBean.CommonBean>
) {
    private var dialog: AlertDialog? = null
    var callBack: OnConfimCallBack? = null
    private var tvContent: WebView? = null
    private var shangyitiao: View? = null
    private var xiayitiao: View? = null
    private var curPos = 0


    fun show() {
        activity?.apply {
            if (dialog == null) {
                val view: View = View.inflate(
                    activity,
                    R.layout.xitonggonggao_dialog_broad_layout,
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
                tvContent = view.findViewById(R.id.tvContent)
                shangyitiao = view.findViewById(R.id.shangyitiao)
                xiayitiao = view.findViewById(R.id.xiayitiao)

                view.findViewById<View>(R.id.tvAgree)?.setOnClickListener {
                    dataList.removeAt(curPos)
                    callBack?.shanchu(dataList[curPos].id?:"")
                    if (dataList.isEmpty()) {
                        dialog?.dismiss()
                    } else {
                        curPos = 0
                        tvContent?.loadUrl(dataList[curPos].sinfo ?: "")
                        callBack?.yidu(dataList[curPos].id?:"")
                    }

                }
                view.findViewById<View>(R.id.tvNotAgree)?.setOnClickListener {
                    dialog?.dismiss()
                    callBack?.onClickConfim(false)
                }

                dialog = LiXInCenterBroadDialog(activity, view, isCancle = true).createAndShowDialog()
            } else {
                dialog?.show()
            }


            if (dataList.isNotEmpty()) {
                curPos = 0
                tvContent?.loadUrl(dataList[curPos].sinfo ?: "")
                callBack?.yidu(dataList[curPos].id?:"")
            }

            shangyitiao?.setOnClickListener {
                curPos--
                if (curPos < 0) {
                    curPos = 0
                }

                tvContent?.loadUrl(dataList[curPos].sinfo ?: "")
                callBack?.yidu(dataList[curPos].id?:"")
            }

            xiayitiao?.setOnClickListener {
                curPos++
                if (curPos >= dataList.size) {
                    curPos = dataList.size - 1
                }

                tvContent?.loadUrl(dataList[curPos].sinfo ?: "")
                callBack?.yidu(dataList[curPos].id?:"")
            }
        }
    }

    interface OnConfimCallBack {
        fun onClickConfim(isConfim: Boolean)
        fun yidu(id:String)
        fun shanchu(id: String)
    }
}