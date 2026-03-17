/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.search;

import java.util.List;

import com.cboda.chatui.search.module.ContactSearchBroadModule;
import com.cboda.chatui.search.module.ConversationSearchBroadModule2;
import com.cboda.chatui.search.module.GroupSearchViewBroadModule2;

public class SearchPortalBroadActivity extends SearchBroadActivity2 {
    @Override
    protected void initSearchModule(List<SearchableBroadModule> modules) {
//        Log.d(APP_TAG,"SearchBroadActivity2=========initSearchModule");
        SearchableBroadModule module = new ContactSearchBroadModule();
//        modules.add(module);

        module = new GroupSearchViewBroadModule2();
        modules.add(module);

        module = new ConversationSearchBroadModule2();
        modules.add(module);
//        modules.add(new ChannelSearchBroadModule());
    }
}
