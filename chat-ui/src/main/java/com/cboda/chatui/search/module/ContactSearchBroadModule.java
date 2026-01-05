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
import com.cboda.chatui.contact.model.UIUserBroadInfo;
import com.cboda.chatui.contact.viewholder.UserViewBroadHolder;
import com.cboda.chatui.conversation.ConversationBroadActivity;
import com.cboda.chatui.search.SearchableBroadModule;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;

public class ContactSearchBroadModule extends SearchableBroadModule<UserInfo, UserViewBroadHolder> {
    @Override
    public UserViewBroadHolder onCreateViewHolder(Fragment fragment, @NonNull ViewGroup parent, int viewType) {
        View itemView;
        itemView = LayoutInflater.from(fragment.getActivity()).inflate(R.layout.contact_item_broad_contact, parent, false);
        return new UserViewBroadHolder(fragment, null, itemView);
    }

    @Override
    public void onBind(Fragment fragment, UserViewBroadHolder holder, UserInfo userInfo) {
        holder.onBind(new UIUserBroadInfo(userInfo));
    }

    @Override
    public void onClick(Fragment fragment, UserViewBroadHolder holder, View view, UserInfo userInfo) {
        Intent intent = new Intent(fragment.getActivity(), ConversationBroadActivity.class);
        Conversation conversation = new Conversation(Conversation.ConversationType.Single, userInfo.uid, 0);
        intent.putExtra("conversation", conversation);
        fragment.startActivity(intent);
        fragment.getActivity().finish();
    }

    @Override
    public int getViewType(UserInfo userInfo) {
        return R.layout.contact_item_broad_contact;
    }

    @Override
    public int priority() {
        return 100;
    }

    @Override
    public String category() {
        return "联系人";
    }

    @Override
    public List<UserInfo> search(String keyword) {
        return ChatManager.Instance().searchFriends(keyword);
    }
}
