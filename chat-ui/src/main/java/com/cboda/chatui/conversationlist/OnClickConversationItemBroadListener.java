/*
 * Copyright (c) 2022 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversationlist;

import cn.wildfirechat.model.ConversationInfo;

public interface OnClickConversationItemBroadListener {
    void onClickConversationItem(ConversationInfo conversationInfo);
    void onClickConversationItemNew(ConversationInfo conversationInfo,int position);
}
