package com.freddy.im.bean;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.http.model.im.ImMessageItem;
import com.freddy.im.message.ImMessageManager;
import com.freddy.im.message.MessageRepealHelper;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * HTTP {@link ImMessageItem} 与 SDK {@link AppMessage} 互转。
 */
public final class ImMessageMapper {

    private ImMessageMapper() {
    }

    @NonNull
    public static AppMessage toAppMessage(@NonNull ImMessageItem item) {
        AppMessage m = new AppMessage();
        m.setFromId(item.getSender());
        m.setSendType(item.getSendType());
        m.setMsgId(item.getMessageId());
        String clientMsgId = item.getClientMsgId();
        if (clientMsgId == null || clientMsgId.isEmpty()) {
            clientMsgId = item.getMessageId();
        }
        m.setClientMsgId(clientMsgId);
        m.setMsgType(item.getMsgType());
        m.setMsgContentType(parseContentType(item.getContentType()));
        m.setCustomInfo(item.getCustomInfo());
        m.setContent(item.getContent());
        m.setTimestamp(item.getSendTime());
        m.setConversationId(resolveSessionId(item.getGroupId(), item.getMessageId()));
        m.setToId(item.getTo());
        m.setSequenceId(item.getSequenceId());
        m.setReadPersonCount(item.getReadPersonCount());
        m.setStatus(MessageRepealHelper.parseStatus(item.getStatus()));
        return m;
    }

    @Nullable
    public static String resolveSessionId(@Nullable String groupId, @Nullable String messageId) {
        if (groupId != null && !groupId.isEmpty()) {
            return groupId;
        }
        return ImMessageManager.extractConversationAnchor(messageId);
    }

    @NonNull
    public static List<AppMessage> toAppMessageList(@Nullable List<ImMessageItem> items) {
        return toAppMessageList(items, false);
    }

    /**
     * @param reverseOrder 为 true 时对结果做倒序（与接口返回顺序相反），常用于聊天列表新消息在上
     */
    @NonNull
    public static List<AppMessage> toAppMessageList(@Nullable List<ImMessageItem> items, boolean reverseOrder) {
        if (items == null || items.isEmpty()) {
            return Collections.emptyList();
        }
        List<AppMessage> list = new ArrayList<>(items.size());
        for (ImMessageItem item : items) {
            if (item != null) {
                list.add(toAppMessage(item));
            }
        }
        if (reverseOrder) {
            Collections.reverse(list);
        }
        return list;
    }

    private static int parseContentType(@Nullable String contentType) {
        if (contentType == null || contentType.isEmpty()) {
            return 0;
        }
        try {
            return Integer.parseInt(contentType.trim());
        } catch (NumberFormatException ignored) {
            return 0;
        }
    }
}
