package com.freddy.chat.viewholder;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.intelligent.R;
import com.freddy.chat.utils.AvatarViews;
import com.freddy.chat.utils.WeChatUiHelper;
import com.freddy.im.IMSConfig;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.http.model.im.GroupMemberItem;

public class BaseMessageVH extends RecyclerView.ViewHolder {
    public final TextView tvTimeDivider;
    public final TextView tvNickname;
    public final View avatarLeft;
    public final View avatarRight;
    public final LinearLayout messageColumn;
    public final LinearLayout bubbleRow;
    public final ProgressBar progress;
    public final TextView tvTimeInline;
    public final TextView tvReadStatus;
    public final FrameLayout flMessageContentHost;
    public final View contentView;

    public BaseMessageVH(@NonNull View itemView, @LayoutRes int contentLayoutRes) {
        super(itemView);
        tvTimeDivider = itemView.findViewById(R.id.tv_time_divider);
        tvNickname = itemView.findViewById(R.id.tv_nickname);
        avatarLeft = itemView.findViewById(R.id.avatar_left);
        avatarRight = itemView.findViewById(R.id.avatar_right);
        messageColumn = itemView.findViewById(R.id.message_column);
        bubbleRow = itemView.findViewById(R.id.bubble_row);
        progress = itemView.findViewById(R.id.progress);
        tvTimeInline = itemView.findViewById(R.id.tv_time_inline);
        tvReadStatus = itemView.findViewById(R.id.tv_read_status);
        flMessageContentHost = itemView.findViewById(R.id.fl_message_content_host);
        contentView = LayoutInflater.from(itemView.getContext())
                .inflate(contentLayoutRes, flMessageContentHost, false);
        flMessageContentHost.addView(contentView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    public void bindCommon(@NonNull AppMessage message, int position, @NonNull MessageViewHolderContext context) {
        boolean sentByMe = message.isSender(context.currentUserId);
        String senderId = message.getFromId();
        GroupMemberItem member = context.resolveMember(senderId);
        AppMessage prev = context.getPrevMessage(position);

        long currentMillis = WeChatUiHelper.normalizeToMillis(message.getTimestamp());
        long prevMillis = 0L;
        if (prev != null && !WeChatUiHelper.isSystemMessage(prev)) {
            prevMillis = WeChatUiHelper.normalizeToMillis(prev.getTimestamp());
        }
        if (WeChatUiHelper.shouldShowTimeDivider(prevMillis, currentMillis)) {
            tvTimeDivider.setVisibility(View.VISIBLE);
            tvTimeDivider.setText(WeChatUiHelper.formatMessageTime(message.getTimestamp()));
        } else {
            tvTimeDivider.setVisibility(View.GONE);
        }

        String timeLabel = WeChatUiHelper.formatMessageTime(message.getTimestamp());
        if (!timeLabel.isEmpty()) {
            tvTimeInline.setVisibility(View.VISIBLE);
            tvTimeInline.setText(timeLabel);
            tvTimeInline.setGravity(sentByMe ? Gravity.END : Gravity.START);
        } else {
            tvTimeInline.setVisibility(View.GONE);
        }

        if (sentByMe) {
            tvNickname.setVisibility(View.GONE);
            avatarLeft.setVisibility(View.GONE);
            avatarRight.setVisibility(View.VISIBLE);
            AvatarViews.bind(avatarRight, context.selfAvatarUrl, "我", null);
            alignMessageColumn(Gravity.END);
            bubbleRow.setGravity(Gravity.BOTTOM);
            flMessageContentHost.setBackgroundResource(R.drawable.bg_bubble_sent);
            progress.setVisibility(message.getSendStatus() == IMSConfig.SEND_STATUS_SENDING
                    ? View.VISIBLE : View.GONE);
            bindReadReceipt(message, context, true);
            return;
        }

        boolean showNickname = context.isGroupChat
                && MessageViewHolderContext.shouldShowGroupNickname(prev, senderId, context.currentUserId);
        if (showNickname) {
            tvNickname.setVisibility(View.VISIBLE);
            tvNickname.setText(resolveDisplayName(member, senderId));
            tvNickname.setGravity(Gravity.START);
        } else {
            tvNickname.setVisibility(View.GONE);
        }
        avatarLeft.setVisibility(View.VISIBLE);
        avatarRight.setVisibility(View.GONE);
        String avatarUrl = member != null && !TextUtils.isEmpty(member.getHeadPortrait())
                ? member.getHeadPortrait()
                : context.peerAvatarUrl;
        String nickLabel = member != null ? member.getNickname() : null;
        AvatarViews.bind(avatarLeft, avatarUrl, nickLabel, senderId);
        alignMessageColumn(Gravity.START);
        bubbleRow.setGravity(Gravity.START);
        flMessageContentHost.setBackgroundResource(R.drawable.bg_bubble_received);
        progress.setVisibility(View.GONE);
        tvReadStatus.setVisibility(View.GONE);
    }

    private void bindReadReceipt(@NonNull AppMessage message,
                                 @NonNull MessageViewHolderContext context,
                                 boolean sentByMe) {
        if (!sentByMe) {
            tvReadStatus.setVisibility(View.GONE);
            return;
        }
        tvReadStatus.setGravity(Gravity.END);
        if (context.isGroupChat) {
            int percent = context.readUiState.getGroupReadPercent(
                    message, context.currentUserId, context.groupMemberCount);
            if (percent < 0) {
                tvReadStatus.setVisibility(View.GONE);
                return;
            }
            tvReadStatus.setVisibility(View.VISIBLE);
            tvReadStatus.setText(itemView.getContext().getString(
                    R.string.message_read_group_percent, percent));
            return;
        }
        if (context.readUiState.shouldShowPeerRead(message, context.currentUserId)) {
            tvReadStatus.setVisibility(View.VISIBLE);
            tvReadStatus.setText(R.string.message_read_peer);
        } else {
            tvReadStatus.setVisibility(View.GONE);
        }
    }

    private void alignMessageColumn(int gravity) {
        messageColumn.setGravity(gravity);
        ViewGroup.LayoutParams lp = bubbleRow.getLayoutParams();
        if (lp instanceof LinearLayout.LayoutParams) {
            ((LinearLayout.LayoutParams) lp).gravity = gravity;
            bubbleRow.setLayoutParams(lp);
        }
    }

    @NonNull
    private static String resolveDisplayName(@Nullable GroupMemberItem member, @Nullable String userId) {
        if (member != null && !TextUtils.isEmpty(member.getNickname())) {
            return member.getNickname();
        }
        return TextUtils.isEmpty(userId) ? "" : userId;
    }
}
