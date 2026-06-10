package com.freddy.im.db;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.bean.AppMessage;
import com.freddy.im.db.entity.SessionEntity;
import com.freddy.im.db.mapper.MessageRealmMapper;
import com.freddy.im.db.mapper.SessionRealmMapper;
import com.freddy.im.db.repository.ImSessionRepository;
import com.freddy.im.http.GroupDetailFetcher;
import com.freddy.im.http.model.im.SessionItem;
import com.freddy.im.MessageType;
import com.freddy.im.message.ImActiveChatSession;
import com.freddy.im.message.ImMessageManager;
import com.freddy.im.message.MessageContentFormatter;
import com.freddy.im.signal.ImSignalProtos;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import io.realm.Realm;

/**
 * 消息落库后，将各会话「最新一条」摘要写入会话表。
 * <p>
 * 本地无会话的群聊：先 {@code GET im/group/detail/{groupId}}，再用返回数据创建会话行。
 */
public final class SessionLastMessageSync {

    public interface WriteWithSessionSyncCallback {
        void onWrite(@NonNull Realm realm,
                     @NonNull Map<String, SessionItem> prefetchedGroupSessions,
                     @NonNull List<SessionItem> sessionUpdates);
    }

    private SessionLastMessageSync() {
    }

    /**
     * 写库前拉取缺失群会话详情，再在写事务中落消息并同步会话摘要。
     */
    public static void runWriteWithSessionSync(@NonNull List<AppMessage> messages,
                                               @NonNull WriteWithSessionSyncCallback writeCallback,
                                               @Nullable Runnable afterWrite) {
        if (messages.isEmpty() || !ImRealm.isUserBound()) {
            if (afterWrite != null) {
                afterWrite.run();
            }
            return;
        }
        List<String> missingGroupIds = collectMissingGroupSessionIds(messages);
        GroupDetailFetcher.fetchSessionItems(missingGroupIds, prefetched -> ImDbExecutor.runWrite(() -> {
            List<SessionItem> sessionUpdates = new ArrayList<>();
            Realm realm = ImRealm.getRealm();
            try {
                realm.executeTransaction(r -> writeCallback.onWrite(r, prefetched, sessionUpdates));
            } finally {
                realm.close();
            }
            ImLocalStore.dispatchSessionPreviewUpdated(sessionUpdates);
            if (afterWrite != null) {
                afterWrite.run();
            }
        }));
    }

    /**
     * 须在 Realm 写事务内调用。
     */
    /**
     * 按 {@code fetchNewMessageList} 本批新消息条数统计各会话未读增量（排除当前打开的消息页会话、排除自己发送）。
     */
    @NonNull
    public static Map<String, Integer> countUnreadDeltaBySession(@NonNull List<AppMessage> messages) {
        Map<String, Integer> delta = new LinkedHashMap<>();
        String currentUserId = ImRealm.getCurrentUserId();
        for (AppMessage message : messages) {
            if (message == null) {
                continue;
            }
            String sessionId = resolveSessionId(message);
            if (TextUtils.isEmpty(sessionId) || ImActiveChatSession.isActive(sessionId)) {
                continue;
            }
            if (!TextUtils.isEmpty(currentUserId) && message.isSender(currentUserId)) {
                continue;
            }
            delta.put(sessionId, delta.getOrDefault(sessionId, 0) + 1);
        }
        return delta;
    }

    /**
     * 消息撤回后，若被撤回的是会话最近一条，则刷新会话摘要（须在 Realm 写事务内调用）。
     */
    @Nullable
    public static SessionItem applyRecallPreviewInTransaction(@NonNull Realm realm,
                                                              @NonNull String msgId,
                                                              @NonNull AppMessage recalledMessage) {
        String sessionId = resolveSessionId(recalledMessage);
        if (TextUtils.isEmpty(sessionId)) {
            return null;
        }
        SessionEntity session = realm.where(SessionEntity.class)
                .equalTo("sessionId", sessionId)
                .findFirst();
        if (session == null || !matchesLastMessageId(session.getLastMessageId(), msgId, recalledMessage)) {
            return null;
        }
        fillLastMessageFields(session, recalledMessage);
        session.setLastMessageContent(previewContent(recalledMessage, session));
        return SessionRealmMapper.toSessionItem(realm.copyFromRealm(session));
    }

    @NonNull
    public static List<SessionItem> applyInTransaction(@NonNull Realm realm,
                                                       @NonNull List<AppMessage> messages,
                                                       @NonNull Map<String, SessionItem> prefetchedGroupSessions) {
        return applyInTransaction(realm, messages, prefetchedGroupSessions,
                countUnreadDeltaBySession(messages));
    }

    @NonNull
    public static List<SessionItem> applyInTransaction(@NonNull Realm realm,
                                                       @NonNull List<AppMessage> messages,
                                                       @NonNull Map<String, SessionItem> prefetchedGroupSessions,
                                                       @NonNull Map<String, Integer> unreadDeltaBySession) {
        Map<String, AppMessage> latestBySession = pickLatestPerSession(messages);
        Set<String> sessionIds = new LinkedHashSet<>();
        sessionIds.addAll(latestBySession.keySet());
        sessionIds.addAll(unreadDeltaBySession.keySet());

        List<SessionItem> updated = new ArrayList<>(sessionIds.size());
        for (String sessionId : sessionIds) {
            SessionItem item = applySessionInTransaction(
                    realm,
                    sessionId,
                    latestBySession.get(sessionId),
                    prefetchedGroupSessions,
                    unreadDeltaBySession);
            if (item != null) {
                updated.add(item);
            }
        }
        return updated;
    }

    /**
     * 收集「群聊且本地无会话记录」的 sessionId，用于拉群详情。
     */
    @NonNull
    public static List<String> collectMissingGroupSessionIds(@NonNull List<AppMessage> messages) {
        Set<String> ids = new LinkedHashSet<>();
        for (AppMessage message : messages) {
            if (message == null) {
                continue;
            }
            String sessionId = resolveSessionId(message);
            if (TextUtils.isEmpty(sessionId) || !isGroupSession(message, sessionId)) {
                continue;
            }
            if (ImSessionRepository.getInstance().queryBySessionId(sessionId) == null) {
                ids.add(sessionId);
            }
        }
        return new ArrayList<>(ids);
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

    @Nullable
    private static SessionItem applySessionInTransaction(@NonNull Realm realm,
                                                         @NonNull String sessionId,
                                                         @Nullable AppMessage latestMessage,
                                                         @NonNull Map<String, SessionItem> prefetchedGroupSessions,
                                                         @NonNull Map<String, Integer> unreadDeltaBySession) {
        SessionEntity entity = realm.where(SessionEntity.class)
                .equalTo("sessionId", sessionId)
                .findFirst();
        boolean created = false;
        if (entity == null) {
            SessionItem fromGroup = prefetchedGroupSessions.get(sessionId);
            if (fromGroup != null) {
                entity = realm.copyToRealmOrUpdate(SessionRealmMapper.toEntity(fromGroup));
                created = true;
            } else if (latestMessage != null && isGroupSession(latestMessage, sessionId)) {
                return null;
            } else if (latestMessage != null) {
                entity = realm.createObject(SessionEntity.class, sessionId);
                entity.setConversation(latestMessage.getConversationType());
                if (!TextUtils.isEmpty(latestMessage.getToId())) {
                    entity.setOppositeUserId(latestMessage.getToId());
                }
                created = true;
            } else {
                return null;
            }
        }

        boolean previewUpdated = false;
        if (latestMessage != null && (created || isNewerThanSession(latestMessage, entity))) {
            fillLastMessageFields(entity, latestMessage);
            previewUpdated = true;
        }

        boolean unreadUpdated = applyUnreadCount(entity, sessionId, unreadDeltaBySession);
        if (!previewUpdated && !unreadUpdated) {
            return null;
        }
        return SessionRealmMapper.toSessionItem(realm.copyFromRealm(entity));
    }

    private static boolean applyUnreadCount(@NonNull SessionEntity entity,
                                            @NonNull String sessionId,
                                            @NonNull Map<String, Integer> unreadDeltaBySession) {
        if (ImActiveChatSession.isActive(sessionId)) {
            if (entity.getUnreadCount() != 0) {
                entity.setUnreadCount(0);
                return true;
            }
            return false;
        }
        Integer delta = unreadDeltaBySession.get(sessionId);
        if (delta != null && delta > 0) {
            entity.setUnreadCount(entity.getUnreadCount() + delta);
            return true;
        }
        return false;
    }

    static boolean isGroupSession(@NonNull AppMessage message, @NonNull String sessionId) {
        if (message.getConversationType() == MessageType.GROUP_CHAT.getMsgType()) {
            return true;
        }
        return sessionId.startsWith("g_");
    }

    private static void fillLastMessageFields(@NonNull SessionEntity entity, @NonNull AppMessage message) {
        String msgId = !TextUtils.isEmpty(message.getMsgId())
                ? message.getMsgId()
                : message.getClientMsgId();
        entity.setLastMessageId(msgId);
        entity.setLastMessageContent(previewContent(message));
        entity.setLastContentType(String.valueOf(message.getMsgContentType()));
        entity.setLastCustomInfo(message.getCustomInfo());
        entity.setLastMsgType(message.getMsgType());
        entity.setLastMessageTime(message.getTimestamp());
        long seq = message.getSequenceId();
        if (seq <= 0L && !TextUtils.isEmpty(msgId)) {
            seq = ImMessageManager.extractTrailingSequence(msgId);
        }
        if (seq > 0L && message.getTimestamp() <= 0L) {
            entity.setLastMessageTime(seq);
        }
    }

    @NonNull
    private static String previewContent(@NonNull AppMessage message) {
        return previewContent(message, null);
    }

    @NonNull
    private static String previewContent(@NonNull AppMessage message, @Nullable SessionEntity session) {
        return MessageContentFormatter.formatDisplayText(message,
                ImRealm.getCurrentUserId(), buildPreviewNameMap(session));
    }

    @Nullable
    private static Map<String, String> buildPreviewNameMap(@Nullable SessionEntity session) {
        if (session == null) {
            return null;
        }
        if (session.getConversation() == MessageType.GROUP_CHAT.getMsgType()) {
            return null;
        }
        String peerId = session.getOppositeUserId();
        String peerName = session.getSessionName();
        if (TextUtils.isEmpty(peerId) || TextUtils.isEmpty(peerName)) {
            return null;
        }
        Map<String, String> map = new HashMap<>(1);
        map.put(peerId, peerName);
        return map;
    }

    private static boolean matchesLastMessageId(@Nullable String lastMessageId,
                                                @NonNull String msgId,
                                                @NonNull AppMessage message) {
        if (TextUtils.isEmpty(lastMessageId)) {
            return false;
        }
        if (lastMessageId.equals(msgId)) {
            return true;
        }
        String clientMsgId = message.getClientMsgId();
        return !TextUtils.isEmpty(clientMsgId) && lastMessageId.equals(clientMsgId);
    }

    static boolean isNewerMessage(@NonNull AppMessage candidate, @NonNull AppMessage current) {
        return compareMessageOrder(candidate, current) > 0;
    }

    static boolean isNewerThanSession(@NonNull AppMessage message, @NonNull SessionEntity session) {
        long msgSeq = effectiveSequence(message);
        long sessionSeq = ImMessageManager.extractTrailingSequence(session.getLastMessageId());
        if (msgSeq > 0L && sessionSeq != ImMessageManager.INVALID_SEQUENCE) {
            return msgSeq > sessionSeq;
        }
        if (msgSeq > sessionSeq) {
            return true;
        }
        long msgTime = message.getTimestamp();
        if (msgTime <= 0L) {
            msgTime = msgSeq;
        }
        return msgTime > session.getLastMessageTime();
    }

    private static int compareMessageOrder(@NonNull AppMessage a, @NonNull AppMessage b) {
        long seqA = effectiveSequence(a);
        long seqB = effectiveSequence(b);
        if (seqA != seqB) {
            return Long.compare(seqA, seqB);
        }
        return Long.compare(a.getTimestamp(), b.getTimestamp());
    }

    private static long effectiveSequence(@NonNull AppMessage message) {
        if (message.getSequenceId() > 0L) {
            return message.getSequenceId();
        }
        String id = !TextUtils.isEmpty(message.getMsgId())
                ? message.getMsgId()
                : message.getClientMsgId();
        return ImMessageManager.extractTrailingSequence(id);
    }

    @Nullable
    private static String resolveSessionId(@NonNull AppMessage message) {
        if (!TextUtils.isEmpty(message.getConversationId())) {
            return message.getConversationId();
        }
        String anchor = !TextUtils.isEmpty(message.getMsgId())
                ? message.getMsgId()
                : message.getClientMsgId();
        return ImMessageManager.extractConversationAnchor(anchor);
    }
}
