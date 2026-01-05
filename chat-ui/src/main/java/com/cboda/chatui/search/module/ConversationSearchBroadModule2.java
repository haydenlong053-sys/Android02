/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.search.module;

import static cn.wildfirechat.model.Conversation.ConversationType.Group;
import static cn.wildfirechat.model.Conversation.ConversationType.Single;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.Arrays;
import java.util.List;

import com.cboda.chatui.R;
import com.cboda.chatui.conversation.ConversationBroadActivity;
import com.cboda.chatui.search.SearchMessageBroadActivity;
import com.cboda.chatui.search.SearchableBroadModule;
import com.cboda.chatui.search.viewHolder.ConversationViewBroadHolder2;
import cn.wildfirechat.model.ConversationSearchResult;
import cn.wildfirechat.remote.ChatManager;

public class ConversationSearchBroadModule2 extends SearchableBroadModule<ConversationSearchResult, ConversationViewBroadHolder2> {
    @Override
    public ConversationViewBroadHolder2 onCreateViewHolder(Fragment fragment, @NonNull ViewGroup parent, int type) {
        View itemView;
        itemView = LayoutInflater.from(fragment.getActivity()).inflate(R.layout.search_item_broad_conversation2, parent, false);
        return new ConversationViewBroadHolder2(fragment, itemView);
    }

    @Override
    public void onBind(Fragment fragment, ConversationViewBroadHolder2 holder, ConversationSearchResult conversationSearchResult) {
        holder.onBind(keyword, conversationSearchResult);
    }

    @Override
    public void onBind2(Fragment fragment, ConversationViewBroadHolder2 holder, ConversationSearchResult conversationSearchResult, int currentPosition, int startPosition, int endPosition) {
        holder.onBind2(keyword, conversationSearchResult,currentPosition,startPosition,endPosition);
    }

    @Override
    public int getViewType(ConversationSearchResult conversationSearchResult) {
        return R.layout.search_item_broad_conversation2;
    }

    @Override
    public int priority() {
        return 80;
    }

    @Override
    public void onClick(Fragment fragment, ConversationViewBroadHolder2 holder, View view, ConversationSearchResult conversationSearchResult) {
        if (conversationSearchResult.marchedCount == 1) {
            // TODO hide fragment
            Intent intent = new Intent(fragment.getActivity(), ConversationBroadActivity.class);
            intent.putExtra("conversation", conversationSearchResult.conversation);
            intent.putExtra("toFocusMessageId", conversationSearchResult.marchedMessage.messageId);
            fragment.startActivity(intent);
            fragment.getActivity().finish();
        } else {
            Intent intent = new Intent(fragment.getActivity(), SearchMessageBroadActivity.class);
            intent.putExtra("conversation", conversationSearchResult.conversation);
            intent.putExtra("keyword", keyword);
            fragment.startActivity(intent);
        }
    }

    @Override
    public String category() {
        return "聊天记录";
    }

    @Override
    public String category2(View view) {
        return view.getResources().getString(R.string.msg_chat_bibi);
    }

    @Override
    public List<ConversationSearchResult> search(String keyword) {
        return ChatManager.Instance().searchConversation(keyword, Arrays.asList(Single, Group), Arrays.asList(0, 1));
    }
}
