/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.chatroom;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cboda.baselibrary.bean.BaseBroadResponse;
import com.cboda.baselibrary.bean.CommonResultBroadBean;
import com.cboda.baselibrary.utils.HttpsBroadConfig;
import com.cboda.chatui.common.OperateBroadResult;
import com.cboda.chatui.net.OKHttpBroadHelper;
import com.cboda.chatui.net.SimpleCallback;

import java.util.HashMap;
import java.util.Map;

import cn.wildfirechat.model.ChatRoomInfo;
import cn.wildfirechat.model.ChatRoomMembersInfo;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback;
import cn.wildfirechat.remote.GetChatRoomInfoCallback;
import cn.wildfirechat.remote.GetChatRoomMembersInfoCallback;

public class ChatRoomViewBroadModel extends ViewModel {
    public MutableLiveData<OperateBroadResult<Boolean>> joinChatRoom(String chatRoomId) {
        MutableLiveData<OperateBroadResult<Boolean>> result = new MutableLiveData<>();
        ChatManager.Instance().joinChatRoom(chatRoomId, new GeneralCallback() {
            @Override
            public void onSuccess() {
                result.setValue(new OperateBroadResult<>(true, 0));

            }

            @Override
            public void onFail(int errorCode) {
                result.setValue(new OperateBroadResult<>(false, errorCode));
            }
        });
        return result;
    }

    public MutableLiveData<OperateBroadResult<Boolean>> quitChatRoom(String chatRoomId) {
        MutableLiveData<OperateBroadResult<Boolean>> result = new MutableLiveData<>();
        ChatManager.Instance().quitChatRoom(chatRoomId, new GeneralCallback() {
            @Override
            public void onSuccess() {
                result.setValue(new OperateBroadResult<>(true, 0));
            }

            @Override
            public void onFail(int errorCode) {
                result.setValue(new OperateBroadResult<>(false, 0));
            }
        });
        return result;
    }

    public MutableLiveData<OperateBroadResult<ChatRoomInfo>> getChatRoomInfo(String chatRoomId, long updateDt) {
        MutableLiveData<OperateBroadResult<ChatRoomInfo>> result = new MutableLiveData<>();
        ChatManager.Instance().getChatRoomInfo(chatRoomId, updateDt, new GetChatRoomInfoCallback() {
            @Override
            public void onSuccess(ChatRoomInfo chatRoomInfo) {
                result.setValue(new OperateBroadResult<>(chatRoomInfo, 0));
            }

            @Override
            public void onFail(int errorCode) {
                result.setValue(new OperateBroadResult<>(null, errorCode));

            }
        });
        return result;
    }

    public MutableLiveData<OperateBroadResult<ChatRoomMembersInfo>> getChatRoomMembersInfo(String chatRoomId, int maxCount) {
        MutableLiveData<OperateBroadResult<ChatRoomMembersInfo>> result = new MutableLiveData<>();
        ChatManager.Instance().getChatRoomMembersInfo(chatRoomId, maxCount, new GetChatRoomMembersInfoCallback() {
            @Override
            public void onSuccess(ChatRoomMembersInfo chatRoomMembersInfo) {
                result.setValue(new OperateBroadResult<>(chatRoomMembersInfo, 0));
            }

            @Override
            public void onFail(int errorCode) {
                result.setValue(new OperateBroadResult<>(null, errorCode));
            }
        });
        return result;
    }
}
