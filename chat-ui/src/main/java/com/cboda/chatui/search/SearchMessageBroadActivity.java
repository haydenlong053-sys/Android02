/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.search;

import java.util.List;

import com.cboda.chatui.R;
import com.cboda.chatui.search.module.ConversationMessageSearchBroadModule;
import cn.wildfirechat.model.Conversation;

public class SearchMessageBroadActivity extends SearchBroadActivity {
    private Conversation conversation;

    @Override
    protected void beforeViews() {
        conversation = getIntent().getParcelableExtra("conversation");
        setStatusBarColor(R.color.color_main_bg);
    }

    @Override
    protected void initSearchModule(List<SearchableBroadModule> modules) {
        modules.add(new ConversationMessageSearchBroadModule(conversation));
    }


}
