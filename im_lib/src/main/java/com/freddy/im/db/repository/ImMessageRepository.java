package com.freddy.im.db.repository;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.IMSConfig;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.db.ImDbExecutor;
import com.freddy.im.db.SessionLastMessageSync;
import com.freddy.im.db.SessionSyncCursor;
import com.freddy.im.db.ImRealm;
import com.freddy.im.db.entity.MessageEntity;
import com.freddy.im.db.entity.SessionEntity;
import com.freddy.im.db.mapper.MessageRealmMapper;
import com.freddy.im.db.ImLocalStore;
import com.freddy.im.http.model.im.SessionItem;
import com.freddy.im.message.ImMessageManager;
import com.freddy.im.message.MessageRepealHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * 消息表读写。
 */
public final class ImMessageRepository {

    /** 消息列表单页条数（与 {@link com.freddy.im.db.ImLocalStore#MESSAGE_PAGE_SIZE} 对齐） */
    public static final int DEFAULT_MESSAGE_PAGE_SIZE = 30;

    private static final ImMessageRepository INSTANCE = new ImMessageRepository();

    private ImMessageRepository() {
    }

    public static ImMessageRepository getInstance() {
        return INSTANCE;
    }

    /**
     * 批量 upsert，事务提交成功后在<strong>同一条写线程</strong>执行 {@code afterSuccess}（失败不执行）。
     */
    public void insertOrUpdateBatchThen(@Nullable List<AppMessage> messages, @Nullable Runnable afterSuccess) {
        if (messages == null || messages.isEmpty() || !ImRealm.isUserBound()) {
            return;
        }
        final List<AppMessage> snapshot = snapshotMessages(messages);
        final List<MessageEntity> entities = buildUpsertEntities(snapshot);
        if (entities.isEmpty()) {
            return;
        }
        final Runnable callback = afterSuccess;
        SessionLastMessageSync.runWriteWithSessionSync(snapshot, (realm, prefetched, sessionUpdates) -> {
            realm.insertOrUpdate(entities);
            sessionUpdates.addAll(SessionLastMessageSync.applyInTransaction(
                    realm, snapshot, prefetched, SessionLastMessageSync.countUnreadDeltaBySession(snapshot)));
        }, callback);
    }

    /**
     * 发送前写入；事务<strong>成功提交后</strong>在 Realm 写线程执行 {@code afterSuccess}。
     */
    public void insertOutgoing(@NonNull AppMessage message, @Nullable Runnable afterSuccess) {
        if (!ImRealm.isUserBound()
                || TextUtils.isEmpty(MessageRealmMapper.clientMsgIdForPersist(message))) {
            return;
        }
        final AppMessage snapshot = copyMessage(message);
        final Runnable callback = afterSuccess;
        SessionLastMessageSync.runWriteWithSessionSync(
                Collections.singletonList(snapshot),
                (realm, prefetched, sessionUpdates) -> {
                    realm.copyToRealmOrUpdate(MessageRealmMapper.toEntity(snapshot));
                    sessionUpdates.addAll(SessionLastMessageSync.applyInTransaction(
                            realm,
                            Collections.singletonList(snapshot),
                            prefetched,
                            SessionLastMessageSync.countUnreadDeltaBySession(
                                    Collections.singletonList(snapshot))));
                },
                callback);
    }

    @NonNull
    public List<AppMessage> queryBySessionId(@Nullable String sessionId) {
        if (TextUtils.isEmpty(sessionId) || !ImRealm.isUserBound()) {
            return Collections.emptyList();
        }
        Realm realm = ImRealm.getRealm();
        try {
            RealmResults<MessageEntity> results = realm.where(MessageEntity.class)
                    .equalTo("sessionId", sessionId)
                    .sort("sequenceId", Sort.ASCENDING)
                    .findAll();
            List<AppMessage> list = new ArrayList<>(results.size());
            for (MessageEntity entity : results) {
                list.add(MessageRealmMapper.toAppMessage(entity));
            }
            return list;
        } finally {
            realm.close();
        }
    }

    @NonNull
    public List<AppMessage> queryLatestPageBySessionId(@Nullable String sessionId, int limit) {
        if (TextUtils.isEmpty(sessionId) || !ImRealm.isUserBound() || limit <= 0) {
            return Collections.emptyList();
        }
        Realm realm = ImRealm.getRealm();
        try {
            RealmResults<MessageEntity> results = realm.where(MessageEntity.class)
                    .equalTo("sessionId", sessionId)
                    .sort("sequenceId", Sort.DESCENDING)
                    .limit(limit)
                    .findAll();
            return toAscendingAppMessages(realm, results);
        } finally {
            realm.close();
        }
    }

    @NonNull
    public List<AppMessage> queryOlderPageBySessionId(@Nullable String sessionId,
                                                      long boundarySequenceId,
                                                      int limit) {
        if (TextUtils.isEmpty(sessionId) || !ImRealm.isUserBound() || limit <= 0) {
            return Collections.emptyList();
        }
        Realm realm = ImRealm.getRealm();
        try {
            RealmResults<MessageEntity> results = realm.where(MessageEntity.class)
                    .equalTo("sessionId", sessionId)
                    .lessThan("sequenceId", boundarySequenceId)
                    .sort("sequenceId", Sort.DESCENDING)
                    .limit(limit)
                    .findAll();
            return toAscendingAppMessages(realm, results);
        } finally {
            realm.close();
        }
    }

    @NonNull
    private static List<AppMessage> toAscendingAppMessages(@NonNull Realm realm,
                                                            @NonNull RealmResults<MessageEntity> descResults) {
        int n = descResults.size();
        List<AppMessage> list = new ArrayList<>(n);
        for (int i = n - 1; i >= 0; i--) {
            list.add(MessageRealmMapper.toAppMessage(realm.copyFromRealm(descResults.get(i))));
        }
        return list;
    }

    /**
     * 发送成功：按 {@code clientMsgId} 更新服务端 {@code msgId} 与发送状态，并同步会话最近一条。
     */
    public void markOutgoingSendSuccess(@NonNull String clientMsgId,
                                        @NonNull String serverMsgId,
                                        @Nullable OnMessageUpdatedCallback onUpdated) {
        if (!ImRealm.isUserBound()
                || TextUtils.isEmpty(clientMsgId)
                || TextUtils.isEmpty(serverMsgId)) {
            return;
        }
        ImDbExecutor.runRead(() -> {
            MessageEntity peek = queryEntityByClientMsgId(clientMsgId);
            if (peek == null) {
                return;
            }
            AppMessage basis = MessageRealmMapper.toAppMessage(peek);
            SessionLastMessageSync.runWriteWithSessionSync(
                    Collections.singletonList(basis),
                    (realm, prefetched, sessionUpdates) -> {
                        MessageEntity entity = realm.where(MessageEntity.class)
                                .equalTo("clientMsgId", clientMsgId)
                                .findFirst();
                        if (entity == null) {
                            return;
                        }
                        entity.setMsgId(serverMsgId);
                        entity.setSendStatus(IMSConfig.SEND_STATUS_SENT);
                        long sequence = ImMessageManager.extractTrailingSequence(serverMsgId);
                        if (sequence != ImMessageManager.INVALID_SEQUENCE) {
                            entity.setSequenceId(sequence);
                        }
                        AppMessage updated = MessageRealmMapper.toAppMessage(entity);
                        sessionUpdates.addAll(SessionLastMessageSync.applyInTransaction(
                                realm,
                                Collections.singletonList(updated),
                                prefetched,
                                SessionLastMessageSync.countUnreadDeltaBySession(
                                        Collections.singletonList(updated))));
                    },
                    () -> {
                        AppMessage updated = queryAppMessageByClientMsgId(clientMsgId);
                        if (onUpdated != null && updated != null) {
                            onUpdated.onUpdated(updated);
                        }
                    });
        });
    }

    /** {@link #markOutgoingSendSuccess} 写库完成后的回调。 */
    public interface OnMessageUpdatedCallback {
        void onUpdated(@NonNull AppMessage message);
    }

    /** {@link #deleteMessages} 写库完成后的回调。 */
    public interface OnMessagesDeletedCallback {
        void onDeleted(@NonNull List<AppMessage> deletedMessages);
    }

    /**
     * 更新待发媒体消息的 content（如上传完成替换为 CDN URL），保持 {@link IMSConfig#SEND_STATUS_SENDING}。
     */
    public void updateOutgoingMessage(@NonNull AppMessage message,
                                      @Nullable OnMessageUpdatedCallback onUpdated) {
        if (!ImRealm.isUserBound()
                || TextUtils.isEmpty(MessageRealmMapper.clientMsgIdForPersist(message))) {
            return;
        }
        final AppMessage snapshot = copyMessage(message);
        ImDbExecutor.runWrite(() -> {
            Realm realm = ImRealm.getRealm();
            try {
                realm.executeTransaction(r -> {
                    MessageEntity entity = r.where(MessageEntity.class)
                            .equalTo("clientMsgId", snapshot.getClientMsgId())
                            .findFirst();
                    if (entity != null) {
                        entity.setContent(snapshot.getContent());
                    }
                });
            } finally {
                realm.close();
            }
            if (onUpdated != null) {
                AppMessage updated = queryAppMessageByClientMsgId(snapshot.getClientMsgId());
                if (updated != null) {
                    onUpdated.onUpdated(updated);
                }
            }
        });
    }

    public void markOutgoingSendFailed(@Nullable String clientMsgId,
                                       @Nullable OnMessageUpdatedCallback onUpdated) {
        updateOutgoingSendStatus(clientMsgId, IMSConfig.SEND_STATUS_FAILED, onUpdated);
    }

    public void markOutgoingResending(@Nullable String clientMsgId,
                                      @Nullable OnMessageUpdatedCallback onUpdated) {
        updateOutgoingSendStatus(clientMsgId, IMSConfig.SEND_STATUS_SENDING, onUpdated);
    }

    private void updateOutgoingSendStatus(@Nullable String clientMsgId,
                                          int sendStatus,
                                          @Nullable OnMessageUpdatedCallback onUpdated) {
        if (TextUtils.isEmpty(clientMsgId) || !ImRealm.isUserBound()) {
            return;
        }
        final String id = clientMsgId;
        ImDbExecutor.runWrite(() -> {
            Realm realm = ImRealm.getRealm();
            try {
                realm.executeTransaction(r -> {
                    MessageEntity entity = r.where(MessageEntity.class)
                            .equalTo("clientMsgId", id)
                            .findFirst();
                    if (entity != null) {
                        entity.setSendStatus(sendStatus);
                    }
                });
            } finally {
                realm.close();
            }
            if (onUpdated != null) {
                AppMessage updated = queryAppMessageByClientMsgId(id);
                if (updated != null) {
                    onUpdated.onUpdated(updated);
                }
            }
        });
    }

    @Nullable
    public MessageEntity queryMaxSequenceIdBySessionId(@Nullable String sessionId) {
        if (TextUtils.isEmpty(sessionId) || !ImRealm.isUserBound()) {
            return null;
        }
        Realm realm = ImRealm.getRealm();
        try {
            MessageEntity managed = realm.where(MessageEntity.class)
                    .equalTo("sessionId", sessionId)
                    .sort("sequenceId", Sort.DESCENDING)
                    .findFirst();
            if (managed == null) {
                return null;
            }
            return realm.copyFromRealm(managed);
        } finally {
            realm.close();
        }
    }

    @Nullable
    private static MessageEntity queryEntityByClientMsgId(@NonNull String clientMsgId) {
        if (!ImRealm.isUserBound()) {
            return null;
        }
        Realm realm = ImRealm.getRealm();
        try {
            MessageEntity entity = realm.where(MessageEntity.class)
                    .equalTo("clientMsgId", clientMsgId)
                    .findFirst();
            return entity != null ? realm.copyFromRealm(entity) : null;
        } finally {
            realm.close();
        }
    }

    @Nullable
    private static AppMessage queryAppMessageByClientMsgId(@NonNull String clientMsgId) {
        MessageEntity entity = queryEntityByClientMsgId(clientMsgId);
        return entity != null ? MessageRealmMapper.toAppMessage(entity) : null;
    }

    @NonNull
    private static List<AppMessage> snapshotMessages(@Nullable List<AppMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return Collections.emptyList();
        }
        List<AppMessage> snapshot = new ArrayList<>(messages.size());
        for (AppMessage message : messages) {
            if (message != null) {
                snapshot.add(copyMessage(message));
            }
        }
        return snapshot;
    }

    @NonNull
    private static List<MessageEntity> buildUpsertEntities(@NonNull List<AppMessage> messages) {
        List<MessageEntity> entities = new ArrayList<>(messages.size());
        for (AppMessage message : messages) {
            if (message == null) {
                continue;
            }
            if (TextUtils.isEmpty(MessageRealmMapper.clientMsgIdForPersist(message))) {
                continue;
            }
            entities.add(MessageRealmMapper.toEntity(message));
        }
        return entities;
    }

    @NonNull
    private static AppMessage copyMessage(@NonNull AppMessage source) {
        AppMessage target = new AppMessage();
        target.setConversationType(source.getConversationType());
        target.setConversationId(source.getConversationId());
        target.setMsgId(source.getMsgId());
        target.setClientMsgId(source.getClientMsgId());
        target.setMsgType(source.getMsgType());
        target.setSendType(source.getSendType());
        target.setMsgContentType(source.getMsgContentType());
        target.setFromId(source.getFromId());
        target.setToId(source.getToId());
        target.setTimestamp(source.getTimestamp());
        target.setStatusReport(source.getStatusReport());
        target.setExtend(source.getExtend());
        target.setCustomInfo(source.getCustomInfo());
        target.setExtra(source.getExtra());
        target.setContent(source.getContent());
        target.setSequenceId(source.getSequenceId());
        target.setSendStatus(source.getSendStatus());
        target.setReadPersonCount(source.getReadPersonCount());
        return target;
    }

    /**
     * 按服务端 {@code msgId} 将消息替换为撤回系统提示；未找到时回调 {@code null}。
     */
    public void applyRecallByMsgId(@NonNull String msgId,
                                   @Nullable OnMessageUpdatedCallback onUpdated) {
        if (TextUtils.isEmpty(msgId) || !ImRealm.isUserBound()) {
            if (onUpdated != null) {
                onUpdated.onUpdated(null);
            }
            return;
        }
        final String id = msgId;
        ImDbExecutor.runWrite(() -> {
            Realm realm = ImRealm.getRealm();
            AppMessage updated = null;
            SessionItem sessionUpdate = null;
            try {
                final SessionItem[] sessionHolder = new SessionItem[1];
                realm.executeTransaction(r -> {
                    MessageEntity entity = r.where(MessageEntity.class)
                            .equalTo("msgId", id)
                            .findFirst();
                    if (entity == null) {
                        return;
                    }
                    AppMessage original = MessageRealmMapper.toAppMessage(entity);
                    AppMessage recalled = MessageRepealHelper.toRecallNotification(original);
                    entity.setMsgType(recalled.getMsgType());
                    entity.setMsgContentType(recalled.getMsgContentType());
                    entity.setContent(recalled.getContent());
                    entity.setSendStatus(recalled.getSendStatus());
                    entity.setReadPersonCount(recalled.getReadPersonCount());
                    sessionHolder[0] = SessionLastMessageSync.applyRecallPreviewInTransaction(r, id, recalled);
                });
                sessionUpdate = sessionHolder[0];
                MessageEntity entity = realm.where(MessageEntity.class)
                        .equalTo("msgId", id)
                        .findFirst();
                if (entity != null) {
                    updated = MessageRealmMapper.toAppMessage(realm.copyFromRealm(entity));
                }
            } finally {
                realm.close();
            }
            if (sessionUpdate != null) {
                ImLocalStore.dispatchSessionPreviewUpdated(Collections.singletonList(sessionUpdate));
            }
            final AppMessage result = updated;
            if (onUpdated != null) {
                onUpdated.onUpdated(result);
            }
        });
    }

    /**
     * 批量删除本地消息；若删到会话最后一条则重算摘要，并回调已删快照。
     */
    public void deleteMessages(@Nullable List<AppMessage> messages,
                               @Nullable OnMessagesDeletedCallback onDeleted) {
        if (messages == null || messages.isEmpty() || !ImRealm.isUserBound()) {
            if (onDeleted != null) {
                onDeleted.onDeleted(Collections.emptyList());
            }
            return;
        }
        final List<String> persistKeys = new ArrayList<>();
        for (AppMessage message : messages) {
            if (message == null) {
                continue;
            }
            String key = MessageRealmMapper.clientMsgIdForPersist(message);
            if (!TextUtils.isEmpty(key)) {
                persistKeys.add(key);
            }
        }
        if (persistKeys.isEmpty()) {
            if (onDeleted != null) {
                onDeleted.onDeleted(Collections.emptyList());
            }
            return;
        }
        final List<String> ids = persistKeys;
        ImDbExecutor.runWrite(() -> {
            Realm realm = ImRealm.getRealm();
            final List<AppMessage> deletedSnapshots = new ArrayList<>();
            final List<SessionItem> sessionUpdates = new ArrayList<>();
            try {
                realm.executeTransaction(r -> {
                    Set<String> sessionsNeedingPreviewRefresh = new LinkedHashSet<>();
                    List<AppMessage> deleted = new ArrayList<>();
                    for (String id : ids) {
                        MessageEntity entity = r.where(MessageEntity.class)
                                .equalTo("clientMsgId", id)
                                .findFirst();
                        if (entity == null) {
                            entity = r.where(MessageEntity.class)
                                    .equalTo("msgId", id)
                                    .findFirst();
                        }
                        if (entity == null) {
                            continue;
                        }
                        AppMessage snapshot = MessageRealmMapper.toAppMessage(entity);
                        deleted.add(snapshot);
                        String sessionId = entity.getSessionId();
                        if (!TextUtils.isEmpty(sessionId)) {
                            SessionEntity session = r.where(SessionEntity.class)
                                    .equalTo("sessionId", sessionId)
                                    .findFirst();
                            if (session != null
                                    && SessionLastMessageSync.isSessionLastMessage(session, snapshot)) {
                                sessionsNeedingPreviewRefresh.add(sessionId);
                            }
                        }
                        entity.deleteFromRealm();
                    }
                    if (!deleted.isEmpty()) {
                        SessionSyncCursor.advanceInTransaction(r, deleted);
                    }
                    for (String sessionId : sessionsNeedingPreviewRefresh) {
                        SessionItem updated = SessionLastMessageSync.refreshSessionPreviewAfterDelete(
                                r, sessionId);
                        if (updated != null) {
                            sessionUpdates.add(updated);
                        }
                    }
                    deletedSnapshots.addAll(deleted);
                });
            } finally {
                realm.close();
            }
            if (!sessionUpdates.isEmpty()) {
                ImLocalStore.dispatchSessionPreviewUpdated(sessionUpdates);
            }
            if (onDeleted != null) {
                onDeleted.onDeleted(deletedSnapshots);
            }
        });
    }

    /**
     * 按 {@code clientMsgId} 批量删除本地消息。
     *
     * @deprecated 请使用 {@link #deleteMessages(List, OnMessagesDeletedCallback)}。
     */
    @Deprecated
    public void deleteByClientMsgIds(@Nullable List<String> clientMsgIds,
                                     @Nullable Runnable afterCommit) {
        if (clientMsgIds == null || clientMsgIds.isEmpty()) {
            if (afterCommit != null) {
                afterCommit.run();
            }
            return;
        }
        List<AppMessage> stubs = new ArrayList<>(clientMsgIds.size());
        for (String id : clientMsgIds) {
            if (TextUtils.isEmpty(id)) {
                continue;
            }
            AppMessage stub = new AppMessage();
            stub.setClientMsgId(id);
            stub.setMsgId(id);
            stubs.add(stub);
        }
        deleteMessages(stubs, deleted -> {
            if (afterCommit != null) {
                afterCommit.run();
            }
        });
    }

    /**
     * 群聊 0x0210：按服务端 {@code msgId} 更新已读人数。
     */
    public void updateReadPersonCountByMsgId(@NonNull String msgId,
                                            int readPersonCount,
                                            @Nullable Runnable afterCommit) {
        if (TextUtils.isEmpty(msgId) || !ImRealm.isUserBound()) {
            if (afterCommit != null) {
                afterCommit.run();
            }
            return;
        }
        final String id = msgId;
        final int count = readPersonCount;
        ImDbExecutor.runWrite(() -> {
            Realm realm = ImRealm.getRealm();
            try {
                realm.executeTransaction(r -> {
                    MessageEntity entity = r.where(MessageEntity.class)
                            .equalTo("msgId", id)
                            .findFirst();
                    if (entity != null) {
                        entity.setReadPersonCount(count);
                    }
                });
                if (afterCommit != null) {
                    afterCommit.run();
                }
            } finally {
                realm.close();
            }
        });
    }
}
