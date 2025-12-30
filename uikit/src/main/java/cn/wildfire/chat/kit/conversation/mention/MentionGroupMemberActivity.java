/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.conversation.mention;

import android.util.Log;
import android.view.View;

import java.util.List;

import cn.wildfire.chat.kit.R;
import cn.wildfire.chat.kit.search.SearchActivity;
import cn.wildfire.chat.kit.search.SearchableModule;
import cn.wildfire.chat.kit.widget.SearchView;
import cn.wildfirechat.model.GroupInfo;

public class MentionGroupMemberActivity extends SearchActivity {
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
            .replace(R.id.mentionGroupMemberContainer, MentionGroupMemberFragment.newInstance(groupInfo))
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
    protected void initSearchModule(List<SearchableModule> modules) {
        modules.add(new GroupMemberSearchModule(groupInfo.target));
    }

    @Override
    protected int contentLayout() {
        return R.layout.group_mention_activity;
    }
}
