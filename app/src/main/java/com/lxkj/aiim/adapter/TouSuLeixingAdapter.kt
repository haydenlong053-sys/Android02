package com.lxkj.aiim.adapter

import android.graphics.Color
import com.cboda.baselibrary.shapeview.shape.ShapeTextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lxkj.aiim.R
import com.cboda.baselibrary.bean.CommonDataListBean

class TouSuLeixingAdapter(layoutResId: Int) :
    BaseQuickAdapter<CommonDataListBean.CommonBean, BaseViewHolder>(layoutResId) {
    override fun convert(holder: BaseViewHolder, item: CommonDataListBean.CommonBean) {
        holder.setText(R.id.tousuleixing,item.type?:"")
        holder.getView<ShapeTextView>(R.id.tousuleixing)?.shapeBuilder
            ?.setShapeSolidColor(Color.parseColor(if (item.isSelect)"#5456F6" else "#00000000"))
            ?.into(holder.getView<ShapeTextView>(R.id.tousuleixing))
        holder.setTextColor(R.id.tousuleixing,Color.parseColor(if (item.isSelect)"#ffffff" else "#5456F6"))
    }
}