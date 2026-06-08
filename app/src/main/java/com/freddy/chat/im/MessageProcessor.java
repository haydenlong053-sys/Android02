package com.freddy.chat.im;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.chat.event.CEventCenter;
import com.freddy.chat.event.Events;
import com.freddy.chat.event.MessageReadStateEvent;
import com.freddy.chat.utils.CThreadPoolExecutor;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.db.ImLocalStore;
import com.freddy.im.http.model.im.SessionItem;
import com.freddy.im.message.ImMessageManager;
import com.freddy.im.signal.ImSignalProtos;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>@ProjectName:     NettyChat</p>
 * <p>@ClassName:       MessageProcessor.java</p>
 * <p>@PackageName:     com.freddy.chat.im</p>
 * <b>
 * <p>@Description:     消息处理器</p>
 * </b>
 */
public class MessageProcessor implements IMessageProcessor {

    private static final String TAG = MessageProcessor.class.getSimpleName();

    private MessageProcessor() {
        ImMessageManager.getInstance().setOnOutgoingMessageListener(message ->
                CThreadPoolExecutor.runInBackground(() -> {
                    try {
                        System.out.println("更新消息---》"+message.getMsgId());
                        CEventCenter.dispatchEvent(
                                Events.CHAT_MESSAGE_UPDATE, 0, 0, message);
                    } catch (Exception e) {
                        Log.e(TAG, "发送成功消息分发失败，reason=" + e.getMessage());
                    }
                }));
        ImLocalStore.setOnSessionPreviewUpdatedListener(this::notifySessionsUpdated);
        ImLocalStore.setOnGroupMembersUpdatedListener(this::notifyGroupMembersUpdated);
        ImLocalStore.setOnSessionRemovedListener(this::notifySessionRemoved);
    }

    private static class MessageProcessorInstance {
        private static final IMessageProcessor INSTANCE = new MessageProcessor();
    }

    public static IMessageProcessor getInstance() {
        return MessageProcessorInstance.INSTANCE;
    }

    /**
     * 接收一批消息：后台线程中一次性 {@link CEventCenter#dispatchEvent}，事件载荷为 {@link List}{@code <AppMessage>}。
     *
     * @param messages 可为空，null 或空列表直接返回
     */
    @Override
    public void receiveMsg(final List<AppMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        final List<AppMessage> batch = new ArrayList<>(messages.size());
        for (AppMessage m : messages) {
            if (m != null) {
                batch.add(m);
            }
        }
        if (batch.isEmpty()) {
            return;
        }
        CThreadPoolExecutor.runInBackground(new Runnable() {

            @Override
            public void run() {
                try {
                    Log.d(TAG, "收到消息批量，count=" + batch.size());
                    CEventCenter.dispatchEvent(Events.CHAT_MESSAGE, 0, 0, batch);
                } catch (Exception e) {
                    Log.e(TAG, "消息处理出错，reason=" + e.getMessage());
                }
            }
        });
    }

    @Override
    public void notifyConversationList() {
        CThreadPoolExecutor.runInBackground(new Runnable() {

            @Override
            public void run() {
                try {
                    CEventCenter.dispatchEvent(Events.NOTIFY_CONVERSATION_LIST, 0, 0, "");
                } catch (Exception e) {
                    Log.e(TAG, "消息处理出错，reason=" + e.getMessage());
                }
            }
        });
    }

    @Override
    public void notifyGroupMembersUpdated(@NonNull final String groupId) {
        if (TextUtils.isEmpty(groupId)) {
            return;
        }
        CThreadPoolExecutor.runOnMainThread(() -> {
            try {
                CEventCenter.dispatchEvent(Events.GROUP_MEMBERS_UPDATED, 0, 0, groupId);
            } catch (Exception e) {
                Log.e(TAG, "群成员更新分发失败，reason=" + e.getMessage());
            }
        });
    }

    @Override
    public void notifySessionsUpdated(final List<SessionItem> sessions) {
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        final List<SessionItem> batch = new ArrayList<>(sessions);
        CThreadPoolExecutor.runOnMainThread(() -> {
            try {
                CEventCenter.dispatchEvent(Events.SESSION_LIST_UPDATED, 0, 0, batch);
            } catch (Exception e) {
                Log.e(TAG, "会话局部更新分发失败，reason=" + e.getMessage());
            }
        });
    }

    @Override
    public void notifySessionRemoved(@NonNull final String sessionId) {
        if (TextUtils.isEmpty(sessionId)) {
            return;
        }
        CThreadPoolExecutor.runOnMainThread(() -> {
            try {
                CEventCenter.dispatchEvent(Events.SESSION_REMOVED, 0, 0, sessionId);
            } catch (Exception e) {
                Log.e(TAG, "会话删除分发失败，reason=" + e.getMessage());
            }
        });
    }

    @Override
    public void sendMsg(final AppMessage message) {
        sendMsg(message, null, true);
    }

    /**
     * 先 {@link ImLocalStore#saveOutgoingMessage}，事务提交成功后：主线程 UI 回调（可选），再后台发信令。
     */
    @Override
    public void sendMsg(@NonNull final AppMessage message,
                        @Nullable final Runnable onUiAfterOutgoingPersisted) {
        sendMsg(message, onUiAfterOutgoingPersisted, true);
    }

    @Override
    public void sendMsg(@NonNull final AppMessage message,
                        @Nullable final Runnable onUiAfterOutgoingPersisted,
                        final boolean transmitImmediately) {
        if (message == null) {
            return;
        }
        ImLocalStore.saveOutgoingMessage(message, () -> {
            if (onUiAfterOutgoingPersisted != null) {
                CThreadPoolExecutor.runOnMainThread(onUiAfterOutgoingPersisted);
            }
            if (transmitImmediately) {
                CThreadPoolExecutor.runInBackground(() -> transmitOutgoingMessage(message));
            }
        });
    }

    @Override
    public void transmitOutgoingMessage(@NonNull final AppMessage message) {
        if (message == null) {
            return;
        }
        CThreadPoolExecutor.runInBackground(() -> {
            boolean isActive = IMSClientBootstrap.getInstance().isActive();
            if (isActive) {
                ImSignalProtos.SignalRequest req = MessageBuilder.buildSendMsgSignalRequest(message,
                        IMSClientBootstrap.getInstance().getBindToken());
                IMSClientBootstrap.getInstance().sendSignalRequest(req, true);
            } else {
                Log.e(TAG, "发送消息失败");
                ImLocalStore.markOutgoingSendFailed(message.getClientMsgId(), updated -> {
                    if (updated != null) {
                        dispatchOutgoingUpdated(updated);
                    }
                });
            }
        });
    }

    private void dispatchOutgoingUpdated(@NonNull AppMessage message) {
        CThreadPoolExecutor.runInBackground(() -> {
            try {
                CEventCenter.dispatchEvent(Events.CHAT_MESSAGE_UPDATE, 0, 0, message);
            } catch (Exception e) {
                Log.e(TAG, "消息更新分发失败，reason=" + e.getMessage());
            }
        });
    }

    /**
     * 单聊 0x0207：对方已读至 {@code messageId} 对应序列（含该序列及之前己方消息展示已读）。
     */
    public void notifyPeerMessageRead(@NonNull String messageId) {
        if (TextUtils.isEmpty(messageId)) {
            return;
        }
        String sessionId = ImMessageManager.extractConversationAnchor(messageId);
        if (TextUtils.isEmpty(sessionId)) {
            return;
        }
        final MessageReadStateEvent event = new MessageReadStateEvent(
                sessionId,
                MessageReadStateEvent.Type.PEER_READ,
                messageId,
                0);
        ImLocalStore.persistPeerMessageRead(sessionId, messageId, () ->
                dispatchMessageReadState(event));
    }

    /**
     * 群聊 0x0210：某消息已读人数变更。
     */
    public void notifyGroupMessageReadCount(@NonNull String messageId, int readPersonCount) {
        if (TextUtils.isEmpty(messageId)) {
            return;
        }
        String sessionId = ImMessageManager.extractConversationAnchor(messageId);
        if (TextUtils.isEmpty(sessionId)) {
            sessionId = messageId;
        }
        final String finalSessionId = sessionId;
        final MessageReadStateEvent event = new MessageReadStateEvent(
                finalSessionId,
                MessageReadStateEvent.Type.GROUP_READ_COUNT,
                messageId,
                readPersonCount);
        ImLocalStore.persistGroupMessageReadCount(messageId, readPersonCount, () ->
                dispatchMessageReadState(event));
    }

    private void dispatchMessageReadState(@NonNull MessageReadStateEvent event) {
        CThreadPoolExecutor.runOnMainThread(() -> {
            try {
                CEventCenter.dispatchEvent(Events.MESSAGE_READ_STATE_CHANGED, 0, 0, event);
            } catch (Exception e) {
                Log.e(TAG, "已读状态分发失败，reason=" + e.getMessage());
            }
        });
    }

//    /**
//     * 发送消息
//     *
//     * @param message
//     */
//    @Override
//    public void sendMsg(ContentMessage message) {
//        this.sendMsg(MessageBuilder.buildAppMessage(message));
//    }


}
