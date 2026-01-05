/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.group;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.cboda.chatui.contact.pick.PickUserBroadFragment;
import com.cboda.chatui.contact.pick.PickUserViewBroadModel;
import cn.wildfirechat.model.GroupInfo;

public class PickGroupMemberBroadFragment extends PickUserBroadFragment {
    private GroupInfo groupInfo;

    public static PickGroupMemberBroadFragment newInstance(GroupInfo groupInfo) {
        Bundle args = new Bundle();
        args.putParcelable("groupInfo", groupInfo);
        PickGroupMemberBroadFragment fragment = new PickGroupMemberBroadFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        groupInfo = getArguments().getParcelable("groupInfo");
    }

    @Override
    protected void setupPickFromUsers() {
        PickUserViewBroadModel pickUserViewModel = ViewModelProviders.of(getActivity()).get(PickUserViewBroadModel.class);
        GroupViewBroadModel groupViewModel = ViewModelProviders.of(getActivity()).get(GroupViewBroadModel.class);
        groupViewModel.getGroupMemberUIUserInfosLiveData(groupInfo.target, false).observe(this, uiUserInfos -> {
            showContent();
            pickUserViewModel.setUsers(uiUserInfos);
            for (int i = 0; i < uiUserInfos.size(); i++) {
                uiUserInfos.get(i).getUserInfo().lixinUserName = uiUserInfos.get(i).getUserInfo().displayName;
            }
            userListAdapter.setUsers(uiUserInfos);
        });
    }
}
