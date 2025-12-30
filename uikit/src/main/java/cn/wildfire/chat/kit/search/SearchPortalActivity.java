/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.search;

import java.util.List;

import cn.wildfire.chat.kit.search.module.ContactSearchModule;
import cn.wildfire.chat.kit.search.module.ConversationSearchModule2;
import cn.wildfire.chat.kit.search.module.GroupSearchViewModule2;

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
