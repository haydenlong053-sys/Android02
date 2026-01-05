/*
 * Copyright (c) 2022 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.pick;

import android.app.Activity;
import android.content.Intent;

import com.cboda.chatui.R;
import com.cboda.chatui.WfcBaseActivity;
import com.cboda.chatui.conversationlist.ConversationListFragment;
import com.cboda.chatui.conversationlist.OnClickConversationItemListener;
import cn.wildfirechat.model.ConversationInfo;

public class PickConversationActivity extends WfcBaseActivity implements OnClickConversationItemListener {
    @Override
    protected void afterViews() {
        ConversationListFragment conversationListFragment = new ConversationListFragment();
        conversationListFragment.setOnClickConversationItemListener(this);
        getSupportFragmentManager()
            .beginTransaction()
            .add(R.id.containerFrameLayout, conversationListFragment)
            .commit();
    }

    @Override
    protected int contentLayout() {
        return R.layout.fragment_container_activity;
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
