/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.group.manage;

import android.widget.Toast;

import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.material.switchmaterial.SwitchMaterial;

import java.util.Collections;
import java.util.List;

import com.cboda.chatui.R;
import com.cboda.chatui.WfcBaseBroadActivity;
import com.cboda.chatui.group.GroupViewBroadModel;
import cn.wildfirechat.model.GroupInfo;

public class GroupMuteOrAllowBroadActivity extends WfcBaseBroadActivity {
    private GroupInfo groupInfo;
    private GroupViewBroadModel groupViewModel;

    SwitchMaterial switchButton;

    protected void bindViews() {
        super.bindViews();
        switchButton = findViewById(R.id.muteSwitchButton);
    }

    @Override
    protected int contentLayout() {
        return R.layout.group_manage_mute_broad_activity;
    }

    @Override
    protected void afterViews() {
        groupInfo = getIntent().getParcelableExtra("groupInfo");
        if (groupInfo != null) {
            init();
        } else {
            finish();
        }
    }

    private void init() {
        groupViewModel = ViewModelProviders.of(this).get(GroupViewBroadModel.class);
        groupViewModel.groupInfoUpdateLiveData().observe(this, new Observer<List<GroupInfo>>() {
            @Override
            public void onChanged(List<GroupInfo> groupInfos) {
                if (groupInfos != null) {
                    for (GroupInfo info : groupInfos) {
                        if (info.target.equals(groupInfo.target)) {
                            boolean oMuted = groupInfo.mute == 1;
                            boolean nMuted = info.mute == 1;
                            groupInfo = info;
                            break;
                        }
                    }
                }

            }
        });
        switchButton.setChecked(groupInfo.mute == 1);
        switchButton.setOnCheckedChangeListener((buttonView, isChecked) -> {
            groupViewModel.muteAll(groupInfo.target, isChecked, null, Collections.singletonList(0)).observe(this, booleanOperateResult -> {
                if (!booleanOperateResult.isSuccess()) {
                    switchButton.setChecked(!isChecked);
                    Toast.makeText(this, "禁言失败 " + booleanOperateResult.getErrorCode(), Toast.LENGTH_SHORT).show();
                } else {
                    initGroupMemberMuteListFragment(true);
                }
            });
        });

        initGroupMemberMuteListFragment(false);
    }

    private GroupMemberMuteOrAllowListBroadFragment fragment;

    private void initGroupMemberMuteListFragment(boolean forceUpdate) {
        // 全局禁言
        if (groupInfo.mute == 1) {
            if (fragment == null || forceUpdate) {
                fragment = GroupMemberMuteOrAllowListBroadFragment.newInstance(groupInfo, true);
            }
        } else {
            if (fragment == null || forceUpdate) {
                fragment = GroupMemberMuteOrAllowListBroadFragment.newInstance(groupInfo, false);
            }
        }
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.containerFrameLayout, fragment)
            .commit();
    }
}
