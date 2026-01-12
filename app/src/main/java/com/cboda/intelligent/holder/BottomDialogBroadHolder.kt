package com.cboda.intelligent.holder

import android.app.Dialog
import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cboda.intelligent.R
import com.cboda.intelligent.widge.MyBottomBroadDialog
import com.cboda.intelligent.widge.WrapContentLinearLayoutBroadManager


/**
 * 简述：底部弹出框组件
 */
class BottomDialogBroadHolder(
    private val activity: Context?,
    private val contentList: ArrayList<String>?
) {

    private var adapter: BottomDialogAdapter? = null
    private var dialog: Dialog? = null
    var callBack: BottomDialogClickCallBack? = null

    /**
     * 弹出弹框
     */
    fun show() {
        activity?.apply {
            if (dialog == null) {
                val view: View = View.inflate(
                    activity,
                    R.layout.bottom_dialog_broad_layout,
                    null
                )
                dialog = MyBottomBroadDialog(activity, view)

                view.findViewById<View>(R.id.tvCancel).setOnClickListener {
                    dialog?.dismiss()
                }
                val recyclerView = view.findViewById<RecyclerView>(R.id.recyclerView)
                recyclerView.layoutManager = WrapContentLinearLayoutBroadManager(activity)
                setContent(recyclerView)

            }

            dialog?.show()

        }
    }

    interface BottomDialogClickCallBack {
        fun clickItem(position: Int)
    }

    private fun setContent(recyclerView: RecyclerView) {
        if (adapter == null) {
            adapter = BottomDialogAdapter(R.layout.item_bottom_dialog_broad_layout)
            recyclerView.adapter = adapter
            adapter?.setList(contentList)
        }
        // 设置点击事件
        adapter?.setOnItemClickListener { _, _, position ->
            callBack?.clickItem(position)
            dialog?.dismiss()
        }
    }

    inner class BottomDialogAdapter(layoutResId: Int) :
        BaseQuickAdapter<String, BaseViewHolder>(layoutResId) {
        override fun convert(holder: BaseViewHolder, item: String) {
            holder.setText(R.id.tvContent, item)
        }

    }
}