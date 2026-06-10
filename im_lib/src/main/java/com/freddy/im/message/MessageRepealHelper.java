package com.freddy.im.message;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.bean.AppMessage;

/**
 * 将已撤回消息转为系统提示（对齐野火「XXX撤回了一条消息」展示）。
 */
public final class MessageRepealHelper {

    private MessageRepealHelper() {
    }

    @NonNull
    public static String buildRecallContent(@Nullable String fromId) {
        if (TextUtils.isEmpty(fromId)) {
            return "{\"msg\":\"撤回了一条消息\"}";
        }
        return "{\"msg\":\"${" + fromId + "}撤回了一条消息\"}";
    }

    /**
     * 在原消息位保留序列与 id，仅改为系统提示内容。
     */
    @NonNull
    public static AppMessage toRecallNotification(@NonNull AppMessage original) {
        AppMessage recalled = copyMessage(original);
        recalled.setMsgType(MessageContentFormatter.API_MSG_TYPE_SYSTEM);
        recalled.setMsgContentType(0);
        recalled.setContent(buildRecallContent(original.getFromId()));
        recalled.setSendStatus(0);
        recalled.setReadPersonCount(0);
        return recalled;
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
        return target;
    }
}
