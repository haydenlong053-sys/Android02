/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.group.manage;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Collections;

import com.cboda.chatui.R;
import com.cboda.chatui.group.GroupViewBroadModel;
import cn.wildfirechat.model.GroupInfo;

public class GroupMemberPermissionBroadFragment extends Fragment {
    private GroupInfo groupInfo;

    SwitchMaterial privateChatSwitchButton;

    public static GroupMemberPermissionBroadFragment newInstance(GroupInfo groupInfo) {
        Bundle args = new Bundle();
        args.putParcelable("groupInfo", groupInfo);
        GroupMemberPermissionBroadFragment fragment = new GroupMemberPermissionBroadFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        groupInfo = getArguments().getParcelable("groupInfo");
        if (groupInfo == null) {
            getActivity().finish();
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.group_manage_member_permission_broad_fragment, container, false);
        if (groupInfo != null) {
            bindViews(view);
            init();
        }
        return view;
    }

    private void bindViews(View view) {
        privateChatSwitchButton = view.findViewById(R.id.privateChatSwitchButton);
    }

    private void init() {
        privateChatSwitchButton.setChecked(groupInfo.privateChat == 0);
        privateChatSwitchButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            GroupViewBroadModel groupViewModel = ViewModelProviders.of(this).get(GroupViewBroadModel.class);
            groupViewModel.enablePrivateChat(groupInfo.target, isChecked, null, Collections.singletonList(0)).observe(this, booleanOperateResult -> {
                if (!booleanOperateResult.isSuccess()) {
                    privateChatSwitchButton.setChecked(!isChecked);
                    Toast.makeText(getActivity(), "设置群成员权限失败 " + booleanOperateResult.getErrorCode(), Toast.LENGTH_SHORT).show();
                }
            });
        });
    }
}
