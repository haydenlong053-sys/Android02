package com.freddy.im.db.mapper;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.db.entity.SessionEntity;
import com.freddy.im.http.model.im.SessionItem;

import io.realm.Realm;

/**
 * HTTP {@link SessionItem} 与 {@link SessionEntity} 互转。
 */
public final class SessionRealmMapper {

    private SessionRealmMapper() {
    }

    @NonNull
    public static SessionEntity toEntity(@NonNull SessionItem item) {
        SessionEntity entity = new SessionEntity();
        entity.setSessionId(item.getSessionId());
        entity.setSessionName(item.getSessionName());
        entity.setUsername(item.getUsername());
        entity.setConversation(item.getConversation());
        entity.setCreateTime(item.getCreateTime());
        entity.setIcon(item.getIcon());
        entity.setMemCount(item.getMemCount());
        entity.setUnreadCount(item.getUnreadCount());
        entity.setUnPullCount(item.getUnPullCount());
        entity.setInternalFlag(item.getInternalFlag());
        entity.setTopNum(item.getTopNum());
        entity.setMuteNotify(item.isMuteNotify());
        entity.setReadedPosition(item.getReadedPosition());
        entity.setSelfReadPosition(item.getSelfReadPosition());
        entity.setBusi(item.getBusi());
        entity.setTags(item.getTags());
        entity.setPermission(item.getPermission());
        entity.setOppositeUserId(item.getOppositeUserId());
        entity.setOwerId(item.getOwerId());
        entity.setOppositeCompanyId(item.getOppositeCompanyId());
        entity.setExtAttrs(item.getExtAttrs());
        entity.setUserExtAttrs(item.getUserExtAttrs());
        entity.setLastMessageId(item.getLastMessageId());
        entity.setLastMessageContent(item.getLastMessageContent());
        entity.setLastContentType(item.getLastContentType());
        entity.setLastCustomInfo(item.getLastCustomInfo());
        entity.setLastMsgType(item.getLastMsgType());
        entity.setLastMessageTime(item.getLastMessageTime());
        return entity;
    }

    /**
     * HTTP 会话 upsert：保留库内 {@link SessionEntity#getPeerReadUpToSeq()}（仅本地维护，接口不返回）。
     */
    @NonNull
    public static SessionEntity toEntityPreservingLocalFields(@NonNull Realm realm,
                                                              @NonNull SessionItem item) {
        SessionEntity entity = toEntity(item);
        SessionEntity existing = realm.where(SessionEntity.class)
                .equalTo("sessionId", item.getSessionId())
                .findFirst();
        if (existing != null) {
            entity.setPeerReadUpToSeq(existing.getPeerReadUpToSeq());
            entity.setSyncedMaxMsgId(existing.getSyncedMaxMsgId());
            entity.setSyncedMaxSequence(existing.getSyncedMaxSequence());
            if (TextUtils.isEmpty(item.getSessionName()) && !TextUtils.isEmpty(existing.getSessionName())) {
                entity.setSessionName(existing.getSessionName());
            }
            if (TextUtils.isEmpty(item.getUsername()) && !TextUtils.isEmpty(existing.getUsername())) {
                entity.setUsername(existing.getUsername());
            }
            if (TextUtils.isEmpty(item.getIcon()) && !TextUtils.isEmpty(existing.getIcon())) {
                entity.setIcon(existing.getIcon());
            }
            if (TextUtils.isEmpty(item.getOppositeUserId()) && !TextUtils.isEmpty(existing.getOppositeUserId())) {
                entity.setOppositeUserId(existing.getOppositeUserId());
            }
        }
        return entity;
    }

    @NonNull
    public static SessionItem toSessionItem(@NonNull SessionEntity entity) {
        SessionItem item = new SessionItem();
        item.setSessionId(entity.getSessionId());
        item.setSessionName(entity.getSessionName());
        item.setUsername(entity.getUsername());
        item.setConversation(entity.getConversation());
        item.setCreateTime(entity.getCreateTime());
        item.setIcon(entity.getIcon());
        item.setMemCount(entity.getMemCount());
        item.setUnreadCount(entity.getUnreadCount());
        item.setUnPullCount(entity.getUnPullCount());
        item.setInternalFlag(entity.getInternalFlag());
        item.setTopNum(entity.getTopNum());
        item.setMuteNotify(entity.isMuteNotify());
        item.setReadedPosition(entity.getReadedPosition());
        item.setSelfReadPosition(entity.getSelfReadPosition());
        item.setBusi(entity.getBusi());
        item.setTags(entity.getTags());
        item.setPermission(entity.getPermission());
        item.setOppositeUserId(entity.getOppositeUserId());
        item.setOwerId(entity.getOwerId());
        item.setOppositeCompanyId(entity.getOppositeCompanyId());
        item.setExtAttrs(entity.getExtAttrs());
        item.setUserExtAttrs(entity.getUserExtAttrs());
        item.setLastMessageId(entity.getLastMessageId());
        item.setLastMessageContent(entity.getLastMessageContent());
        item.setLastContentType(entity.getLastContentType());
        item.setLastCustomInfo(entity.getLastCustomInfo());
        item.setLastMsgType(entity.getLastMsgType());
        item.setLastMessageTime(entity.getLastMessageTime());
        return item;
    }
}
