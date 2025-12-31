package com.lxkj.aiim.adapter

import android.graphics.Color
import com.cboda.baselibrary.shapeview.shape.ShapeTextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lxkj.aiim.R
import com.cboda.baselibrary.bean.CommonDataListBean
import com.lxkj.aiim.utils.GlideLoaderHelper
import com.lxkj.aiim.utils.PixelUtils

class QunUserAdapter(layoutResId: Int) :
    BaseQuickAdapter<CommonDataListBean.CommonBean, BaseViewHolder>(layoutResId) {
    override fun convert(holder: BaseViewHolder, item: CommonDataListBean.CommonBean) {
        when (item.type) {
            "add" -> {
                holder.setGone(R.id.tvshenfen,true)
                holder.setText(R.id.tvusername, context.getString(R.string.tianjaiuaer))
                holder.setImageResource(R.id.ivTouxiang, R.mipmap.ic_add_qun_user)
            }
            "remove" -> {
                holder.setGone(R.id.tvshenfen, true)
                holder.setText(R.id.tvusername, context.getString(R.string.yichuuser))
                holder.setImageResource(R.id.ivTouxiang, R.mipmap.ic_yichu_qun_user)
            }
            else -> {
                holder.setText(R.id.tvusername, item.name ?: "")
                GlideLoaderHelper.loadCenterCopRoundedCornersUrl(
                    context,
                    item.icon ?: "",
                    holder.getView(R.id.ivTouxiang),
                    PixelUtils.dip2px(context, 5f)
                )

                if(item.type == "1"){
                    holder.setGone(R.id.tvshenfen,false)
                    holder.getView<ShapeTextView>(R.id.tvshenfen)?.shapeBuilder?.
                            setShapeSolidColor(Color.parseColor("#FFF2E4"))?.
                            into(holder.getView<ShapeTextView>(R.id.tvshenfen))
                    holder.getView<ShapeTextView>(R.id.tvshenfen)?.setTextColor(Color.parseColor("#F99D3A"))
                    holder.getView<ShapeTextView>(R.id.tvshenfen)?.text = context.getString(R.string.qunzhushenfen)
                } else if (item.type == "2"){
                    holder.setGone(R.id.tvshenfen,false)
                    holder.getView<ShapeTextView>(R.id.tvshenfen)?.shapeBuilder?.
                    setShapeSolidColor(Color.parseColor("#D4FFE6"))?.
                    into(holder.getView<ShapeTextView>(R.id.tvshenfen))
                    holder.getView<ShapeTextView>(R.id.tvshenfen)?.setTextColor(Color.parseColor("#20C064"))
                    holder.getView<ShapeTextView>(R.id.tvshenfen)?.text = context.getString(R.string.qunguanlishenfen)

                } else {
                    holder.setGone(R.id.tvshenfen,true)
                }
            }
        }
    }
}