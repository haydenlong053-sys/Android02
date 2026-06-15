package com.freddy.chat.menu;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cboda.chatui.BroadConfig;
import com.freddy.chat.utils.WeChatUiHelper;
import com.freddy.im.IMSConfig;
import com.freddy.im.MessageType;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.message.MessageContentFormatter;

/**
 * 长按菜单项可见性（对齐野火 {@code contextMenuItemFilter} 与 ConversationMessageBroadAdapter 逻辑）。
 */
public final class MessageMenuPolicy {

    private MessageMenuPolicy() {
    }

    public static boolean supportsContextMenu(@Nullable AppMessage message) {
        return message != null && !WeChatUiHelper.isSystemMessage(message);
    }

    public static boolean canRecall(@NonNull AppMessage message, @Nullable String currentUserId) {
        if (!message.isSender(currentUserId)) {
            return false;
        }
        if (TextUtils.isEmpty(message.getMsgId())) {
            return false;
        }
        if (message.getSendStatus() == IMSConfig.SEND_STATUS_SENDING) {
            return false;
        }
        long millis = WeChatUiHelper.normalizeToMillis(message.getTimestamp());
        if (millis <= 0L) {
            return false;
        }
        long limitMs = BroadConfig.RECALL_TIME_LIMIT * 1000L;
        return System.currentTimeMillis() - millis < limitMs;
    }

    public static boolean canCopy(@NonNull AppMessage message) {
        return MessageType.MessageContentType.isText(message.getMsgContentType());
    }

    public static boolean canForward(@NonNull AppMessage message) {
        if (TextUtils.isEmpty(message.getMsgId())) {
            return false;
        }
        int type = message.getMsgContentType();
        return MessageContentFormatter.isImageMessage(type)
                || MessageContentFormatter.isVoiceMessage(type)
                || MessageContentFormatter.isVideoMessage(type)
                || MessageContentFormatter.isFileMessage(type)
                || MessageContentFormatter.isMapMessage(type)
                || MessageContentFormatter.isCardMessage(type)
                || MessageContentFormatter.isNoticeMessage(type)
                || MessageType.MessageContentType.isText(type);
    }

    /** 对齐野火长按「收藏」：文本 / 图片 / 视频 / 语音（需有可收藏内容）。 */
    public static boolean canCollect(@NonNull AppMessage message) {
        return !TextUtils.isEmpty(com.freddy.chat.collect.MessageCollectHelper.buildCollectParams(message));
    }

    public static boolean canMultiSelect(@NonNull AppMessage message) {
        return supportsContextMenu(message);
    }

    public static boolean canDelete(@NonNull AppMessage message) {
        return supportsContextMenu(message);
    }
}
