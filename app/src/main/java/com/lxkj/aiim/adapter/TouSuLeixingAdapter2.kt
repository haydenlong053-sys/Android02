package com.lxkj.aiim.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lxkj.aiim.R
import com.cboda.baselibrary.bean.CommonDataListBean

class TouSuLeixingAdapter2(layoutResId: Int) :
    BaseQuickAdapter<CommonDataListBean.CommonBean, BaseViewHolder>(layoutResId) {
    override fun convert(holder: BaseViewHolder, item: CommonDataListBean.CommonBean) {
        holder.setText(R.id.tousuleixing,item.type?:"")
    }
}