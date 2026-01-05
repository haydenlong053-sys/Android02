/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.settings.blacklist;

import androidx.lifecycle.ViewModel;

import java.util.List;

import cn.wildfirechat.remote.ChatManager;

public class BlacklistViewBroadModel extends ViewModel {

    public BlacklistViewBroadModel() {
        super();
    }

    @Override
    protected void onCleared() {
        super.onCleared();
    }

    public List<String> getBlacklists() {
        return ChatManager.Instance().getBlackList(true);
    }

}
