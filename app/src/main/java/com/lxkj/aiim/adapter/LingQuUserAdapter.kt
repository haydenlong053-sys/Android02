package com.lxkj.aiim.adapter

import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cboda.intelligent.R
import com.cboda.baselibrary.bean.CommonResultBroadBean
import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.bumptech.glide.Glide


class LingQuUserAdapter(layoutResId: Int) :
    BaseQuickAdapter<CommonResultBroadBean.CommonResult, BaseViewHolder>(layoutResId) {
    override fun convert(holder: BaseViewHolder, item: CommonResultBroadBean.CommonResult) {
        holder.setGone(R.id.space, holder.layoutPosition == 0)
        Glide.with(context)
            .load(item.icon ?: "")
            .placeholder(com.cboda.baselibrary.R.mipmap.ic_logo_login)
            .into(holder.getView(R.id.ivTouxiang))
        holder.setText(R.id.tvname, item.name ?: "")
        holder.setText(R.id.tvprice, item.money)
        holder.setText(R.id.tvnicheng, item.time ?: "")
        val tvColor =
            context.getColor(if (item.userId == MMKVBroadUtils.getRealUserId()) R.color.color_73DBE2 else com.luck.picture.lib.R.color.ps_color_white)
        holder.getView<TextView>(R.id.tvprice).setTextColor(tvColor)
        holder.getView<TextView>(R.id.tvpriceAfter).setTextColor(tvColor)
    }
}