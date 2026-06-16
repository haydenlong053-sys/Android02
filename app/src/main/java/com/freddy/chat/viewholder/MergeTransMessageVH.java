package com.freddy.chat.viewholder;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cboda.chatui.R;
import com.freddy.chat.merge.MergeTranspondDetailActivity;
import com.freddy.chat.merge.MergeTranspondMessageContentUtil;
import com.freddy.chat.merge.MergeTranspondMessageItem;
import com.freddy.im.bean.AppMessage;

import java.util.List;

/**
 * 合并转发消息 UI（对齐野火 {@link com.cboda.chatui.conversation.message.viewholder.CompositeMessageContentViewBroadHolder}）。
 */
public final class MergeTransMessageVH extends BaseMessageVH {

    private int boundContentLayoutRes;
    private View compositeContentLayout;
    private TextView titleTextView;
    private TextView contentTextView;
    @NonNull
    private String mergeTitle = "";
    @Nullable
    private AppMessage boundMergeMessage;
    @Nullable
    private MessageViewHolderContext boundMergeContext;

    public MergeTransMessageVH(@NonNull View itemView) {
        super(itemView, R.layout.conversation_item_composite_broad_receive, false);
        cacheCompositeViews();
    }

    public void bind(@NonNull AppMessage message, int position, @NonNull MessageViewHolderContext context) {
        boolean sentByMe = message.isSender(context.currentUserId);
        int layoutRes = sentByMe
                ? R.layout.conversation_item_composite_broad_send
                : R.layout.conversation_item_composite_broad_receive;
        if (boundContentLayoutRes != layoutRes) {
            inflateContentLayout(layoutRes);
            boundContentLayoutRes = layoutRes;
            cacheCompositeViews();
        }
        bindCommon(message, position, context);
        boundMergeMessage = message;
        boundMergeContext = context;

        List<MergeTranspondMessageItem> items = MergeTranspondMessageContentUtil.parseMessageList(
                message.getContent());
        mergeTitle = MergeTranspondMessageContentUtil.buildTitle(
                context.isGroupChat, context.peerDisplayName);
        titleTextView.setText(mergeTitle);
        contentTextView.setText(MergeTranspondMessageContentUtil.buildCompositeDigest(
                items, context.currentUserId, context.buildUserDisplayNameMap()));
        refreshContextMenuLongClickOnContent();
    }

    private void cacheCompositeViews() {
        compositeContentLayout = contentView.findViewById(R.id.compositeContentLayout);
        titleTextView = contentView.findViewById(R.id.titleTextView);
        contentTextView = contentView.findViewById(R.id.contentTextView);
        if (compositeContentLayout != null) {
            compositeContentLayout.setOnClickListener(v -> onCompositeClick());
        }
    }

    private void onCompositeClick() {
        if (boundMergeMessage == null || boundMergeContext == null) {
            return;
        }
        Intent intent = new Intent(itemView.getContext(), MergeTranspondDetailActivity.class);
        intent.putExtra(MergeTranspondDetailActivity.EXTRA_MESSAGE, boundMergeMessage);
        intent.putExtra(MergeTranspondDetailActivity.EXTRA_TITLE, mergeTitle);
        intent.putExtra(MergeTranspondDetailActivity.EXTRA_SESSION_ID, boundMergeContext.sessionId);
        intent.putExtra(MergeTranspondDetailActivity.EXTRA_IS_GROUP, boundMergeContext.isGroupChat);
        intent.putExtra(MergeTranspondDetailActivity.EXTRA_PEER_DISPLAY_NAME, boundMergeContext.peerDisplayName);
        itemView.getContext().startActivity(intent);
    }
}
