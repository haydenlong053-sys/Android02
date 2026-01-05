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
import com.cboda.chatui.search.viewHolder.GroupViewBroadHolder;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.GroupSearchResult;
import cn.wildfirechat.remote.ChatManager;

public class GroupSearchViewBroadModule extends SearchableBroadModule<GroupSearchResult, GroupViewBroadHolder> {
    @Override
    public GroupViewBroadHolder onCreateViewHolder(Fragment fragment, @NonNull ViewGroup parent, int type) {
        View itemView;
        itemView = LayoutInflater.from(fragment.getActivity()).inflate(R.layout.search_item_group, parent, false);
        return new GroupViewBroadHolder(fragment, itemView);
    }

    @Override
    public void onBind(Fragment fragment, GroupViewBroadHolder holder, GroupSearchResult groupSearchResult) {
        holder.onBind(keyword, groupSearchResult);
    }

    @Override
    public void onClick(Fragment fragment, GroupViewBroadHolder holder, View view, GroupSearchResult groupSearchResult) {
        Intent intent = new Intent(fragment.getActivity(), ConversationBroadActivity.class);
        Conversation conversation = new Conversation(Conversation.ConversationType.Group, groupSearchResult.groupInfo.target, 0);
        intent.putExtra("conversation", conversation);
        fragment.startActivity(intent);
        fragment.getActivity().finish();
    }

    @Override
    public int getViewType(GroupSearchResult groupSearchResult) {
        return R.layout.search_item_group;
    }

    @Override
    public int priority() {
        return 90;
    }

    @Override
    public String category() {
        return "群组";
    }

    @Override
    public List<GroupSearchResult> search(String keyword) {
        return ChatManager.Instance().searchGroups(keyword);
    }

}
