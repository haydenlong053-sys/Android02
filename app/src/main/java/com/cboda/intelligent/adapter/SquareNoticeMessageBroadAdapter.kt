package com.cboda.intelligent.adapter

import androidx.core.view.isVisible
import com.cboda.baselibrary.utils.anyToInt
import com.cboda.baselibrary.widge.VpGradeView
import com.cboda.intelligent.R
import com.cboda.intelligent.bean.SquareMessageBean
import com.cboda.intelligent.utils.TimeBroadUtils
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder

class SquareNoticeMessageBroadAdapter :
    BaseQuickAdapter<SquareMessageBean, BaseViewHolder>(R.layout.item_square_notice_message) {

    init {
        addChildClickViewIds(R.id.noticeItemContent, R.id.noticeDeleteBtn)
    }

    override fun convert(holder: BaseViewHolder, item: SquareMessageBean) {
        holder.setText(R.id.noticeUserName, item.displayName())
        holder.setText(R.id.noticeContent, item.displayMessage())
        holder.setText(R.id.noticeTime, TimeBroadUtils.formatNoticeTime(item.time))
        holder.getView<android.view.View>(R.id.noticeUnreadDot).isVisible = item.isUnread()

        val avatar = holder.getView<VpGradeView>(R.id.noticeAvatar)
        if (item.isSystemMessage()) {
            avatar.setData3(false, false, null, null)
            avatar.getUserImgView().setImageResource(com.cboda.baselibrary.R.mipmap.icon_app_notice)
        } else {
            avatar.setData(
                item.icon,
                anyToInt(item.isLiang) == 1,
                anyToInt(item.isvip) == 1,
                item.displayScore(),
                null,
            )
        }
    }

    fun markAllRead() {
        val updated = data.map { item ->
            if (item.isUnread()) item.copy(isRead = "1") else item
        }
        setList(updated)
    }

    fun markRead(messageId: String) {
        val index = data.indexOfFirst { it.id == messageId }
        if (index < 0) return
        val item = data[index]
        if (!item.isUnread()) return
        setData(index, item.copy(isRead = "1"))
    }
}
