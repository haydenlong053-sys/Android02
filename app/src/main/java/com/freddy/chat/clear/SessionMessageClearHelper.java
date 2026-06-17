package com.freddy.chat.clear;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.chat.im.MessageProcessor;
import com.freddy.im.db.ImLocalStore;
import com.freddy.im.util.ImUtils;

/**
 * 自研 IM 清空会话聊天记录（仅本地，保留会话行与同步高水位）。
 */
public final class SessionMessageClearHelper {

    private SessionMessageClearHelper() {
    }

    public static void clearP2pMessages(@NonNull String currentUserId,
                                        @NonNull String peerUserId,
                                        @Nullable Runnable onComplete) {
        String sessionId = ImUtils.getP2pSessionIdId(currentUserId, peerUserId);
        clearSessionMessages(sessionId, onComplete);
    }

    public static void clearSessionMessages(@Nullable String sessionId,
                                            @Nullable Runnable onComplete) {
        if (TextUtils.isEmpty(sessionId)) {
            if (onComplete != null) {
                onComplete.run();
            }
            return;
        }
        ImLocalStore.clearSessionMessages(sessionId, () -> {
            MessageProcessor.getInstance().notifySessionMessagesCleared(sessionId);
            if (onComplete != null) {
                onComplete.run();
            }
        });
    }
}
