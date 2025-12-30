/*
 * Copyright (c) 2022 WildFireChat. All rights reserved.
 */

package cn.wildfire.chat.kit.conversationlist;

import cn.wildfirechat.model.ConversationInfo;

public interface OnClickConversationItemListener {
    void onClickConversationItem(ConversationInfo conversationInfo);
    void onClickConversationItemNew(ConversationInfo conversationInfo,int position);
}
