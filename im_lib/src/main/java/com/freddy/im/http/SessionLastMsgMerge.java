package com.freddy.im.http;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.freddy.im.db.SessionLastMessageSync;
import com.freddy.im.db.repository.ImSessionRepository;
import com.freddy.im.http.model.im.SessionItem;

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
                // getList 可能缺 conversation / owerId，不回填则群进不了私群/公群
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
            // 1) 有本地消息：一律用消息表最后一条
            if (SessionLastMessageSync.applyLatestMessagePreviewToItem(item)) {
                continue;
            }
            // 2) 无本地消息且 getList 未带 last*：回退会话表摘要
            if (local == null || !TextUtils.isEmpty(item.getLastMessageId())) {
                continue;
            }
            if (!TextUtils.isEmpty(local.getLastMessageId())) {
                item.setLastMessageId(local.getLastMessageId());
            }
            if (!TextUtils.isEmpty(local.getLastMessageContent())) {
                item.setLastMessageContent(local.getLastMessageContent());
            }
            if (!TextUtils.isEmpty(local.getLastContentType())) {
                item.setLastContentType(local.getLastContentType());
            }
            if (!TextUtils.isEmpty(local.getLastCustomInfo())) {
                item.setLastCustomInfo(local.getLastCustomInfo());
            }
            item.setLastMsgType(local.getLastMsgType());
            item.setLastMessageStatus(local.getLastMessageStatus());
            if (local.getLastMessageTime() > 0) {
                item.setLastMessageTime(local.getLastMessageTime());
            }
        }
    }
}
