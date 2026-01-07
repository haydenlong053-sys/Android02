package com.cboda.intelligent.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cboda.intelligent.R
import com.cboda.baselibrary.bean.CommonDataListBroadBean
import com.cboda.intelligent.utils.GlideLoaderBroadHelper
import com.cboda.intelligent.utils.PixelBroadBroadUtils

class SearchUserBroadAdapter(layoutResId: Int) :
    BaseQuickAdapter<CommonDataListBroadBean.CommonBean, BaseViewHolder>(layoutResId) {
    override fun convert(holder: BaseViewHolder, item: CommonDataListBroadBean.CommonBean) {
        GlideLoaderBroadHelper.loadCenterCopRoundedCornersUrl(
            context,
            item.icon?:"",
            holder.getView(R.id.ivTouxiang),
            PixelBroadBroadUtils.dip2px(context, 5f)
        )

        holder.setText(R.id.tvname2,item.name?:"")
    }
}