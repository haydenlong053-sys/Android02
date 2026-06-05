package com.freddy.im.message;

import android.text.TextUtils;

import androidx.annotation.Nullable;

/**
 * 当前唯一打开的消息列表页所属会话（im_lib 层记录，供未读数逻辑使用）。
 */
public final class ImActiveChatSession {

    @Nullable
    private static volatile String activeSessionId;

    private ImActiveChatSession() {
    }

    public static void setActive(@Nullable String sessionId) {
        activeSessionId = TextUtils.isEmpty(sessionId) ? null : sessionId.trim();
    }

    public static void clearActive() {
        activeSessionId = null;
    }

    @Nullable
    public static String getActiveSessionId() {
        return activeSessionId;
    }

    public static boolean isActive(@Nullable String sessionId) {
        return !TextUtils.isEmpty(sessionId)
                && !TextUtils.isEmpty(activeSessionId)
                && activeSessionId.equals(sessionId);
    }
}
