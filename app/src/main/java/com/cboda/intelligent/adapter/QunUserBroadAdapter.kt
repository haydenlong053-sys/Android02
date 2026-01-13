package com.cboda.intelligent.adapter

import android.graphics.Color
import com.cboda.baselibrary.shapeview.shape.ShapeTextBroadView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cboda.intelligent.R
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.cboda.intelligent.utils.GlideLoaderBroadHelper
import com.cboda.intelligent.utils.PixelBroadBroadUtils

class QunUserBroadAdapter(layoutResId: Int) :
    BaseQuickAdapter<CommonDataListBroadBean.CommonBean, BaseViewHolder>(layoutResId) {
    override fun convert(holder: BaseViewHolder, item: CommonDataListBroadBean.CommonBean) {
        when (item.type) {
            "add" -> {
                holder.setGone(R.id.tvshenfen,true)
                holder.setText(R.id.tvusername, context.getString(R.string.tianjaiuaer))
                holder.setImageResource(R.id.ivTouxiang, R.mipmap.ic_add_qun_broad_user)
            }
            "remove" -> {
                holder.setGone(R.id.tvshenfen, true)
                holder.setText(R.id.tvusername, context.getString(R.string.yichuuser))
                holder.setImageResource(R.id.ivTouxiang, R.mipmap.ic_yichu_qun_broad_user)
            }
            else -> {
                holder.setText(R.id.tvusername, item.name ?: "")
                GlideLoaderBroadHelper.loadCenterCopRoundedCornersUrl(
                    context,
                    item.icon ?: "",
                    holder.getView(R.id.ivTouxiang),
                    PixelBroadBroadUtils.dip2px(context, 5f)
                )

                if(item.type == "1"){
                    holder.setGone(R.id.tvshenfen,false)
                    holder.getView<ShapeTextBroadView>(R.id.tvshenfen)?.shapeBuilder?.
                            setShapeSolidColor(Color.parseColor("#FFF2E4"))?.
                            into(holder.getView<ShapeTextBroadView>(R.id.tvshenfen))
                    holder.getView<ShapeTextBroadView>(R.id.tvshenfen)?.setTextColor(Color.parseColor("#F99D3A"))
                    holder.getView<ShapeTextBroadView>(R.id.tvshenfen)?.text = context.getString(R.string.qunzhushenfen)
                } else if (item.type == "2"){
                    holder.setGone(R.id.tvshenfen,false)
                    holder.getView<ShapeTextBroadView>(R.id.tvshenfen)?.shapeBuilder?.
                    setShapeSolidColor(Color.parseColor("#D4FFE6"))?.
                    into(holder.getView<ShapeTextBroadView>(R.id.tvshenfen))
                    holder.getView<ShapeTextBroadView>(R.id.tvshenfen)?.setTextColor(Color.parseColor("#20C064"))
                    holder.getView<ShapeTextBroadView>(R.id.tvshenfen)?.text = context.getString(R.string.qunguanlishenfen)

                } else {
                    holder.setGone(R.id.tvshenfen,true)
                }
            }
        }
    }
}