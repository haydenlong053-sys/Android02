package com.freddy.chat.im;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.chat.event.Events;
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
     * 群公告变更信令（0x0501 code=1001）；事件 {@link Events#GROUP_NOTICE_UPDATED}，payload 为 groupId。
     */
    void notifyGroupNoticeUpdated(@NonNull String groupId);

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

    /** 本地消息已删除。 */
    void notifyMessagesDeleted(@NonNull List<AppMessage> deletedMessages);

    /** 会话本地消息已清空。 */
    void notifySessionMessagesCleared(@NonNull String sessionId);
}
