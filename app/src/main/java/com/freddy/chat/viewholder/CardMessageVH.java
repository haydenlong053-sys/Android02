package com.freddy.chat.viewholder;

import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.cboda.chatui.R;
import com.cboda.intelligent.utils.JumpBroadUtils;
import com.freddy.chat.utils.CardMessageContentUtil;
import com.freddy.chat.utils.GroupInviteMessageContentUtil;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.http.GroupVoSessionMapper;
import com.freddy.im.http.model.im.GroupVo;

/**
 * 个人名片 / 群邀请卡片（contentType=700，按 content.msg 区分）。
 */
public final class CardMessageVH extends BaseMessageVH {

    private int boundContentLayoutRes;
    private View cardContentLayout;
    private ImageView portraitImageView;
    private TextView nameTextView;
    private TextView userIdTextView;
    private TextView groupInviteDescTextView;
    private boolean boundAsGroupInvite;
    @NonNull
    private String cardTargetUserId = "";
    @NonNull
    private String inviteGroupId = "";

    public CardMessageVH(@NonNull View itemView) {
        super(itemView, R.layout.conversation_item_user_card_broad_receive, false);
        cacheCardViews(false);
    }

    public void bind(@NonNull AppMessage message, int position, @NonNull MessageViewHolderContext context) {
        boolean sentByMe = message.isSender(context.currentUserId);
        String content = message.getContent();
        boolean groupInvite = GroupInviteMessageContentUtil.isGroupInviteContent(content);
        int layoutRes;
        if (groupInvite) {
            layoutRes = sentByMe
                    ? R.layout.conversation_item_group_invite_card_send
                    : R.layout.conversation_item_group_invite_card_receive;
        } else {
            layoutRes = sentByMe
                    ? R.layout.conversation_item_user_card_broad_send
                    : R.layout.conversation_item_user_card_broad_receive;
        }
        if (boundContentLayoutRes != layoutRes || boundAsGroupInvite != groupInvite) {
            inflateContentLayout(layoutRes);
            boundContentLayoutRes = layoutRes;
            boundAsGroupInvite = groupInvite;
            cacheCardViews(groupInvite);
            bindCardClick(groupInvite);
        }
        bindCommon(message, position, context);

        if (groupInvite) {
            bindGroupInvite(content);
        } else {
            bindUserCard(content);
        }
    }

    private void bindUserCard(@NonNull String content) {
        cardTargetUserId = CardMessageContentUtil.resolveUserId(content);
        inviteGroupId = "";
        nameTextView.setText(CardMessageContentUtil.resolveDisplayName(content));
        String idno = CardMessageContentUtil.resolveIdno(content);
        if (userIdTextView != null) {
            if (TextUtils.isEmpty(idno)) {
                userIdTextView.setText("");
            } else {
                userIdTextView.setText("ID:" + idno);
            }
        }
        Glide.with(portraitImageView.getContext())
                .load(CardMessageContentUtil.resolvePortrait(content))
                .placeholder(R.mipmap.avatar_broad_def)
                .into(portraitImageView);
    }

    private void bindGroupInvite(@NonNull String content) {
        cardTargetUserId = "";
        inviteGroupId = GroupInviteMessageContentUtil.resolveGroupId(content);
        nameTextView.setText(GroupInviteMessageContentUtil.resolveGroupName(content));
        String fromName = GroupInviteMessageContentUtil.resolveFromName(content);
        if (TextUtils.isEmpty(fromName)) {
            fromName = GroupInviteMessageContentUtil.resolveFrom(content);
        }
        if (groupInviteDescTextView != null) {
            groupInviteDescTextView.setText(
                    itemView.getContext().getString(
                            com.cboda.baselibrary.R.string.group_invite_card_desc,
                            fromName));
        }
        Glide.with(portraitImageView.getContext())
                .load(GroupInviteMessageContentUtil.resolveGroupIcon(content))
                .placeholder(R.mipmap.avatar_broad_def)
                .into(portraitImageView);
    }

    private void cacheCardViews(boolean groupInvite) {
        cardContentLayout = contentView.findViewById(R.id.contentLayout);
        if (groupInvite) {
            portraitImageView = contentView.findViewById(R.id.groupInvitePortraitImageView);
            nameTextView = contentView.findViewById(R.id.groupInviteNameTextView);
            groupInviteDescTextView = contentView.findViewById(R.id.groupInviteDescTextView);
            userIdTextView = null;
        } else {
            portraitImageView = contentView.findViewById(R.id.userCardPortraitImageView);
            nameTextView = contentView.findViewById(R.id.userCardNameTextView);
            userIdTextView = contentView.findViewById(R.id.userIdTextView);
            groupInviteDescTextView = null;
        }
        bindCardClick(groupInvite);
    }

    private void bindCardClick(boolean groupInvite) {
        if (cardContentLayout == null) {
            return;
        }
        cardContentLayout.setOnClickListener(v -> {
            if (groupInvite) {
                onGroupInviteClick();
            } else {
                onUserCardClick();
            }
        });
    }

    private void onUserCardClick() {
        if (TextUtils.isEmpty(cardTargetUserId)) {
            return;
        }
        JumpBroadUtils.Companion.startHaoYouAddActivity(itemView.getContext(), cardTargetUserId, "2");
    }

    private void onGroupInviteClick() {
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
