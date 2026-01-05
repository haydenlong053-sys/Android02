/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.group.manage;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.afollestad.materialdialogs.MaterialDialog;

import java.util.ArrayList;
import java.util.Collections;

import com.cboda.chatui.R;
import com.cboda.chatui.contact.BaseUserListBroadFragment;
import com.cboda.chatui.contact.UserListBroadAdapter;
import com.cboda.chatui.contact.model.HeaderBroadValue;
import com.cboda.chatui.contact.model.UIUserBroadInfo;
import com.cboda.chatui.contact.viewholder.header.HeaderViewBroadHolder;
import com.cboda.chatui.group.BasePickGroupMemberBroadActivity;
import com.cboda.chatui.group.GroupViewBroadModel;
import com.cboda.chatui.user.UserViewBroadModel;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.GroupMember;
import cn.wildfirechat.remote.ChatManager;

public class GroupMemberMuteOrAllowListBroadFragment extends BaseUserListBroadFragment implements UserListBroadAdapter.OnUserLongClickListener {
    private GroupViewBroadModel groupViewModel;
    private GroupInfo groupInfo;
    private GroupMember groupMember;
    private boolean groupMuted = false;

    public static GroupMemberMuteOrAllowListBroadFragment newInstance(GroupInfo groupInfo, boolean mute) {
        Bundle args = new Bundle();
        args.putParcelable("groupInfo", groupInfo);
        GroupMemberMuteOrAllowListBroadFragment fragment = new GroupMemberMuteOrAllowListBroadFragment();
        fragment.groupMuted = mute;
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
        UserListBroadAdapter adapter = new UserListBroadAdapter(this);
        adapter.setOnUserLongClickListener(this);
        return adapter;
    }

    @Override
    public void initHeaderViewHolders() {
        if (groupMember.type == GroupMember.GroupMemberType.Owner || groupMember.type == GroupMember.GroupMemberType.Manager) {
            addHeaderViewHolder(MuteGroupMemberViewBroadHolder.class, R.layout.group_manage_item_mute_broad_member, new HeaderBroadValue(groupInfo, groupMuted));
        }
    }

    @Override
    public void onUserClick(UIUserBroadInfo userInfo) {
        UserViewBroadModel userViewModel = ViewModelProviders.of(this).get(UserViewBroadModel.class);
        GroupMember me = groupViewModel.getGroupMember(groupInfo.target, userViewModel.getUserId());
        if (me == null || me.type != GroupMember.GroupMemberType.Owner) {
            return;
        }

        new MaterialDialog.Builder(getActivity())
            .items(Collections.singleton(groupMuted ? "移除白名单" : "取消禁言"))
            .itemsCallback((dialog, itemView, position, text) -> {
                if (groupMuted) {
                    groupViewModel.allowGroupMember(groupInfo.target, false, Collections.singletonList(userInfo.getUserInfo().uid), null, Collections.singletonList(0));
                } else {
                    groupViewModel.muteGroupMember(groupInfo.target, false, Collections.singletonList(userInfo.getUserInfo().uid), null, Collections.singletonList(0));
                }
            })
            .cancelable(true)
            .build()
            .show();
    }

    @Override
    public void onUserLongClick(UIUserBroadInfo userInfo) {
        new MaterialDialog.Builder(getActivity())
            .content(!groupMuted ? "取消禁言" : "取消允许发言")
            .positiveText("确定")
            .negativeText("取消")
            .onPositive((dialog, which) -> {
                if (!groupMuted) {
                    groupViewModel.muteGroupMember(groupInfo.target, false, Collections.singletonList(userInfo.getUserInfo().uid), null, Collections.singletonList(0))
                        .observe(this, booleanOperateResult -> {
                            if (!booleanOperateResult.isSuccess()) {
                                Toast.makeText(getActivity(), "操作失败", Toast.LENGTH_SHORT).show();
                            } else {
                                this.loadAndShowGroupMembers(true);
                            }
                        });
                } else {
                    groupViewModel.allowGroupMember(groupInfo.target, false, Collections.singletonList(userInfo.getUserInfo().uid), null, Collections.singletonList(0))
                        .observe(this, booleanOperateResult -> {
                            if (!booleanOperateResult.isSuccess()) {
                                Toast.makeText(getActivity(), "操作失败", Toast.LENGTH_SHORT).show();
                            } else {
                                this.loadAndShowGroupMembers(true);
                            }
                        });
                }
            })
            .show();
    }

    @Override
    public void onHeaderClick(HeaderViewBroadHolder holder) {
        Intent intent = new Intent(getActivity(), MuteGroupMemberBroadActivity.class);
        intent.putExtra(BasePickGroupMemberBroadActivity.GROUP_INFO, groupInfo);
        intent.putExtra("groupMuted", groupMuted);

        ArrayList<String> uncheckableMemberIds = new ArrayList<>();
        uncheckableMemberIds.add(groupInfo.owner);
        uncheckableMemberIds.addAll(groupViewModel.getGroupManagerIds(groupInfo.target));
        uncheckableMemberIds.addAll(groupViewModel.getMutedOrAllowedMemberIds(groupInfo.target, groupMuted));
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
        groupViewModel.getMutedOrAllowedMemberUIUserInfosLiveData(groupInfo.target, groupMuted, refresh).observe(this, uiUserInfos -> {
            showContent();
            userListAdapter.setUsers(uiUserInfos);
            userListAdapter.notifyDataSetChanged();
        });
    }
}
