/*
 * Copyright (c) 2022 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.pick;

import android.app.Activity;
import android.content.Intent;

import com.cboda.chatui.R;
import com.cboda.chatui.WfcBaseBroadActivity;
import com.cboda.chatui.conversationlist.ConversationListBroadFragment;
import com.cboda.chatui.conversationlist.OnClickConversationItemBroadListener;
import cn.wildfirechat.model.ConversationInfo;

public class PickConversationBroadActivity extends WfcBaseBroadActivity implements OnClickConversationItemBroadListener {
    @Override
    protected void afterViews() {
        ConversationListBroadFragment conversationListFragment = new ConversationListBroadFragment();
        conversationListFragment.setOnClickConversationItemListener(this);
        getSupportFragmentManager()
            .beginTransaction()
            .add(R.id.containerFrameLayout, conversationListFragment)
            .commit();
    }

    @Override
    protected int contentLayout() {
        return R.layout.fragment_container_broad_activity;
    }

    @Override
    public void onClickConversationItem(ConversationInfo conversationInfo) {
        Intent intent = new Intent();
        intent.putExtra("conversationInfo", conversationInfo);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    @Override
    public void onClickConversationItemNew(ConversationInfo conversationInfo, int position) {

    }
}
