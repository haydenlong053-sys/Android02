package com.freddy.chat.im;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.chat.event.Events;
import com.freddy.chat.event.GroupMuteUpdateEvent;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.http.model.im.SessionItem;

import java.util.List;

/**
 * <p>@ProjectName:     NettyChat</p>
 * <p>@ClassName:       IMessageProcessor.java</p>
 * <p>@PackageName:     com.freddy.chat.im</p>
 * <b>
 * <p>@Description:     消息处理器接口</p>
 * </b>
 */
public interface IMessageProcessor {

    /**
     * 接收一批消息；{@code messages} 为 null 或空列表时实现类应直接忽略。
     * <p>
     * 事件 {@link Events#CHAT_MESSAGE} 的 {@code obj} 载荷为 {@link List}{@code <AppMessage>}（新列表副本，可批量处理）。
     */
    void receiveMsg(List<AppMessage> messages);
    void notifyConversationList();

    /**
     * 局部会话已更新（如群变更入库）；事件 {@link Events#SESSION_LIST_UPDATED}。
     */
    void notifySessionsUpdated(List<SessionItem> sessions);

    /**
     * 群成员已更新；事件 {@link Events#GROUP_MEMBERS_UPDATED}，payload 为 groupId。
     */
    void notifyGroupMembersUpdated(@NonNull String groupId);

    /**
     * 群成员角色变更（0x0501 code=1004）；事件 {@link Events#GROUP_MEMBERS_ROLE_UPDATE}，payload 为 groupId。
     */
    void notifyGroupMemberRolesUpdated(@NonNull String groupId);

    /**
     * 群公告变更信令（0x0501 code=1001）；事件 {@link Events#GROUP_NOTICE_UPDATED}，payload 为 groupId。
     */
    void notifyGroupNoticeUpdated(@NonNull String groupId);

    /**
     * 群置顶消息变更（0x0501 code=1010 或 HTTP 成功后重拉）；事件 {@link Events#GROUP_PINNED_UPDATED}。
     */
    void notifyGroupPinnedUpdated(@NonNull String groupId);

    /**
     * 群禁言状态变更；事件 {@link Events#GROUP_MUTE_UPDATED}，payload 为 {@link GroupMuteUpdateEvent}。
     */
    void notifyGroupMuteUpdated(@NonNull String groupId);

    /**
     * 0x0501 code=1002 等场景：payload 携带 {@code allMute}（0/1），UI 可直接应用。
     */
    void notifyGroupMuteUpdated(@NonNull String groupId, int allMute);

    /**
     * 0x0501 code=1003：指定成员禁言变更（推送给被操作成员），payload 携带 {@code mute}（0/1）。
     */
    void notifyGroupMemberMuteUpdated(@NonNull String groupId, int mute);

    /**
     * 本地会话已删除；事件 {@link Events#SESSION_REMOVED}，payload 为 sessionId。
     */
    void notifySessionRemoved(@NonNull String sessionId);

    /**
     * 发送消息：落库成功后在主线程无 UI 回调，再后台发信令。
     */
    void sendMsg(AppMessage message);

    /**
     * 发送消息：落库成功后先在主线程执行 {@code onUiAfterOutgoingPersisted}（列表刷新等），再后台发信令。
     */
    void sendMsg(@NonNull AppMessage message, @Nullable Runnable onUiAfterOutgoingPersisted);

    /**
     * 落库并刷新 UI；{@code transmitImmediately=false} 时仅本地展示，待上传完成后再 {@link #transmitOutgoingMessage}。
     */
    void sendMsg(@NonNull AppMessage message,
                 @Nullable Runnable onUiAfterOutgoingPersisted,
                 boolean transmitImmediately);

    /** 上传完成后发送 IM 信令（消息需已落库）。 */
    void transmitOutgoingMessage(@NonNull AppMessage message);

    /** 单聊 0x0207：对方已读游标推进。 */
    void notifyPeerMessageRead(@NonNull String messageId);

    /** 群聊 0x0210：消息已读人数变更。 */
    void notifyGroupMessageReadCount(@NonNull String messageId, int readPersonCount);

    /** 0x0202 / HTTP repeal：消息已撤回并替换为系统提示。 */
    void notifyMessageRepealed(@NonNull String messageId, @Nullable AppMessage recalledMessage);

    /** 消息内容/状态变更（如编辑）；事件 {@link Events#CHAT_MESSAGE_UPDATE}。 */
    void notifyMessageUpdated(@NonNull AppMessage message);

    /** 本地消息已删除。 */
    void notifyMessagesDeleted(@NonNull List<AppMessage> deletedMessages);

    /** 会话本地消息已清空。 */
    void notifySessionMessagesCleared(@NonNull String sessionId);
}
