package com.lxkj.aiim.adapter

import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lxkj.aiim.R
import com.base.commonlibrary.bean.CommonResultBean
import com.base.commonlibrary.utils.MMKVUtils
import com.bumptech.glide.Glide
import com.lxkj.aiim.utils.GlideLoaderHelper
import com.lxkj.aiim.utils.PixelUtils

class LingQuUserAdapter(layoutResId: Int) :
    BaseQuickAdapter<CommonResultBean.CommonResult, BaseViewHolder>(layoutResId) {
    override fun convert(holder: BaseViewHolder, item: CommonResultBean.CommonResult) {
        holder.setGone(R.id.space, holder.layoutPosition == 0)
        Glide.with(context)
            .load(item.icon ?: "")
            .placeholder(com.base.commonlibrary.R.mipmap.ic_logo_login)
            .into(holder.getView(R.id.ivTouxiang))
        holder.setText(R.id.tvname, item.name ?: "")
        holder.setText(R.id.tvprice, item.money)
        holder.setText(R.id.tvnicheng, item.time ?: "")
        val tvColor =
            context.getColor(if (item.userId == MMKVUtils.getRealUserId()) R.color.color_73DBE2 else com.luck.picture.lib.R.color.ps_color_white)
        holder.getView<TextView>(R.id.tvprice).setTextColor(tvColor)
        holder.getView<TextView>(R.id.tvpriceAfter).setTextColor(tvColor)
    }
}