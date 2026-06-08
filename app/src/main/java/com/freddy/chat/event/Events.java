package com.freddy.chat.event;

/**
 * Created by
 */
public class Events {

    public static final String CHAT_MESSAGE = "chat_message";
    public static final String CHAT_MESSAGE_UPDATE = "chat_message_update";
    public static final String NOTIFY_CONVERSATION_LIST = "notify_ConversationList";
    /** 群变更等场景：局部会话列表已入库，payload 为 {@link java.util.List}{@code <com.freddy.im.http.model.im.SessionItem>} */
    public static final String SESSION_LIST_UPDATED = "session_list_updated";
    /** 群成员变更入库；payload 为 {@code groupId}（String） */
    public static final String GROUP_MEMBERS_UPDATED = "group_members_updated";
    /** 本地会话已删除；payload 为 {@code sessionId}（String） */
    public static final String SESSION_REMOVED = "session_removed";
    /** 消息已读状态变更；payload 为 {@link MessageReadStateEvent} */
    public static final String MESSAGE_READ_STATE_CHANGED = "message_read_state_changed";
//    public static final String CHAT_GROUP_MESSAGE = "chat_group_message";
}
