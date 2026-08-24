package com.cboda.intelligent.adapter.im

import android.widget.EditText
import android.widget.ImageView
import androidx.core.view.isVisible
import androidx.core.widget.doAfterTextChanged
import com.cboda.baselibrary.utils.loadImg
import com.cboda.intelligent.R
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.freddy.im.http.model.im.GroupMemberItem

data class GroupBlacklistCandidateUi(
    val member: GroupMemberItem,
    var selected: Boolean = false,
    var reason: String = "",
)

class GroupBlacklistCandidateAdapter :
    BaseQuickAdapter<GroupBlacklistCandidateUi, BaseViewHolder>(R.layout.item_group_blacklist_candidate) {

    init {
        addChildClickViewIds(R.id.rowUser, R.id.ivCheck)
    }

    override fun convert(holder: BaseViewHolder, item: GroupBlacklistCandidateUi) {
        val member = item.member
        holder.setText(R.id.tvName, member.displayName)
        val idText = member.username?.takeIf { it.isNotBlank() } ?: member.userId.orEmpty()
        holder.setText(R.id.tvUserId, "ID: $idText")
        holder.getView<ImageView>(R.id.ivAvatar).loadImg(member.headPortrait.orEmpty())
        holder.getView<ImageView>(R.id.ivCheck).setImageResource(
            if (item.selected) R.mipmap.ic_broad_yixuanz else R.mipmap.ic_broad_weixuanz,
        )
        val etReason = holder.getView<EditText>(R.id.etReason)
        etReason.isVisible = item.selected
        if (etReason.tag != holder.bindingAdapterPosition) {
            etReason.tag = holder.bindingAdapterPosition
            etReason.setText(item.reason)
            etReason.doAfterTextChanged { editable ->
                val pos = holder.bindingAdapterPosition
                if (pos in data.indices) {
                    data[pos].reason = editable?.toString().orEmpty()
                }
            }
        } else if (etReason.text?.toString() != item.reason && !etReason.hasFocus()) {
            etReason.setText(item.reason)
        }
    }
}
