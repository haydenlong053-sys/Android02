/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.message.viewholder;

import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cboda.chatui.*;
import com.cboda.chatui.annotation.EnableContextBroadMenu;
import com.cboda.chatui.annotation.MessageContentBroadType;
import com.cboda.chatui.conversation.ConversationBroadFragment;
import com.cboda.chatui.conversation.message.model.UiBroadMessage;
import cn.wildfirechat.message.UnknownMessageContent;

@MessageContentBroadType(UnknownMessageContent.class)
@EnableContextBroadMenu
public class UnkownMessageContentViewBroadHolder extends NormalMessageContentViewBroadHolder {
    TextView contentTextView;

    public UnkownMessageContentViewBroadHolder(ConversationBroadFragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        bindViews(itemView);
    }

    private void bindViews(View itemView) {
        contentTextView =itemView.findViewById(R.id.contentTextView);
    }

    @Override
    public void onBind(UiBroadMessage message) {
        contentTextView.setText("暂不支持此消息，请升级最新版本!");
    }

}
