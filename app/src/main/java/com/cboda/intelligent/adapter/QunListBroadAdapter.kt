package com.cboda.intelligent.adapter

import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cboda.intelligent.R
import com.cboda.baselibrary.bean.CommonDataListBroadBean

class QunListBroadAdapter(layoutResId: Int) :
    BaseQuickAdapter<CommonDataListBroadBean.CommonBean, BaseViewHolder>(layoutResId) {
    override fun convert(holder: BaseViewHolder, item: CommonDataListBroadBean.CommonBean) {
//        GlideLoaderBroadHelper.loadUrl(
//            context,
//            item.icon?:"",
//            holder.itemView.ivTouxiang,
//        )
        Glide.with(context).load( item.icon?:"").placeholder(com.cboda.chatui.R.mipmap.ic_group_chat)
            .into( holder.getView(R.id.ivTouxiang))
        holder.setText(R.id.tvname2,item.name?:"")
    }
}