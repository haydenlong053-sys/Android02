package com.cboda.intelligent.adapter

import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.cboda.intelligent.R
import com.cboda.intelligent.bean.AiResultListBean

class AiRecordListBroadAdapter(
    layoutResId: Int = R.layout.item_rv_ai_result_broad_record
) :
    BaseQuickAdapter<AiResultListBean, BaseViewHolder>(layoutResId) {
    override fun convert(holder: BaseViewHolder, item: AiResultListBean) {
        val name = if (item.fileNames?.isNotEmpty() == true && item.fileNames!!.size > 1) {
            item.fileNames!![0] + "等"
        } else if (item.fileNames?.isNotEmpty() == true) {
            item.fileNames!![0]
        } else {
            ""
        }
        holder.setText(R.id.tvFileName, name)
        holder.setText(R.id.tvTimestamp, item.createTime)
        when (item.resourceType) {
            "document" -> {
                holder.setImageResource(R.id.ivDocumentIcon, R.mipmap.ic_ai_r_broad_doc)
            }

            "image" -> {
                holder.setImageResource(R.id.ivDocumentIcon, R.mipmap.ic_ai_r_broad_img)
            }

            "voice" -> {
                holder.setImageResource(R.id.ivDocumentIcon, R.mipmap.ic_ai_r_broad_audio)
            }

            "video" -> {
                holder.setImageResource(R.id.ivDocumentIcon, R.mipmap.ic_ai_r_broad_video)
            }

            else -> {
                holder.setImageResource(R.id.ivDocumentIcon, R.mipmap.ic_ai_r_broad_doc)
            }
        }

        if (item.aiGenerated) {
            // 异常状态 - 红色背景
            holder.setBackgroundResource(R.id.tvStatus, R.drawable.bg_status_broad_abnormal)
            holder.setText(R.id.tvStatus, context.getString(R.string.abnormal))
            holder.setTextColorRes(R.id.tvStatus, com.cboda.baselibrary.R.color.color_EE7171)

        } else {
            // 正常状态 - 绿色背景
            holder.setBackgroundResource(R.id.tvStatus, R.drawable.bg_status_broad_normal)
            holder.setText(R.id.tvStatus, context.getString(R.string.normal))
            holder.setTextColorRes(R.id.tvStatus, com.cboda.baselibrary.R.color.color_3FCA37)
        }
    }
}