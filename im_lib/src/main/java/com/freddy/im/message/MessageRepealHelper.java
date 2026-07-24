package com.freddy.im.message;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.bean.AppMessage;

/**
 * 消息业务状态：0 正常 / 1 撤回。
 * 撤回后保留原 content/msgType，仅改 status，展示层按 status 渲染「撤回了一条消息」。
 */
public final class MessageRepealHelper {

    /** 正常 */
    public static final int STATUS_NORMAL = 0;
    /** 已撤回 */
    public static final int STATUS_RECALLED = 1;

    private MessageRepealHelper() {
    }

    public static boolean isRecalled(@Nullable AppMessage message) {
        return message != null && message.getStatus() == STATUS_RECALLED;
    }

    public static boolean isRecalled(int status) {
        return status == STATUS_RECALLED;
    }

    @NonNull
    public static String buildRecallContent(@Nullable String fromId) {
        if (TextUtils.isEmpty(fromId)) {
            return "{\"msg\":\"撤回了一条消息\"}";
        }
        return "{\"msg\":\"${" + fromId + "}撤回了一条消息\"}";
    }

    /**
     * 撤回：保留原消息内容，仅标记 status=1，并清零 sendStatus / readPersonCount。
     */
    @NonNull
    public static AppMessage toRecalledMessage(@NonNull AppMessage original) {
        AppMessage recalled = copyMessage(original);
        recalled.setStatus(STATUS_RECALLED);
        recalled.setSendStatus(0);
        recalled.setReadPersonCount(0);
        return recalled;
    }

    /** @deprecated 使用 {@link #toRecalledMessage(AppMessage)} */
    @Deprecated
    @NonNull
    public static AppMessage toRecallNotification(@NonNull AppMessage original) {
        return toRecalledMessage(original);
    }

    @NonNull
    private static AppMessage copyMessage(@NonNull AppMessage source) {
        AppMessage target = new AppMessage();
        target.setConversationType(source.getConversationType());
        target.setConversationId(source.getConversationId());
        target.setMsgId(source.getMsgId());
        target.setClientMsgId(source.getClientMsgId());
        target.setMsgType(source.getMsgType());
        target.setSendType(source.getSendType());
        target.setMsgContentType(source.getMsgContentType());
        target.setFromId(source.getFromId());
        target.setToId(source.getToId());
        target.setTimestamp(source.getTimestamp());
        target.setSequenceId(source.getSequenceId());
        target.setStatusReport(source.getStatusReport());
        target.setExtend(source.getExtend());
        target.setCustomInfo(source.getCustomInfo());
        target.setExtra(source.getExtra());
        target.setContent(source.getContent());
        target.setSendStatus(source.getSendStatus());
        target.setReadPersonCount(source.getReadPersonCount());
        target.setStatus(source.getStatus());
        return target;
    }

    /** 解析 HTTP / JSON 字符串 status。 */
    public static int parseStatus(@Nullable String status) {
        if (TextUtils.isEmpty(status)) {
            return STATUS_NORMAL;
        }
        try {
            return Integer.parseInt(status.trim());
        } catch (NumberFormatException ignored) {
            return STATUS_NORMAL;
        }
    }
}
