package com.freddy.im.db.repository;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.db.SessionSyncCursor;
import com.freddy.im.db.ImDbExecutor;
import com.freddy.im.db.ImLocalStore;
import com.freddy.im.db.ImRealm;
import com.freddy.im.db.entity.MessageEntity;
import com.freddy.im.db.entity.SessionEntity;
import com.freddy.im.db.entity.GroupMemberEntity;
import com.freddy.im.db.mapper.SessionRealmMapper;
import com.freddy.im.http.model.im.SessionItem;
import com.freddy.im.message.ImMessageManager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * 会话表读写。
 */
public final class ImSessionRepository {

    private static final ImSessionRepository INSTANCE = new ImSessionRepository();

    private ImSessionRepository() {
    }

    public static ImSessionRepository getInstance() {
        return INSTANCE;
    }

    public void upsertAll(@Nullable List<SessionItem> sessions) {
        upsertAllThen(sessions, null);
    }

    /**
     * 批量 upsert；事务提交后在 Realm 写线程执行 {@code afterCommit}。
     */
    public void upsertAllThen(@Nullable List<SessionItem> sessions, @Nullable Runnable afterCommit) {
        if (sessions == null || sessions.isEmpty() || !ImRealm.isUserBound()) {
            if (afterCommit != null) {
                afterCommit.run();
            }
            return;
        }
        final List<SessionItem> snapshot = new ArrayList<>(sessions);
        ImDbExecutor.runWrite(() -> {
            Realm realm = ImRealm.getRealm();
            try {
                realm.executeTransaction(r -> {
                    for (SessionItem item : snapshot) {
                        if (item == null || TextUtils.isEmpty(item.getSessionId())) {
                            continue;
                        }
                        r.copyToRealmOrUpdate(SessionRealmMapper.toEntityPreservingLocalFields(r, item));
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
     * 将指定会话未读数置 0；成功后在主线程回调 UI。
     */
    public void clearUnreadCount(@Nullable String sessionId) {
        setUnreadCount(sessionId, 0);
    }

    /**
     * 设置会话未读数（设为已读传 0，标记未读通常传 1），并通知列表刷新。
     */
    public void setUnreadCount(@Nullable String sessionId, int unreadCount) {
        if (TextUtils.isEmpty(sessionId) || !ImRealm.isUserBound()) {
            return;
        }
        final String id = sessionId;
        final int count = Math.max(0, unreadCount);
        ImDbExecutor.runWrite(() -> {
            List<SessionItem> updated = new ArrayList<>();
            Realm realm = ImRealm.getRealm();
            try {
                realm.executeTransaction(r -> {
                    SessionEntity entity = r.where(SessionEntity.class)
                            .equalTo("sessionId", id)
                            .findFirst();
                    if (entity != null) {
                        entity.setUnreadCount(count);
                        updated.add(SessionRealmMapper.toSessionItem(r.copyFromRealm(entity)));
                    }
                });
            } finally {
                realm.close();
            }
            ImLocalStore.dispatchSessionPreviewUpdated(updated);
        });
    }

    /**
     * 更新会话置顶 / 免打扰本地字段，并通知列表刷新。
     */
    public void updateTopMuteThen(@Nullable String sessionId,
                                  boolean top,
                                  boolean muteNotify,
                                  @Nullable Runnable afterCommit) {
        if (TextUtils.isEmpty(sessionId) || !ImRealm.isUserBound()) {
            if (afterCommit != null) {
                afterCommit.run();
            }
            return;
        }
        final String id = sessionId;
        ImDbExecutor.runWrite(() -> {
            List<SessionItem> updated = new ArrayList<>();
            Realm realm = ImRealm.getRealm();
            try {
                realm.executeTransaction(r -> {
                    SessionEntity entity = r.where(SessionEntity.class)
                            .equalTo("sessionId", id)
                            .findFirst();
                    if (entity == null) {
                        entity = r.createObject(SessionEntity.class, id);
                    }
                    entity.setTopNum(top ? System.currentTimeMillis() : 0L);
                    entity.setMuteNotify(muteNotify);
                    updated.add(SessionRealmMapper.toSessionItem(r.copyFromRealm(entity)));
                });
                if (afterCommit != null) {
                    afterCommit.run();
                }
            } finally {
                realm.close();
            }
            ImLocalStore.dispatchSessionPreviewUpdated(updated);
        });
    }

    @Nullable
    public SessionItem queryBySessionId(@Nullable String sessionId) {
        SessionEntity entity = queryEntityBySessionId(sessionId);
        if (entity == null) {
            return null;
        }
        return SessionRealmMapper.toSessionItem(entity);
    }

    public void updateMemChgTimeThen(@NonNull String sessionId,
                                    long memChgTime,
                                    @Nullable Runnable afterCommit) {
        if (TextUtils.isEmpty(sessionId) || !ImRealm.isUserBound()) {
            if (afterCommit != null) {
                afterCommit.run();
            }
            return;
        }
        final String sid = sessionId;
        ImDbExecutor.runWrite(() -> {
            Realm realm = ImRealm.getRealm();
            try {
                realm.executeTransaction(r -> {
                    SessionEntity entity = r.where(SessionEntity.class)
                            .equalTo("sessionId", sid)
                            .findFirst();
                    if (entity != null) {
                        entity.setMemChgTime(memChgTime);
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

    @Nullable
    public SessionEntity queryEntityBySessionId(@Nullable String sessionId) {
        if (TextUtils.isEmpty(sessionId) || !ImRealm.isUserBound()) {
            return null;
        }
        Realm realm = ImRealm.getRealm();
        try {
            SessionEntity entity = realm.where(SessionEntity.class)
                    .equalTo("sessionId", sessionId)
                    .findFirst();
            if (entity == null) {
                return null;
            }
            return realm.copyFromRealm(entity);
        } finally {
            realm.close();
        }
    }

    @Nullable
    public String resolveMessageSyncAnchor(@Nullable String sessionId) {
        if (TextUtils.isEmpty(sessionId) || !ImRealm.isUserBound()) {
            return null;
        }
        Realm realm = ImRealm.getRealm();
        try {
            return SessionSyncCursor.resolveAnchorMsgId(realm, sessionId);
        } finally {
            realm.close();
        }
    }

    public void deleteBySessionId(@Nullable String sessionId) {
        removeSessionWithDataThen(sessionId, null);
    }

    /**
     * 删除会话及其本地消息、群成员缓存；事务提交后在 Realm 写线程执行 {@code afterCommit}。
     */
    public void removeSessionWithDataThen(@Nullable String sessionId, @Nullable Runnable afterCommit) {
        if (TextUtils.isEmpty(sessionId) || !ImRealm.isUserBound()) {
            if (afterCommit != null) {
                afterCommit.run();
            }
            return;
        }
        final String id = sessionId;
        ImDbExecutor.runWrite(() -> {
            Realm realm = ImRealm.getRealm();
            try {
                realm.executeTransaction(r -> {
                    r.where(MessageEntity.class)
                            .equalTo("sessionId", id)
                            .findAll()
                            .deleteAllFromRealm();
                    r.where(SessionEntity.class)
                            .equalTo("sessionId", id)
                            .findAll()
                            .deleteAllFromRealm();
                    r.where(GroupMemberEntity.class)
                            .equalTo("groupId", id)
                            .findAll()
                            .deleteAllFromRealm();
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
     * 单聊 0x0207：推进对方已读游标（取 messageId 序列与库内较大值）。
     */
    public void advancePeerReadUpToSeq(@Nullable String sessionId,
                                      @NonNull String messageId,
                                      @Nullable Runnable afterCommit) {
        if (TextUtils.isEmpty(sessionId) || !ImRealm.isUserBound()) {
            if (afterCommit != null) {
                afterCommit.run();
            }
            return;
        }
        long seq = ImMessageManager.extractTrailingSequence(messageId);
        if (seq == ImMessageManager.INVALID_SEQUENCE) {
            if (afterCommit != null) {
                afterCommit.run();
            }
            return;
        }
        final String id = sessionId;
        final long incomingSeq = seq;
        ImDbExecutor.runWrite(() -> {
            Realm realm = ImRealm.getRealm();
            try {
                realm.executeTransaction(r -> {
                    SessionEntity entity = r.where(SessionEntity.class)
                            .equalTo("sessionId", id)
                            .findFirst();
                    if (entity == null) {
                        entity = r.createObject(SessionEntity.class, id);
                    }
                    if (incomingSeq > entity.getPeerReadUpToSeq()) {
                        entity.setPeerReadUpToSeq(incomingSeq);
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

    public long queryPeerReadUpToSeq(@Nullable String sessionId) {
        if (TextUtils.isEmpty(sessionId) || !ImRealm.isUserBound()) {
            return 0L;
        }
        Realm realm = ImRealm.getRealm();
        try {
            SessionEntity entity = realm.where(SessionEntity.class)
                    .equalTo("sessionId", sessionId)
                    .findFirst();
            return entity != null ? entity.getPeerReadUpToSeq() : 0L;
        } finally {
            realm.close();
        }
    }

    @NonNull
    public List<SessionItem> queryAllAsDto() {
        if (!ImRealm.isUserBound()) {
            return Collections.emptyList();
        }
        Realm realm = ImRealm.getRealm();
        try {
            RealmResults<SessionEntity> results = realm.where(SessionEntity.class)
                    .sort("topNum", Sort.DESCENDING, "lastMessageTime", Sort.DESCENDING)
                    .findAll();
            List<SessionItem> list = new ArrayList<>(results.size());
            for (SessionEntity entity : results) {
                list.add(SessionRealmMapper.toSessionItem(entity));
            }
            return list;
        } finally {
            realm.close();
        }
    }

    /**
     * 群聊会话：{@code sessionName} 包含关键字（忽略大小写）。
     * 供搜索页后台线程调用。
     */
    @NonNull
    public List<SessionItem> searchGroupSessionsByName(@Nullable String keyword) {
        if (TextUtils.isEmpty(keyword) || !ImRealm.isUserBound()) {
            return Collections.emptyList();
        }
        final String trimmed = keyword.trim();
        Realm realm = ImRealm.getRealm();
        try {
            RealmResults<SessionEntity> results = realm.where(SessionEntity.class)
                    .equalTo("conversation", com.freddy.im.MessageType.GROUP_CHAT.getMsgType())
                    .contains("sessionName", trimmed, io.realm.Case.INSENSITIVE)
                    .sort("topNum", Sort.DESCENDING, "lastMessageTime", Sort.DESCENDING)
                    .findAll();
            List<SessionItem> list = new ArrayList<>(results.size());
            for (SessionEntity entity : results) {
                list.add(SessionRealmMapper.toSessionItem(entity));
            }
            return list;
        } finally {
            realm.close();
        }
    }
}
