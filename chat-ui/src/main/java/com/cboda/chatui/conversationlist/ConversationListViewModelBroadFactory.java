/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversationlist;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import java.util.List;

import cn.wildfirechat.model.Conversation;

public class ConversationListViewModelBroadFactory extends ViewModelProvider.NewInstanceFactory {
    private List<Conversation.ConversationType> types;
    private List<Integer> lines;

    public ConversationListViewModelBroadFactory(List<Conversation.ConversationType> types, List<Integer> lines) {
        this.types = types;
        this.lines = lines;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        return (T) new ConversationListViewBroadModel(this.types, this.lines);
    }
}
