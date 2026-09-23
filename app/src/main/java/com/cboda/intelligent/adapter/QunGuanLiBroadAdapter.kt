package com.cboda.intelligent.adapter

import android.widget.ImageView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cboda.intelligent.R
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.cboda.baselibrary.utils.loadImg
import com.cboda.intelligent.utils.GlideLoaderBroadHelper
import com.cboda.intelligent.utils.PixelBroadBroadUtils

class QunGuanLiBroadAdapter(layoutResId: Int, private val type: String = "") :
    BaseQuickAdapter<CommonDataListBroadBean.CommonBean, BaseViewHolder>(layoutResId) {
    override fun convert(holder: BaseViewHolder, item: CommonDataListBroadBean.CommonBean) {
//        GlideLoaderBroadHelper.loadCenterCopRoundedCornersUrl(
//            context,
//            item.icon?:"",
//            holder.getView(R.id.ivTouxiang),
//            PixelBroadBroadUtils.dip2px(context, 5f)
//        )
        holder.getView<ImageView>(R.id.ivTouxiang).loadImg(item.icon ?: "")

        holder.setText(R.id.tvname, item.name?:"")

        if (item.isZy == "1") {
            holder.setGone(R.id.rightLayout, true)
        } else {
            holder.setVisible(R.id.rightLayout, true)
        }

        if (type == "jiechujinyan") {
            holder.setText(R.id.tvNotAgree, context.getString(R.string.jiechujianyan))
        }
    }
}