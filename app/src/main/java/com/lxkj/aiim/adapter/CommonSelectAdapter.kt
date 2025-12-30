package com.lxkj.aiim.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.lxkj.aiim.R
import com.lxkj.aiim.lj.bean.CommonSelectBean

class CommonSelectAdapter(layoutResId: Int =R.layout.item_rv_common_select) :
    BaseQuickAdapter<CommonSelectBean, BaseViewHolder>(layoutResId) {
    override fun convert(holder: BaseViewHolder, item: CommonSelectBean) {
            holder.setText(R.id.mTvName,item.title)

    }
}