/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.group.manage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.Collections;

import com.cboda.chatui.R;
import com.cboda.chatui.contact.BaseUserListBroadFragment;
import com.cboda.chatui.contact.UserListBroadAdapter;
import com.cboda.chatui.contact.model.FooterBroadValue;
import com.cboda.chatui.contact.model.UIUserBroadInfo;
import com.cboda.chatui.group.BasePickGroupMemberBroadActivity;
import com.cboda.chatui.group.GroupViewBroadModel;
import com.cboda.chatui.user.UserViewBroadModel;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.GroupMember;
import cn.wildfirechat.remote.ChatManager;

public class GroupManagerListBroadFragment extends BaseUserListBroadFragment {
    private GroupViewBroadModel groupViewModel;
    private GroupInfo groupInfo;
    private GroupMember groupMember;

    public static GroupManagerListBroadFragment newInstance(GroupInfo groupInfo) {
        Bundle args = new Bundle();
        args.putParcelable("groupInfo", groupInfo);
        GroupManagerListBroadFragment fragment = new GroupManagerListBroadFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        groupInfo = getArguments().getParcelable("groupInfo");
        showQuickIndexBar(false);

        groupViewModel = ViewModelProviders.of(getActivity()).get(GroupViewBroadModel.class);
        groupMember = groupViewModel.getGroupMember(groupInfo.target, ChatManager.Instance().getUserId());
        observerGroupMemberUpdate();
    }

    @Override
    protected void afterViews(View view) {
        super.afterViews(view);
        loadAndShowGroupMembers(true);
    }

    @Override
    public UserListBroadAdapter onCreateUserListAdapter() {
        return new UserListBroadAdapter(this);
    }

    @Override
    public void initFooterViewHolders() {
        if (groupMember.type == GroupMember.GroupMemberType.Owner) {
            addFooterViewHolder(AddGroupManagerViewBroadHolder.class, R.layout.group_manage_item_add_manager, new FooterBroadValue());
        }
    }

    @Override
    public void onUserClick(UIUserBroadInfo userInfo) {
        UserViewBroadModel userViewModel = ViewModelProviders.of(this).get(UserViewBroadModel.class);
        GroupMember me = groupViewModel.getGroupMember(groupInfo.target, userViewModel.getUserId());
        if (me == null || me.type != GroupMember.GroupMemberType.Owner) {
            return;
        }

        GroupMember groupMember = groupViewModel.getGroupMember(groupInfo.target, userInfo.getUserInfo().uid);
        if (groupMember.type == GroupMember.GroupMemberType.Manager) {
            new MaterialDialog.Builder(getActivity())
                .items(Collections.singleton("移除群管理"))
                .itemsCallback((dialog, itemView, position, text) -> {
                    groupViewModel.setGroupManager(groupInfo.target, false, Collections.singletonList(userInfo.getUserInfo().uid), null, Collections.singletonList(0));
                })
                .cancelable(true)
                .build()
                .show();
        }
    }

    @Override
    public void onFooterClick(int index) {
        Intent intent = new Intent(getActivity(), AddGroupManagerBroadActivity.class);
        intent.putExtra(BasePickGroupMemberBroadActivity.GROUP_INFO, groupInfo);

        ArrayList<String> uncheckableMemberIds = new ArrayList<>();
        uncheckableMemberIds.add(groupInfo.owner);
        uncheckableMemberIds.addAll(groupViewModel.getGroupManagerIds(groupInfo.target));
        intent.putExtra(BasePickGroupMemberBroadActivity.UNCHECKABLE_MEMBER_IDS, uncheckableMemberIds);
        startActivity(intent);
    }

    private void observerGroupMemberUpdate() {
        groupViewModel.groupMembersUpdateLiveData().observe(this, groupMembers -> {
            if (groupInfo.target.equals(groupMembers.get(0).groupId)) {
                loadAndShowGroupMembers(false);
            }
        });
    }

    private void loadAndShowGroupMembers(boolean refresh) {
        groupViewModel.getGroupManagerUIUserInfosLiveData(groupInfo.target, refresh).observe(this, uiUserInfos -> {
            showContent();
            userListAdapter.setUsers(uiUserInfos);
            userListAdapter.notifyDataSetChanged();
        });
    }
}
