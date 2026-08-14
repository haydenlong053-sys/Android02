package com.freddy.chat.search.viewholder

import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.cboda.baselibrary.view.AppRoundBroadImage
import com.cboda.chatui.R
import com.cboda.chatui.search.viewHolder.ResultItemViewBroadHolder
import com.freddy.im.MessageType
import com.freddy.im.bean.AppMessage
import com.freddy.im.bean.ImConversationSearchResult
import com.freddy.im.message.MessageContentFormatter

/** 自研聊天记录搜索项 ViewHolder（复用野火会话搜索布局）。 */
class ImConversationSearchViewHolder(fragment: Fragment, itemView: View) :
    ResultItemViewBroadHolder<ImConversationSearchResult>(fragment, itemView) {

    private val portraitImageView: AppRoundBroadImage = itemView.findViewById(R.id.portraitImageView)
    private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
    private val descTextView: TextView = itemView.findViewById(R.id.descTextView)
    private val dividerLine: View = itemView.findViewById(R.id.dividerLine)
    private val mLlItemBg: View = itemView.findViewById(R.id.mLlItemBg)

    override fun onBind(keyword: String, item: ImConversationSearchResult) {
        bindContent(item)
    }

    override fun onBind2(
        keyword: String,
        item: ImConversationSearchResult,
        currentPosition: Int,
        startPosition: Int,
        endPosition: Int
    ) {
        bindContent(item)
        dividerLine.visibility = if (currentPosition == endPosition) View.GONE else View.VISIBLE
        when {
            currentPosition == startPosition + 1 && currentPosition == endPosition ->
                mLlItemBg.setBackgroundResource(R.drawable.bg_rect_white_broad_16dp)
            currentPosition == startPosition + 1 ->
                mLlItemBg.setBackgroundResource(R.drawable.bg_rect_white_only_top_broad_16dp)
            currentPosition == endPosition ->
                mLlItemBg.setBackgroundResource(R.drawable.bg_rect_white_only_bottom_broad_16dp)
            else -> mLlItemBg.setBackgroundResource(R.color.white)
        }
    }

    private fun bindContent(item: ImConversationSearchResult) {
        val session = item.sessionItem
        val name = session?.sessionName?.takeIf { it.isNotBlank() }
            ?: session?.sessionId
            ?: ""
        nameTextView.text = name
        val icon = session?.icon
        val headDefault = com.cboda.baselibrary.R.mipmap.ic_head_broad_defaut
        val placeholder = if (session != null && session.conversation == MessageType.GROUP_CHAT.msgType) {
            R.mipmap.ic_group_broad_chat
        } else {
            headDefault
        }
        Glide.with(fragment)
            .load(icon)
            .placeholder(placeholder)
            .error(placeholder)
            .into(portraitImageView)
        descTextView.text = buildDesc(item)
    }

    private fun buildDesc(item: ImConversationSearchResult): CharSequence {
        val message: AppMessage = item.latestMatchedMessage ?: return ""
        return MessageContentFormatter.formatDisplayText(message, null, null)
    }
}
