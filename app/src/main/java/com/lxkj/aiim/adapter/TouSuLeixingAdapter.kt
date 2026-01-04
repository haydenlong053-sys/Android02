package com.lxkj.aiim.adapter

import android.graphics.Color
import com.cboda.baselibrary.shapeview.shape.ShapeTextBroadView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lxkj.aiim.R
import com.cboda.baselibrary.bean.CommonDataListBroadBean

class TouSuLeixingAdapter(layoutResId: Int) :
    BaseQuickAdapter<CommonDataListBroadBean.CommonBean, BaseViewHolder>(layoutResId) {
    override fun convert(holder: BaseViewHolder, item: CommonDataListBroadBean.CommonBean) {
        holder.setText(R.id.tousuleixing,item.type?:"")
        holder.getView<ShapeTextBroadView>(R.id.tousuleixing)?.shapeBuilder
            ?.setShapeSolidColor(Color.parseColor(if (item.isSelect)"#5456F6" else "#00000000"))
            ?.into(holder.getView<ShapeTextBroadView>(R.id.tousuleixing))
        holder.setTextColor(R.id.tousuleixing,Color.parseColor(if (item.isSelect)"#ffffff" else "#5456F6"))
    }
}