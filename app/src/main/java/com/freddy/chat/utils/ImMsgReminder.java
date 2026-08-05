package com.freddy.chat.utils;

import android.content.Context;
import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.cboda.baselibrary.app.UtilBroadApplication;
import com.cboda.baselibrary.utils.MMKVBroadUtils;
import com.cboda.chatui.bridge.TiXingBroadUtils;
import com.cboda.chatui.quickmeet.MeetCallManager;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.db.repository.ImSessionRepository;
import com.freddy.im.http.model.im.SessionItem;
import com.freddy.im.message.ImActiveChatSession;
import com.freddy.webtrc.activity.CallActivity;

import java.util.List;

/**
 * 自研 IM 新消息提醒：对齐野火 {@link TiXingBroadUtils#imMsgReminder}（声音 + 有震动则震动）。
 * <p>
 * 免打扰会话、自己发出的消息、当前正在查看的会话、通话中不提醒。
 */
public final class ImMsgReminder {

    /** 过旧消息不响铃（略宽于野火后台 10s，覆盖拉列表网络耗时）。 */
    private static final long MAX_AGE_MS = 60_000L;
    /** 连续批次防抖，避免连响。 */
    private static final long MIN_INTERVAL_MS = 800L;

    private static volatile long lastPlayAtMs;

    private ImMsgReminder() {
    }

    /**
     * 收到一批新消息后调用；整批最多响一次。
     */
    public static void onIncomingBatch(@Nullable List<AppMessage> messages) {
        if (messages == null || messages.isEmpty()) {
            return;
        }
        if (MeetCallManager.getInstance().isCallingState() || CallActivity.isInCall()) {
            return;
        }
        String selfId = MMKVBroadUtils.Companion.getRealUserId();
        long now = System.currentTimeMillis();
        for (AppMessage message : messages) {
            if (message == null) {
                continue;
            }
            if (!TextUtils.isEmpty(selfId) && TextUtils.equals(selfId, message.getFromId())) {
                continue;
            }
            String sessionId = resolveSessionId(message);
            if (TextUtils.isEmpty(sessionId)) {
                continue;
            }
            // 正在该会话聊天页：不提醒（对齐未读角标逻辑）
            if (ImActiveChatSession.isActive(sessionId)) {
                continue;
            }
            if (isSessionMuted(sessionId)) {
                continue;
            }
            if (isTooOld(message, now)) {
                continue;
            }
            play(now);
            return;
        }
    }

    private static void play(long now) {
        if (now - lastPlayAtMs < MIN_INTERVAL_MS) {
            return;
        }
        lastPlayAtMs = now;
        Context context = UtilBroadApplication.cxt;
        TiXingBroadUtils.Companion.imMsgReminder(context);
    }

    private static boolean isSessionMuted(@Nullable String sessionId) {
        SessionItem session = ImSessionRepository.getInstance().queryBySessionId(sessionId);
        return session != null && session.isMuteNotify();
    }

    private static boolean isTooOld(@Nullable AppMessage message, long nowMs) {
        if (message == null || message.getTimestamp() <= 0L) {
            return false;
        }
        long millis = WeChatUiHelper.normalizeToMillis(message.getTimestamp());
        return millis > 0L && (nowMs - millis) > MAX_AGE_MS;
    }

    @Nullable
    private static String resolveSessionId(@Nullable AppMessage message) {
        if (message == null) {
            return null;
        }
        if (!TextUtils.isEmpty(message.getConversationId())) {
            return message.getConversationId();
        }
        if (!TextUtils.isEmpty(message.getGroupId())) {
            return message.getGroupId();
        }
        return null;
    }
}
