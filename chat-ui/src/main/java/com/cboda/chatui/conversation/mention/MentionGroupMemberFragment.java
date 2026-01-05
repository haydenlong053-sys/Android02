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
import com.cboda.chatui.contact.BaseUserListFragment;
import com.cboda.chatui.contact.UserListAdapter;
import com.cboda.chatui.contact.model.HeaderValue;
import com.cboda.chatui.contact.model.UIUserInfo;
import com.cboda.chatui.contact.viewholder.header.HeaderViewHolder;
import com.cboda.chatui.group.GroupViewModel;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.GroupMember;
import cn.wildfirechat.remote.ChatManager;

public class MentionGroupMemberFragment extends BaseUserListFragment {
    private GroupInfo groupInfo;
    private GroupMember groupMember;
    private GroupViewModel groupViewModel;

    public static MentionGroupMemberFragment newInstance(GroupInfo groupInfo) {
        Bundle args = new Bundle();
        args.putParcelable("groupInfo", groupInfo);
        MentionGroupMemberFragment fragment = new MentionGroupMemberFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showQuickIndexBar(true);
        groupInfo = getArguments().getParcelable("groupInfo");
        groupViewModel = ViewModelProviders.of(getActivity()).get(GroupViewModel.class);
        groupMember = groupViewModel.getGroupMember(groupInfo.target, ChatManager.Instance().getUserId());
        Log.e("group--", "onCreate");
    }

    @Override
    protected void afterViews(View view) {
        super.afterViews(view);
        Log.e("group--", "afterViews");
        groupViewModel.getGroupMemberUIUserInfosLiveData(groupInfo.target, false).observe(this, uiUserInfos -> {
            showContent();
            UIUserInfo xbao = null;
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
    public UserListAdapter onCreateUserListAdapter() {
        Log.e("group--", "onCreateUserListAdapter");
        return new UserListAdapter(this);
    }

    @Override
    public void initHeaderViewHolders() {
        if (groupMember != null) {
            if (groupMember.type == GroupMember.GroupMemberType.Manager || groupMember.type == GroupMember.GroupMemberType.Owner) {
                addHeaderViewHolder(MentionAllHeaderViewHolder.class, R.layout.conversation_header_mention_all, new HeaderValue());
//                addHeaderViewHolder(MentionAllHeaderViewHolder.class, R.layout.conversation_header_mention_xbao, new HeaderValue());
            }
        }
    }

    @Override
    public void onHeaderClick(HeaderViewHolder holder) {
        Intent intent = new Intent();
        intent.putExtra("mentionAll", true);
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }

    @Override
    public void onUserClick(UIUserInfo userInfo) {
        Intent intent = new Intent();
        intent.putExtra("userId", userInfo.getUserInfo().uid);
        getActivity().setResult(Activity.RESULT_OK, intent);
        getActivity().finish();
    }
}
