package com.lxkj.aiim.lj.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cboda.intelligent.R
import com.lxkj.aiim.lj.bean.CheckResultItem

/**
 * 检测结果列表适配器
 */
class CheckResultAdapter(
    layoutResId: Int = R.layout.item_check_result
) : BaseQuickAdapter<CheckResultItem, BaseViewHolder>(layoutResId) {

    override fun convert(holder: BaseViewHolder, item: CheckResultItem) {
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
