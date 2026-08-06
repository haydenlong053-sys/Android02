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
     * 同时回填缺省的 muteNotify，避免刷新冲掉本地免打扰。
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
            if (local == null) {
                continue;
            }
            if (item.getMuteNotify() == null) {
                item.setMuteNotify(local.isMuteNotify());
            }
            // getList 通常不带 createTime；回填本地（GroupVo 群详情写入）供群天数展示
            if (item.getCreateTime() <= 0L && local.getCreateTime() > 0L) {
                item.setCreateTime(local.getCreateTime());
            }
            // getList 未读为 0 时保留本地角标，避免冲掉推送已累加的未读
            if (item.getUnreadCount() <= 0 && local.getUnreadCount() > 0) {
                item.setUnreadCount(local.getUnreadCount());
            }
            if (!TextUtils.isEmpty(item.getLastMessageId())) {
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
