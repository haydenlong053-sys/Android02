/*
 * Copyright (c) 2021 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.voip.conference;

import com.cboda.chatui.R;
import com.cboda.chatui.WfcBaseBroadActivity;

public class ConferenceHistoryListBroadActivity extends WfcBaseBroadActivity {
    @Override
    protected int contentLayout() {
        return R.layout.fragment_container_broad_activity;
    }

    @Override
    protected void afterViews() {
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.containerFrameLayout, new ConferenceHistoryListBroadFragment())
            .commit();
    }
}
