/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.mention;

import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

import com.cboda.chatui.R;
import com.cboda.chatui.contact.model.UIUserBroadInfo;
import com.cboda.chatui.contact.viewholder.UserViewBroadHolder;
import com.cboda.chatui.search.SearchableBroadModule;
import cn.wildfirechat.model.GroupMember;
import cn.wildfirechat.model.UserInfo;
import cn.wildfirechat.remote.ChatManager;

public class GroupMemberSearchBroadModule extends SearchableBroadModule<UserInfo, UserViewBroadHolder> {
    private String groupId;

    public GroupMemberSearchBroadModule(String groupId) {
        this.groupId = groupId;
    }

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
        Intent intent = new Intent();
        intent.putExtra("userId", userInfo.uid);
        fragment.getActivity().setResult(Activity.RESULT_OK, intent);
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
    public boolean expandable() {
        return false;
    }

    @Override
    public String category() {
        return null;
    }

    @Override
    public List<UserInfo> search(String keyword) {
        List<GroupMember> groupMembers = ChatManager.Instance().getGroupMembers(groupId, false);
        if (groupMembers == null || groupMembers.isEmpty()) {
            return null;
        }
        List<String> memberIds = new ArrayList<>(groupMembers.size());
        for (GroupMember member : groupMembers) {
            memberIds.add(member.memberId);
        }
        List<UserInfo> userInfos = ChatManager.Instance().getUserInfos(memberIds, groupId);
        List<UserInfo> resultUserInfos = new ArrayList<>();
        if (userInfos != null) {
            for (UserInfo userInfo : userInfos) {
                if ((userInfo.displayName != null && userInfo.displayName.contains(keyword))
                        || (userInfo.friendAlias != null && userInfo.friendAlias.contains(keyword))
                        || (userInfo.groupAlias != null && userInfo.groupAlias.contains(keyword))) {
                    resultUserInfos.add(userInfo);
                }
            }
        }
        return resultUserInfos;
    }
}
