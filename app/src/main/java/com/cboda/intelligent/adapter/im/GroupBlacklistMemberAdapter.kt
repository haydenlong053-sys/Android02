package com.cboda.intelligent.adapter.im

import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.isVisible
import com.cboda.baselibrary.utils.loadImg
import com.cboda.intelligent.R
import com.cboda.intelligent.bean.GroupBlacklistItem
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class GroupBlacklistMemberAdapter :
    BaseQuickAdapter<GroupBlacklistItem, BaseViewHolder>(R.layout.item_group_blacklist_member) {

    init {
        addChildClickViewIds(R.id.btnRemove)
    }

    override fun convert(holder: BaseViewHolder, item: GroupBlacklistItem) {
        holder.getView<ImageView>(R.id.ivAvatar).loadImg(item.avatarUrl())
        holder.setText(R.id.tvName, item.displayName())
        holder.setText(R.id.tvUserId, "ID: ${item.displayId()}")
        val reason = item.displayReason()
        val time = item.displayTime()
        val meta = buildString {
            if (reason.isNotBlank()) {
                append(holder.itemView.context.getString(com.cboda.baselibrary.R.string.group_blacklist_reason_prefix, reason))
            }
            if (time.isNotBlank()) {
                if (isNotEmpty()) append('\n')
                append(holder.itemView.context.getString(com.cboda.baselibrary.R.string.group_blacklist_time_prefix, time))
            }
        }
        val tvMeta = holder.getView<TextView>(R.id.tvMeta)
        tvMeta.isVisible = meta.isNotBlank()
        tvMeta.text = meta
    }
}
