package com.cboda.baselibrary.popup

import android.content.Context
import android.graphics.Color
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.ColorRes
import androidx.core.view.isGone
import com.cboda.baselibrary.R
import com.cboda.baselibrary.base.BaseBroadAdapter
import com.cboda.baselibrary.bean.ComBottomListBean
import com.cboda.baselibrary.databinding.CommonBottomListPopBinding
import com.cboda.baselibrary.utils.click
import com.cboda.baselibrary.utils.getAppColor
import com.cboda.baselibrary.utils.loadImg
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class CommonBottomListPop(
    val context: Context,
    val list: List<ComBottomListBean>,
    @ColorRes val viewColor: Int = R.color.color_F7F8FA,
    /** 选中项文字色；0 表示不特殊着色 */
    @androidx.annotation.ColorInt val selectedItemTextColor: Int = 0,
) : BaseBottomBroadPop(context) {
    @JvmField
    var backInvoke: (Int, ComBottomListBean) -> Unit = { _, _ -> }
    private val binding: CommonBottomListPopBinding = CommonBottomListPopBinding.inflate(
        LayoutInflater.from(context)
    )

    private val adapter = ComposeAdapter(selectedItemTextColor)

    init {
        contentView = binding.root
        setBackgroundColor(Color.parseColor("#8f000000"))
//        setBackgroundColor(getAppColor(context, R.color.transparent))
    }

    override fun onViewCreated(contentView: View) {
        super.onViewCreated(contentView)
        binding.commonBottomView1.setBackgroundColor(getAppColor(context, viewColor))
        adapter.setData(list)
        binding.commonBottomListPopListView.adapter = adapter
        adapter.setOnItemClickListener { pos, bean ->
            backInvoke.invoke(pos, bean)
            dismiss()
        }
        binding.commonBottomCancel.click {
            dismiss()
        }
    }

    private class ComposeAdapter(
        @androidx.annotation.ColorInt private val selectedItemTextColor: Int,
    ) : BaseBroadAdapter<ComBottomListBean>(R.layout.item_bottom_list_layout) {

        override fun onBindData(holder: BaseViewHolder, bean: ComBottomListBean) {
            holder.apply {
                setGone(R.id.itemBottomListView, layoutPosition == 0)
                setGone(R.id.itemBottomListTv, TextUtils.isEmpty(bean.itemContent))
                val tv = getView<TextView>(R.id.itemBottomListTv)
                tv.text = bean.itemContent
                if (bean.isSelected && selectedItemTextColor != 0) {
                    tv.setTextColor(selectedItemTextColor)
                } else {
                    tv.setTextColor(getAppColor(holder.itemView.context, R.color.black_90))
                }
                val iv = getView<ImageView>(R.id.itemBottomListImg)
                if (bean.iconRes != 0) {
                    iv.setImageResource(bean.iconRes)
                    iv.isGone = false
                } else if (!TextUtils.isEmpty(bean.iconUrl)) {
                    iv.loadImg(bean.iconUrl)
                    iv.isGone = false
                } else {
                    iv.isGone = true
                }
            }
        }


    }
}