package com.freddy.chat.search.viewholder

import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.cboda.chatui.R
import com.cboda.chatui.search.viewHolder.ResultItemViewBroadHolder
import com.cboda.chatui.third.utils.UIBroadUtils
import com.freddy.im.http.model.im.SessionItem

/** 自研群搜索项 ViewHolder（复用野火群搜索布局）。 */
class ImGroupSearchViewHolder(fragment: Fragment, itemView: View) :
    ResultItemViewBroadHolder<SessionItem>(fragment, itemView) {

    private val portraitImageView: ImageView = itemView.findViewById(R.id.portraitImageView)
    private val nameTextView: TextView = itemView.findViewById(R.id.nameTextView)
    private val descTextView: TextView = itemView.findViewById(R.id.descTextView)
    private val dividerLine: View = itemView.findViewById(R.id.dividerLine)
    private val mLlItemBg: View = itemView.findViewById(R.id.mLlItemBg)

    override fun onBind(keyword: String, item: SessionItem) {
        bindContent(keyword, item)
    }

    override fun onBind2(
        keyword: String,
        item: SessionItem,
        currentPosition: Int,
        startPosition: Int,
        endPosition: Int
    ) {
        bindContent(keyword, item)
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

    private fun bindContent(keyword: String, item: SessionItem) {
        val name = item.sessionName?.takeIf { it.isNotBlank() } ?: item.sessionId ?: ""
        nameTextView.text = name
        Glide.with(fragment)
            .load(item.icon)
            .placeholder(R.mipmap.ic_group_broad_chat)
            .transforms(
                CenterCrop(),
                RoundedCorners(UIBroadUtils.dip2Px(fragment.requireContext(), 4))
            )
            .into(portraitImageView)
        descTextView.text = if (!TextUtils.isEmpty(keyword)) {
            "群名称包含: $keyword"
        } else {
            ""
        }
    }
}
