/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.search.module;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.List;

import com.cboda.chatui.R;
import com.cboda.chatui.conversation.ConversationBroadActivity;
import com.cboda.chatui.search.SearchableBroadModule;
import com.cboda.chatui.search.viewHolder.MessageViewBroadHolder;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.remote.ChatManager;

public class ConversationMessageSearchBroadModule extends SearchableBroadModule<Message, MessageViewBroadHolder> {
    private Conversation conversation;

    public ConversationMessageSearchBroadModule(Conversation conversation) {
        this.conversation = conversation;
    }

    @Override
    public MessageViewBroadHolder onCreateViewHolder(Fragment fragment, @NonNull ViewGroup parent, int type) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.search_item_message, parent, false);
        return new MessageViewBroadHolder(fragment, view);
    }

    @Override
    public void onBind(Fragment fragment, MessageViewBroadHolder holder, Message message) {
        holder.onBind(message);
    }

    @Override
    public int getViewType(Message message) {
        return R.layout.search_item_message;
    }

    @Override
    public void onClick(Fragment fragment, MessageViewBroadHolder holder, View view, Message message) {
        Intent intent = new Intent(fragment.getContext(), ConversationBroadActivity.class);
        intent.putExtra("conversation", message.conversation);
        intent.putExtra("toFocusMessageId", message.messageId);
        fragment.startActivity(intent);
        fragment.getActivity().finish();
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public String category() {
        return "聊天记录";
    }

    @Override
    public List<Message> search(String keyword) {
        return ChatManager.Instance().searchMessage(conversation, keyword, true, 100, 0, null);
    }

    @Override
    public boolean expandable() {
        return false;
    }
}
