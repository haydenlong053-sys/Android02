/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.group;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;

import com.cboda.chatui.contact.pick.PickUserFragment;
import com.cboda.chatui.contact.pick.PickUserViewModel;
import cn.wildfirechat.model.GroupInfo;

public class PickGroupMemberFragment extends PickUserFragment {
    private GroupInfo groupInfo;

    public static PickGroupMemberFragment newInstance(GroupInfo groupInfo) {
        Bundle args = new Bundle();
        args.putParcelable("groupInfo", groupInfo);
        PickGroupMemberFragment fragment = new PickGroupMemberFragment();
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
        PickUserViewModel pickUserViewModel = ViewModelProviders.of(getActivity()).get(PickUserViewModel.class);
        GroupViewModel groupViewModel = ViewModelProviders.of(getActivity()).get(GroupViewModel.class);
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
