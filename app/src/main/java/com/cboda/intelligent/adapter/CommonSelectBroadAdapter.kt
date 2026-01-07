package com.cboda.intelligent.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cboda.intelligent.R
import com.cboda.intelligent.bean.CommonSelectBroadBean

class CommonSelectBroadAdapter(layoutResId: Int =R.layout.item_rv_common_select) :
    BaseQuickAdapter<CommonSelectBroadBean, BaseViewHolder>(layoutResId) {
    override fun convert(holder: BaseViewHolder, item: CommonSelectBroadBean) {
            holder.setText(R.id.mTvName,item.title)

    }
}