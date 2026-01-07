package com.cboda.intelligent.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cboda.intelligent.R

/**
 * 简述： <功能描述>
 * Created by ysh on 2023/3/21.
 */
class SelectTypeBroadAdapter(layoutResId: Int) :
    BaseQuickAdapter<String, BaseViewHolder>(layoutResId) {
    override fun convert(holder: BaseViewHolder, item: String) {
        holder.setText(R.id.tvTypeName, item)
    }
}