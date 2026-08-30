package com.freddy.chat.viewholder

import android.text.TextUtils
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.bumptech.glide.Glide
import com.cboda.baselibrary.utils.click
import com.cboda.baselibrary.utils.loadImg
import com.cboda.chatui.R
import com.freddy.chat.group.GroupInviteClickHelper
import com.freddy.chat.utils.GroupInviteMessageContentUtil
import com.freddy.im.bean.AppMessage

/**
 * 邀请进群消息 UI（contentType=730，[com.freddy.chat.MessageListAdapter] VIEW_TYPE_INVITE_JOIN_GROUP）。
 */
class InviteJoinGroupMessageVH(itemView: View) : BaseMessageVH(itemView, R.layout.conversation_item_group_invite_card_receive, false) {
    private var boundContentLayoutRes = 0
    private var cardContentLayout: View? = null
    private var portraitImageView: ImageView? = null
    private var nameTextView: TextView? = null
    private var descTextView: TextView? = null
    private var inviteGroupId = ""
    private var inviteGroupName = ""
    private var inviteGroupIcon = ""
    private var inviteGroupIdNum = ""

    init {
        cacheViews()
    }

    fun bind(message: AppMessage, position: Int, context: MessageViewHolderContext) {
        val sentByMe = message.isSender(context.currentUserId)
        val layoutRes = if (sentByMe)
            R.layout.conversation_item_group_invite_card_send
        else
            R.layout.conversation_item_group_invite_card_receive
        if (boundContentLayoutRes != layoutRes) {
            inflateContentLayout(layoutRes)
            boundContentLayoutRes = layoutRes
            cacheViews()
            bindClick()
        }
        bindCommon(message, position, context)

        val content = message.content
        inviteGroupId = GroupInviteMessageContentUtil.resolveGroupId(content)
        inviteGroupName = GroupInviteMessageContentUtil.resolveGroupName(content)
        inviteGroupIcon = GroupInviteMessageContentUtil.resolveGroupIcon(content)
        inviteGroupIdNum = GroupInviteMessageContentUtil.resolveGroupIdno(content)
        nameTextView?.text = inviteGroupName
        if (sentByMe) {
            var toName: String? = GroupInviteMessageContentUtil.resolveToName(content)
            if (TextUtils.isEmpty(toName) && !TextUtils.isEmpty(context.peerDisplayName)) {
                toName = context.peerDisplayName
            }
            if (TextUtils.isEmpty(toName)) {
                toName = GroupInviteMessageContentUtil.resolveTo(content)
            }
            if (TextUtils.isEmpty(toName)) {
                toName = message.toId ?: ""
            }
            descTextView?.text = itemView.context.getString(
                com.cboda.baselibrary.R.string.group_invite_card_desc_sent,
                toName
            )
        } else {
            var fromName = GroupInviteMessageContentUtil.resolveFromName(content)
            if (TextUtils.isEmpty(fromName)) {
                fromName = GroupInviteMessageContentUtil.resolveFrom(content)
            }
            descTextView?.text = itemView.context.getString(
                com.cboda.baselibrary.R.string.group_invite_card_desc,
                fromName
            )
        }
        portraitImageView?.loadImg(inviteGroupIcon, R.mipmap.ic_group_broad_chat)
    }

    private fun cacheViews() {
        cardContentLayout = contentView.findViewById(R.id.contentLayout)
        portraitImageView = contentView.findViewById(R.id.groupInvitePortraitImageView)
        nameTextView = contentView.findViewById(R.id.groupInviteNameTextView)
        descTextView = contentView.findViewById(R.id.groupInviteDescTextView)
        bindClick()
    }

    private fun bindClick() {
        itemView.click {
            onInviteClick()
        }
    }

    private fun onInviteClick() {
        if (TextUtils.isEmpty(inviteGroupId)) {
            return
        }
        GroupInviteClickHelper.handleClick(
            itemView.context,
            inviteGroupId,
            inviteGroupName,
            inviteGroupIcon,
            inviteGroupIdNum
        )
    }
}
