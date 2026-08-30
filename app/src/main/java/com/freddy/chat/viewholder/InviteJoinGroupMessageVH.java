package com.freddy.chat.viewholder;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.cboda.chatui.R;
import com.cboda.intelligent.utils.JumpBroadUtils;
import com.freddy.chat.utils.GroupInviteMessageContentUtil;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.http.GroupVoSessionMapper;
import com.freddy.im.http.model.im.GroupVo;

/**
 * 邀请进群消息 UI（contentType=730，{@link com.freddy.chat.MessageListAdapter} VIEW_TYPE_INVITE_JOIN_GROUP）。
 */
public final class InviteJoinGroupMessageVH extends BaseMessageVH {

    private int boundContentLayoutRes;
    private View cardContentLayout;
    private ImageView portraitImageView;
    private TextView nameTextView;
    private TextView descTextView;
    @NonNull
    private String inviteGroupId = "";

    public InviteJoinGroupMessageVH(@NonNull View itemView) {
        super(itemView, R.layout.conversation_item_group_invite_card_receive, false);
        cacheViews();
    }

    public void bind(@NonNull AppMessage message, int position, @NonNull MessageViewHolderContext context) {
        boolean sentByMe = message.isSender(context.currentUserId);
        int layoutRes = sentByMe
                ? R.layout.conversation_item_group_invite_card_send
                : R.layout.conversation_item_group_invite_card_receive;
        if (boundContentLayoutRes != layoutRes) {
            inflateContentLayout(layoutRes);
            boundContentLayoutRes = layoutRes;
            cacheViews();
            bindClick();
        }
        bindCommon(message, position, context);

        String content = message.getContent();
        inviteGroupId = GroupInviteMessageContentUtil.resolveGroupId(content);
        nameTextView.setText(GroupInviteMessageContentUtil.resolveGroupName(content));
        if (sentByMe) {
            String toName = GroupInviteMessageContentUtil.resolveToName(content);
            if (TextUtils.isEmpty(toName) && !TextUtils.isEmpty(context.peerDisplayName)) {
                toName = context.peerDisplayName;
            }
            if (TextUtils.isEmpty(toName)) {
                toName = GroupInviteMessageContentUtil.resolveTo(content);
            }
            if (TextUtils.isEmpty(toName)) {
                toName = message.getToId() != null ? message.getToId() : "";
            }
            descTextView.setText(
                    itemView.getContext().getString(
                            com.cboda.baselibrary.R.string.group_invite_card_desc_sent,
                            toName));
        } else {
            String fromName = GroupInviteMessageContentUtil.resolveFromName(content);
            if (TextUtils.isEmpty(fromName)) {
                fromName = GroupInviteMessageContentUtil.resolveFrom(content);
            }
            descTextView.setText(
                    itemView.getContext().getString(
                            com.cboda.baselibrary.R.string.group_invite_card_desc,
                            fromName));
        }
        Glide.with(portraitImageView.getContext())
                .load(GroupInviteMessageContentUtil.resolveGroupIcon(content))
                .placeholder(R.mipmap.ic_group_broad_chat)
                .into(portraitImageView);
    }

    private void cacheViews() {
        cardContentLayout = contentView.findViewById(R.id.contentLayout);
        portraitImageView = contentView.findViewById(R.id.groupInvitePortraitImageView);
        nameTextView = contentView.findViewById(R.id.groupInviteNameTextView);
        descTextView = contentView.findViewById(R.id.groupInviteDescTextView);
        bindClick();
    }

    private void bindClick() {
        if (cardContentLayout == null) {
            return;
        }
        cardContentLayout.setOnClickListener(v -> onInviteClick());
    }

    private void onInviteClick() {
        if (TextUtils.isEmpty(inviteGroupId)) {
            return;
        }
        GroupVo vo = new GroupVo();
        vo.setId(inviteGroupId);
        JumpBroadUtils.Companion.startChatMsgActivity(
                GroupVoSessionMapper.toSessionItem(vo, null, inviteGroupId),
                itemView.getContext());
    }
}
