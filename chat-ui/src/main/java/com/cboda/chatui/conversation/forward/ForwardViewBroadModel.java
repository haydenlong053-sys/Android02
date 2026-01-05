/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversation.forward;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import com.cboda.chatui.common.OperateBroadResult;
import cn.wildfirechat.message.ArticlesMessageContent;
import cn.wildfirechat.message.LinkMessageContent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.remote.ChatManager;

public class ForwardViewBroadModel extends ViewModel {
    public MutableLiveData<OperateBroadResult<Integer>> forward(Conversation targetConversation, Message... messages) {
        MutableLiveData<OperateBroadResult<Integer>> resultMutableLiveData = new MutableLiveData<>();

        for (Message message : messages) {
            if (message == null) {
                continue;
            }
            message.conversation = targetConversation;
            if (message.content instanceof ArticlesMessageContent) {
                List<LinkMessageContent> contents = ((ArticlesMessageContent) message.content).toLinkMessageContent();
                for (LinkMessageContent content : contents) {
                    ChatManager.Instance().sendMessage(targetConversation, content, null, 0, null);
                }
            } else {
                ChatManager.Instance().sendMessage(message, null);
            }
        }
        resultMutableLiveData.postValue(new OperateBroadResult<Integer>(0));
        return resultMutableLiveData;
    }
}
