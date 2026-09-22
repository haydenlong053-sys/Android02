package com.freddy.chat.viewholder;

import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.cboda.baselibrary.utils.MMKVBroadUtils;
import com.cboda.baselibrary.widge.VpGradeView;
import com.cboda.chatui.third.utils.TimeBroadBroadUtils;
import com.cboda.intelligent.R;
import com.freddy.chat.im.MessageReadUiState;
import com.freddy.chat.menu.MessageContextMenuHelper;
import com.freddy.chat.menu.MessageListActions;
import com.freddy.chat.menu.MessageMenuPolicy;
import com.freddy.chat.utils.MessageAvatarBinder;
import com.freddy.chat.utils.WeChatUiHelper;
import com.freddy.im.IMSConfig;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.group.GroupRole;
import com.freddy.im.http.model.im.GroupMemberItem;
import com.hjq.shape.view.ShapeTextView;

public class BaseMessageVH extends RecyclerView.ViewHolder {
    public final TextView tvNickname;
    public final ConstraintLayout nameLayout;
    public final ShapeTextView joinTimeTv;
    public final VpGradeView avatarLeft;
    public final VpGradeView avatarRight;
    public final LinearLayout rowContainer;
    public final LinearLayout messageColumn;
    public final LinearLayout bubbleRow;
    public final LinearLayout errorLinearLayout;
    public final ProgressBar progress;
    public final TextView timeTextView;
    public final ImageView singleReceiptImageView;
    public final FrameLayout groupReceiptFrameLayout;
    public final ProgressBar readProgressBar;
    public final FrameLayout flMessageContentHost;
    public final CheckBox checkbox;
    public View contentView;

    private final boolean hostBubbleEnabled;
    @Nullable
    private AppMessage boundMessage;
    @Nullable
    private MessageViewHolderContext boundContext;
    @Nullable
    private View.OnLongClickListener contextMenuLongClickListener;

    public BaseMessageVH(@NonNull View itemView, @LayoutRes int contentLayoutRes) {
        this(itemView, contentLayoutRes, true);
    }

    public BaseMessageVH(@NonNull View itemView, @LayoutRes int contentLayoutRes, boolean hostBubbleEnabled) {
        super(itemView);
        this.hostBubbleEnabled = hostBubbleEnabled;
        tvNickname = itemView.findViewById(R.id.tv_nickname);
        nameLayout = itemView.findViewById(R.id.name_layout);
        joinTimeTv = itemView.findViewById(R.id.join_time_tv);
        avatarLeft = itemView.findViewById(R.id.avatar_left);
        avatarRight = itemView.findViewById(R.id.avatar_right);
        rowContainer = itemView.findViewById(R.id.row_container);
        messageColumn = itemView.findViewById(R.id.message_column);
        bubbleRow = itemView.findViewById(R.id.bubble_row);
        errorLinearLayout = itemView.findViewById(R.id.error_linear_layout);
        progress = itemView.findViewById(R.id.progress);
        timeTextView = itemView.findViewById(R.id.time_text_view);
        singleReceiptImageView = itemView.findViewById(R.id.single_receipt_image_view);
        groupReceiptFrameLayout = itemView.findViewById(R.id.group_receipt_frame_layout);
        readProgressBar = itemView.findViewById(R.id.read_progress_bar);
        flMessageContentHost = itemView.findViewById(R.id.fl_message_content_host);
        checkbox = itemView.findViewById(R.id.checkbox);
        errorLinearLayout.setOnClickListener(v -> onRetryClick());
        inflateContentLayout(contentLayoutRes);
    }

    protected void inflateContentLayout(@LayoutRes int contentLayoutRes) {
        flMessageContentHost.removeAllViews();
        contentView = LayoutInflater.from(itemView.getContext())
                .inflate(contentLayoutRes, flMessageContentHost, false);
        flMessageContentHost.addView(contentView, new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
    }

    public void bindCommon(@NonNull AppMessage message, int position, @NonNull MessageViewHolderContext context) {
        boundMessage = message;
        boundContext = context;
        boolean sentByMe = message.isSender(context.currentUserId);
        String senderId = message.getFromId();
        GroupMemberItem member = context.resolveMember(senderId);
//        AppMessage prev = context.getPrevMessage(position);

        bindMessageTime(message, sentByMe);
        updateRowInsets(sentByMe);
        updateContentHostMargin(sentByMe);
        bindSelectionMode(message, context);
        //消息发送方
        if (sentByMe) {
            nameLayout.setVisibility(View.GONE);
            avatarLeft.setVisibility(View.GONE);
            avatarRight.setVisibility(View.VISIBLE);
            MessageAvatarBinder.bindAvatar(avatarRight, context.selfAvatarUrl, member, context.isGroupChat, context.currentUserId);
            alignMessageColumn(Gravity.END);
            alignTimeTextView(Gravity.END);
            bubbleRow.setGravity(Gravity.BOTTOM | Gravity.END);
            applyHostBubbleBackground(true);
            bindSendStatus(message);
            bindReadReceipt(message, context, true);
            bindAvatarClick(context, senderId);
            return;
        }

//        boolean showNickname = context.isGroupChat
//                && MessageViewHolderContext.shouldShowGroupNickname(prev, senderId, context.currentUserId);
        //消息接收方
        boolean showNickname = context.isGroupChat;
        if (showNickname) {
            nameLayout.setVisibility(View.VISIBLE);
            tvNickname.setVisibility(View.VISIBLE);
            tvNickname.setText(resolveDisplayName(member, senderId));
            MessageAvatarBinder.bindJoinTime(joinTimeTv, true, member);
        } else {
            nameLayout.setVisibility(View.GONE);
            tvNickname.setVisibility(View.GONE);
            joinTimeTv.setVisibility(View.GONE);
        }
        avatarLeft.setVisibility(View.VISIBLE);
        avatarRight.setVisibility(View.GONE);
        if (context.isGroupChat){
            String avatarUrl = member != null && !TextUtils.isEmpty(member.getHeadPortrait())
                    ? member.getHeadPortrait():"";
            MessageAvatarBinder.bindAvatar(avatarLeft, avatarUrl, member, true, senderId);
        }else {
            String avatarUrl = context.peerAvatarUrl;
            MessageAvatarBinder.bindAvatar(avatarLeft, avatarUrl, member, false, senderId);
        }

        bindAvatarMentionLongClick(context, sentByMe, member, senderId);
        bindAvatarClick(context, senderId);
        alignMessageColumn(Gravity.START);
        alignTimeTextView(Gravity.START);
        bubbleRow.setGravity(Gravity.BOTTOM | Gravity.START);
        applyHostBubbleBackground(false);
        progress.setVisibility(View.GONE);
        errorLinearLayout.setVisibility(View.GONE);
        hideReadReceipt();
    }

    private void bindAvatarMentionLongClick(@NonNull MessageViewHolderContext context,
                                            boolean sentByMe,
                                            @Nullable GroupMemberItem member,
                                            @Nullable String senderId) {
        if (!context.isGroupChat || sentByMe || context.mentionMemberListener == null) {
            avatarLeft.setOnLongClickListener(null);
            return;
        }
        avatarLeft.setOnLongClickListener(v -> {
            String name = resolveDisplayName(member, senderId);
            if (!TextUtils.isEmpty(senderId) && !TextUtils.isEmpty(name)) {
                context.mentionMemberListener.onMentionMember(senderId, name);
                return true;
            }
            return false;
        });
    }

    private void bindAvatarClick(@NonNull MessageViewHolderContext context,
                                 @Nullable String senderId) {
        if (context.isCheckableMode()) {
            avatarLeft.setOnClickListener(null);
            avatarRight.setOnClickListener(null);
            return;
        }
        String keFuId = MMKVBroadUtils.Companion.getKeFuId();
        if (context.portraitClickListener == null
                || TextUtils.isEmpty(senderId)
                || TextUtils.equals(senderId,WeChatUiHelper.IM_GROUP_BOT)
                || (!TextUtils.isEmpty(keFuId) && TextUtils.equals(senderId, keFuId))) {
            avatarLeft.setOnClickListener(null);
            avatarRight.setOnClickListener(null);
            return;
        }
        View.OnClickListener listener = v -> context.portraitClickListener.onPortraitClick(senderId);
        avatarLeft.setOnClickListener(listener);
        avatarRight.setOnClickListener(listener);
    }

    private void bindMessageTime(@NonNull AppMessage message, boolean sentByMe) {
        long millis = WeChatUiHelper.normalizeToMillis(message.getTimestamp());
        if (millis <= 0L) {
            timeTextView.setVisibility(View.GONE);
            return;
        }
        timeTextView.setVisibility(View.VISIBLE);
        timeTextView.setText(TimeBroadBroadUtils.getMsgFormatTime(millis));
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) timeTextView.getLayoutParams();
        if (lp instanceof LinearLayout.LayoutParams) {
            LinearLayout.LayoutParams rowLp = (LinearLayout.LayoutParams) lp;
            int margin = itemView.getResources().getDimensionPixelSize(com.cboda.baselibrary.R.dimen.dp_60);
            if (sentByMe) {
                rowLp.gravity = Gravity.END;
                rowLp.setMarginStart(0);
                rowLp.setMarginEnd(margin);
            } else {
                rowLp.gravity = Gravity.START;
                rowLp.setMarginStart(margin);
                rowLp.setMarginEnd(0);
            }
            timeTextView.setLayoutParams(rowLp);
        }
    }

    private void alignTimeTextView(int gravity) {
        ViewGroup.LayoutParams lp = timeTextView.getLayoutParams();
        if (lp instanceof LinearLayout.LayoutParams) {
            ((LinearLayout.LayoutParams) lp).gravity = gravity;
            timeTextView.setLayoutParams(lp);
        }
    }

    private void bindSendStatus(@NonNull AppMessage message) {
        int status = message.getSendStatus();
        if (status == IMSConfig.SEND_STATUS_SENDING) {
            progress.setVisibility(View.VISIBLE);
            errorLinearLayout.setVisibility(View.GONE);
            return;
        }
        if (status == IMSConfig.SEND_STATUS_FAILED) {
            progress.setVisibility(View.GONE);
            errorLinearLayout.setVisibility(View.VISIBLE);
            return;
        }
        progress.setVisibility(View.GONE);
        errorLinearLayout.setVisibility(View.GONE);
    }

    private void onRetryClick() {
        if (boundMessage == null || boundContext == null || boundContext.retryListener == null) {
            return;
        }
        boundContext.retryListener.onRetryFailedMessage(boundMessage);
    }

    protected void applyHostBubbleBackground(boolean sentByMe) {
        if (!hostBubbleEnabled) {
            flMessageContentHost.setBackgroundResource(0);
            return;
        }
        flMessageContentHost.setBackgroundResource(sentByMe
                ? com.cboda.chatui.R.drawable.bg_bubble_broad_send
                : com.cboda.chatui.R.drawable.bg_bubble_broad_receive);
    }

    protected void applyBroadBubbleBackground(@NonNull View target, boolean sentByMe) {
        target.setBackgroundResource(sentByMe
                ? com.cboda.chatui.R.drawable.bg_bubble_broad_send
                : com.cboda.chatui.R.drawable.bg_bubble_broad_receive);
    }

    protected void applyWhiteBubbleBackground(@NonNull View target, boolean sentByMe) {
        target.setBackgroundResource(sentByMe
                ? com.cboda.chatui.R.drawable.bg_bubble_broad_send_white
                : com.cboda.chatui.R.drawable.bg_bubble_broad_receive);
    }

    private void bindReadReceipt(@NonNull AppMessage message,
                                 @NonNull MessageViewHolderContext context,
                                 boolean sentByMe) {
        if (!sentByMe || !MessageReadUiState.supportsReadReceipt(message)) {
            hideReadReceipt();
            return;
        }
        int sendStatus = message.getSendStatus();
        if (sendStatus == IMSConfig.SEND_STATUS_SENDING
                || sendStatus == IMSConfig.SEND_STATUS_FAILED) {
            hideReadReceipt();
            return;
        }
        // 自研暂不展示群聊已读回执；单聊圆点仍保留。
        if (context.isGroupChat) {
            hideReadReceipt();
        } else {
            bindSingleReadReceipt(message, context);
        }
    }

    private void bindSingleReadReceipt(@NonNull AppMessage message,
                                       @NonNull MessageViewHolderContext context) {
        groupReceiptFrameLayout.setVisibility(View.GONE);
        singleReceiptImageView.setVisibility(View.VISIBLE);
        if (context.readUiState.shouldShowPeerRead(message, context.currentUserId)) {
            singleReceiptImageView.setImageResource(
                    com.cboda.chatui.R.drawable.bg_circle_read_broad_icon2);
        } else {
            singleReceiptImageView.setImageResource(
                    com.cboda.chatui.R.drawable.bg_circle_unread_broad_icon);
        }
    }

    //群聊的已读回执逻辑（暂不使用）
    private void bindGroupReadReceipt(@NonNull AppMessage message,
                                      @NonNull MessageViewHolderContext context) {
        singleReceiptImageView.setVisibility(View.GONE);
        int readCount = context.readUiState.getGroupReadCount(message, context.currentUserId);
        if (readCount < 0 || context.groupMemberCount <= 0) {
            groupReceiptFrameLayout.setVisibility(View.GONE);
            return;
        }
        groupReceiptFrameLayout.setVisibility(View.VISIBLE);
        int max = Math.max(context.groupMemberCount - 1, 1);
        readProgressBar.setMax(max);
        readProgressBar.setProgress(Math.min(readCount, max));
    }

    private void hideReadReceipt() {
        singleReceiptImageView.setVisibility(View.GONE);
        groupReceiptFrameLayout.setVisibility(View.GONE);
    }

    private void updateRowInsets(boolean sentByMe) {
        int bottom = itemView.getResources().getDimensionPixelSize(com.cboda.baselibrary.R.dimen.dp_2);
        if (sentByMe) {
            int horizontal = itemView.getResources().getDimensionPixelSize(com.cboda.baselibrary.R.dimen.dp_5);
            rowContainer.setPadding(horizontal, 0, horizontal, bottom);
        } else {
            int horizontal = itemView.getResources().getDimensionPixelSize(com.cboda.baselibrary.R.dimen.dp_16);
            rowContainer.setPadding(horizontal, 0, horizontal, bottom);
        }
    }

    private void updateContentHostMargin(boolean sentByMe) {
        ViewGroup.MarginLayoutParams lp = (ViewGroup.MarginLayoutParams) flMessageContentHost.getLayoutParams();
        if (sentByMe) {
            lp.setMarginStart(0);
            lp.setMarginEnd(itemView.getResources().getDimensionPixelSize(com.cboda.baselibrary.R.dimen.dp_4));
        } else {
            int marginStart = itemView.getResources()
                    .getDimensionPixelSize(com.cboda.baselibrary.R.dimen.dp_10);
            lp.setMarginStart(marginStart);
            lp.setMarginEnd(0);
        }
        flMessageContentHost.setLayoutParams(lp);
    }

    private void alignMessageColumn(int gravity) {
        messageColumn.setGravity(gravity);
        ViewGroup.LayoutParams lp = bubbleRow.getLayoutParams();
        if (lp instanceof LinearLayout.LayoutParams) {
            ((LinearLayout.LayoutParams) lp).gravity = gravity;
            bubbleRow.setLayoutParams(lp);
        }
    }

    private void bindSelectionMode(@NonNull AppMessage message, @NonNull MessageViewHolderContext context) {
        if (context.isCheckableMode() && MessageMenuPolicy.canMultiSelect(message)) {
            checkbox.setVisibility(View.VISIBLE);
            checkbox.setChecked(context.isMessageChecked(message));
            itemView.setOnClickListener(v -> toggleChecked(message, context));
            itemView.setOnLongClickListener(null);
            flMessageContentHost.setOnLongClickListener(null);
            if (contentView != null) {
                contentView.setOnLongClickListener(null);
            }
            return;
        }
        checkbox.setVisibility(View.GONE);
        itemView.setOnClickListener(null);
        bindContextMenu(message, context);
    }

    private void toggleChecked(@NonNull AppMessage message, @NonNull MessageViewHolderContext context) {
        MessageListActions actions = context.messageActions;
        if (actions == null) {
            return;
        }
        boolean next = !context.isMessageChecked(message);
        actions.onToggleMessageChecked(message, next);
        checkbox.setChecked(next);
    }

    private void bindContextMenu(@NonNull AppMessage message, @NonNull MessageViewHolderContext context) {
        if (!MessageMenuPolicy.supportsContextMenu(message) || context.messageActions == null) {
            contextMenuLongClickListener = null;
            itemView.setOnLongClickListener(null);
            flMessageContentHost.setOnLongClickListener(null);
            if (contentView != null) {
                contentView.setOnLongClickListener(null);
            }
            return;
        }
        contextMenuLongClickListener = v -> {
            int[] location = new int[2];
            v.getLocationOnScreen(location);
            MessageContextMenuHelper.show(
                    v,
                    location[1],
                    message,
                    context.currentUserId,
                    shouldSkipRecallTimeLimit(context),
                    context.messageActions);
            return true;
        };
        flMessageContentHost.setOnLongClickListener(contextMenuLongClickListener);
        if (contentView != null) {
            contentView.setOnLongClickListener(contextMenuLongClickListener);
            propagateLongClickToClickableChildren(contentView, contextMenuLongClickListener);
        }
        itemView.setOnLongClickListener(contextMenuLongClickListener);
    }

    /**
     * 子 ViewHolder 在 content 上设置 {@code OnClickListener} 后调用，避免可点击子 View 吞掉长按。
     */
    protected final void refreshContextMenuLongClickOnContent() {
        if (contextMenuLongClickListener == null || contentView == null) {
            return;
        }
        contentView.setOnLongClickListener(contextMenuLongClickListener);
        propagateLongClickToClickableChildren(contentView, contextMenuLongClickListener);
    }

    private static void propagateLongClickToClickableChildren(@NonNull View view,
                                                              @NonNull View.OnLongClickListener listener) {
        if (view instanceof ViewGroup) {
            ViewGroup group = (ViewGroup) view;
            for (int i = 0; i < group.getChildCount(); i++) {
                propagateLongClickToClickableChildren(group.getChildAt(i), listener);
            }
        }
        if (view.isClickable() || view.hasOnClickListeners()) {
            view.setOnLongClickListener(listener);
        }
    }

    /** 群聊中自己是群主/管理员时，撤回不受时限约束。 */
    private static boolean shouldSkipRecallTimeLimit(@NonNull MessageViewHolderContext context) {
        if (!context.isGroupChat) {
            return false;
        }
        GroupMemberItem self = context.resolveMember(context.currentUserId);
        if (self == null){
            return true;
        }else {
            return GroupRole.isOwnerOrAdmin(self.getRole());
        }
    }

    @NonNull
    private static String resolveDisplayName(@Nullable GroupMemberItem member, @Nullable String userId) {
        return WeChatUiHelper.resolveGroupMemberDisplayName(member, userId);
    }
}
