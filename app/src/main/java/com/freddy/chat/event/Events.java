package com.freddy.chat.event;

/**
 * Created by
 */
public class Events {

    public static final String CHAT_MESSAGE = "chat_message";
    public static final String CHAT_MESSAGE_UPDATE = "chat_message_update";
    //全量刷新会话列表的通知
    public static final String NOTIFY_CONVERSATION_LIST = "notify_ConversationList";
    /** 群变更等场景：局部会话列表已入库，payload 为 {@link java.util.List}{@code <com.freddy.im.http.model.im.SessionItem>} */
    public static final String SESSION_LIST_UPDATED = "session_list_updated";
    /** 群成员变更入库；payload 为 {@code groupId}（String） */
    public static final String GROUP_MEMBERS_UPDATED = "group_members_updated";
    /** 群成员角色变更（0x0501 code=1004 设/撤管理员）；payload 为 {@code groupId}（String） */
    public static final String GROUP_MEMBERS_ROLE_UPDATE = "group_members_role_update";
    /** 本地会话已删除；payload 为 {@code sessionId}（String） */
    public static final String SESSION_REMOVED = "session_removed";
    /** 消息已读状态变更；payload 为 {@link MessageReadStateEvent} */
    public static final String MESSAGE_READ_STATE_CHANGED = "message_read_state_changed";
    /** 消息撤回；payload 为 {@link MessageRepealEvent} */
    public static final String MESSAGE_REPEAL = "message_repeal";
    /** 本地消息删除；payload 为 {@link MessageDeletedEvent} */
    public static final String MESSAGE_DELETED = "message_deleted";
    /** 会话消息已清空；payload 为 {@code sessionId}（String） */
    public static final String SESSION_MESSAGES_CLEARED = "session_messages_cleared";
    /** 群公告变更信令（0x0501 code=1001）；payload 为 {@code groupId}（String） */
    public static final String GROUP_NOTICE_UPDATED = "group_notice_updated";
    /** 群置顶消息变更（0x0501 code=1010 / HTTP 成功后）；payload 为 {@code groupId}（String） */
    public static final String GROUP_PINNED_UPDATED = "group_pinned_updated";
    /** 群禁言状态变更；payload 为 {@link GroupMuteUpdateEvent} */
    public static final String GROUP_MUTE_UPDATED = "group_mute_updated";
}
