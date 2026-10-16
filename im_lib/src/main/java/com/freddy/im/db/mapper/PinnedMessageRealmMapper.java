package com.freddy.im.db.mapper;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.MessageType;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.db.entity.PinnedMessageEntity;
import com.freddy.im.http.model.im.GroupPinnedMessageRes;
import com.freddy.im.http.model.im.PinnedMessageItem;

public final class PinnedMessageRealmMapper {

    private PinnedMessageRealmMapper() {
    }

    @NonNull
    public static PinnedMessageItem fromRes(@NonNull String groupId, @NonNull GroupPinnedMessageRes res) {
        PinnedMessageItem item = new PinnedMessageItem();
        item.setGroupId(groupId);
        item.setMessageId(res.getMessageId());
        item.setSender(res.getSender());
        item.setContentType(res.getContentType());
        item.setContent(res.getContent());
        item.setCustomInfo(res.getCustomInfo());
        item.setSendTime(res.getSendTime());
        item.setPinnedBy(res.getPinnedBy());
        item.setPinTime(res.getPinTime());
        return item;
    }

    /**
     * 填充非主键字段。{@code pinKey} 须在 {@code Realm#createObject(..., primaryKey)} 时已设定，不可再改。
     */
    public static void fillEntity(@NonNull PinnedMessageEntity entity, @NonNull PinnedMessageItem item) {
        String groupId = item.getGroupId() != null ? item.getGroupId() : "";
        String messageId = item.getMessageId() != null ? item.getMessageId() : "";
        entity.setGroupId(groupId);
        entity.setMessageId(messageId);
        entity.setSender(item.getSender());
        entity.setContentType(item.getContentType());
        entity.setContent(item.getContent());
        entity.setCustomInfo(item.getCustomInfo());
        entity.setSendTime(item.getSendTime());
        entity.setPinnedBy(item.getPinnedBy());
        entity.setPinTime(item.getPinTime());
    }

    @NonNull
    public static PinnedMessageItem toItem(@NonNull PinnedMessageEntity entity) {
        PinnedMessageItem item = new PinnedMessageItem();
        item.setGroupId(entity.getGroupId());
        item.setMessageId(entity.getMessageId());
        item.setSender(entity.getSender());
        item.setContentType(entity.getContentType());
        item.setContent(entity.getContent());
        item.setCustomInfo(entity.getCustomInfo());
        item.setSendTime(entity.getSendTime());
        item.setPinnedBy(entity.getPinnedBy());
        item.setPinTime(entity.getPinTime());
        return item;
    }

    /** 映射为可复用聊天气泡的 {@link AppMessage}。 */
    @NonNull
    public static AppMessage toAppMessage(@NonNull PinnedMessageItem item) {
        AppMessage message = new AppMessage();
        String groupId = item.getGroupId() != null ? item.getGroupId() : "";
        message.setConversationType(MessageType.GROUP_CHAT.getMsgType());
        message.setConversationId(groupId);
        message.setGroupId(groupId);
        message.setToId(groupId);
        message.setMsgId(item.getMessageId());
        message.setClientMsgId(item.clientMsgId());
        message.setFromId(item.getSender());
        message.setContent(item.getContent());
        message.setCustomInfo(item.getCustomInfo());
        message.setContentType(item.getContentType());
        message.setMsgContentType(parseContentType(item.getContentType()));
        message.setTimestamp(item.getSendTime());
        message.setMsgType(1);
        message.setSendStatus(0);
        message.setStatus(0);
        return message;
    }

    private static int parseContentType(@Nullable String contentType) {
        if (TextUtils.isEmpty(contentType)) {
            return MessageType.MessageContentType.TEXT.getMsgContentType();
        }
        try {
            return Integer.parseInt(contentType.trim());
        } catch (NumberFormatException ignored) {
            return MessageType.MessageContentType.TEXT.getMsgContentType();
        }
    }
}
