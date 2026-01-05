/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.search;

import java.util.List;

import com.cboda.chatui.search.module.ContactSearchModule;
import com.cboda.chatui.search.module.ConversationSearchModule2;
import com.cboda.chatui.search.module.GroupSearchViewModule2;

public class SearchPortalActivity extends SearchActivity2 {
    @Override
    protected void initSearchModule(List<SearchableModule> modules) {
//        Log.d("zhanglong","SearchActivity2=========initSearchModule");
        SearchableModule module = new ContactSearchModule();
//        modules.add(module);

        module = new GroupSearchViewModule2();
        modules.add(module);

        module = new ConversationSearchModule2();
        modules.add(module);
//        modules.add(new ChannelSearchModule());
    }
}
