/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversationlist.viewholder;

import android.util.Log;
import android.util.SparseArray;

import com.cboda.chatui.annotation.ConversationInfoBroadType;

public class ConversationViewHolderBroadManager {
    private static ConversationViewHolderBroadManager instance = new ConversationViewHolderBroadManager();

    private ConversationViewHolderBroadManager() {
        registerConversationViewHolder(SingleConversationViewBroadHolder.class);
        registerConversationViewHolder(SecretConversationViewBroadHolder.class);
        registerConversationViewHolder(GroupConversationViewBroadHolder.class);
        registerConversationViewHolder(ChannelConversationViewBroadHolder.class);
        registerConversationViewHolder(ChatRoomConversationViewBroadHolder.class);
    }

    public static ConversationViewHolderBroadManager getInstance() {
        return instance;
    }

    private SparseArray<Class<? extends ConversationViewBroadHolder>> messageViewHolders = new SparseArray<>();

    public void registerConversationViewHolder(Class<? extends ConversationViewBroadHolder> clazz) {
        ConversationInfoBroadType conversationInfoType = clazz.getAnnotation(ConversationInfoBroadType.class);
        int type = conversationInfoType.type().getValue() << 24 | conversationInfoType.line();
        if (messageViewHolders.get(type) != null) {
            throw new RuntimeException("type is already registered or viewHolder's annotation is error " + clazz.getSimpleName());
        }
        messageViewHolders.put(type, clazz);
    }

    public Class<? extends ConversationViewBroadHolder> getConversationContentViewHolder(int type) {
        Class clazz = messageViewHolders.get(type);
        if (clazz == null) {
            type = type & 0xFFFFFF00;
            clazz = messageViewHolders.get(type);
            if (clazz == null) {
                clazz = UnknownConversationViewBroadHolder.class;
            }
            Log.e("wfc", "未配置对应的ConversationViewHolder，" + (type >> 24) + " " + (type & 0xFF) + " " + clazz.getSimpleName());
        }
        return clazz;
    }
}
