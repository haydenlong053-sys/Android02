package com.freddy.im.http;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.db.SessionLastMessageSync;
import com.freddy.im.db.repository.ImSessionRepository;
import com.freddy.im.http.model.im.SessionItem;
import com.freddy.im.message.ImMessageManager;

import java.util.List;

/**
 * 会话列表最近消息字段合并：优先用本地消息表最后一条，其次会话表摘要。
 */
public final class SessionLastMsgMerge {

    private SessionLastMsgMerge() {
    }

    /**
     * {@code getList} 回填：展示字段用本地会话补全；
     * <b>消息预览 last* 优先取消息表最后一条</b>（与聊天页消息列表同源）。
     */
    public static void applyFromLocal(@Nullable List<SessionItem> sessions) {
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        for (SessionItem item : sessions) {
            if (item == null || TextUtils.isEmpty(item.getSessionId())) {
                continue;
            }
            SessionItem local = ImSessionRepository.getInstance().queryBySessionId(item.getSessionId());
            if (local != null) {
                if (item.getConversation() <= 0 && local.getConversation() > 0) {
                    item.setConversation(local.getConversation());
                }
                if (TextUtils.isEmpty(item.getOwerId()) && !TextUtils.isEmpty(local.getOwerId())) {
                    item.setOwerId(local.getOwerId());
                }
                if (item.getMemCount() <= 0 && local.getMemCount() > 0) {
                    item.setMemCount(local.getMemCount());
                }
                if (TextUtils.isEmpty(item.getSessionName()) && !TextUtils.isEmpty(local.getSessionName())) {
                    item.setSessionName(local.getSessionName());
                }
                if (TextUtils.isEmpty(item.getIcon()) && !TextUtils.isEmpty(local.getIcon())) {
                    item.setIcon(local.getIcon());
                }
                if (item.getMuteNotify() == null) {
                    item.setMuteNotify(local.isMuteNotify());
                }
                if (item.getCreateTime() <= 0L && local.getCreateTime() > 0L) {
                    item.setCreateTime(local.getCreateTime());
                }
                if (item.getRenameCount() == null && local.getRenameCount() != null) {
                    item.setRenameCount(local.getRenameCount());
                }
                if (item.getUnreadCount() <= 0 && local.getUnreadCount() > 0) {
                    item.setUnreadCount(local.getUnreadCount());
                }
            }
            if (SessionLastMessageSync.applyLatestMessagePreviewToItem(item)) {
                continue;
            }
            if (local == null || !TextUtils.isEmpty(item.getLastMessageId())) {
                continue;
            }
            copyLastMsgFields(item, local);
        }
    }

    /**
     * 将 {@code candidate} 与 {@code target} 的 last* 比较：若 candidate 更新则写到 target。
     * 用于内存合并 / 节流队列，避免旧摘要覆盖新预览。
     */
    public static void preferNewerLastMsgOnto(@NonNull SessionItem target, @Nullable SessionItem candidate) {
        if (candidate == null) {
            return;
        }
        if (TextUtils.isEmpty(target.getLastMessageId())) {
            copyLastMsgFields(target, candidate);
            return;
        }
        if (TextUtils.isEmpty(candidate.getLastMessageId())) {
            return;
        }
        if (isLastMsgNewer(
                candidate.getLastMessageId(),
                candidate.getLastMessageTime(),
                target.getLastMessageId(),
                target.getLastMessageTime())) {
            copyLastMsgFields(target, candidate);
        }
    }

    /**
     * @return true 若 candidate 的 last* 比 baseline 更新（优先 seq，其次 time）
     */
    public static boolean isLastMsgNewer(@Nullable String candidateId,
                                         long candidateTime,
                                         @Nullable String baselineId,
                                         long baselineTime) {
        long candSeq = ImMessageManager.extractTrailingSequence(candidateId);
        long baseSeq = ImMessageManager.extractTrailingSequence(baselineId);
        if (candSeq > 0L && baseSeq > 0L) {
            return candSeq > baseSeq;
        }
        if (candidateTime > 0L && baselineTime > 0L && candidateTime != baselineTime) {
            return candidateTime > baselineTime;
        }
        if (TextUtils.isEmpty(baselineId) && !TextUtils.isEmpty(candidateId)) {
            return true;
        }
        if (candSeq > baseSeq) {
            return true;
        }
        return candidateTime > baselineTime;
    }

    private static void copyLastMsgFields(@NonNull SessionItem target, @NonNull SessionItem source) {
        if (!TextUtils.isEmpty(source.getLastMessageId())) {
            target.setLastMessageId(source.getLastMessageId());
        }
        if (!TextUtils.isEmpty(source.getLastMessageContent())) {
            target.setLastMessageContent(source.getLastMessageContent());
        }
        if (!TextUtils.isEmpty(source.getLastContentType())) {
            target.setLastContentType(source.getLastContentType());
        }
        if (!TextUtils.isEmpty(source.getLastCustomInfo())) {
            target.setLastCustomInfo(source.getLastCustomInfo());
        }
        target.setLastMsgType(source.getLastMsgType());
        target.setLastMessageStatus(source.getLastMessageStatus());
        if (source.getLastMessageTime() > 0) {
            target.setLastMessageTime(source.getLastMessageTime());
        }
    }
}
