package com.freddy.chat.im;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.chat.utils.WeChatUiHelper;
import com.freddy.im.IMSConfig;
import com.freddy.im.MessageType;
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

    /** 是否展示已读回执（对齐野火 Persist_And_Count 类消息）。 */
    public static boolean supportsReadReceipt(@NonNull AppMessage message) {
        if (WeChatUiHelper.isSystemMessage(message)) {
            return false;
        }
        int type = message.getMsgContentType();
        return type == MessageType.MessageContentType.TEXT.getMsgContentType()
                || WeChatUiHelper.isImageMessage(type)
                || WeChatUiHelper.isVoiceMessage(type)
                || WeChatUiHelper.isVideoMessage(type)
                || WeChatUiHelper.isFileMessage(type);
    }

    /**
     * 群聊已读人数；不可展示时返回 -1。
     */
    public int getGroupReadCount(@NonNull AppMessage message,
                                 @Nullable String currentUserId) {
        if (!message.isSender(currentUserId) || !isDelivered(message)) {
            return -1;
        }
        String msgId = message.getMsgId();
        Integer count = null;
        if (!TextUtils.isEmpty(msgId)) {
            count = groupReadCountByMsgId.get(msgId);
        }
        if (count == null) {
            count = message.getReadPersonCount();
        }
        return Math.max(0, count);
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
