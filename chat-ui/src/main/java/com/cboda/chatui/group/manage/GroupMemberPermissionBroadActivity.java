/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.group.manage;

import com.cboda.chatui.R;
import com.cboda.chatui.WfcBaseBroadActivity;
import cn.wildfirechat.model.GroupInfo;

public class GroupMemberPermissionBroadActivity extends WfcBaseBroadActivity {

    @Override
    protected void afterViews() {
        GroupInfo groupInfo = getIntent().getParcelableExtra("groupInfo");
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.containerFrameLayout, GroupMemberPermissionBroadFragment.newInstance(groupInfo))
                .commit();
    }

    @Override
    protected int contentLayout() {
        return R.layout.fragment_container_broad_activity;
    }
}
