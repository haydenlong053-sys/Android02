/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.search;

import java.util.List;

class SearchBroadResult {
    SearchableBroadModule module;
    List<Object> result;

    public SearchBroadResult(SearchableBroadModule module, List<Object> result) {
        this.module = module;
        this.result = result;
    }
}
