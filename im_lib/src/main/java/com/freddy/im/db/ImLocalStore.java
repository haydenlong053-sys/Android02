package com.freddy.im.db;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.bean.AppMessage;
import com.freddy.im.bean.ImConversationSearchResult;
import com.freddy.im.db.entity.MessageEntity;
import com.freddy.im.db.entity.SessionEntity;
import com.freddy.im.db.repository.ImGroupMemberRepository;
import com.freddy.im.db.repository.ImMessageRepository;
import com.freddy.im.db.repository.ImSessionRepository;
import com.freddy.im.group.GroupRole;
import com.freddy.im.http.model.im.GroupMemberItem;
import com.freddy.im.http.model.im.SessionItem;

import java.util.HashMap;
import java.util.Map;

import java.util.ArrayList;
import java.util.List;

/**
 * SDK 本地库统一入口：Realm 初始化与消息/会话持久化。
 * <p>
 * 分层：HTTP DTO / {@link AppMessage} POJO / Realm Entity 分离，经 Repository 写入。
 */
public final class ImLocalStore {

    private static final Handler MAIN_HANDLER = new Handler(Looper.getMainLooper());

    @Nullable
    private static volatile OnSessionPreviewUpdatedListener sessionPreviewUpdatedListener;

    @Nullable
    private static volatile OnGroupMembersUpdatedListener groupMembersUpdatedListener;

    @Nullable
    private static volatile OnSessionRemovedListener sessionRemovedListener;

    private ImLocalStore() {
    }

    /**
     * 消息落库后会话最近一条摘要已更新时回调（主线程）。
     */
    public interface OnSessionPreviewUpdatedListener {
        void onSessionPreviewUpdated(@NonNull List<SessionItem> sessions);
    }

    /**
     * 群成员入库或软删除后回调（主线程），payload 为 {@code groupId}。
     */
    public interface OnGroupMembersUpdatedListener {
        void onGroupMembersUpdated(@NonNull String groupId);
    }

    /**
     * 本地会话（及消息、群成员缓存）已删除时回调（主线程），payload 为 {@code sessionId}。
     */
    public interface OnSessionRemovedListener {
        void onSessionRemoved(@NonNull String sessionId);
    }

    public static void setOnSessionPreviewUpdatedListener(
            @Nullable OnSessionPreviewUpdatedListener listener) {
        sessionPreviewUpdatedListener = listener;
    }

    public static void setOnGroupMembersUpdatedListener(
            @Nullable OnGroupMembersUpdatedListener listener) {
        groupMembersUpdatedListener = listener;
    }

    public static void setOnSessionRemovedListener(@Nullable OnSessionRemovedListener listener) {
        sessionRemovedListener = listener;
    }

    public static void dispatchGroupMembersUpdated(@NonNull String groupId) {
        if (TextUtils.isEmpty(groupId)) {
            return;
        }
        OnGroupMembersUpdatedListener listener = groupMembersUpdatedListener;
        if (listener == null) {
            return;
        }
        final String id = groupId;
        MAIN_HANDLER.post(() -> listener.onGroupMembersUpdated(id));
    }

    public static void dispatchSessionRemoved(@NonNull String sessionId) {
        if (TextUtils.isEmpty(sessionId)) {
            return;
        }
        OnSessionRemovedListener listener = sessionRemovedListener;
        if (listener == null) {
            return;
        }
        final String id = sessionId;
        MAIN_HANDLER.post(() -> listener.onSessionRemoved(id));
    }

    public static void dispatchSessionPreviewUpdated(@NonNull List<SessionItem> sessions) {
        if (sessions.isEmpty()) {
            return;
        }
        OnSessionPreviewUpdatedListener listener = sessionPreviewUpdatedListener;
        if (listener == null) {
            return;
        }
        final List<SessionItem> copy = new ArrayList<>(sessions);
        MAIN_HANDLER.post(() -> listener.onSessionPreviewUpdated(copy));
    }

    /**
     * 在 Application#onCreate 与 EasyHttp 一同初始化（仅 Realm SDK，不绑定用户）。
     */
    public static void init(@NonNull Context context) {
        ImRealm.init(context);
    }

    /**
     * 登录/切号：打开该用户独立库 {@code im_sdk_<userId>.realm}。
     */
    public static void openForUser(@NonNull String userId) {
        ImRealm.openForUser(userId);
    }

    /**
     * 登出：关闭当前用户库。
     */
    public static void closeForUser() {
        ImRealm.closeForUser();
    }

    @Nullable
    public static String getCurrentUserId() {
        return ImRealm.getCurrentUserId();
    }

    /**
     * SDK 已初始化且已 {@link #openForUser(String)}。
     */
    public static boolean isReady() {
        return ImRealm.isUserBound();
    }

    // ---------- 消息 ----------

    /** 消息列表默认分页大小（与 {@link com.freddy.im.db.repository.ImMessageRepository#DEFAULT_MESSAGE_PAGE_SIZE} 一致） */
    public static final int MESSAGE_PAGE_SIZE = ImMessageRepository.DEFAULT_MESSAGE_PAGE_SIZE;


    /**
     * 批量落库；事务<strong>成功提交后</strong>在 Realm 写线程执行 {@code afterCommit}（失败不执行）。
     * 适合在 {@code afterCommit} 里再调用 {@code MessageProcessor#receiveMsg}，避免 UI 早于 DB 可见。
     */
    public static void saveMessagesThen(@Nullable List<AppMessage> messages, @Nullable Runnable afterCommit) {
        ImMessageRepository.getInstance().insertOrUpdateBatchThen(messages, afterCommit);
    }


    /**
     * 发送前落库；事务提交成功后在 Realm 写线程调用 {@code onCommitSuccess}（可为 null）。
     */
    public static void saveOutgoingMessage(@NonNull AppMessage message,
                                           @Nullable Runnable onCommitSuccess) {
        ImMessageRepository.getInstance().insertOutgoing(message, onCommitSuccess);
    }

    public static void markOutgoingSendSuccess(@NonNull String clientMsgId,
                                               @NonNull String serverMsgId,
                                               @Nullable ImMessageRepository.OnMessageUpdatedCallback onUpdated) {
        ImMessageRepository.getInstance().markOutgoingSendSuccess(clientMsgId, serverMsgId, onUpdated);
    }

    public static void updateOutgoingMessage(@NonNull AppMessage message,
                                             @Nullable ImMessageRepository.OnMessageUpdatedCallback onUpdated) {
        ImMessageRepository.getInstance().updateOutgoingMessage(message, onUpdated);
    }

    public static void markOutgoingSendFailed(@Nullable String clientMsgId,
                                            @Nullable ImMessageRepository.OnMessageUpdatedCallback onUpdated) {
        ImMessageRepository.getInstance().markOutgoingSendFailed(clientMsgId, onUpdated);
    }

    public static void markOutgoingResending(@Nullable String clientMsgId,
                                             @Nullable ImMessageRepository.OnMessageUpdatedCallback onUpdated) {
        ImMessageRepository.getInstance().markOutgoingResending(clientMsgId, onUpdated);
    }

    /** 批量删除本地消息；删到会话最后一条时重算摘要。 */
    public static void deleteMessages(@Nullable List<AppMessage> messages,
                                      @Nullable ImMessageRepository.OnMessagesDeletedCallback onDeleted) {
        ImMessageRepository.getInstance().deleteMessages(messages, onDeleted);
    }

    /** 清空会话本地消息（保留会话与同步高水位，不回退增量锚点）。 */
    public static void clearSessionMessages(@Nullable String sessionId,
                                            @Nullable Runnable afterCommit) {
        ImMessageRepository.getInstance().clearMessagesBySessionId(sessionId, afterCommit);
    }

    /** 批量删除本地消息（仅删库，不通知 UI）。 */
    @Deprecated
    public static void deleteMessagesByClientMsgIds(@Nullable List<String> clientMsgIds,
                                                    @Nullable Runnable onCommit) {
        ImMessageRepository.getInstance().deleteByClientMsgIds(clientMsgIds, onCommit);
    }

    /** 按服务端 msgId 将本地消息替换为撤回系统提示。 */
    public static void applyMessageRepeal(@NonNull String msgId,
                                          @Nullable ImMessageRepository.OnMessageUpdatedCallback onUpdated) {
        ImMessageRepository.getInstance().applyRecallByMsgId(msgId, onUpdated);
    }

    /**
     * 加载会话「最新」一页本地消息，最多 {@link #MESSAGE_PAGE_SIZE} 条（按 {@code sequenceId} 升序展示）。
     * <p>
     * 禁止在主线程调用；请使用 {@link #loadMessages(String, OnLoadCallback)}。
     */
    @NonNull
    private static List<AppMessage> loadMessages(@Nullable String sessionId) {
        return ImMessageRepository.getInstance().queryLatestPageBySessionId(sessionId, MESSAGE_PAGE_SIZE);
    }

    /** 后台读库，结果在主线程 {@link OnLoadCallback#onLoaded}。 */
    public static void loadMessages(@Nullable String sessionId,
                                    @NonNull OnLoadCallback<List<AppMessage>> callback) {
        ImDbExecutor.runRead(() -> {
            List<AppMessage> list = loadMessages(sessionId);
            MAIN_HANDLER.post(() -> callback.onLoaded(list));
        });
    }

    /**
     * 加载比 {@code boundarySequenceId} 更旧的一页，最多 {@code limit} 条（升序）。
     */
    @NonNull
    public static List<AppMessage> loadMessagesOlderThan(@Nullable String sessionId,
                                                         long boundarySequenceId,
                                                         int limit) {
        return ImMessageRepository.getInstance()
                .queryOlderPageBySessionId(sessionId, boundarySequenceId, limit);
    }

    /**
     * 禁止在主线程调用；仅供 {@link com.freddy.im.message.ImMessageManager} 在 Realm 线程内使用。
     */
    @Nullable
    public static MessageEntity queryMaxSequenceIdBySessionId(@Nullable String sessionId) {
        return ImMessageRepository.getInstance().queryMaxSequenceIdBySessionId(sessionId);
    }

    /** 增量拉取锚点 messageId（同步高水位，本地删除不回退）。 */
    @Nullable
    public static String resolveMessageSyncAnchor(@Nullable String sessionId) {
        return ImSessionRepository.getInstance().resolveMessageSyncAnchor(sessionId);
    }

    @Nullable
    public static SessionEntity querySessionEntity(@Nullable String sessionId) {
        return ImSessionRepository.getInstance().queryEntityBySessionId(sessionId);
    }

    /** 单聊：落库对方已读游标，提交后在当前线程执行 {@code afterCommit}。 */
    public static void persistPeerMessageRead(@Nullable String sessionId,
                                              @NonNull String messageId,
                                              @Nullable Runnable afterCommit) {
        ImSessionRepository.getInstance().advancePeerReadUpToSeq(sessionId, messageId, afterCommit);
    }

    /** 群聊：落库消息已读人数。 */
    public static void persistGroupMessageReadCount(@NonNull String messageId,
                                                    int readPersonCount,
                                                    @Nullable Runnable afterCommit) {
        ImMessageRepository.getInstance().updateReadPersonCountByMsgId(
                messageId, readPersonCount, afterCommit);
    }

    /** 单聊：读取对方已读游标（后台线程）。 */
    public static void loadPeerReadUpToSeq(@Nullable String sessionId,
                                           @NonNull OnLoadCallback<Long> callback) {
        ImDbExecutor.runRead(() -> {
            long seq = ImSessionRepository.getInstance().queryPeerReadUpToSeq(sessionId);
            MAIN_HANDLER.post(() -> callback.onLoaded(seq));
        });
    }

    // ---------- 会话 ----------

    public static void saveSessions(@Nullable List<SessionItem> sessions) {
        ImSessionRepository.getInstance().upsertAll(sessions);
    }

    /**
     * 批量 upsert 会话；事务提交后在 Realm 写线程执行 {@code afterCommit}。
     */
    public static void saveSessionsThen(@Nullable List<SessionItem> sessions,
                                        @Nullable Runnable afterCommit) {
        ImSessionRepository.getInstance().upsertAllThen(sessions, afterCommit);
    }

    public static void deleteSession(@Nullable String sessionId) {
        removeSessionCompletely(sessionId, null);
    }

    /**
     * 删除本地会话及其消息、群成员缓存；完成后在主线程执行 {@code afterCommit} 并通知 UI。
     */
    public static void removeSessionCompletely(@Nullable String sessionId,
                                               @Nullable Runnable afterCommit) {
        if (TextUtils.isEmpty(sessionId)) {
            if (afterCommit != null) {
                MAIN_HANDLER.post(afterCommit);
            }
            return;
        }
        final String id = sessionId;
        ImSessionRepository.getInstance().removeSessionWithDataThen(id, () -> {
            dispatchSessionRemoved(id);
            if (afterCommit != null) {
                MAIN_HANDLER.post(afterCommit);
            }
        });
    }

    /**
     * 进入消息页时清零该会话未读数，并通知 {@link #dispatchSessionPreviewUpdated}。
     */
    public static void clearSessionUnread(@Nullable String sessionId) {
        ImSessionRepository.getInstance().clearUnreadCount(sessionId);
    }

    /**
     * 标记会话未读（对齐野火 {@code markAsUnRead}，默认未读数 1）。
     */
    public static void markSessionUnread(@Nullable String sessionId) {
        markSessionUnread(sessionId, 1);
    }

    public static void markSessionUnread(@Nullable String sessionId, int unreadCount) {
        ImSessionRepository.getInstance().setUnreadCount(sessionId, unreadCount);
    }

    public static void updateSessionTopMute(@Nullable String sessionId,
                                            boolean top,
                                            boolean muteNotify) {
        ImSessionRepository.getInstance().updateTopMuteThen(sessionId, top, muteNotify, null);
    }

    /** 更新群会话已同步的 {@code memChgTime}（全量成员拉取成功后写入）。 */
    public static void updateGroupMemChgTime(@NonNull String groupId,
                                             long memChgTime,
                                             @Nullable Runnable afterCommit) {
        ImSessionRepository.getInstance().updateMemChgTimeThen(groupId, memChgTime, afterCommit);
    }

    /** 后台读取群会话本地 {@code memChgTime}，主线程回调。 */
    public static void loadGroupMemChgTime(@NonNull String groupId,
                                           @NonNull OnLoadCallback<Long> callback) {
        ImDbExecutor.runRead(() -> {
            SessionItem session = ImSessionRepository.getInstance().queryBySessionId(groupId);
            long value = session != null ? session.getMemChgTime() : 0L;
            MAIN_HANDLER.post(() -> callback.onLoaded(value));
        });
    }

    @NonNull
    public static List<SessionItem> loadSessions() {
        return ImSessionRepository.getInstance().queryAllAsDto();
    }

    /** 后台读库，结果在主线程 {@link OnLoadCallback#onLoaded}。 */
    public static void loadSessions(@NonNull OnLoadCallback<List<SessionItem>> callback) {
        ImDbExecutor.runRead(() -> {
            List<SessionItem> list = loadSessions();
            MAIN_HANDLER.post(() -> callback.onLoaded(list));
        });
    }

    /** 群聊会话名模糊搜索（Realm 本地，供搜索后台线程同步调用）。 */
    @NonNull
    public static List<SessionItem> searchGroupSessionsByName(@Nullable String keyword) {
        return ImSessionRepository.getInstance().searchGroupSessionsByName(keyword);
    }

    /** 聊天记录搜索：每会话最近一条命中（Realm 本地，供搜索后台线程同步调用）。 */
    @NonNull
    public static List<ImConversationSearchResult> searchConversationsByMessageContent(
            @Nullable String keyword) {
        return ImMessageRepository.getInstance().searchConversationsByMessageContent(keyword);
    }

    /** 定位消息时加载：目标前一页 + 目标至最新消息（主线程回调）。 */
    public static void loadMessagesForFocus(@Nullable String sessionId,
                                            @Nullable String focusMsgId,
                                            @NonNull OnFocusMessagesLoadCallback callback) {
        ImDbExecutor.runRead(() -> {
            List<AppMessage> messages;
            boolean hasMore;
            if (TextUtils.isEmpty(sessionId) || TextUtils.isEmpty(focusMsgId)) {
                messages = loadMessages(sessionId);
                hasMore = messages.size() >= MESSAGE_PAGE_SIZE;
            } else {
                AppMessage target = ImMessageRepository.getInstance().queryByMsgId(focusMsgId);
                if (target == null || !sessionId.equals(target.getConversationId())) {
                    messages = loadMessages(sessionId);
                    hasMore = messages.size() >= MESSAGE_PAGE_SIZE;
                } else {
                    long targetSeq = target.getSequenceId();
                    List<AppMessage> latestPage = ImMessageRepository.getInstance()
                            .queryLatestPageBySessionId(sessionId, MESSAGE_PAGE_SIZE);
                    if (containsMessageId(latestPage, focusMsgId)) {
                        messages = latestPage;
                        hasMore = !latestPage.isEmpty()
                                && ImMessageRepository.getInstance().hasOlderMessages(
                                sessionId, latestPage.get(0).getSequenceId());
                    } else {
                        List<AppMessage> older = ImMessageRepository.getInstance()
                                .queryOlderPageBySessionId(sessionId, targetSeq, MESSAGE_PAGE_SIZE);
                        List<AppMessage> fromTarget = ImMessageRepository.getInstance()
                                .queryFromSequenceId(sessionId, targetSeq);
                        messages = new ArrayList<>(older.size() + fromTarget.size());
                        messages.addAll(older);
                        messages.addAll(fromTarget);
                        hasMore = !older.isEmpty()
                                && ImMessageRepository.getInstance().hasOlderMessages(
                                sessionId, older.get(0).getSequenceId());
                    }
                }
            }
            final List<AppMessage> result = messages;
            final boolean more = hasMore;
            MAIN_HANDLER.post(() -> callback.onLoaded(result, more));
        });
    }

    private static boolean containsMessageId(@NonNull List<AppMessage> messages,
                                             @NonNull String messageId) {
        for (AppMessage message : messages) {
            if (message == null) {
                continue;
            }
            if (messageId.equals(message.getMsgId())
                    || messageId.equals(message.getClientMsgId())) {
                return true;
            }
        }
        return false;
    }

    public interface OnFocusMessagesLoadCallback {
        void onLoaded(@NonNull List<AppMessage> messages, boolean hasMoreHistory);
    }

    // ---------- 群成员 ----------

    public static void saveGroupMembersThen(@Nullable List<GroupMemberItem> members,
                                            @NonNull String groupId,
                                            @Nullable Runnable afterCommit) {
        ImGroupMemberRepository.getInstance().upsertAllThen(members, () -> {
            if (afterCommit != null) {
                afterCommit.run();
            }
            dispatchGroupMembersUpdated(groupId);
        });
    }

    public static void softDeleteGroupMembersThen(@NonNull String groupId,
                                                  @Nullable List<String> userIds,
                                                  @Nullable Runnable afterCommit) {
        ImGroupMemberRepository.getInstance().softDeleteThen(groupId, userIds, () -> {
            if (afterCommit != null) {
                afterCommit.run();
            }
            dispatchGroupMembersUpdated(groupId);
        });
    }

    /** 信令 0x0501 code=1004：批量更新成员角色；完成后由调用方分发角色变更事件。 */
    public static void updateGroupMemberRolesThen(@NonNull String groupId,
                                                  @Nullable List<String> userIds,
                                                  int role,
                                                  @Nullable Runnable afterCommit) {
        ImGroupMemberRepository.getInstance().updateRolesThen(groupId, userIds, role, afterCommit);
    }

    /**
     * 信令 0x0501 code=3001：按 userId 更新所有群内该成员的信用分（grade），
     * 并分发受影响群的成员变更通知。
     */
    public static void updateGroupMemberGradeByUserIdThen(@NonNull String userId,
                                                          @Nullable String grade,
                                                          @Nullable Runnable afterCommit) {
        ImGroupMemberRepository.getInstance().updateGradeByUserIdThen(userId, grade, groupIds -> {
            //目前仅本地群成员数据库的信用分更新，不做ui通知，防止刷新过于频繁，仅下次打开展示最新信用分
//            if (groupIds != null) {
//                for (String groupId : groupIds) {
//                    dispatchGroupMembersUpdated(groupId);
//                }
//            }
//            if (afterCommit != null) {
//                afterCommit.run();
//            }
        });
    }

    @Nullable
    public static GroupMemberItem queryGroupMember(@NonNull String groupId, @NonNull String userId) {
        return ImGroupMemberRepository.getInstance().queryActiveMember(groupId, userId);
    }

    @NonNull
    public static Map<String, GroupMemberItem> queryGroupMembersMap(@NonNull String groupId) {
        List<GroupMemberItem> list = ImGroupMemberRepository.getInstance().queryActiveByGroupId(groupId);
        Map<String, GroupMemberItem> map = new HashMap<>(list.size() * 2);
        for (GroupMemberItem item : list) {
            if (item != null && !TextUtils.isEmpty(item.getUserId())) {
                map.put(item.getUserId(), item);
            }
        }
        return map;
    }

    /** 后台读群成员，结果在主线程回调。 */
    public static void loadGroupMembersMap(@NonNull String groupId,
                                         @NonNull OnLoadCallback<Map<String, GroupMemberItem>> callback) {
        ImDbExecutor.runRead(() -> {
            Map<String, GroupMemberItem> map = queryGroupMembersMap(groupId);
            MAIN_HANDLER.post(() -> callback.onLoaded(map));
        });
    }

    /**
     * 群成员分页（本地库）：{@code openQykj=1} 且本人为普通成员时仅返回群主/管理员。
     *
     * @param knownMyRole &gt;=0 时直接使用，否则按 ownerId / 本地成员表推断
     */
    public static void loadGroupMembersPage(@NonNull String groupId,
                                            @Nullable String currentUserId,
                                            @Nullable String ownerId,
                                            @Nullable String openQykj,
                                            int knownMyRole,
                                            int offset,
                                            int limit,
                                            @NonNull OnLoadCallback<GroupMemberPageResult> callback) {
        ImDbExecutor.runRead(() -> {
            int myRole = knownMyRole >= 0
                    ? knownMyRole
                    : resolveMyGroupRole(groupId, currentUserId, ownerId);
            boolean ownerOrAdminOnly = "1".equals(openQykj) && !GroupRole.isOwnerOrAdmin(myRole);
            ImGroupMemberRepository repo = ImGroupMemberRepository.getInstance();
            int total = repo.countActive(groupId, ownerOrAdminOnly);
            List<GroupMemberItem> page = repo.queryActivePage(groupId, offset, limit, ownerOrAdminOnly);
            GroupMemberPageResult result = new GroupMemberPageResult(page, total, myRole);
            MAIN_HANDLER.post(() -> callback.onLoaded(result));
        });
    }

    /** 解析本人在群内角色：群主 id 优先，否则读本地成员表。 */
    public static int resolveMyGroupRole(@NonNull String groupId,
                                         @Nullable String currentUserId,
                                         @Nullable String ownerId) {
        if (!TextUtils.isEmpty(currentUserId) && currentUserId.equals(ownerId)) {
            return GroupRole.OWNER;
        }
        if (TextUtils.isEmpty(currentUserId)) {
            return GroupRole.MEMBER;
        }
        GroupMemberItem me = queryGroupMember(groupId, currentUserId);
        return me != null ? me.getRole() : GroupRole.MEMBER;
    }
}
