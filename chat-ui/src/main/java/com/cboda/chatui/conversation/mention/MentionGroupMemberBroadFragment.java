/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.mention;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.cboda.chatui.R;
import com.cboda.chatui.contact.BaseUserListBroadFragment;
import com.cboda.chatui.contact.UserListBroadAdapter;
import com.cboda.chatui.contact.model.HeaderBroadValue;
import com.cboda.chatui.contact.model.UIUserBroadInfo;
import com.cboda.chatui.contact.viewholder.header.HeaderViewBroadHolder;
import com.cboda.chatui.group.GroupViewBroadModel;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.GroupMember;
import cn.wildfirechat.remote.ChatManager;

public class MentionGroupMemberBroadFragment extends BaseUserListBroadFragment {
    private GroupInfo groupInfo;
    private GroupMember groupMember;
    private GroupViewBroadModel groupViewModel;

    public static MentionGroupMemberBroadFragment newInstance(GroupInfo groupInfo) {
        Bundle args = new Bundle();
        args.putParcelable("groupInfo", groupInfo);
        MentionGroupMemberBroadFragment fragment = new MentionGroupMemberBroadFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showQuickIndexBar(true);
        groupInfo = getArguments().getParcelable("groupInfo");
        groupViewModel = ViewModelProviders.of(getActivity()).get(GroupViewBroadModel.class);
        groupMember = groupViewModel.getGroupMember(groupInfo.target, ChatManager.Instance().getUserId());
        Log.e("group--", "onCreate");
    }

    @Override
    protected void afterViews(View view) {
        super.afterViews(view);
        Log.e("group--", "afterViews");
        groupViewModel.getGroupMemberUIUserInfosLiveData(groupInfo.target, false).observe(this, uiUserInfos -> {
            showContent();
            UIUserBroadInfo xbao = null;
            for (int i = 0; i < uiUserInfos.size(); i++) {
                if (!ChatManager.Instance().getUserId().equals(uiUserInfos.get(i).getUserInfo().uid))
                    uiUserInfos.get(i).getUserInfo().lixinUserName = uiUserInfos.get(i).getUserInfo().displayName;
                if (uiUserInfos.get(i).getUserInfo().uid.equals("robot001")) {
                    xbao = uiUserInfos.get(i);
                    uiUserInfos.remove(i);
                    i--;
                }
            }
            if (null != xbao){
                xbao.setShowCategory(false);
                uiUserInfos.add(0, xbao);
            }
            Log.e("group--", "uiUserInfos" + uiUserInfos.size());
            userListAdapter.setUsers(uiUserInfos);
        });
    }

    @Override
    public UserListBroadAdapter onCreateUserListAdapter() {
        Log.e("group--", "onCreateUserListAdapter");
        return new UserListBroadAdapter(this);
    }

    @Override
    public void initHeaderViewHolders() {
        if (groupMember != null) {
            if (groupMember.type == GroupMember.GroupMemberType.Manager || groupMember.type == GroupMember.GroupMemberType.Owner) {
                addHeaderViewHolder(MentionAllHeaderViewBroadHolder.class, R.layout.conversation_header_mention_broad_all, new HeaderBroadValue());
//                addHeaderViewHolder(MentionAllHeaderViewBroadHolder.class, R.layout.conversation_header_mention_broad_xbao, new HeaderBroadValue());
            }
        }
    }

    @Override
    public void onHeaderClick(HeaderViewBroadHolder holder) {
        Intent intent = new Intent();
        intent.putExtra("mentionAll", true);
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }

    @Override
    public void onUserClick(UIUserBroadInfo userInfo) {
        Intent intent = new Intent();
        intent.putExtra("userId", userInfo.getUserInfo().uid);
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }
}
