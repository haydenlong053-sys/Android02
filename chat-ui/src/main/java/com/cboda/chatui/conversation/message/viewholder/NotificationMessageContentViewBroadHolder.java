/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.message.viewholder;

import android.content.Context;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;

import com.cboda.chatui.conversation.ConversationBroadFragment;
import com.cboda.chatui.conversation.message.model.UiBroadMessage;

public abstract class NotificationMessageContentViewBroadHolder extends MessageContentViewBroadHolder {
    public NotificationMessageContentViewBroadHolder(ConversationBroadFragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
    }

    @Override
    public boolean contextMenuItemFilter(UiBroadMessage uiMessage, String tag) {
        return true;
    }

    @Override
    public String contextMenuTitle(Context context, String tag) {
        return null;
    }

    @Override
    public String contextConfirmPrompt(Context context, String tag) {
        return null;
    }
}
