package com.cboda.intelligent.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cboda.intelligent.R

class ShuZiBroadAdapter(layoutResId: Int) :
    BaseQuickAdapter<String, BaseViewHolder>(layoutResId) {
    override fun convert(holder: BaseViewHolder, item: String) {
        holder.setText(R.id.tvshuzi,item)
    }
}