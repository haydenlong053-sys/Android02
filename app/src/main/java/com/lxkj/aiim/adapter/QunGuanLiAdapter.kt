package com.lxkj.aiim.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lxkj.aiim.R
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.lxkj.aiim.utils.GlideLoaderHelper
import com.lxkj.aiim.utils.PixelBroadUtils

class QunGuanLiAdapter(layoutResId: Int, private val type: String = "") :
    BaseQuickAdapter<CommonDataListBroadBean.CommonBean, BaseViewHolder>(layoutResId) {
    override fun convert(holder: BaseViewHolder, item: CommonDataListBroadBean.CommonBean) {
        GlideLoaderHelper.loadCenterCopRoundedCornersUrl(
            context,
            item.icon?:"",
            holder.getView(R.id.ivTouxiang),
            PixelBroadUtils.dip2px(context, 5f)
        )

        holder.setText(R.id.tvname, item.name?:"")

        if (type == "jiechujinyan") {
            holder.setText(R.id.tvNotAgree, context.getString(R.string.jiechujianyan))
        }
    }
}