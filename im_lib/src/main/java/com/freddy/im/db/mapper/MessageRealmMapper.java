package com.freddy.im.db.mapper;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.IMSConfig;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.bean.ImMessageMapper;
import com.freddy.im.db.entity.MessageEntity;

/**
 * {@link AppMessage}（内存/回调 POJO）与 {@link MessageEntity}（Realm）互转。
 */
public final class MessageRealmMapper {

    private MessageRealmMapper() {
    }

    /**
     * Realm 主键：优先 {@link AppMessage#getClientMsgId()}；
     * 服务端拉取无 clientMsgId 时退化为 {@link AppMessage#getMsgId()}。
     */
    @Nullable
    public static String clientMsgIdForPersist(@NonNull AppMessage message) {
        if (!TextUtils.isEmpty(message.getClientMsgId())) {
            return message.getClientMsgId();
        }
        if (!TextUtils.isEmpty(message.getMsgId())) {
            return message.getMsgId();
        }
        return null;
    }

    @NonNull
    public static MessageEntity toEntity(@NonNull AppMessage message) {
        MessageEntity entity = new MessageEntity();
        String clientMsgId = clientMsgIdForPersist(message);
        entity.setClientMsgId(clientMsgId);
        entity.setMsgId(message.getMsgId());
        entity.setSessionId(resolveSessionId(message));
        entity.setConversationType(message.getConversationType());
        entity.setMsgType(message.getMsgType());
        entity.setSendType(message.getSendType());
        entity.setMsgContentType(message.getMsgContentType());
        entity.setFromId(message.getFromId());
        entity.setToId(message.getToId());
        entity.setTimestamp(message.getTimestamp());
        entity.setStatusReport(message.getStatusReport());
        entity.setExtend(message.getExtend());
        entity.setCustomInfo(message.getCustomInfo());
        entity.setExtra(message.getExtra());
        entity.setContent(message.getContent());
        entity.setSendStatus(message.getSendStatus());
        entity.setSequenceId(message.getSequenceId());
        entity.setReadPersonCount(message.getReadPersonCount());
        return entity;
    }

    @NonNull
    public static AppMessage toAppMessage(@NonNull MessageEntity entity) {
        AppMessage message = new AppMessage();
        message.setClientMsgId(entity.getClientMsgId());
        message.setMsgId(entity.getMsgId());
        message.setConversationId(entity.getSessionId());
        message.setConversationType(entity.getConversationType());
        message.setMsgType(entity.getMsgType());
        message.setSendType(entity.getSendType());
        message.setMsgContentType(entity.getMsgContentType());
        message.setFromId(entity.getFromId());
        message.setToId(entity.getToId());
        message.setTimestamp(entity.getTimestamp());
        message.setStatusReport(entity.getStatusReport());
        message.setExtend(entity.getExtend());
        message.setCustomInfo(entity.getCustomInfo());
        message.setExtra(entity.getExtra());
        message.setContent(entity.getContent());
        message.setSequenceId(entity.getSequenceId());
        message.setSendStatus(entity.getSendStatus());
        message.setReadPersonCount(entity.getReadPersonCount());
        return message;
    }

    @Nullable
    private static String resolveSessionId(@NonNull AppMessage message) {
        if (!TextUtils.isEmpty(message.getConversationId())) {
            return message.getConversationId();
        }
        String anchorId = !TextUtils.isEmpty(message.getMsgId())
                ? message.getMsgId()
                : message.getClientMsgId();
        return ImMessageMapper.resolveSessionId(null, anchorId);
    }
}
