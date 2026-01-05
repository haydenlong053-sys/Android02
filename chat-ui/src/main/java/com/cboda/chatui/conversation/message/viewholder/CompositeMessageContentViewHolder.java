/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.message.viewholder;

import android.content.Intent;
import android.view.View;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.cboda.chatui.*;
import com.cboda.chatui.annotation.EnableContextMenu;
import com.cboda.chatui.annotation.MessageContentType;
import com.cboda.chatui.conversation.ConversationFragment;
import com.cboda.chatui.conversation.message.CompositeMessageContentActivity;
import com.cboda.chatui.conversation.message.model.UiMessage;
import cn.wildfirechat.message.CompositeMessageContent;


@MessageContentType(value = {
    CompositeMessageContent.class,

})
@EnableContextMenu
public class CompositeMessageContentViewHolder extends NormalMessageContentViewHolder {
    TextView titleTextView;
    TextView contentTextView;

    public CompositeMessageContentViewHolder(ConversationFragment fragment, RecyclerView.Adapter adapter, View itemView) {
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
    public void onBind(UiMessage message) {
        CompositeMessageContent content = (CompositeMessageContent) message.message.content;
        titleTextView.setText(content.getTitle());
        contentTextView.setText(content.compositeDigest());
    }

    public void onClick(View view) {
        Intent intent = new Intent(fragment.getContext(), CompositeMessageContentActivity.class);
        intent.putExtra("message", message.message);
        fragment.startActivity(intent);
    }

}
