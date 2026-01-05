/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.mention;

import android.util.Log;
import android.view.View;

import java.util.List;

import com.cboda.chatui.R;
import com.cboda.chatui.search.SearchBroadActivity;
import com.cboda.chatui.search.SearchableBroadModule;
import com.cboda.chatui.widget.SearchBroadView;
import cn.wildfirechat.model.GroupInfo;

public class MentionGroupMemberBroadActivity extends SearchBroadActivity {
    private GroupInfo groupInfo;

    @Override
    protected void beforeViews() {
        super.beforeViews();
        groupInfo = getIntent().getParcelableExtra("groupInfo");
    }

    @Override
    protected boolean hideSearchDescView() {
        return true;
    }

    @Override
    protected void afterViews() {
        super.afterViews();
        getSupportFragmentManager().beginTransaction()
            .replace(R.id.mentionGroupMemberContainer, MentionGroupMemberBroadFragment.newInstance(groupInfo))
            .commit();


    }

    @Override
    public void hintSearch(String keyWord) {
//
        if (keyWord.length() == 0){
            findViewById(R.id.mentionGroupMemberContainer).setVisibility(View.VISIBLE);
            findViewById(R.id.containerFrameLayout).setVisibility(View.GONE);
        }else{
            findViewById(R.id.mentionGroupMemberContainer).setVisibility(View.GONE);
            findViewById(R.id.containerFrameLayout).setVisibility(View.VISIBLE);
        }

        Log.e("group--", "keyWord"+keyWord);
    }

    @Override
    protected void initSearchModule(List<SearchableBroadModule> modules) {
        modules.add(new GroupMemberSearchBroadModule(groupInfo.target));
    }

    @Override
    protected int contentLayout() {
        return R.layout.group_mention_activity;
    }
}
