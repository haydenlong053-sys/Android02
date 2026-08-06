package com.freddy.im.db;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.bean.AppMessage;
import com.freddy.im.db.entity.MessageEntity;
import com.freddy.im.db.entity.SessionEntity;
import com.freddy.im.db.mapper.MessageRealmMapper;
import com.freddy.im.message.ImMessageManager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.Sort;

/**
 * 会话消息同步高水位：与本地可见消息解耦，本地删除不回退，增量拉取以此为锚点。
 */
public final class SessionSyncCursor {

    private SessionSyncCursor() {
    }

    /**
     * 消息落库后推进各会话 syncedMax（只增不减；仅以本地已持久化消息为准）。
     */
    public static void advanceInTransaction(@NonNull Realm realm, @NonNull List<AppMessage> messages) {
        Map<String, AppMessage> latestBySession = pickLatestPerSession(messages);
        for (Map.Entry<String, AppMessage> entry : latestBySession.entrySet()) {
            advanceSessionInTransaction(realm, entry.getKey(), entry.getValue());
        }
    }

    /**
     * 解析增量拉取锚点 messageId：优先 syncedMax，否则本地可见最大条（不用会话摘要 lastMessageId）。
     */
    @Nullable
    public static String resolveAnchorMsgId(@NonNull Realm realm, @NonNull String sessionId) {
        if (TextUtils.isEmpty(sessionId)) {
            return null;
        }
        SessionEntity session = realm.where(SessionEntity.class)
                .equalTo("sessionId", sessionId)
                .findFirst();
        if (session != null && !TextUtils.isEmpty(session.getSyncedMaxMsgId())) {
            return session.getSyncedMaxMsgId();
        }
        MessageEntity localMax = realm.where(MessageEntity.class)
                .equalTo("sessionId", sessionId)
                .sort("sequenceId", Sort.DESCENDING)
                .findFirst();
        if (localMax != null && !TextUtils.isEmpty(localMax.getMsgId())) {
            return localMax.getMsgId();
        }
        return null;
    }

    /**
     * 本地是否已与服务端会话摘要对齐。
     * <p>
     * 以 {@code syncedMax} vs {@code lastMessageId} 为准；未读/unPull 不再强制未对齐，
     * 避免 sticky unread 在已追上水位后仍反复补拉。
     */
    public static boolean isCaughtUp(@Nullable SessionEntity session,
                                     @Nullable String serverLastMessageId,
                                     int unreadCount,
                                     int unPullCount) {
        // unreadCount / unPullCount 保留入参以兼容调用方；对齐判断仅看水位
        if (unPullCount > 0 || unreadCount > 0) {
            return false;
        }
        if (session == null || TextUtils.isEmpty(serverLastMessageId)) {
            return false;
        }
        long serverSeq = ImMessageManager.extractTrailingSequence(serverLastMessageId);
        if (serverSeq == ImMessageManager.INVALID_SEQUENCE) {
            return false;
        }
        long syncedSeq = session.getSyncedMaxSequence();
        // 水位已齐：即使 unread/unPull 仍 >0（sticky），也视为已追上，避免再拉 tip 导致未读多计
        return syncedSeq > 0L && syncedSeq >= serverSeq;
    }

    /**
     * @deprecated 请使用 {@link #isCaughtUp(SessionEntity, String, int, int)}。
     */
    @Deprecated
    public static boolean isCaughtUp(@Nullable SessionEntity session,
                                     @Nullable String serverLastMessageId) {
        return isCaughtUp(session, serverLastMessageId, 0, 0);
    }

    /**
     * 推送 messageId 是否不新于当前同步高水位（无需为填补本地删除缺口而重拉）。
     */
    public static boolean isPushAlreadySynced(@Nullable String syncAnchorMsgId, @NonNull String pushMsgId) {
        if (TextUtils.isEmpty(syncAnchorMsgId)) {
            return false;
        }
        long syncedSeq = ImMessageManager.extractTrailingSequence(syncAnchorMsgId);
        long pushSeq = ImMessageManager.extractTrailingSequence(pushMsgId);
        return syncedSeq != ImMessageManager.INVALID_SEQUENCE
                && pushSeq != ImMessageManager.INVALID_SEQUENCE
                && pushSeq <= syncedSeq;
    }

    private static void advanceSessionInTransaction(@NonNull Realm realm,
                                                    @NonNull String sessionId,
                                                    @NonNull AppMessage message) {
        SessionEntity session = realm.where(SessionEntity.class)
                .equalTo("sessionId", sessionId)
                .findFirst();
        if (session == null) {
            return;
        }
        long candidateSeq = effectiveSequence(message);
        if (candidateSeq == ImMessageManager.INVALID_SEQUENCE) {
            return;
        }
        if (candidateSeq <= session.getSyncedMaxSequence()) {
            return;
        }
        String candidateMsgId = resolveMsgId(message);
        if (TextUtils.isEmpty(candidateMsgId)) {
            return;
        }
        session.setSyncedMaxSequence(candidateSeq);
        session.setSyncedMaxMsgId(candidateMsgId);
    }

    @NonNull
    private static Map<String, AppMessage> pickLatestPerSession(@NonNull List<AppMessage> messages) {
        Map<String, AppMessage> map = new LinkedHashMap<>();
        for (AppMessage message : messages) {
            if (message == null) {
                continue;
            }
            String sessionId = resolveSessionId(message);
            if (TextUtils.isEmpty(sessionId)) {
                continue;
            }
            AppMessage existing = map.get(sessionId);
            if (existing == null || isNewerMessage(message, existing)) {
                map.put(sessionId, message);
            }
        }
        return map;
    }

    private static boolean isNewerMessage(@NonNull AppMessage candidate, @NonNull AppMessage current) {
        return effectiveSequence(candidate) > effectiveSequence(current);
    }

    private static long effectiveSequence(@NonNull AppMessage message) {
        if (message.getSequenceId() > 0L) {
            return message.getSequenceId();
        }
        String id = resolveMsgId(message);
        return ImMessageManager.extractTrailingSequence(id);
    }

    @Nullable
    private static String resolveMsgId(@NonNull AppMessage message) {
        if (!TextUtils.isEmpty(message.getMsgId())) {
            return message.getMsgId();
        }
        return MessageRealmMapper.clientMsgIdForPersist(message);
    }

    @Nullable
    private static String resolveSessionId(@NonNull AppMessage message) {
        if (!TextUtils.isEmpty(message.getConversationId())) {
            return message.getConversationId();
        }
        String anchor = resolveMsgId(message);
        return ImMessageManager.extractConversationAnchor(anchor);
    }
}
