/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.multimsg;

import java.lang.reflect.Constructor;
import java.util.ArrayList;
import java.util.List;

import cn.wildfirechat.model.Conversation;

public class MultiMessageActionBroadManager {
    private static MultiMessageActionBroadManager instance;
    private List<MultiMessageBroadAction> conversationMultiMessageActions;

    private MultiMessageActionBroadManager() {
        conversationMultiMessageActions = new ArrayList<>();
        init();
    }

    public static synchronized MultiMessageActionBroadManager getInstance() {
        if (instance == null) {
            instance = new MultiMessageActionBroadManager();
        }
        return instance;
    }

    private void init() {
        registerAction(DeleteMultiMessageBroadAction.class);
        registerAction(ForwardMessageBroadAction.class);
    }

    public void registerAction(Class<? extends MultiMessageBroadAction> clazz) {
        Constructor constructor;
        try {
            constructor = clazz.getConstructor();
            MultiMessageBroadAction action = (MultiMessageBroadAction) constructor.newInstance();
            conversationMultiMessageActions.add(action);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void unregisterAction(Class<? extends MultiMessageBroadAction> clazz) {
        // TODO
    }

    public List<MultiMessageBroadAction> getConversationActions(Conversation conversation) {
        List<MultiMessageBroadAction> currentActions = new ArrayList<>();
        for (MultiMessageBroadAction ext : this.conversationMultiMessageActions) {
            if (!ext.filter(conversation)) {
                currentActions.add(ext);
            }
        }
        return currentActions;
    }
}
