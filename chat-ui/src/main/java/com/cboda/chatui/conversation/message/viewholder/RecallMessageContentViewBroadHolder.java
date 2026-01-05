/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.message.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cboda.chatui.*;
import com.cboda.chatui.BroadConfig;
import com.cboda.chatui.annotation.EnableContextBroadMenu;
import com.cboda.chatui.annotation.MessageContentBroadType;
import com.cboda.chatui.conversation.ConversationBroadFragment;
import com.cboda.chatui.conversation.message.model.UiBroadMessage;
import cn.wildfirechat.message.notification.NotificationMessageContent;
import cn.wildfirechat.message.notification.RecallMessageContent;
import cn.wildfirechat.remote.ChatManager;

@MessageContentBroadType(RecallMessageContent.class)
@EnableContextBroadMenu
public class RecallMessageContentViewBroadHolder extends NotificationMessageContentViewBroadHolder {
    TextView notificationTextView;
    TextView reeditTextView;

    private RecallMessageContent content;

    public RecallMessageContentViewBroadHolder(ConversationBroadFragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        bindViews(itemView);
        bindEvents(itemView);
    }

    private void bindEvents(View itemView) {
       itemView.findViewById(R.id.reeditTextView).setOnClickListener(this::onClick);
    }

    private void bindViews(View itemView) {
        notificationTextView =itemView.findViewById(R.id.notificationTextView);
        reeditTextView =itemView.findViewById(R.id.reeditTextView);
    }

    @Override
    public void onBind(UiBroadMessage message, int position) {
        super.onBind(message, position);
        content = (RecallMessageContent) message.message.content;
        notificationTextView.setText(message.message.digest());
        long delta = ChatManager.Instance().getServerDeltaTime();
        long now = System.currentTimeMillis();
        if (content.getOriginalContentType() == cn.wildfirechat.message.core.MessageContentBroadType.ContentType_Text
            && ((NotificationMessageContent) message.message.content).fromSelf
            && now - (message.message.serverTime - delta) < BroadConfig.RECALL_REEDIT_TIME_LIMIT * 1000) {
            reeditTextView.setVisibility(View.VISIBLE);
        } else {
            reeditTextView.setVisibility(View.GONE);
        }
    }

    public void onClick(View view) {
        fragment.setInputText(content.getOriginalSearchableContent());
    }

    @Override
    public boolean contextMenuItemFilter(UiBroadMessage uiMessage, String itemTitle) {
        return false;
    }

}
