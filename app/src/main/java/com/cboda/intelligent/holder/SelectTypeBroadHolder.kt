package com.cboda.intelligent.holder

import android.app.Activity
import android.content.Context
import android.graphics.drawable.BitmapDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.PopupWindow
import androidx.recyclerview.widget.RecyclerView
import com.cboda.intelligent.R
import com.cboda.intelligent.adapter.SelectTypeBroadAdapter
import com.cboda.intelligent.utils.PixelBroadBroadUtils
import com.cboda.intelligent.widge.WrapContentLinearLayoutBroadManager


/**
 * 简述：选择类型
 * Created by ysh on 2023/2/28.
 */
class SelectTypeBroadHolder(
    private val context: Context?,
    private val view: View?,
    private val marginEnd: Float = 0F,
    private val gravity:Int = Gravity.END,
    private val bgAlpha: Float = 0.5f

) {

    private var pop: PopupWindow? = null
    var itemListener: OnItemClickListener? = null
    var itemClickPosListener:OnItemClickPosListener? = null
    private var adapter: SelectTypeBroadAdapter? = null
    private var authList = ArrayList<String>()
    var disMissListener: DisMissListener? = null

    fun show() {
        context?.apply {
            if (pop == null) {
                val popView = View.inflate(context, R.layout.select_auth_type_pop_layout, null)
                pop = PopupWindow(
                    popView, ViewGroup.LayoutParams.WRAP_CONTENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT, true
                )
                pop?.isTouchable = true
                pop?.isOutsideTouchable = true
                pop?.animationStyle = R.style.pop_animation
                pop?.setBackgroundDrawable(BitmapDrawable())
                val recyclerView = popView.findViewById<RecyclerView>(R.id.recyclerView)
                recyclerView.layoutManager = WrapContentLinearLayoutBroadManager(this)
                adapter = SelectTypeBroadAdapter(R.layout.item_auth_type_layout)
                recyclerView.adapter = adapter
                adapter?.setList(authList)
                adapter?.setOnItemClickListener { _, _, position ->
                    itemListener?.onItemClick(authList[position])
                    itemClickPosListener?.onItemClickPos(position)
                    pop?.dismiss()
                }
                if (marginEnd != 0F) {
                    val params = recyclerView.layoutParams as ViewGroup.MarginLayoutParams
                    params.marginEnd = PixelBroadBroadUtils.dip2px(context, marginEnd)
                    recyclerView.layoutParams = params
                }
            }

            pop?.apply {
                view?.apply {
                    showAsDropDown(view, 0, 0, gravity)
                }
            }
            backgroundAlpha(context, bgAlpha)
            pop?.setOnDismissListener {
                backgroundAlpha(context, 1F)
                disMissListener?.ondisMiss()
            }
        }
    }

    fun setCateList(dataList: List<String>) {
        authList.addAll(dataList)
        adapter?.setList(authList)
    }

    interface OnItemClickListener {
        fun onItemClick(content: String)
    }

    interface OnItemClickPosListener {
        fun onItemClickPos(pos: Int)
    }

    interface DisMissListener {
        fun ondisMiss()
    }

    private fun backgroundAlpha(context: Context?, bgAlpha: Float) {
        if (context is Activity) {
            val lp: WindowManager.LayoutParams? = context.window?.attributes
            lp?.alpha = bgAlpha //0.0-1.0
            context.window.attributes = lp
        }

    }
}