/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.message.viewholder;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cboda.chatui.*;
import com.cboda.chatui.annotation.EnableContextBroadMenu;
import com.cboda.chatui.annotation.MessageContentBroadType;
import com.cboda.chatui.conversation.ConversationBroadFragment;
import com.cboda.chatui.conversation.message.CompositeMessageContentBroadActivity;
import com.cboda.chatui.conversation.message.model.UiBroadMessage;
import cn.wildfirechat.message.CompositeMessageContent;


@MessageContentBroadType(value = {
    CompositeMessageContent.class,

})
@EnableContextBroadMenu
public class CompositeMessageContentViewBroadHolder extends NormalMessageContentViewBroadHolder {
    TextView titleTextView;
    TextView contentTextView;

    public CompositeMessageContentViewBroadHolder(ConversationBroadFragment fragment, RecyclerView.Adapter adapter, View itemView) {
        super(fragment, adapter, itemView);
        bindViews(itemView);
        bindEvents(itemView);
    }

    private void bindEvents(View itemView) {
       itemView.findViewById(R.id.compositeContentLayout).setOnClickListener(this::onClick);
    }

    private void bindViews(View itemView) {
        titleTextView =itemView.findViewById(R.id.titleTextView);
        contentTextView =itemView.findViewById(R.id.contentTextView);
    }

    @Override
    public void onBind(UiBroadMessage message) {
        CompositeMessageContent content = (CompositeMessageContent) message.message.content;
        titleTextView.setText(content.getTitle());
        contentTextView.setText(content.compositeDigest());
    }

    public void onClick(View view) {
        Intent intent = new Intent(fragment.getContext(), CompositeMessageContentBroadActivity.class);
        intent.putExtra("message", message.message);
        fragment.startActivity(intent);
    }

}
