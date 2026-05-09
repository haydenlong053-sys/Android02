package com.cboda.baselibrary.popup

import android.content.Context
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

class CommonBottomListPop(val context: Context, val list: List<ComBottomListBean>, @ColorRes val viewColor: Int = R.color.color_F7F8FA) : BaseBottomBroadPop(context) {
    @JvmField
    var backInvoke: (Int, ComBottomListBean) -> Unit = { _, _ -> }
    private val binding: CommonBottomListPopBinding = CommonBottomListPopBinding.inflate(
        LayoutInflater.from(context)
    )

    private val adapter = ComposeAdapter()

    init {
        contentView = binding.root
        setBackgroundColor(getAppColor(context, R.color.transparent))
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

    private class ComposeAdapter : BaseBroadAdapter<ComBottomListBean>(R.layout.item_bottom_list_layout) {

        override fun onBindData(holder: BaseViewHolder, bean: ComBottomListBean) {
            holder.apply {
                setGone(R.id.itemBottomListView, layoutPosition == 0)
                setGone(R.id.itemBottomListTv, TextUtils.isEmpty(bean.itemContent))
                getView<TextView>(R.id.itemBottomListTv).text = bean.itemContent
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