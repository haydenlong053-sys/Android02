package com.freddy.chat.im;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cboda.baselibrary.toast.ToastBroadUtils;
import com.cboda.baselibrary.utils.ELogUtilKt;
import com.freddy.chat.event.CEventCenter;
import com.freddy.chat.event.Events;
import com.freddy.chat.event.GroupMuteUpdateEvent;
import com.freddy.chat.event.MessageDeletedEvent;
import com.freddy.chat.event.MessageReadStateEvent;
import com.freddy.chat.event.MessageRepealEvent;
import com.freddy.chat.utils.CThreadPoolExecutor;
import com.freddy.chat.utils.ImMsgReminder;
import com.freddy.chat.utils.MediaMessageContentUtil;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.db.ImLocalStore;
import com.freddy.im.http.model.im.SessionItem;
import com.freddy.im.message.ImMessageManager;
import com.freddy.im.signal.ImSignalProtos;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import android.os.SystemClock;

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

    /** BIND 与 onImChannelReady 等短时间重复触发时合并为一次拉会话。 */
    private static final long NOTIFY_CONVERSATION_DEBOUNCE_MS = 1500L;

    private final AtomicLong lastNotifyConversationElapsed = new AtomicLong(0L);

    private MessageProcessor() {
        ImMessageManager.getInstance().setOnOutgoingMessageListener(
                new com.freddy.im.message.OnOutgoingMessageListener() {
                    @Override
                    public void onOutgoingMessageUpdated(@NonNull AppMessage message) {
                        CThreadPoolExecutor.runInBackground(() -> {
                            try {
                                System.out.println("更新消息---》" + message.getMsgId()
                                        + " 消息状态-->" + message.getSendStatus());
                                CEventCenter.dispatchEvent(
                                        Events.CHAT_MESSAGE_UPDATE, 0, 0, message);
                            } catch (Exception e) {
                                Log.e(TAG, "发送成功消息分发失败，reason=" + e.getMessage());
                            }
                        });
                    }

                    @Override
                    public void onSendBizError(int errorCode, @Nullable String errorMsg) {
//                        if (!com.freddy.im.http.ImGroupBizErrorCodes.isMuteSendError(errorCode)) {
//                            return;
//                        }
                        CThreadPoolExecutor.runOnMainThread(() -> {
                            String text = !TextUtils.isEmpty(errorMsg) ? errorMsg : "发送失败";
                            com.cboda.baselibrary.toast.ToastBroadUtils.show(text);
                        });
                    }
                });
        ImMessageManager.getInstance().setDefaultNewMessageListListener(
                batch -> MessageProcessor.getInstance().receiveMsg(batch));
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
                    // 新消息提醒（非免打扰）：对齐野火 TiXingBroadUtils
                    ImMsgReminder.onIncomingBatch(batch);
                    CEventCenter.dispatchEvent(Events.CHAT_MESSAGE, 0, 0, batch);
                } catch (Exception e) {
                    Log.e(TAG, "消息处理出错，reason=" + e.getMessage());
                }
            }
        });
    }

    @Override
    public void notifyConversationList() {
        long now = SystemClock.elapsedRealtime();
        long last = lastNotifyConversationElapsed.get();
        if (last > 0L && now - last < NOTIFY_CONVERSATION_DEBOUNCE_MS) {
            Log.d(TAG, "notifyConversationList debounce skip");
            return;
        }
        lastNotifyConversationElapsed.set(now);
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
    public void notifyGroupMemberRolesUpdated(@NonNull final String groupId) {
        if (TextUtils.isEmpty(groupId)) {
            return;
        }
        CThreadPoolExecutor.runOnMainThread(() -> {
            try {
                CEventCenter.dispatchEvent(Events.GROUP_MEMBERS_ROLE_UPDATE, 0, 0, groupId);
            } catch (Exception e) {
                Log.e(TAG, "群成员角色变更分发失败，reason=" + e.getMessage());
            }
        });
    }

    @Override
    public void notifyGroupNoticeUpdated(@NonNull final String groupId) {
        if (TextUtils.isEmpty(groupId)) {
            return;
        }
        CThreadPoolExecutor.runOnMainThread(() -> {
            try {
                CEventCenter.dispatchEvent(Events.GROUP_NOTICE_UPDATED, 0, 0, groupId);
            } catch (Exception e) {
                Log.e(TAG, "群公告更新分发失败，reason=" + e.getMessage());
            }
        });
    }

    @Override
    public void notifyGroupMuteUpdated(@NonNull final String groupId) {
        notifyGroupMuteUpdated(groupId, GroupMuteUpdateEvent.REFRESH_FROM_SERVER);
    }

    @Override
    public void notifyGroupMuteUpdated(@NonNull final String groupId, final int allMute) {
        if (TextUtils.isEmpty(groupId)) {
            return;
        }
        final GroupMuteUpdateEvent event = new GroupMuteUpdateEvent(groupId, allMute);
        dispatchGroupMuteEvent(event);
    }

    @Override
    public void notifyGroupMemberMuteUpdated(@NonNull final String groupId, final int mute) {
        if (TextUtils.isEmpty(groupId)) {
            return;
        }
        final GroupMuteUpdateEvent event = GroupMuteUpdateEvent.memberMute(groupId, mute);
        dispatchGroupMuteEvent(event);
    }

    private void dispatchGroupMuteEvent(@NonNull final GroupMuteUpdateEvent event) {
        CThreadPoolExecutor.runOnMainThread(() -> {
            try {
                CEventCenter.dispatchEvent(Events.GROUP_MUTE_UPDATED, 0, 0, event);
            } catch (Exception e) {
                Log.e(TAG, "群禁言更新分发失败，reason=" + e.getMessage());
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
            dispatchOutgoingPersisted(message);
            if (onUiAfterOutgoingPersisted != null) {
                CThreadPoolExecutor.runOnMainThread(onUiAfterOutgoingPersisted);
            }
            if (transmitImmediately) {
                CThreadPoolExecutor.runInBackground(() -> transmitOutgoingMessage(message));
            }
        });
    }

    /**
     * 外发消息落库后通知消息列表刷新（与会话内 {@code adapter.appendMessages} 等价，供转发等场景复用）。
     */
    private void dispatchOutgoingPersisted(@NonNull final AppMessage message) {
        CThreadPoolExecutor.runInBackground(() -> {
            try {
                CEventCenter.dispatchEvent(
                        Events.CHAT_MESSAGE, 0, 0, Collections.singletonList(message));
            } catch (Exception e) {
                Log.e(TAG, "外发消息落库后分发失败，reason=" + e.getMessage());
            }
        });
    }

    @Override
    public void transmitOutgoingMessage(@NonNull final AppMessage message) {
        if (message == null) {
            return;
        }
        CThreadPoolExecutor.runInBackground(() -> {
            if (!MediaMessageContentUtil.isMediaReadyForTransmit(
                    message.getMsgContentType(), message.getContent())) {
                ELogUtilKt.eLogE(TAG, "拒绝发送未上传完成的媒体消息 clientMsgId="
                        + message.getClientMsgId()
                        + " contentType=" + message.getMsgContentType()
                        + " content=" + message.getContent());
                ImLocalStore.markOutgoingSendFailed(message.getClientMsgId(), updated -> {
                    if (updated != null) {
                        dispatchOutgoingUpdated(updated);
                    }
                });
                return;
            }
            boolean isActive = IMSClientBootstrap.getInstance().isActive();
            if (isActive) {
                ImSignalProtos.SignalRequest req = MessageBuilder.buildSendMsgSignalRequest(message,
                        IMSClientBootstrap.getInstance().getBindToken());
                IMSClientBootstrap.getInstance().sendSignalRequest(req, true);
            } else {
                ELogUtilKt.eLogE(TAG, "发送消息失败");
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

    @Override
    public void notifyMessageRepealed(@NonNull String messageId, @Nullable AppMessage recalledMessage) {
        if (TextUtils.isEmpty(messageId)) {
            return;
        }
        String sessionId = ImMessageManager.extractConversationAnchor(messageId);
        final MessageRepealEvent event = new MessageRepealEvent(messageId, sessionId, recalledMessage);
        CThreadPoolExecutor.runOnMainThread(() -> {
            try {
                CEventCenter.dispatchEvent(Events.MESSAGE_REPEAL, 0, 0, event);
            } catch (Exception e) {
                Log.e(TAG, "撤回状态分发失败，reason=" + e.getMessage());
            }
        });
    }

    @Override
    public void notifyMessagesDeleted(@NonNull List<AppMessage> deletedMessages) {
        if (deletedMessages.isEmpty()) {
            return;
        }
        String sessionId = resolveSessionIdForDeletedBatch(deletedMessages);
        final MessageDeletedEvent event = new MessageDeletedEvent(sessionId, deletedMessages);
        CThreadPoolExecutor.runOnMainThread(() -> {
            try {
                CEventCenter.dispatchEvent(Events.MESSAGE_DELETED, 0, 0, event);
            } catch (Exception e) {
                Log.e(TAG, "删除消息分发失败，reason=" + e.getMessage());
            }
        });
    }

    @Override
    public void notifySessionMessagesCleared(@NonNull String sessionId) {
        if (TextUtils.isEmpty(sessionId)) {
            return;
        }
        final String id = sessionId;
        CThreadPoolExecutor.runOnMainThread(() -> {
            try {
                CEventCenter.dispatchEvent(Events.SESSION_MESSAGES_CLEARED, 0, 0, id);
            } catch (Exception e) {
                Log.e(TAG, "清空会话消息分发失败，reason=" + e.getMessage());
            }
        });
    }

    @Nullable
    private static String resolveSessionIdForDeletedBatch(@NonNull List<AppMessage> deletedMessages) {
        for (AppMessage message : deletedMessages) {
            if (message == null) {
                continue;
            }
            if (!TextUtils.isEmpty(message.getConversationId())) {
                return message.getConversationId();
            }
            String anchor = !TextUtils.isEmpty(message.getMsgId())
                    ? message.getMsgId()
                    : message.getClientMsgId();
            String sessionId = ImMessageManager.extractConversationAnchor(anchor);
            if (!TextUtils.isEmpty(sessionId)) {
                return sessionId;
            }
        }
        return null;
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
