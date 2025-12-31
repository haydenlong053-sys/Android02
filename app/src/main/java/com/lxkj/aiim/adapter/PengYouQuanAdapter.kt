package com.lxkj.aiim.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lxkj.aiim.R
import com.cboda.baselibrary.bean.CommonDataListBean
import com.lxkj.aiim.utils.GlideLoaderHelper
import com.lxkj.aiim.utils.PixelUtils

class PengYouQuanAdapter(layoutResId: Int) :
    BaseQuickAdapter<CommonDataListBean.CommonBean, BaseViewHolder>(layoutResId) {
    override fun convert(holder: BaseViewHolder, item: CommonDataListBean.CommonBean) {
        GlideLoaderHelper.loadCenterCopRoundedCornersUrl(
            context,
            item.icon?:"",
            holder.getView(R.id.ivtupian),
            PixelUtils.dip2px(context,5f)
        )
    }
}