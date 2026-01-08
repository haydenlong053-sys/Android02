package com.cboda.intelligent.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cboda.intelligent.R
import com.cboda.intelligent.bean.CheckResultBroadItem

/**
 * 检测结果列表适配器
 */
class CheckResultBroadAdapter(
    layoutResId: Int = R.layout.item_check_broad_result
) : BaseQuickAdapter<CheckResultBroadItem, BaseViewHolder>(layoutResId) {

    override fun convert(holder: BaseViewHolder, item: CheckResultBroadItem) {
        // 设置标题
        holder.setText(R.id.tvTitle, item.title)
        
        // 设置描述
        holder.setText(R.id.tvDescription, item.description)
        
        // 设置状态标签
        if (!item.isNormal) {
            // 异常状态 - 红色背景
            holder.setBackgroundResource(R.id.tvStatus, R.drawable.bg_status_abnormal)
            holder.setText(R.id.tvStatus, item.stateDes)
            holder.setTextColorRes(R.id.tvStatus, com.cboda.baselibrary.R.color.color_EE7171)
        } else {
            // 正常状态 - 绿色背景
            holder.setBackgroundResource(R.id.tvStatus, R.drawable.bg_status_normal)
            holder.setText(R.id.tvStatus, item.stateDes)
            holder.setTextColorRes(R.id.tvStatus, com.cboda.baselibrary.R.color.color_3FCA37)
        }
    }
}
