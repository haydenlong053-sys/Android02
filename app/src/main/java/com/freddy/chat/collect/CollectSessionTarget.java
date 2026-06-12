package com.freddy.chat.collect;

import android.content.Intent;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.MessageType;

/**
 * 从收藏页 Intent（uid / qunid）解析当前聊天发送目标。
 */
public final class CollectSessionTarget {

    public final int conversationType;
    @NonNull
    public final String toId;
    @Nullable
    public final String sessionId;

    private CollectSessionTarget(int conversationType, @NonNull String toId, @Nullable String sessionId) {
        this.conversationType = conversationType;
        this.toId = toId;
        this.sessionId = sessionId;
    }

    @Nullable
    public static CollectSessionTarget fromIntent(@Nullable Intent intent) {
        if (intent == null) {
            return null;
        }
        String qunid = intent.getStringExtra(CollectIntent.EXTRA_QUN_ID);
        String uid = intent.getStringExtra(CollectIntent.EXTRA_UID);
        String sessionId = intent.getStringExtra(CollectIntent.EXTRA_SESSION_ID);
        if (!TextUtils.isEmpty(qunid)) {
            return new CollectSessionTarget(
                    MessageType.GROUP_CHAT.getMsgType(), qunid.trim(), sessionId);
        }
        if (!TextUtils.isEmpty(uid)) {
            return new CollectSessionTarget(
                    MessageType.SINGLE_CHAT.getMsgType(), uid.trim(), sessionId);
        }
        return null;
    }

    public boolean isValid() {
        return !TextUtils.isEmpty(toId);
    }
}
