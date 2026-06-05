package com.freddy.chat.im;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.IMSConfig;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.message.ImMessageManager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 当前会话消息已读展示状态（单聊：对方已读游标；群聊：各消息已读人数）。
 */
public final class MessageReadUiState {

    private long peerReadUpToSeq = -1L;
    private final Map<String, Integer> groupReadCountByMsgId = new HashMap<>();

    public void setPeerReadUpToSeq(long seq) {
        if (seq > peerReadUpToSeq) {
            peerReadUpToSeq = seq;
        }
    }

    public void applyPeerRead(@NonNull String messageId) {
        long seq = ImMessageManager.extractTrailingSequence(messageId);
        if (seq == ImMessageManager.INVALID_SEQUENCE) {
            return;
        }
        setPeerReadUpToSeq(seq);
    }

    public void applyGroupReadCount(@NonNull String messageId, int readPersonCount) {
        if (TextUtils.isEmpty(messageId) || readPersonCount < 0) {
            return;
        }
        groupReadCountByMsgId.put(messageId, readPersonCount);
    }

    public void seedFromMessages(@Nullable List<AppMessage> messages) {
        if (messages == null) {
            return;
        }
        for (AppMessage message : messages) {
            if (message == null) {
                continue;
            }
            int count = message.getReadPersonCount();
            String msgId = message.getMsgId();
            if (count > 0 && !TextUtils.isEmpty(msgId)) {
                groupReadCountByMsgId.put(msgId, count);
            }
        }
    }

    public boolean shouldShowPeerRead(@NonNull AppMessage message,
                                      @Nullable String currentUserId) {
        if (!message.isSender(currentUserId)) {
            return false;
        }
        if (!isDelivered(message)) {
            return false;
        }
        long seq = effectiveSequence(message);
        return peerReadUpToSeq >= 0 && seq > 0 && seq <= peerReadUpToSeq;
    }

    /**
     * @return 0–100；无数据返回 -1
     */
    public int getGroupReadPercent(@NonNull AppMessage message,
                                   @Nullable String currentUserId,
                                   int groupMemberCount) {
        if (!message.isSender(currentUserId)) {
            return -1;
        }
        if (!isDelivered(message)) {
            return -1;
        }
        String msgId = message.getMsgId();
        if (TextUtils.isEmpty(msgId)) {
            return -1;
        }
        Integer count = groupReadCountByMsgId.get(msgId);
        if (count == null) {
            count = message.getReadPersonCount();
        }
        if (count <= 0) {
            return -1;
        }
        int total = Math.max(groupMemberCount, 1);
        int percent = (int) Math.round(count * 100.0 / total);
        return Math.min(100, Math.max(0, percent));
    }

    private static boolean isDelivered(@NonNull AppMessage message) {
        int status = message.getSendStatus();
        return status != IMSConfig.SEND_STATUS_SENDING
                && status != IMSConfig.SEND_STATUS_FAILED;
    }

    private static long effectiveSequence(@NonNull AppMessage message) {
        long seq = message.getSequenceId();
        if (seq > 0) {
            return seq;
        }
        return ImMessageManager.extractTrailingSequence(message.getMsgId());
    }

}
