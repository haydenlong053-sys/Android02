/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.group;

import com.cboda.chatui.R;
import com.cboda.chatui.WfcBaseActivity;

public class GroupMemberMessageHistoryActivity extends WfcBaseActivity {

    @Override
    protected void afterViews() {
        String groupId = getIntent().getStringExtra("groupId");
        String groupMemberId = getIntent().getStringExtra("groupMemberId");
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.containerFrameLayout, GroupMemberMessageHistoryFragment.newInstance(groupId, groupMemberId))
            .commit();
    }

    @Override
    protected int contentLayout() {
        return R.layout.fragment_container_activity;
    }
}
