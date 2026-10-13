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
import com.freddy.im.message.MessageRepealHelper;

/**
 * 长按菜单项可见性（对齐野火 {@code contextMenuItemFilter} 与 ConversationMessageBroadAdapter 逻辑）。
 */
public final class MessageMenuPolicy {

    private MessageMenuPolicy() {
    }

    public static boolean supportsContextMenu(@Nullable AppMessage message) {
        return message != null
                && !WeChatUiHelper.isSystemMessage(message)
                && !WeChatUiHelper.isRecalledMessage(message)
                && !WeChatUiHelper.isMeetStatusMessage(message.getMsgContentType());
    }

    /**
     * @param skipTimeLimit 群主/管理员可跳过 {@link BroadConfig#RECALL_TIME_LIMIT} 时限
     *                      （仍仅能撤回自己发出的消息）。
     *                      长按菜单已隐藏撤回入口；本方法仍可供其它逻辑判断。
     */
    public static boolean canRecall(@NonNull AppMessage message,
                                    @Nullable String currentUserId,
                                    boolean skipTimeLimit) {
        if (!message.isSender(currentUserId)) {
            return false;
        }
        if (WeChatUiHelper.isRecalledMessage(message)) {
            return false;
        }
        if (TextUtils.isEmpty(message.getMsgId())) {
            return false;
        }
        if (message.getSendStatus() == IMSConfig.SEND_STATUS_SENDING) {
            return false;
        }
        if (skipTimeLimit) {
            return true;
        }
        long millis = WeChatUiHelper.normalizeToMillis(message.getTimestamp());
        if (millis <= 0L) {
            return false;
        }
        long limitMs = BroadConfig.RECALL_TIME_LIMIT * 1000L;
        return System.currentTimeMillis() - millis < limitMs;
    }

    /**
     * 是否可「对所有人删除」：需已有服务端 messageId（不限发送人、不限时间）。
     */
    public static boolean canDeleteForAll(@NonNull AppMessage message) {
        if (!supportsContextMenu(message)) {
            return false;
        }
        if (TextUtils.isEmpty(message.getMsgId())) {
            return false;
        }
        return message.getSendStatus() != IMSConfig.SEND_STATUS_SENDING;
    }

//    public static boolean canRecall(@NonNull AppMessage message, @Nullable String currentUserId) {
//        return canRecall(message, currentUserId, false);
//    }

    public static boolean canCopy(@NonNull AppMessage message) {
        return MessageType.MessageContentType.isText(message.getMsgContentType());
    }

    /**
     * 是否可编辑：仅自己发出的纯文本（contentType=100），15 分钟内，未撤回。
     * 转发 / 合并不可编辑（对齐文档 msgEdit）。
     */
    public static boolean canEdit(@NonNull AppMessage message, @Nullable String currentUserId) {
        if (!supportsContextMenu(message)) {
            return false;
        }
        if (!message.isSender(currentUserId)) {
            return false;
        }
        if (MessageRepealHelper.isRecalled(message)) {
            return false;
        }
        if (message.getMsgContentType() != MessageType.MessageContentType.TEXT.getMsgContentType()) {
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
        long limitMs = BroadConfig.MESSAGE_EDIT_TIME_LIMIT * 1000L;
        return System.currentTimeMillis() - millis < limitMs;
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

    /** 长按「收藏」入口已隐藏；保留方法供后续恢复。 */
    public static boolean canCollect(@NonNull AppMessage message) {
        return false;
    }

    public static boolean canMultiSelect(@NonNull AppMessage message) {
        return supportsContextMenu(message);
    }

    public static boolean canDelete(@NonNull AppMessage message) {
        return supportsContextMenu(message);
    }
}
