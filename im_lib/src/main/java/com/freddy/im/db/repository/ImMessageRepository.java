package com.freddy.im.db.repository;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.IMSConfig;
import com.freddy.im.MessageType;
import com.freddy.im.OutgoingSendTimeout;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.bean.ImConversationSearchResult;
import com.freddy.im.db.ImDbExecutor;
import com.freddy.im.db.SessionLastMessageSync;
import com.freddy.im.db.SessionSyncCursor;
import com.freddy.im.db.ImRealm;
import com.freddy.im.db.entity.MessageEntity;
import com.freddy.im.db.entity.SessionEntity;
import com.freddy.im.db.mapper.MessageRealmMapper;
import com.freddy.im.db.mapper.SessionRealmMapper;
import com.freddy.im.db.ImLocalStore;
import com.freddy.im.http.model.im.SessionItem;
import com.freddy.im.message.ImMessageManager;
import com.freddy.im.message.MessageContentFormatter;
import com.freddy.im.message.MessageEditHelper;
import com.freddy.im.message.MessageRepealHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * 消息表读写。
 */
public final class ImMessageRepository {

    /** 消息列表单页条数（与 {@link com.freddy.im.db.ImLocalStore#MESSAGE_PAGE_SIZE} 对齐） */
    public static final int DEFAULT_MESSAGE_PAGE_SIZE = 30;

    /** 搜索定位后，从目标消息向最新加载的上限（含目标本身） */
    public static final int FOCUS_FORWARD_MESSAGE_MAX = 1000;

    private static final int SEARCH_MESSAGE_MAX_MATCHES = 30;

    private static final ImMessageRepository INSTANCE = new ImMessageRepository();

    private ImMessageRepository() {
    }

    public static ImMessageRepository getInstance() {
        return INSTANCE;
    }

    /**
     * 批量 upsert，事务提交成功后在<strong>同一条写线程</strong>执行 {@code afterSuccess}（失败不执行）。
     * 默认会为<strong>新插入</strong>的未读消息累加会话未读。
     */
    public void insertOrUpdateBatchThen(@Nullable List<AppMessage> messages, @Nullable Runnable afterSuccess) {
        insertOrUpdateBatchThen(messages, afterSuccess, true);
    }

    /**
     * @param incrementUnread {@code false} 时不改会话未读（用于 getList 后按服务端未读数补拉消息正文，避免再 +N）。
     */
    public void insertOrUpdateBatchThen(@Nullable List<AppMessage> messages,
                                        @Nullable Runnable afterSuccess,
                                        boolean incrementUnread) {
        insertOrUpdateBatchThen(messages, afterSuccess, incrementUnread, true);
    }

    /**
     * @param notifySessionPreview {@code false}：后台静默补拉，不刷会话列表 UI。
     */
    public void insertOrUpdateBatchThen(@Nullable List<AppMessage> messages,
                                        @Nullable Runnable afterSuccess,
                                        boolean incrementUnread,
                                        boolean notifySessionPreview) {
        if (messages == null || messages.isEmpty() || !ImRealm.isUserBound()) {
            if (afterSuccess != null) {
                afterSuccess.run();
            }
            return;
        }
        final List<AppMessage> snapshot = snapshotMessages(messages);
        final List<MessageEntity> entities = buildUpsertEntities(snapshot);
        if (entities.isEmpty()) {
            if (afterSuccess != null) {
                afterSuccess.run();
            }
            return;
        }
        final Runnable callback = afterSuccess;
        final boolean countUnread = incrementUnread;
        SessionLastMessageSync.runWriteWithSessionSync(snapshot, (realm, prefetched, sessionUpdates) -> {
            // 必须在 insertOrUpdate 之前统计，否则已写入的消息会被当成「已存在」而漏计
            Map<String, Integer> unreadDelta = countUnread
                    ? SessionLastMessageSync.countUnreadDeltaForNewMessages(realm, snapshot)
                    : Collections.emptyMap();
            realm.insertOrUpdate(entities);
            sessionUpdates.addAll(SessionLastMessageSync.applyInTransaction(
                    realm, snapshot, prefetched, unreadDelta));
        }, callback, notifySessionPreview);
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
                    // 自己发出的消息不累加未读
                    sessionUpdates.addAll(SessionLastMessageSync.applyInTransaction(
                            realm,
                            Collections.singletonList(snapshot),
                            prefetched,
                            Collections.emptyMap(),true));
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
            // 试验：按时间升序（sequenceId 作同毫秒兜底），避免失败消息 sequenceId=0 顶到最前
            RealmResults<MessageEntity> results = realm.where(MessageEntity.class)
                    .equalTo("sessionId", sessionId)
                    .sort(new String[]{"timestamp", "sequenceId"},
                            new Sort[]{Sort.ASCENDING, Sort.ASCENDING})
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
            // 试验：按时间取最新一页（sequenceId 作同毫秒兜底）
            RealmResults<MessageEntity> results = realm.where(MessageEntity.class)
                    .equalTo("sessionId", sessionId)
                    .sort(new String[]{"timestamp", "sequenceId"},
                            new Sort[]{Sort.DESCENDING, Sort.DESCENDING})
                    .limit(limit)
                    .findAll();
            return toAscendingAppMessages(realm, results);
        } finally {
            realm.close();
        }
    }

    /**
     * 加载比 {@code boundaryTimestamp} 更旧的一页（按时间降序取后反转为升序）。
     */
    @NonNull
    public List<AppMessage> queryOlderPageBySessionId(@Nullable String sessionId,
                                                      long boundaryTimestamp,
                                                      int limit) {
        if (TextUtils.isEmpty(sessionId) || !ImRealm.isUserBound() || limit <= 0) {
            return Collections.emptyList();
        }
        Realm realm = ImRealm.getRealm();
        try {
            RealmResults<MessageEntity> results = realm.where(MessageEntity.class)
                    .equalTo("sessionId", sessionId)
                    .lessThan("timestamp", boundaryTimestamp)
                    .sort(new String[]{"timestamp", "sequenceId"},
                            new Sort[]{Sort.DESCENDING, Sort.DESCENDING})
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
     * syncedMax 仅在 serverSeq 与当前水位连续（+1）时推进。
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
            final boolean[] reportReadIfContiguous = {false};
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
                            reportReadIfContiguous[0] = isContiguousWithPreviousServerMsg(
                                    realm, entity.getSessionId(), clientMsgId, sequence);
                        }
                        AppMessage updated = MessageRealmMapper.toAppMessage(entity);
                        // 发送成功：仅连续（serverSeq == syncedMax+1）才抬水位，跳跃不推进以免漏拉
                        sessionUpdates.addAll(SessionLastMessageSync.applyInTransaction(
                                realm,
                                Collections.singletonList(updated),
                                prefetched,
                                Collections.emptyMap(),
                                true));
                    },
                    () -> {
                        if (reportReadIfContiguous[0]) {
                            ImMessageManager.getInstance().reportMessageRead(serverMsgId);
                        }
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

    /**
     * 失败消息重发：状态改回发送中，并刷新为当前时间，保证按时间排序时落在会话底部。
     */
    public void markOutgoingResending(@Nullable String clientMsgId,
                                      @Nullable OnMessageUpdatedCallback onUpdated) {
        updateOutgoingSendStatus(clientMsgId, IMSConfig.SEND_STATUS_SENDING, onUpdated);
    }

    /** 进程恢复：仍为发送中的外发消息（含 clientMsgId 与落库时间）。 */
    public static final class OutgoingSendingRef {
        @NonNull
        public final String clientMsgId;
        public final long timestamp;

        OutgoingSendingRef(@NonNull String clientMsgId, long timestamp) {
            this.clientMsgId = clientMsgId;
            this.timestamp = timestamp;
        }
    }

    public interface OnPendingSendingTimeoutCallback {
        void onPending(@NonNull List<OutgoingSendingRef> pending);
    }

    /**
     * 进程被杀后恢复：已超过发送超时的标失败；未超时的交给 {@code onPending} 继续倒计时。
     */
    public void recoverOutgoingSendingTimeouts(@Nullable OnMessageUpdatedCallback onExpiredUpdated,
                                               @Nullable OnPendingSendingTimeoutCallback onPending) {
        if (!ImRealm.isUserBound()) {
            return;
        }
        ImDbExecutor.runWrite(() -> {
            Realm realm = ImRealm.getRealm();
            List<String> expiredIds = new ArrayList<>();
            List<OutgoingSendingRef> pending = new ArrayList<>();
            try {
                RealmResults<MessageEntity> sending = realm.where(MessageEntity.class)
                        .equalTo("sendStatus", IMSConfig.SEND_STATUS_SENDING)
                        .findAll();
                for (MessageEntity entity : sending) {
                    if (entity == null || TextUtils.isEmpty(entity.getClientMsgId())) {
                        continue;
                    }
                    String id = entity.getClientMsgId();
                    long timestamp = entity.getTimestamp();
                    if (OutgoingSendTimeout.isTimedOut(timestamp)) {
                        expiredIds.add(id);
                    } else {
                        pending.add(new OutgoingSendingRef(id, timestamp));
                    }
                }
                if (!expiredIds.isEmpty()) {
                    realm.executeTransaction(r -> {
                        for (String id : expiredIds) {
                            MessageEntity entity = r.where(MessageEntity.class)
                                    .equalTo("clientMsgId", id)
                                    .findFirst();
                            if (entity != null
                                    && entity.getSendStatus() == IMSConfig.SEND_STATUS_SENDING
                                    && OutgoingSendTimeout.isTimedOut(entity.getTimestamp())) {
                                entity.setSendStatus(IMSConfig.SEND_STATUS_FAILED);
                            }
                        }
                    });
                }
            } finally {
                realm.close();
            }
            if (onExpiredUpdated != null) {
                for (String id : expiredIds) {
                    AppMessage updated = queryAppMessageByClientMsgId(id);
                    if (updated != null) {
                        onExpiredUpdated.onUpdated(updated);
                    }
                }
            }
            if (onPending != null && !pending.isEmpty()) {
                onPending.onPending(pending);
            }
        });
    }


    private void updateOutgoingSendStatus(@Nullable String clientMsgId,
                                          int sendStatus,
                                          @Nullable OnMessageUpdatedCallback onUpdated) {
        if (TextUtils.isEmpty(clientMsgId) || !ImRealm.isUserBound()) {
            return;
        }
        final String id = clientMsgId;
        final long now = System.currentTimeMillis();
        ImDbExecutor.runWrite(() -> {
            Realm realm = ImRealm.getRealm();
            try {
                realm.executeTransaction(r -> {
                    MessageEntity entity = r.where(MessageEntity.class)
                            .equalTo("clientMsgId", id)
                            .findFirst();
                    if (entity != null) {
                        entity.setSendStatus(sendStatus);
                        if (sendStatus == IMSConfig.SEND_STATUS_SENDING){
                            entity.setTimestamp(now);
                        }
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

    /**
     * 己方回包 seq 是否与会话内上一条有效服务端消息连续（prevSeq + 1）。
     * 连续则需再 reportRead，把已读水位推到本条。
     */
    private static boolean isContiguousWithPreviousServerMsg(@NonNull Realm realm,
                                                             @Nullable String sessionId,
                                                             @NonNull String clientMsgId,
                                                             long currentSeq) {
        if (TextUtils.isEmpty(sessionId)
                || currentSeq <= ImMessageManager.INVALID_SEQUENCE
                || ImMessageManager.isFileAssistantSession(sessionId)) {
            return false;
        }
        MessageEntity prev = realm.where(MessageEntity.class)
                .equalTo("sessionId", sessionId)
                .notEqualTo("clientMsgId", clientMsgId)
                .greaterThan("sequenceId", ImMessageManager.INVALID_SEQUENCE)
                .lessThan("sequenceId", currentSeq)
                .sort("sequenceId", Sort.DESCENDING)
                .findFirst();
        if (prev == null) {
//            return false;
            //首次会话，直接认定为连续
            return true;
        }
        long prevSeq = prev.getSequenceId();
        if (prevSeq + 1L != currentSeq) {
            return false;
        }
        long msgIdSeq = ImMessageManager.extractTrailingSequence(prev.getMsgId());
        return msgIdSeq == prevSeq;
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
        target.setStatus(source.getStatus());
        return target;
    }

    /**
     * 按服务端 {@code msgId} 应用消息编辑（status=5，更新 content/customInfo）；未找到时回调 {@code null}。
     */
    public void applyEditByMsgId(@NonNull String msgId,
                                 @NonNull String contentJson,
                                 @Nullable String customInfoJson,
                                 long editedAtMs,
                                 @Nullable OnMessageUpdatedCallback onUpdated) {
        if (TextUtils.isEmpty(msgId) || TextUtils.isEmpty(contentJson) || !ImRealm.isUserBound()) {
            if (onUpdated != null) {
                onUpdated.onUpdated(null);
            }
            return;
        }
        final String id = msgId;
        final String content = contentJson;
        final String customInfo = customInfoJson;
        final long editedAt = editedAtMs;
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
                    if (MessageRepealHelper.isRecalled(original)) {
                        return;
                    }
                    AppMessage edited = MessageEditHelper.toEditedMessage(
                            original, content, customInfo, editedAt);
                    entity.setContent(edited.getContent());
                    entity.setCustomInfo(edited.getCustomInfo());
                    entity.setStatus(edited.getStatus());
                    sessionHolder[0] = SessionLastMessageSync.applyEditPreviewInTransaction(r, id, edited);
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
     * 按服务端 {@code msgId} 将消息标记为撤回（status=1）；未找到时回调 {@code null}。
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
                    AppMessage recalled = MessageRepealHelper.toRecalledMessage(original);
                    entity.setStatus(recalled.getStatus());
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
                            // 凡本会话有删消息都重算摘要，避免 lastMessageId 与主键不一致时漏刷
                            sessionsNeedingPreviewRefresh.add(sessionId);
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

    /**
     * 清空会话本地消息（保留会话行与同步高水位）；事务提交后执行 {@code afterCommit}。
     */
    public void clearMessagesBySessionId(@Nullable String sessionId,
                                         @Nullable Runnable afterCommit) {
        if (TextUtils.isEmpty(sessionId) || !ImRealm.isUserBound()) {
            if (afterCommit != null) {
                afterCommit.run();
            }
            return;
        }
        final String id = sessionId;
        ImDbExecutor.runWrite(() -> {
            List<SessionItem> sessionUpdates = new ArrayList<>();
            try {
                Realm realm = ImRealm.getRealm();
                try {
                    realm.executeTransaction(r -> {
                        MessageEntity maxEntity = r.where(MessageEntity.class)
                                .equalTo("sessionId", id)
                                .sort("sequenceId", Sort.DESCENDING)
                                .findFirst();
                        if (maxEntity != null) {
                            SessionSyncCursor.advanceInTransaction(
                                    r,
                                    Collections.singletonList(
                                            MessageRealmMapper.toAppMessage(maxEntity)));
                        }
                        r.where(MessageEntity.class)
                                .equalTo("sessionId", id)
                                .findAll()
                                .deleteAllFromRealm();
                        SessionItem updated = SessionLastMessageSync.refreshSessionPreviewAfterDelete(r, id);
                        if (updated != null) {
                            sessionUpdates.add(updated);
                        }
                    });
                } finally {
                    realm.close();
                }
            } finally {
                if (!sessionUpdates.isEmpty()) {
                    ImLocalStore.dispatchSessionPreviewUpdated(sessionUpdates);
                }
                if (afterCommit != null) {
                    afterCommit.run();
                }
            }
        });
    }

    /**
     * 按文本消息 {@code content} 模糊搜索，最多取 {@link #SEARCH_MESSAGE_MAX_MATCHES} 条最近命中；
     * 每个会话返回最近一条命中（结果按命中时间降序）。
     */
    @NonNull
    public List<ImConversationSearchResult> searchConversationsByMessageContent(
            @Nullable String keyword) {
        if (TextUtils.isEmpty(keyword) || !ImRealm.isUserBound()) {
            return Collections.emptyList();
        }
        String trimmed = keyword.trim();
        if (trimmed.isEmpty()) {
            return Collections.emptyList();
        }
        Realm realm = ImRealm.getRealm();
        try {
            RealmResults<MessageEntity> results = realm.where(MessageEntity.class)
                    .equalTo("msgContentType", MessageType.MessageContentType.TEXT.getMsgContentType())
                    .notEqualTo("msgType", MessageContentFormatter.API_MSG_TYPE_SYSTEM)
                    .notEqualTo("status", MessageRepealHelper.STATUS_RECALLED)
                    .contains("content", trimmed, Case.INSENSITIVE)
                    .sort("sequenceId", Sort.DESCENDING)
                    .limit(SEARCH_MESSAGE_MAX_MATCHES)
                    .findAll();
            Map<String, ImConversationSearchResult> map = new LinkedHashMap<>();
            for (MessageEntity entity : results) {
                String sessionId = entity.getSessionId();
                if (TextUtils.isEmpty(sessionId)) {
                    continue;
                }
                ImConversationSearchResult existing = map.get(sessionId);
                if (existing != null) {
                    existing.matchedCount++;
                    continue;
                }
                ImConversationSearchResult result = new ImConversationSearchResult();
                result.latestMatchedMessage = MessageRealmMapper.toAppMessage(
                        realm.copyFromRealm(entity));
                result.matchedCount = 1;
                map.put(sessionId, result);
            }
            if (map.isEmpty()) {
                return Collections.emptyList();
            }
            String[] sessionIds = map.keySet().toArray(new String[0]);
            RealmResults<SessionEntity> sessionEntities = realm.where(SessionEntity.class)
                    .in("sessionId", sessionIds)
                    .findAll();
            Map<String, SessionItem> sessionById = new HashMap<>(sessionEntities.size());
            for (SessionEntity sessionEntity : sessionEntities) {
                sessionById.put(sessionEntity.getSessionId(),
                        SessionRealmMapper.toSessionItem(realm.copyFromRealm(sessionEntity)));
            }
            for (Map.Entry<String, ImConversationSearchResult> entry : map.entrySet()) {
                String sessionId = entry.getKey();
                ImConversationSearchResult result = entry.getValue();
                SessionItem session = sessionById.get(sessionId);
                if (session == null) {
                    session = new SessionItem();
                    session.setSessionId(sessionId);
                    AppMessage latest = result.latestMatchedMessage;
                    if (latest != null) {
                        session.setConversation(latest.getConversationType());
                    }
                }
                result.sessionItem = session;
            }
            return new ArrayList<>(map.values());
        } finally {
            realm.close();
        }
    }

    @Nullable
    public AppMessage queryByMsgId(@Nullable String msgId) {
        if (TextUtils.isEmpty(msgId) || !ImRealm.isUserBound()) {
            return null;
        }
        Realm realm = ImRealm.getRealm();
        try {
            MessageEntity entity = realm.where(MessageEntity.class)
                    .equalTo("msgId", msgId)
                    .findFirst();
            if (entity == null) {
                entity = realm.where(MessageEntity.class)
                        .equalTo("clientMsgId", msgId)
                        .findFirst();
            }
            return entity != null
                    ? MessageRealmMapper.toAppMessage(realm.copyFromRealm(entity)) : null;
        } finally {
            realm.close();
        }
    }

    @NonNull
    public List<AppMessage> queryFromSequenceId(@Nullable String sessionId, long sequenceId) {
        return queryFromSequenceId(sessionId, sequenceId, FOCUS_FORWARD_MESSAGE_MAX);
    }

    /**
     * 从 {@code sequenceId} 起向最新加载（升序），最多 {@code limit} 条（含起点消息）。
     */
    @NonNull
    public List<AppMessage> queryFromSequenceId(@Nullable String sessionId,
                                                long sequenceId,
                                                int limit) {
        if (TextUtils.isEmpty(sessionId) || !ImRealm.isUserBound() || limit <= 0) {
            return Collections.emptyList();
        }
        Realm realm = ImRealm.getRealm();
        try {
            RealmResults<MessageEntity> results = realm.where(MessageEntity.class)
                    .equalTo("sessionId", sessionId)
                    .greaterThanOrEqualTo("sequenceId", sequenceId)
                    .sort("sequenceId", Sort.ASCENDING)
                    .limit(limit)
                    .findAll();
            List<AppMessage> list = new ArrayList<>(results.size());
            for (MessageEntity entity : results) {
                list.add(MessageRealmMapper.toAppMessage(realm.copyFromRealm(entity)));
            }
            return list;
        } finally {
            realm.close();
        }
    }

    @NonNull
    public List<AppMessage> queryPageUpToSequenceId(@Nullable String sessionId,
                                                    long sequenceId,
                                                    int limit) {
        if (TextUtils.isEmpty(sessionId) || !ImRealm.isUserBound() || limit <= 0) {
            return Collections.emptyList();
        }
        Realm realm = ImRealm.getRealm();
        try {
            RealmResults<MessageEntity> results = realm.where(MessageEntity.class)
                    .equalTo("sessionId", sessionId)
                    .lessThanOrEqualTo("sequenceId", sequenceId)
                    .sort("sequenceId", Sort.DESCENDING)
                    .limit(limit)
                    .findAll();
            return toAscendingAppMessages(realm, results);
        } finally {
            realm.close();
        }
    }

    public boolean hasOlderMessages(@Nullable String sessionId, long boundaryTimestamp) {
        if (TextUtils.isEmpty(sessionId) || !ImRealm.isUserBound()) {
            return false;
        }
        Realm realm = ImRealm.getRealm();
        try {
            return realm.where(MessageEntity.class)
                    .equalTo("sessionId", sessionId)
                    .lessThan("timestamp", boundaryTimestamp)
                    .count() > 0;
        } finally {
            realm.close();
        }
    }
}
