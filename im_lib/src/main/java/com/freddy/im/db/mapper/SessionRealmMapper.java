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
        entity.setMemChgTime(item.getMemChgTime());
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
        entity.setLastMessageStatus(item.getLastMessageStatus());
        entity.setLastMessageTime(item.getLastMessageTime());
        return entity;
    }

    /**
     * HTTP 会话 upsert：保留库内仅本地维护字段（如 peerRead / syncedMax），
     * 以及 getList 常缺的展示字段（name/icon/last* / memChgTime / owerId）。
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
            // getList 常缺 owerId；保留本地群主，避免私群/公群 Tab 错乱
            if (TextUtils.isEmpty(item.getOwerId()) && !TextUtils.isEmpty(existing.getOwerId())) {
                entity.setOwerId(existing.getOwerId());
            }
            // getList 通常不带 memChgTime；保留本地水位，避免进群聊重复全量 memberList
            if (item.getMemChgTime() <= 0L && existing.getMemChgTime() > 0L) {
                entity.setMemChgTime(existing.getMemChgTime());
            }
            // getList 通常不带 last*；空则保留本地摘要，避免刷新冲掉会话预览
            if (TextUtils.isEmpty(item.getLastMessageId())) {
                if (!TextUtils.isEmpty(existing.getLastMessageId())) {
                    entity.setLastMessageId(existing.getLastMessageId());
                }
                if (!TextUtils.isEmpty(existing.getLastMessageContent())) {
                    entity.setLastMessageContent(existing.getLastMessageContent());
                }
                if (!TextUtils.isEmpty(existing.getLastContentType())) {
                    entity.setLastContentType(existing.getLastContentType());
                }
                if (!TextUtils.isEmpty(existing.getLastCustomInfo())) {
                    entity.setLastCustomInfo(existing.getLastCustomInfo());
                }
                entity.setLastMsgType(existing.getLastMsgType());
                entity.setLastMessageStatus(existing.getLastMessageStatus());
                if (existing.getLastMessageTime() > 0) {
                    entity.setLastMessageTime(existing.getLastMessageTime());
                }
            }
            // getList 可能缺 muteNotify；null 时保留本地免打扰，避免开关被冲掉
            if (item.getMuteNotify() == null) {
                entity.setMuteNotify(existing.isMuteNotify());
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
        item.setMemChgTime(entity.getMemChgTime());
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
        item.setLastMessageStatus(entity.getLastMessageStatus());
        item.setLastMessageTime(entity.getLastMessageTime());
        return item;
    }
}
