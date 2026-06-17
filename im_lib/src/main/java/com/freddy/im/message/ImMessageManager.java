package com.freddy.im.message;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.bean.AppMessage;
import com.freddy.im.bean.ImMessageMapper;
import com.freddy.im.db.ImDbExecutor;
import com.freddy.im.db.ImLocalStore;
import com.freddy.im.db.SessionSyncCursor;
import com.freddy.im.db.entity.SessionEntity;
import com.freddy.im.db.repository.ImSessionRepository;
import com.freddy.im.http.GroupDetailFetcher;
import com.freddy.im.http.ImMessageHttpExample;
import com.freddy.im.http.model.im.ImMessageItem;
import com.freddy.im.http.model.im.MessageListContent;
import com.freddy.im.http.model.im.SessionItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * 消息 ID 解析、按锚点拉取消息列表等能力统一入口，可供 SDK 内部与宿主层调用。
 */
public final class ImMessageManager {

    /**
     * 无法解析序列号时的返回值（用于去重等逻辑判断）
     */
    public static final long INVALID_SEQUENCE = 0L;

    private static final ImMessageManager INSTANCE = new ImMessageManager();

    @Nullable
    private volatile OnOutgoingMessageListener outgoingMessageListener;

    private ImMessageManager() {
    }

    @NonNull
    public static ImMessageManager getInstance() {
        return INSTANCE;
    }

    /**
     * 从 messageId 解析会话锚点：最后一个 {@code -} 之前为 id（如 {@code uid1!uid2}），之后为序列号。
     */
    @Nullable
    public static String extractConversationAnchor(@Nullable String messageId) {
        if (messageId == null || messageId.isEmpty()) {
            return null;
        }
        int lastDash = messageId.lastIndexOf('-');
        if (lastDash <= 0 || lastDash >= messageId.length() - 1) {
            return null;
        }
        return messageId.substring(0, lastDash);
    }

    /**
     * 解析最后一个 {@code -} 后的非负整数序列；失败返回 {@link #INVALID_SEQUENCE}。
     */
    public static long extractTrailingSequence(@Nullable String messageId) {
        if (messageId == null || messageId.isEmpty()) {
            return INVALID_SEQUENCE;
        }
        int lastDash = messageId.lastIndexOf('-');
        if (lastDash < 0 || lastDash >= messageId.length() - 1) {
            return INVALID_SEQUENCE;
        }
        try {
            return Long.parseLong(messageId.substring(lastDash + 1).trim());
        } catch (NumberFormatException e) {
            return INVALID_SEQUENCE;
        }
    }

    /**
     * POST {@code im/message/getList}：按同步高水位与推送 messageId 计算 anchor/op 后拉取并落库。
     *
     * @param msgId       推送或业务侧的 messageId（形如 {@code uid1!uid2-1046}）
     * @param listener    落库成功后回调；可为 null
     */
    public void fetchNewMessageList(@NonNull String msgId, @Nullable OnNewMessageListListener listener) {
        ImDbExecutor.runRead(() -> fetchNewMessageListOnRealmThread(msgId, listener));
    }

    /**
     * 按会话 {@code sessionId} 拉取消息：以同步高水位 messageId 为锚点。
     */
    public void fetchNewMessageListBySessionId(SessionItem sessionItem,
                                               @Nullable OnNewMessageListListener listener) {
        if (sessionItem == null ||TextUtils.isEmpty(sessionItem.getSessionId())) {
            return;
        }
        ImDbExecutor.runRead(() -> fetchNewMessageListBySessionIdOnRealmThread(sessionItem , listener));
    }

    /**
     * 会话列表拉取成功后：在后台对<strong>全部</strong> {@code unreadCount > 0} 的会话依次拉消息并落库（不阻塞主线程）。
     */
    public void syncUnreadMessagesForSessions(@Nullable List<SessionItem> sessions) {
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        ImDbExecutor.runRead(() -> {
            for (SessionItem sessionItem : sessions) {
                if (sessionItem == null || sessionItem.getUnPullCount() <= 0) {
                    continue;
                }
                String sessionId = sessionItem.getSessionId();
                if (TextUtils.isEmpty(sessionId)) {
                    continue;
                }
                fetchNewMessageListBySessionIdOnRealmThread(sessionItem, null);
            }
        });
    }

    private void fetchNewMessageListOnRealmThread(@NonNull String msgId,
                                                  @Nullable OnNewMessageListListener listener) {
        String sessionId = extractConversationAnchor(msgId);
        if (TextUtils.isEmpty(sessionId)) {
            return;
        }
        SessionItem localSession = ImSessionRepository.getInstance().queryBySessionId(sessionId);
        if (localSession != null) {
            proceedFetchNewMessageList(msgId, listener);
            return;
        }
        GroupDetailFetcher.fetchSessionItems(Collections.singletonList(sessionId), map ->
                ensureSessionThenFetchMessages(msgId, sessionId, map, listener));
    }

    private void ensureSessionThenFetchMessages(@NonNull String msgId,
                                                @NonNull String sessionId,
                                                @NonNull Map<String, SessionItem> prefetched,
                                                @Nullable OnNewMessageListListener listener) {
        SessionItem session = prefetched.get(sessionId);
        if (session == null) {
            proceedFetchNewMessageList(msgId, listener);
            return;
        }
        ImLocalStore.saveSessionsThen(Collections.singletonList(session), () -> {
            ImLocalStore.dispatchSessionPreviewUpdated(Collections.singletonList(session));
            proceedFetchNewMessageList(msgId, listener);
        });
    }

    private void proceedFetchNewMessageList(@NonNull String msgId,
                                            @Nullable OnNewMessageListListener listener) {
        String sessionId = extractConversationAnchor(msgId);
        if (TextUtils.isEmpty(sessionId)) {
            return;
        }
        String syncAnchor = ImLocalStore.resolveMessageSyncAnchor(sessionId);
        if (SessionSyncCursor.isPushAlreadySynced(syncAnchor, msgId)) {
            return;
        }
        String queryMsgId;
        int anchor = 1;
        int op = 3;
        if (!TextUtils.isEmpty(syncAnchor)) {
            queryMsgId = syncAnchor;
        } else {
            queryMsgId = msgId;
            anchor = 0;
        }
        requestMessageList(queryMsgId, anchor, op, listener);
    }

    private static boolean isGroupSessionId(@NonNull String sessionId) {
        return sessionId.startsWith("g_");
    }

    private void fetchNewMessageListBySessionIdOnRealmThread(@NonNull SessionItem sessionItem,
                                                              @Nullable OnNewMessageListListener listener) {
        String sessionId = sessionItem.getSessionId();
        SessionEntity sessionEntity = ImSessionRepository.getInstance().queryEntityBySessionId(sessionId);
        if (SessionSyncCursor.isCaughtUp(sessionEntity, sessionItem.getLastMessageId())) {
            return;
        }
        String syncAnchor = ImLocalStore.resolveMessageSyncAnchor(sessionId);
        String queryMsgId;
        int anchor = 1;
        if (TextUtils.isEmpty(syncAnchor)) {
            queryMsgId = sessionId + "-" + sessionItem.getSelfReadPosition();
            anchor = 0;
        } else {
            queryMsgId = syncAnchor;
        }
        requestMessageList(queryMsgId, anchor, 3, listener);
    }

    /**
     * 发起 HTTP 拉取；若 {@link MessageListContent#isHasNextPage()} 为 true 则继续请求直至无下一页，
     * 再将合并后的列表一次性落库并回调 {@link OnNewMessageListListener}。
     */
    private void requestMessageList(@NonNull String queryMsgId,
                                    int anchor,
                                    int op,
                                    @Nullable OnNewMessageListListener listener) {
        requestMessageListRecursive(queryMsgId, anchor, op, listener, new ArrayList<>());
    }

    //循环处理
    private void requestMessageListRecursive(@NonNull String queryMsgId,
                                             int anchor,
                                             int op,
                                             @Nullable OnNewMessageListListener listener,
                                             @NonNull List<ImMessageItem> accumulated) {
        ImMessageHttpExample.requestGetMessageList(queryMsgId, anchor, op, content -> {
            List<ImMessageItem> page = content.getMsgList();
            if (page == null || page.isEmpty()) {
                finishSaveAndDispatch(accumulated, listener);
                return;
            }
            accumulated.addAll(page);
            if (content.isHasNextPage()) {
                ImMessageItem tail = page.get(page.size() - 1);
                String nextMsgId = tail.getMessageId();
                if (TextUtils.isEmpty(nextMsgId) || nextMsgId.equals(queryMsgId)) {
                    finishSaveAndDispatch(accumulated, listener);
                    return;
                }
                requestMessageListRecursive(nextMsgId, 1, op, listener, accumulated);
            } else {
                finishSaveAndDispatch(accumulated, listener);
            }
        });
    }

    private static void finishSaveAndDispatch(@NonNull List<ImMessageItem> accumulated,
                                              @Nullable OnNewMessageListListener listener) {
        if (accumulated.isEmpty()) {
            return;
        }
        List<AppMessage> batch = ImMessageMapper.toAppMessageList(accumulated);
        ImLocalStore.saveMessagesThen(batch, () -> {
            if (listener != null) {
                listener.onNewMessageList(batch);
            }
        });
    }

    /**
     * GET {@code im/message/reportRead/{messageId}}：上报消息已读。
     */
    public void reportMessageRead(@NonNull String messageId) {
        if (TextUtils.isEmpty(messageId)) {
            return;
        }
        ImMessageHttpExample.requestReportMessageRead(messageId, null);
    }

    public void setOnOutgoingMessageListener(@Nullable OnOutgoingMessageListener listener) {
        outgoingMessageListener = listener;
    }

    /**
     * 发送消息成功回包：更新本地 {@code msgId}、发送状态，并通知宿主刷新单条消息。
     */
    public void onSendMsgSuccess(@NonNull String clientMsgId, @NonNull String serverMsgId) {
        if (TextUtils.isEmpty(clientMsgId) || TextUtils.isEmpty(serverMsgId)) {
            return;
        }
        ImLocalStore.markOutgoingSendSuccess(clientMsgId, serverMsgId, updated -> {
            OnOutgoingMessageListener listener = outgoingMessageListener;
            if (listener != null) {
                listener.onOutgoingMessageUpdated(updated);
            }
        });
    }

    /**
     * 发送失败（服务端错误回执或超时重发用尽）：更新本地发送状态并通知宿主刷新单条消息。
     */
    public void onSendMsgFailed(@NonNull String clientMsgId) {
        if (TextUtils.isEmpty(clientMsgId)) {
            return;
        }
        ImLocalStore.markOutgoingSendFailed(clientMsgId, updated -> {
            OnOutgoingMessageListener listener = outgoingMessageListener;
            if (listener != null && updated != null) {
                listener.onOutgoingMessageUpdated(updated);
            }
        });
    }
}
