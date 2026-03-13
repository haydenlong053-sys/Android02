/*
 * Copyright (c) 2020 WildFireChat. All rights reserved.
 */

package com.cboda.chatui.conversationlist;

import static com.cboda.baselibrary.utils.ELogUtilKt.eLogD;

import android.os.RemoteException;
import android.text.TextUtils;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.cboda.baselibrary.utils.MMKVBroadUtils;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import cn.wildfirechat.event.MsgUnReadCountEvent;
import cn.wildfirechat.message.Message;
import cn.wildfirechat.model.Conversation;
import cn.wildfirechat.model.ConversationInfo;
import cn.wildfirechat.model.GroupInfo;
import cn.wildfirechat.model.UnreadCount;
import cn.wildfirechat.remote.ChatManager;
import cn.wildfirechat.remote.GeneralCallback;
import cn.wildfirechat.remote.GetConversationListCallback;
import cn.wildfirechat.remote.OnClearMessageListener;
import cn.wildfirechat.remote.OnConnectionStatusChangeListener;
import cn.wildfirechat.remote.OnConversationInfoUpdateListener;
import cn.wildfirechat.remote.OnDeleteMessageListener;
import cn.wildfirechat.remote.OnRecallMessageListener;
import cn.wildfirechat.remote.OnReceiveMessageListener;
import cn.wildfirechat.remote.OnRemoveConversationListener;
import cn.wildfirechat.remote.OnSendMessageListener;
import cn.wildfirechat.remote.OnSettingUpdateListener;
import cn.wildfirechat.remote.SecretMessageBurnStateListener;

/**
 * how
 * 1. observe conversationInfoLiveData in your activity or fragment, but if you still not called getConversationList,
 * just ignore the data.
 * 2. call getConversationList
 */
public class ConversationListViewBroadModel extends ViewModel implements OnReceiveMessageListener,
        OnSendMessageListener,
        OnRecallMessageListener,
        OnDeleteMessageListener,
        OnConversationInfoUpdateListener,
        OnRemoveConversationListener,
        OnConnectionStatusChangeListener,
        OnClearMessageListener,
        OnSettingUpdateListener, SecretMessageBurnStateListener {
    private MutableLiveData<List<ConversationInfo>> conversationListLiveData;
    private MutableLiveData<UnreadCount> unreadCountLiveData;
    private MutableLiveData<MsgUnReadCountEvent> msgUnreadCountLiveData;
    private MutableLiveData<Integer> connectionStatusLiveData = new MutableLiveData<>();

    private List<Conversation.ConversationType> types;
    private List<Integer> lines;

    public ConversationListViewBroadModel(List<Conversation.ConversationType> types, List<Integer> lines) {
        super();
        this.types = types;
        this.lines = lines;
        ChatManager.Instance().addOnReceiveMessageListener(this);
        ChatManager.Instance().addSendMessageListener(this);
        ChatManager.Instance().addConversationInfoUpdateListener(this);
        ChatManager.Instance().addRecallMessageListener(this);
        ChatManager.Instance().addConnectionChangeListener(this);
        ChatManager.Instance().addDeleteMessageListener(this);
        ChatManager.Instance().addClearMessageListener(this);
        ChatManager.Instance().addRemoveConversationListener(this);
        ChatManager.Instance().addSettingUpdateListener(this);
        ChatManager.Instance().addSecretMessageBurnStateListener(this);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        ChatManager.Instance().removeOnReceiveMessageListener(this);
        ChatManager.Instance().removeSendMessageListener(this);
        ChatManager.Instance().removeConversationInfoUpdateListener(this);
        ChatManager.Instance().removeConnectionChangeListener(this);
        ChatManager.Instance().removeRecallMessageListener(this);
        ChatManager.Instance().removeDeleteMessageListener(this);
        ChatManager.Instance().removeClearMessageListener(this);
        ChatManager.Instance().removeRemoveConversationListener(this);
        ChatManager.Instance().removeSettingUpdateListener(this);
        ChatManager.Instance().removeSecretMessageBurnStateListener(this);
    }

    private AtomicInteger loadingCount = new AtomicInteger(0);

    public void reloadConversationList() {
        reloadConversationList(false);
    }

    public void reloadConversationList(boolean force) {
        if (conversationListLiveData == null) {
            return;
        }
        if (!force) {
            int count = loadingCount.get();
            if (count > 0) {
                return;
            }
        }
        loadingCount.incrementAndGet();

        ChatManager.Instance().getWorkHandler().post(() -> {
            ChatManager.Instance().getConversationListAsync(types, lines, new GetConversationListCallback() {
                @Override
                public void onSuccess(List<ConversationInfo> conversationInfos) {
                    conversationListLiveData.postValue(conversationInfos);
                    loadingCount.decrementAndGet();
                }

                @Override
                public void onFail(int errorCode) {
                    loadingCount.decrementAndGet();
                }
            });
        });
    }

    public MutableLiveData<List<ConversationInfo>> conversationListLiveData() {
        if (conversationListLiveData == null) {
            conversationListLiveData = new MutableLiveData<>();
        }
        ChatManager.Instance().getWorkHandler().post(() -> {
            ChatManager.Instance().getConversationListAsync(types, lines, new GetConversationListCallback() {
                @Override
                public void onSuccess(List<ConversationInfo> conversationInfos) {
                    conversationListLiveData.postValue(conversationInfos);
                }

                @Override
                public void onFail(int errorCode) {

                }
            });
        });

        return conversationListLiveData;
    }

    public MutableLiveData<UnreadCount> unreadCountLiveData() {
        if (unreadCountLiveData == null) {
            unreadCountLiveData = new MutableLiveData<>();
        }
        eLogD(this.getClass().getSimpleName(), "getGroupInfos67y");
        reloadConversationUnreadStatus();
        return unreadCountLiveData;
    }

    public MutableLiveData<MsgUnReadCountEvent> unreadCountEventLiveData() {
        if (msgUnreadCountLiveData == null) {
            msgUnreadCountLiveData = new MutableLiveData<>();
        }
        eLogD(this.getClass().getSimpleName(), "getGroupInfos679");
        reloadConversationUnreadStatus();
        return msgUnreadCountLiveData;
    }

    public MutableLiveData<Integer> connectionStatusLiveData() {
        return connectionStatusLiveData;
    }

    public void reloadConversationUnreadStatus() {
//        eLogD(this.getClass().getSimpleName(), "reloadConversationUnreadStatus");
        int myGroupUnreadCount = 0;
        int otherGroupUnreadCount = 0;
        int realSingleUnreadCount = 0;
//        UnreadCount unreadCount = ChatManager.Instance().getUnreadCountEx(Arrays.asList(Conversation.ConversationType.Single, Conversation.ConversationType.Group, Conversation.ConversationType.Channel), Collections.singletonList(0));
        UnreadCount unreadCount = ChatManager.Instance().getUnreadCountEx(Arrays.asList(Conversation.ConversationType.Single, Conversation.ConversationType.Group), Collections.singletonList(0));

        MsgUnReadCountEvent msgUnReadCountEvent = new MsgUnReadCountEvent();
        msgUnReadCountEvent.unreadMention = unreadCount.unreadMention;
        msgUnReadCountEvent.unreadMentionAll = unreadCount.unreadMentionAll;
//        UnreadCount singleUnread = ChatManager.Instance().getUnreadCountEx(Collections.singletonList(Conversation.ConversationType.Single), Collections.singletonList(0));
//        msgUnReadCountEvent.singleCount=singleUnread.unread;
        List<ConversationInfo> singlecConversationList = ChatManager.Instance().getConversationList(Collections.singletonList(Conversation.ConversationType.Single), Arrays.asList(0));
//        Log.d("zhanglong","singlecConversationList----"+singlecConversationList.size());

        List<ConversationInfo> realSinglecConversationList = singlecConversationList.stream().filter(
                conversationInfo -> {
                    try {
                        return ChatManager.mClient.isMyFriend(conversationInfo.conversation.target) || TextUtils.equals(conversationInfo.conversation.target, MMKVBroadUtils.Companion.getKeFuId());
                    } catch (RemoteException e) {
                        return false;
                    }
                }
        ).collect(Collectors.toList());
        for (ConversationInfo info : realSinglecConversationList) {
            if (info.unreadCount.unread > 0 && !info.isSilent) {
                realSingleUnreadCount += info.unreadCount.unread;
            }
        }
        msgUnReadCountEvent.singleCount = realSingleUnreadCount;

        UnreadCount groupUnread = ChatManager.Instance().getUnreadCountEx(Collections.singletonList(Conversation.ConversationType.Group), Collections.singletonList(0));
        msgUnReadCountEvent.groupCount = groupUnread.unread;
        List<Conversation.ConversationType> groupTypes = Arrays.asList(Conversation.ConversationType.Group);
        List<Integer> lines = Arrays.asList(0);
        List<ConversationInfo> conversationList = ChatManager.Instance().getConversationList(groupTypes, lines);
//        Log.d("zhanglong","groupConversationList----"+conversationList.size());

        //我的群聊
        List<ConversationInfo> myConversationList = conversationList.stream().filter(
                conversationInfo -> {
                    try {
//                        eLogD(this.getClass().getSimpleName(), "getGroupInfos67");
                        GroupInfo groupInfo = ChatManager.Instance().getGroupInfo(conversationInfo.conversation.target, false);
                        String myUserId = ChatManager.Instance().getUserId();
                        return groupInfo != null && groupInfo.owner.equals(myUserId);
                    } catch (Exception e) {
                        return false;
                    }
                }
        ).collect(Collectors.toList());

        for (ConversationInfo conversationInfo : myConversationList) {
            if (conversationInfo.unreadCount.unread > 0) {
                myGroupUnreadCount += conversationInfo.unreadCount.unread;
            }
        }
        otherGroupUnreadCount = groupUnread.unread - myGroupUnreadCount;
        msgUnReadCountEvent.myGroupCount = myGroupUnreadCount;
        msgUnReadCountEvent.otherGroupCount = otherGroupUnreadCount;

        if (msgUnreadCountLiveData != null) {
            msgUnreadCountLiveData.postValue(msgUnReadCountEvent);
        }
//        Log.d("zhanglong","reloadConversationUnreadStatus----"+msgUnReadCountEvent.toString());

//        if (unreadCountLiveData == null) {
//            return;
//        }
//        unreadCountLiveData.postValue(unreadCount);
    }

    @Override
    public void onReceiveMessage(List<Message> messages, boolean hasMore) {
//        if(messages.isEmpty())
        reloadConversationList(true);
//        eLogD(this.getClass().getSimpleName(), "getGroupInfos67a");
        reloadConversationUnreadStatus();
    }

    @Override
    public void onRecallMessage(Message message) {
        reloadConversationList();
//        eLogD(this.getClass().getSimpleName(), "getGroupInfos67b");
        reloadConversationUnreadStatus();
    }


    @Override
    public void onSendSuccess(Message message) {
        reloadConversationList();
    }

    @Override
    public void onSendFail(Message message, int errorCode) {
        reloadConversationList();
    }

    @Override
    public void onSendPrepare(Message message, long savedTime) {
        Conversation conversation = message.conversation;
        if (types.contains(conversation.type) && lines.contains(conversation.line)) {
            if (message.messageId > 0) {
                reloadConversationList();
            }
        }
    }

    public void removeConversation(ConversationInfo conversationInfo, boolean clearMsg) {
        ChatManager.Instance().clearUnreadStatus(conversationInfo.conversation);
        ChatManager.Instance().removeConversation(conversationInfo.conversation, clearMsg);
    }

    public void clearMessages(Conversation conversation) {
        ChatManager.Instance().clearMessages(conversation);
    }

    // TODO move the following to another class
    public void unSubscribeChannel(ConversationInfo conversationInfo) {
        ChatManager.Instance().listenChannel(conversationInfo.conversation.target, false, new GeneralCallback() {
            @Override
            public void onSuccess() {
                removeConversation(conversationInfo, false);
            }

            @Override
            public void onFail(int errorCode) {
                // do nothing
            }
        });
    }

    public void setConversationTop(ConversationInfo conversationInfo, int top) {
        ChatManager.Instance().setConversationTop(conversationInfo.conversation, top);
    }

    public void clearConversationUnread(ConversationInfo conversationInfo) {
        ChatManager.Instance().clearUnreadStatus(conversationInfo.conversation);
    }

    public void markConversationUnread(ConversationInfo conversationInfo) {
        ChatManager.Instance().markAsUnRead(conversationInfo.conversation, true);
    }

    @Override
    public void onDeleteMessage(Message message) {
        reloadConversationList();
        eLogD(this.getClass().getSimpleName(), "getGroupInfos67c");
        reloadConversationUnreadStatus();
    }

    @Override
    public void onConversationDraftUpdate(ConversationInfo conversationInfo, String draft) {
        reloadConversationList();
    }

    @Override
    public void onConversationTopUpdate(ConversationInfo conversationInfo, int top) {
        reloadConversationList();
    }

    @Override
    public void onConversationSilentUpdate(ConversationInfo conversationInfo, boolean silent) {
        reloadConversationList();
    }

    @Override
    public void onConversationUnreadStatusClear(ConversationInfo conversationInfo) {
        reloadConversationList();
        eLogD(this.getClass().getSimpleName(), "getGroupInfos67g");
        reloadConversationUnreadStatus();
    }

    @Override
    public void onConnectionStatusChange(int status) {
        connectionStatusLiveData.postValue(status);
    }

    @Override
    public void onClearMessage(Conversation conversation) {
        reloadConversationList();
        eLogD(this.getClass().getSimpleName(), "getGroupInfos67f");
        reloadConversationUnreadStatus();
    }

    @Override
    public void onConversationRemove(Conversation conversation) {
        reloadConversationList();
        eLogD(this.getClass().getSimpleName(), "getGroupInfos67e");
        reloadConversationUnreadStatus();
    }

    @Override
    public void onSettingUpdate() {
        reloadConversationList();
        eLogD(this.getClass().getSimpleName(), "getGroupInfos67d");
        reloadConversationUnreadStatus();
    }

    @Override
    public void onSecretMessageStartBurning(String targetId, long playedMsgId) {
        // do nothing
    }

    @Override
    public void onSecretMessageBurned(List<Long> messageIds) {
        reloadConversationList();
    }
}
