package com.freddy.im.http;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.freddy.im.db.repository.ImSessionRepository;
import com.freddy.im.http.model.im.SessionItem;

import java.util.List;

/**
 * 会话列表最近消息字段合并：从本地库回填（不再依赖 {@code getLastMsg}）。
 */
public final class SessionLastMsgMerge {

    private SessionLastMsgMerge() {
    }

    /**
     * {@code getList} 通常不带 last*：用本地会话摘要回填，保证列表首屏有预览。
     */
    public static void applyFromLocal(@Nullable List<SessionItem> sessions) {
        if (sessions == null || sessions.isEmpty()) {
            return;
        }
        for (SessionItem item : sessions) {
            if (item == null || TextUtils.isEmpty(item.getSessionId())) {
                continue;
            }
            if (!TextUtils.isEmpty(item.getLastMessageId())) {
                continue;
            }
            SessionItem local = ImSessionRepository.getInstance().queryBySessionId(item.getSessionId());
            if (local == null) {
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
