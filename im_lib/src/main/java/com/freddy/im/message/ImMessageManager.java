package com.freddy.im.message;

import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cboda.baselibrary.utils.ELogUtilKt;
import com.freddy.im.OutgoingSendTimeout;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.bean.ImMessageMapper;
import com.freddy.im.db.ImDbExecutor;
import com.freddy.im.db.ImLocalStore;
import com.freddy.im.db.SessionSyncCursor;
import com.freddy.im.db.entity.SessionEntity;
import com.freddy.im.db.repository.ImMessageRepository;
import com.freddy.im.db.repository.ImSessionRepository;
import com.freddy.im.http.GroupDetailFetcher;
import com.freddy.im.http.ImMessageHttpExample;
import com.freddy.im.http.model.im.ImMessageItem;
import com.freddy.im.http.model.im.MessageListContent;
import com.freddy.im.http.model.im.SessionItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 消息 ID 解析、按锚点拉取消息列表等能力统一入口，可供 SDK 内部与宿主层调用。
 */
public final class ImMessageManager {

    /**
     * 无法解析序列号时的返回值（用于去重等逻辑判断）
     */
    public static final long INVALID_SEQUENCE = 0L;

    /** 文件助手会话前缀（服务端 sessionId / messageId 形如 {@code FILE_ASST:...}，拉消息会报错，客户端直接跳过） */
    public static final String FILE_ASSISTANT_SESSION_PREFIX = "FILE_ASST";

    /** 后台补拉并发上限（当前打开的聊天会话不受此限）。串行避免与 UI 抢 CPU/IO。 */
    private static final int MAX_BACKGROUND_UNREAD_SYNC = 1;

    /** 单次会话列表同步后，后台补拉队列上限（按未读/未拉优先）。 */
    private static final int MAX_BACKGROUND_SYNC_QUEUE = 200;

    /** 会话列表展示后再延迟启动后台补拉，让首屏交互先完成。 */
    private static final long BACKGROUND_SYNC_START_DELAY_MS = 3000L;

    private static final ImMessageManager INSTANCE = new ImMessageManager();

    /** 拉消息 HTTP 回调处理线程（低优先级），尽快离开 EasyHttp 主线程。 */
    private static final ExecutorService PULL_CALLBACK_EXECUTOR = Executors.newSingleThreadExecutor(r -> {
        Thread t = new Thread(r, "im-msg-pull");
        t.setDaemon(true);
        t.setPriority(Thread.NORM_PRIORITY - 1);
        return t;
    });

    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    /** 同会话只保留一条 getList 链；新请求合并 listener 并标记 dirty。 */
    private final ConcurrentHashMap<String, SessionPullState> pullStates = new ConcurrentHashMap<>();

    /** 后台补拉等待队列（跨会话限流）。 */
    private final ConcurrentLinkedQueue<SessionItem> backgroundSyncQueue = new ConcurrentLinkedQueue<>();

    private final Set<String> backgroundQueuedOrRunning = ConcurrentHashMap.newKeySet();

    private final AtomicInteger backgroundSyncInFlight = new AtomicInteger(0);

    @Nullable
    private volatile Runnable pendingBackgroundSyncStart;

    @Nullable
    private volatile OnOutgoingMessageListener outgoingMessageListener;

    /** 进程被杀后，按落库时间继续倒计时发送超时。 */
    private final ConcurrentHashMap<String, Runnable> outgoingSendTimeoutWatches = new ConcurrentHashMap<>();

    /** 宿主注册：无显式 listener 时，当前聊天会话仍能收到分页落库回调。 */
    @Nullable
    private volatile OnNewMessageListListener defaultNewMessageListListener;

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

    /** 是否文件助手会话（sessionId 或 messageId 以 {@code FILE_ASST} 开头）。 */
    public static boolean isFileAssistantSession(@Nullable String sessionIdOrMessageId) {
        return sessionIdOrMessageId != null
                && sessionIdOrMessageId.regionMatches(true, 0, FILE_ASSISTANT_SESSION_PREFIX, 0,
                FILE_ASSISTANT_SESSION_PREFIX.length());
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
        if (sessionItem == null || TextUtils.isEmpty(sessionItem.getSessionId())) {
            return;
        }
        ImDbExecutor.runRead(() -> fetchNewMessageListBySessionIdOnRealmThread(sessionItem, listener));
    }

    /**
     * 会话列表拉取成功后：当前聊天会话立即补拉；其余延后 + 限流补拉。
     * <p>
     * getList 已写入服务端 unreadCount，此处只补消息正文，<b>不再累加未读</b>。
     * 预览/未读仍会通知 UI，由列表 payload 局部刷新。
     */
    public void syncUnreadMessagesForSessions(@Nullable List<SessionItem> sessions) {
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        final List<SessionItem> snapshot = new ArrayList<>(sessions.size());
        for (SessionItem item : sessions) {
            if (item != null) {
                snapshot.add(item);
            }
        }
        if (snapshot.isEmpty()) {
            return;
        }
        // 当前聊天：立刻补拉，不拖到延迟窗口
        ImDbExecutor.runRead(() -> {
            for (SessionItem sessionItem : snapshot) {
                if (sessionItem == null) {
                    continue;
                }
                String sessionId = sessionItem.getSessionId();
                if (TextUtils.isEmpty(sessionId) || isFileAssistantSession(sessionId)) {
                    continue;
                }
                if (sessionItem.getUnPullCount() <= 0 && sessionItem.getUnreadCount() <= 0) {
                    continue;
                }
                if (ImActiveChatSession.isActive(sessionId)) {
                    fetchNewMessageListBySessionIdOnRealmThread(sessionItem, null, false, null);
                    break;
                }
            }
        });
        // 取消尚未启动的上一轮延迟补拉，避免重复风暴
        Runnable previous = pendingBackgroundSyncStart;
        if (previous != null) {
            MAIN_HANDLER.removeCallbacks(previous);
            pendingBackgroundSyncStart = null;
        }
        Runnable start = () -> {
            pendingBackgroundSyncStart = null;
            startBackgroundUnreadSync(snapshot);
        };
        pendingBackgroundSyncStart = start;
        MAIN_HANDLER.postDelayed(start, BACKGROUND_SYNC_START_DELAY_MS);
    }

    private void startBackgroundUnreadSync(@NonNull List<SessionItem> sessions) {
        ImDbExecutor.runRead(() -> {
            List<SessionItem> backgroundCandidates = new ArrayList<>();
            for (SessionItem sessionItem : sessions) {
                if (sessionItem == null) {
                    continue;
                }
                String sessionId = sessionItem.getSessionId();
                if (TextUtils.isEmpty(sessionId) || isFileAssistantSession(sessionId)) {
                    continue;
                }
                if (sessionItem.getUnPullCount() <= 0 && sessionItem.getUnreadCount() <= 0) {
                    continue;
                }
                if (ImActiveChatSession.isActive(sessionId)) {
                    continue;
                }
                backgroundCandidates.add(sessionItem);
            }
            if (backgroundCandidates.isEmpty()) {
                return;
            }
            Collections.sort(backgroundCandidates, BACKGROUND_SYNC_PRIORITY);
            int queued = 0;
            for (SessionItem item : backgroundCandidates) {
                if (queued >= MAX_BACKGROUND_SYNC_QUEUE) {
                    break;
                }
                if (enqueueBackgroundSync(item)) {
                    queued++;
                }
            }
            ELogUtilKt.eLogD(ELogUtilKt.APP_IM_TAG,
                    "background unread sync queued=" + queued
                            + " concurrentMax=" + MAX_BACKGROUND_UNREAD_SYNC);
            pumpBackgroundSync();
        });
    }

    private static final Comparator<SessionItem> BACKGROUND_SYNC_PRIORITY =
            (a, b) -> Integer.compare(backgroundNeedScore(b), backgroundNeedScore(a));

    private static int backgroundNeedScore(@Nullable SessionItem item) {
        if (item == null) {
            return 0;
        }
        return Math.max(0, item.getUnPullCount()) + Math.max(0, item.getUnreadCount());
    }

    /** @return true 若新入队 */
    private boolean enqueueBackgroundSync(@NonNull SessionItem item) {
        String sessionId = item.getSessionId();
        if (TextUtils.isEmpty(sessionId) || !backgroundQueuedOrRunning.add(sessionId)) {
            return false;
        }
        backgroundSyncQueue.offer(item);
        return true;
    }

    private void pumpBackgroundSync() {
        while (true) {
            int inFlight = backgroundSyncInFlight.get();
            if (inFlight >= MAX_BACKGROUND_UNREAD_SYNC) {
                return;
            }
            SessionItem next = backgroundSyncQueue.poll();
            if (next == null) {
                return;
            }
            if (!backgroundSyncInFlight.compareAndSet(inFlight, inFlight + 1)) {
                backgroundSyncQueue.offer(next);
                continue;
            }
            final String sessionId = next.getSessionId();
            final SessionItem toFetch = next;
            ImDbExecutor.runRead(() -> fetchNewMessageListBySessionIdOnRealmThread(
                    toFetch,
                    null,
                    false,
                    () -> {
                        backgroundQueuedOrRunning.remove(sessionId);
                        backgroundSyncInFlight.decrementAndGet();
                        pumpBackgroundSync();
                    }));
        }
    }

    private void fetchNewMessageListOnRealmThread(@NonNull String msgId,
                                                  @Nullable OnNewMessageListListener listener) {
        if (isFileAssistantSession(msgId)) {
            ELogUtilKt.eLogD(ELogUtilKt.APP_IM_TAG, "跳过文件助手拉消息 msgId=" + msgId);
            return;
        }
        String sessionId = extractConversationAnchor(msgId);
        if (TextUtils.isEmpty(sessionId)) {
            ELogUtilKt.eLogD(ELogUtilKt.APP_IM_TAG, "走进TextUtils.isEmpty(sessionId)=" + sessionId);
            return;
        }
        SessionItem localSession = ImSessionRepository.getInstance().queryBySessionId(sessionId);
        if (localSession != null) {
            ELogUtilKt.eLogD(ELogUtilKt.APP_IM_TAG, "走进proceedFetchNewMessageList--localSession.getSessionId=" + localSession.getSessionId()+"  msgId="+msgId);
            proceedFetchNewMessageList(msgId, listener, true);
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
            proceedFetchNewMessageList(msgId, listener, true);
            return;
        }
        ImLocalStore.saveSessionsThen(Collections.singletonList(session), () -> {
            ImLocalStore.dispatchSessionPreviewUpdated(Collections.singletonList(session));
            proceedFetchNewMessageList(msgId, listener, true);
        });
    }

    private void proceedFetchNewMessageList(@NonNull String msgId,
                                            @Nullable OnNewMessageListListener listener,
                                            boolean incrementUnread) {
        String sessionId = extractConversationAnchor(msgId);
        if (TextUtils.isEmpty(sessionId)) {
            return;
        }
        String syncAnchor = ImLocalStore.resolveMessageSyncAnchor(sessionId);
        if (SessionSyncCursor.isPushAlreadySynced(syncAnchor, msgId)) {
            ELogUtilKt.eLogD(ELogUtilKt.APP_IM_TAG, "SessionSyncCursor.isPushAlreadySynced()--syncAnchor=" + syncAnchor+"  msgId="+msgId);
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
        beginOrCoalescePull(sessionId, queryMsgId, anchor, op, listener, incrementUnread);
    }

    private void fetchNewMessageListBySessionIdOnRealmThread(@NonNull SessionItem sessionItem,
                                                              @Nullable OnNewMessageListListener listener) {
        fetchNewMessageListBySessionIdOnRealmThread(sessionItem, listener, true, null);
    }

    private void fetchNewMessageListBySessionIdOnRealmThread(@NonNull SessionItem sessionItem,
                                                              @Nullable OnNewMessageListListener listener,
                                                              boolean incrementUnread,
                                                              @Nullable Runnable onChainFullyDone) {
        String sessionId = sessionItem.getSessionId();
        if (TextUtils.isEmpty(sessionId) || isFileAssistantSession(sessionId)) {
            ELogUtilKt.eLogD(ELogUtilKt.APP_IM_TAG, "跳过文件助手拉消息 sessionId=" + sessionId);
            if (onChainFullyDone != null) {
                onChainFullyDone.run();
            }
            return;
        }
        SessionEntity sessionEntity = ImSessionRepository.getInstance().queryEntityBySessionId(sessionId);
        int unreadCount = sessionItem.getUnreadCount();
        int unPullCount = sessionItem.getUnPullCount();
        if (sessionEntity != null) {
            unreadCount = Math.max(unreadCount, sessionEntity.getUnreadCount());
            unPullCount = Math.max(unPullCount, sessionEntity.getUnPullCount());
        }
        //进聊天页兜底补拉一次
        ELogUtilKt.eLogD(ELogUtilKt.APP_IM_TAG, "incrementUnread==" + incrementUnread);
        if (!incrementUnread && SessionSyncCursor.isCaughtUp(
                sessionEntity,
                sessionItem.getLastMessageId(),
                unreadCount,
                unPullCount)) {
            if (onChainFullyDone != null) {
                onChainFullyDone.run();
            }
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

        beginOrCoalescePull(sessionId, queryMsgId, anchor, 3, listener, incrementUnread, onChainFullyDone);
    }

    /**
     * 同会话串行：已在飞则合并 listener 并标记 dirty，链结束后再拉一轮。
     */
    private void beginOrCoalescePull(@NonNull String sessionId,
                                     @NonNull String queryMsgId,
                                     int anchor,
                                     int op,
                                     @Nullable OnNewMessageListListener listener,
                                     boolean incrementUnread) {
        beginOrCoalescePull(sessionId, queryMsgId, anchor, op, listener, incrementUnread, null);
    }

    private void beginOrCoalescePull(@NonNull String sessionId,
                                     @NonNull String queryMsgId,
                                     int anchor,
                                     int op,
                                     @Nullable OnNewMessageListListener listener,
                                     boolean incrementUnread,
                                     @Nullable Runnable onChainFullyDone) {
        if (isFileAssistantSession(sessionId) || isFileAssistantSession(queryMsgId)) {
            ELogUtilKt.eLogD(ELogUtilKt.APP_IM_TAG, "跳过文件助手 getList sessionId=" + sessionId);
            if (onChainFullyDone != null) {
                onChainFullyDone.run();
            }
            return;
        }
        ELogUtilKt.eLogD(ELogUtilKt.APP_IM_TAG, "---拉2-000--groupid=" + sessionId+" 拉消息的真实游标queryMsgId="+queryMsgId);
        SessionPullState state = pullStates.computeIfAbsent(sessionId, k -> new SessionPullState());
        synchronized (state) {
            state.mergeListener(listener);
            if (state.inFlight) {
                // 任一请求需要本地 +N，则本链保留累加（避免 getList 补拉与推送合并后漏计）
                state.incrementUnread = state.incrementUnread || incrementUnread;
                state.dirty = true;
                // 已有在飞链：不另占后台槽，立刻释放本次槽位
                if (onChainFullyDone != null) {
                    onChainFullyDone.run();
                }
                return;
            }
            state.inFlight = true;
            state.dirty = false;
            state.incrementUnread = incrementUnread;
            state.onChainFullyDone = onChainFullyDone;
        }
        requestMessageListPageByPage(sessionId, queryMsgId, anchor, op);
    }

    /**
     * 分页边拉边落库；后台补拉静默写库不刷会话列表。HTTP 回调立即切到低优先级线程，避免占主线程。
     */
    private void requestMessageListPageByPage(@NonNull String sessionId,
                                              @NonNull String queryMsgId,
                                              int anchor,
                                              int op) {
        ImMessageHttpExample.requestGetMessageList(queryMsgId, anchor, op, content ->
                PULL_CALLBACK_EXECUTOR.execute(() -> handleMessageListPage(sessionId, queryMsgId, op, content)),
                () -> PULL_CALLBACK_EXECUTOR.execute(() -> onPullChainFinished(sessionId, false)));
    }

    private void handleMessageListPage(@NonNull String sessionId,
                                       @NonNull String queryMsgId,
                                       int op,
                                       @NonNull MessageListContent content) {
        List<ImMessageItem> page = content.getMsgList();
        if (page == null || page.isEmpty()) {
            onPullChainFinished(sessionId, true);
            return;
        }
        List<AppMessage> batch = ImMessageMapper.toAppMessageList(page);
        boolean incrementUnread = true;
        SessionPullState state = pullStates.get(sessionId);
        if (state != null) {
            synchronized (state) {
                incrementUnread = state.incrementUnread;
            }
        }
        // 后台补拉也通知预览/未读；列表用 Diff payload 局部刷新，不重绑头像
        boolean notifySessionPreview = true;
        ImLocalStore.saveMessagesThen(batch, () -> {
            dispatchPageToListener(sessionId, batch);
            if (content.isHasNextPage()) {
                ImMessageItem tail = page.get(page.size() - 1);
                String nextMsgId = tail.getMessageId();
                if (TextUtils.isEmpty(nextMsgId) || nextMsgId.equals(queryMsgId)) {
                    onPullChainFinished(sessionId, true);
                    return;
                }
                requestMessageListPageByPage(sessionId, nextMsgId, 1, op);
            } else {
                onPullChainFinished(sessionId, true);
            }
        }, incrementUnread, notifySessionPreview);
    }

    private void dispatchPageToListener(@NonNull String sessionId, @NonNull List<AppMessage> batch) {
        SessionPullState state = pullStates.get(sessionId);
        OnNewMessageListListener listener = null;
        if (state != null) {
            synchronized (state) {
                listener = state.listener;
            }
        }
        if (listener == null && ImActiveChatSession.isActive(sessionId)) {
            listener = defaultNewMessageListListener;
        }
        if (listener != null) {
            listener.onNewMessageList(batch);
        }
    }

    private void onPullChainFinished(@NonNull String sessionId, boolean clearUnPull) {
        if (clearUnPull) {
            ImLocalStore.clearSessionUnPull(sessionId);
        }
        SessionPullState state = pullStates.get(sessionId);
        if (state == null) {
            return;
        }
        boolean restart;
        Runnable done;
        synchronized (state) {
            restart = state.dirty;
            state.dirty = false;
            if (!restart) {
                state.inFlight = false;
                state.listener = null;
                done = state.onChainFullyDone;
                state.onChainFullyDone = null;
                pullStates.remove(sessionId, state);
            } else {
                done = null;
                // dirty：保持 inFlight，从最新水位再拉一轮（合并推送期间抬高的缺口）
                state.inFlight = true;
            }
        }
        if (!restart) {
            if (done != null) {
                done.run();
            }
            return;
        }
        ImDbExecutor.runRead(() -> {
            String syncAnchor = ImLocalStore.resolveMessageSyncAnchor(sessionId);
            if (TextUtils.isEmpty(syncAnchor)) {
                onPullChainFinished(sessionId, true);
                return;
            }
            requestMessageListPageByPage(sessionId, syncAnchor, 1, 3);
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

    public void setDefaultNewMessageListListener(@Nullable OnNewMessageListListener listener) {
        defaultNewMessageListListener = listener;
    }

    /**
     * 进程恢复：把库里仍为发送中的消息按落库时间超时失败，或继续倒计时。
     */
    public void recoverOutgoingSendingTimeouts() {
        if (!ImLocalStore.isReady()) {
            return;
        }
        ImLocalStore.recoverOutgoingSendingTimeouts(
                updated -> {
                    OnOutgoingMessageListener listener = outgoingMessageListener;
                    if (listener != null) {
                        listener.onOutgoingMessageUpdated(updated);
                    }
                },
                pending -> {
                    for (ImMessageRepository.OutgoingSendingRef ref : pending) {
                        watchOutgoingSendTimeout(ref.clientMsgId, ref.timestamp);
                    }
                });
    }

    public void cancelOutgoingSendTimeout(@Nullable String clientMsgId) {
        if (TextUtils.isEmpty(clientMsgId)) {
            return;
        }
        Runnable watch = outgoingSendTimeoutWatches.remove(clientMsgId);
        if (watch != null) {
            MAIN_HANDLER.removeCallbacks(watch);
        }
    }

    public void cancelAllOutgoingSendTimeouts() {
        for (Runnable watch : outgoingSendTimeoutWatches.values()) {
            MAIN_HANDLER.removeCallbacks(watch);
        }
        outgoingSendTimeoutWatches.clear();
    }

    private void watchOutgoingSendTimeout(@NonNull String clientMsgId, long timestamp) {
        cancelOutgoingSendTimeout(clientMsgId);
        long remainingMs = OutgoingSendTimeout.remainingMs(timestamp);
        if (remainingMs <= 0L) {
            onSendMsgFailed(clientMsgId, -1, null, false);
            return;
        }
        Runnable watch = () -> {
            outgoingSendTimeoutWatches.remove(clientMsgId);
            onSendMsgFailed(clientMsgId, -1, null, false);
        };
        outgoingSendTimeoutWatches.put(clientMsgId, watch);
        MAIN_HANDLER.postDelayed(watch, remainingMs);
    }

    /**
     * 发送消息成功回包：更新本地 {@code msgId}、发送状态，并通知宿主刷新单条消息。
     */
    public void onSendMsgSuccess(@NonNull String clientMsgId, @NonNull String serverMsgId) {
        if (TextUtils.isEmpty(clientMsgId) || TextUtils.isEmpty(serverMsgId)) {
            return;
        }
        cancelOutgoingSendTimeout(clientMsgId);
        ImLocalStore.markOutgoingSendSuccess(clientMsgId, serverMsgId, updated -> {
            OnOutgoingMessageListener listener = outgoingMessageListener;
            if (listener != null) {
                listener.onOutgoingMessageUpdated(updated);
            }
        });
    }

    /**
     * 发送失败（服务端错误回执或发送超时）：更新本地发送状态并通知宿主刷新单条消息。
     */
    public void onSendMsgFailed(@NonNull String clientMsgId) {
        onSendMsgFailed(clientMsgId, -1, null, true);
    }

    /**
     * 发送失败（服务端错误回执或发送超时）：更新本地发送状态并通知宿主刷新单条消息。
     */
    public void onSendMsgFailed(@NonNull String clientMsgId, int errorCode, @Nullable String errorMsg) {
        onSendMsgFailed(clientMsgId, errorCode, errorMsg, true);
    }

    private void onSendMsgFailed(@NonNull String clientMsgId,
                                 int errorCode,
                                 @Nullable String errorMsg,
                                 boolean notifyBizError) {
        if (TextUtils.isEmpty(clientMsgId)) {
            return;
        }
        cancelOutgoingSendTimeout(clientMsgId);
        ImLocalStore.markOutgoingSendFailed(clientMsgId, updated -> {
            OnOutgoingMessageListener listener = outgoingMessageListener;
            if (listener != null && updated != null) {
                listener.onOutgoingMessageUpdated(updated);
            }
            if (notifyBizError && listener != null) {
                listener.onSendBizError(errorCode, errorMsg);
            }
        });
    }

    /** 同会话 getList 在飞状态：合并回调、脏标记、是否累加未读。 */
    private static final class SessionPullState {
        boolean inFlight;
        boolean dirty;
        /** 默认 false；begin 时赋值，合并时按「任一需要则 true」。 */
        boolean incrementUnread;
        @Nullable
        OnNewMessageListListener listener;
        /** 整条分页链彻底结束（含 dirty 重启）时回调，用于释放后台并发槽。 */
        @Nullable
        Runnable onChainFullyDone;

        void mergeListener(@Nullable OnNewMessageListListener more) {
            if (more == null) {
                return;
            }
            // 只保留一个 listener，避免同批多次 receiveMsg 导致聊天页重复 append
            if (listener == null) {
                listener = more;
            }
        }
    }
}
