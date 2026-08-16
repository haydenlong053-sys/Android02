package com.freddy.im.db;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cboda.baselibrary.utils.ELogUtilKt;
import com.cboda.baselibrary.utils.ViewExpandUtilKt;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.db.entity.MessageEntity;
import com.freddy.im.db.entity.SessionEntity;
import com.freddy.im.message.ImMessageManager;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.Sort;

/**
 * 会话消息同步高水位：与本地可见消息解耦，本地删除不回退，增量拉取以此为锚点。
 */
public final class SessionSyncCursor {

    private SessionSyncCursor() {
    }

    /**
     * 消息落库后推进各会话 syncedMax（只增不减；仅以本地已持久化消息为准）。
     * getList 分页路径使用：本批取最大 sequence，允许跨号推进。
     */
    public static void advanceInTransaction(@NonNull Realm realm, @NonNull List<AppMessage> messages) {
        advanceInTransaction(realm, messages, false);
    }

    /**
     * 发送成功回包：仅当新 messageId 与当前水位<strong>连续</strong>（{@code seq == syncedMax + 1}）才推进。
     * 跳跃（中间未拉消息）不抬游标，避免后续增量从高水位起步漏拉。
     */
    public static void advanceIfContiguousInTransaction(@NonNull Realm realm,
                                                        @NonNull List<AppMessage> messages) {
        advanceInTransaction(realm, messages, true);
    }

    private static void advanceInTransaction(@NonNull Realm realm,
                                             @NonNull List<AppMessage> messages,
                                             boolean requireContiguous) {
        // 先筛合法服务端 msgId，再按会话取最大 seq 推进一次（避免 UUID 盖过有效消息，且同会话 O(1) 查 Session）
        Map<String, AppMessage> latestBySession = pickLatestValidServerMsgPerSession(messages);
        for (Map.Entry<String, AppMessage> entry : latestBySession.entrySet()) {
            advanceSessionInTransaction(realm, entry.getKey(), entry.getValue(), requireContiguous);
        }
    }

    /**
     * 解析增量拉取锚点 messageId：优先 syncedMax，否则本地可见最大条（不用会话摘要 lastMessageId）。
     * <p>
     * 只读：发现异常游标时不得改 managed 对象（调用方常在 {@code runRead} 中），清理走写事务异步排队。
     */
    @Nullable
    public static String resolveAnchorMsgId(@NonNull Realm realm, @NonNull String sessionId) {
        if (TextUtils.isEmpty(sessionId)) {
            return null;
        }
        SessionEntity session = realm.where(SessionEntity.class)
                .equalTo("sessionId", sessionId)
                .findFirst();
        if (session != null && !TextUtils.isEmpty(session.getSyncedMaxMsgId())) {
            String syncedMax = session.getSyncedMaxMsgId();
            if (!isValidAnchorMsgIdFormat(syncedMax)) {
                ELogUtilKt.eLogD(ELogUtilKt.APP_IM_TAG,
                        "sessionId=" + sessionId + " 出现本地异常游标：" + syncedMax);
                ViewExpandUtilKt.showIMKeyErrorToast(sessionId + "游标开小差了" + syncedMax);
                scheduleClearInvalidSessionCursor(sessionId);
                return "";
            }
            return syncedMax;
        }
        MessageEntity localMax = realm.where(MessageEntity.class)
                .equalTo("sessionId", sessionId)
                .sort("sequenceId", Sort.DESCENDING)
                .findFirst();
        if (localMax != null && !TextUtils.isEmpty(localMax.getMsgId())) {
            String msgId = localMax.getMsgId();
            if (!isValidAnchorMsgIdFormat(msgId)) {
                ELogUtilKt.eLogD(ELogUtilKt.APP_IM_TAG,
                        "sessionId=" + sessionId + " 出现本地异常游标：" + msgId);
                ViewExpandUtilKt.showIMKeyErrorToast(sessionId + "游标开小差了" + msgId);
                // 不改 MessageEntity.msgId，避免读路径写库 / 损坏消息主键
                return "";
            }
            return msgId;
        }
        return null;
    }

    /** messageId 形如 {@code sessionId-seq}：有且仅有一个 '-'，且 '-' 后为非负整数。 */
    private static boolean isValidAnchorMsgIdFormat(@Nullable String msgId) {
        if (TextUtils.isEmpty(msgId)) {
            return false;
        }
        int dash = msgId.indexOf('-');
        if (dash <= 0 || dash != msgId.lastIndexOf('-') || dash >= msgId.length() - 1) {
            return false;
        }
        return ImMessageManager.extractTrailingSequence(msgId) != ImMessageManager.INVALID_SEQUENCE;
    }

    /**
     * 同步水位只能用服务端 messageId，禁止 clientMsgId/UUID 回退。
     * 可选校验前缀与 sessionId 一致（sessionId 含 '-' 的极少见情况仅做 seq 格式校验）。
     */
    private static boolean isValidServerSyncMsgId(@Nullable String msgId, @Nullable String sessionId) {
        if (!isValidAnchorMsgIdFormat(msgId)) {
            return false;
        }
        if (TextUtils.isEmpty(sessionId) || sessionId.indexOf('-') >= 0) {
            return true;
        }
        return msgId.startsWith(sessionId + "-");
    }

    /** 异步写事务清空异常会话水位（不可在 resolve 读路径直接 set managed）。 */
    private static void scheduleClearInvalidSessionCursor(@NonNull String sessionId) {
        ImDbExecutor.runWrite(() -> {
            Realm realm = ImRealm.getRealm();
            try {
                realm.executeTransaction(r -> {
                    SessionEntity s = r.where(SessionEntity.class)
                            .equalTo("sessionId", sessionId)
                            .findFirst();
                    if (s == null) {
                        return;
                    }
                    String cur = s.getSyncedMaxMsgId();
                    if (!TextUtils.isEmpty(cur) && !isValidAnchorMsgIdFormat(cur)) {
                        s.setSyncedMaxMsgId("");
                        s.setSyncedMaxSequence(0L);
                    }
                });
            } finally {
                realm.close();
            }
        });
    }

    /**
     * 本地是否已与服务端会话摘要对齐。
     * <p>
     * 以 {@code syncedMax} vs {@code lastMessageId} 为准；未读/unPull 不再强制未对齐，
     * 避免 sticky unread 在已追上水位后仍反复补拉。
     */
    public static boolean isCaughtUp(@Nullable SessionEntity session,
                                     @Nullable String serverLastMessageId,
                                     int unreadCount,
                                     int unPullCount) {
        // unreadCount / unPullCount 保留入参以兼容调用方；对齐判断仅看水位
        if (unPullCount > 0 || unreadCount > 0) {
            return false;
        }
        if (session == null || TextUtils.isEmpty(serverLastMessageId)) {
            return false;
        }
        long serverSeq = ImMessageManager.extractTrailingSequence(serverLastMessageId);
        if (serverSeq == ImMessageManager.INVALID_SEQUENCE) {
            return false;
        }
        long syncedSeq = session.getSyncedMaxSequence();
        // 水位已齐：即使 unread/unPull 仍 >0（sticky），也视为已追上，避免再拉 tip 导致未读多计
        return syncedSeq > 0L && syncedSeq >= serverSeq;
    }

    /**
     * @deprecated 请使用 {@link #isCaughtUp(SessionEntity, String, int, int)}。
     */
    @Deprecated
    public static boolean isCaughtUp(@Nullable SessionEntity session,
                                     @Nullable String serverLastMessageId) {
        return isCaughtUp(session, serverLastMessageId, 0, 0);
    }

    /**
     * 推送 messageId 是否不新于当前同步高水位（无需为填补本地删除缺口而重拉）。
     */
    public static boolean isPushAlreadySynced(@Nullable String syncAnchorMsgId, @NonNull String pushMsgId) {
        if (TextUtils.isEmpty(syncAnchorMsgId)) {
            return false;
        }
        long syncedSeq = ImMessageManager.extractTrailingSequence(syncAnchorMsgId);
        long pushSeq = ImMessageManager.extractTrailingSequence(pushMsgId);
        return syncedSeq != ImMessageManager.INVALID_SEQUENCE
                && pushSeq != ImMessageManager.INVALID_SEQUENCE
                && pushSeq <= syncedSeq;
    }

    private static void advanceSessionInTransaction(@NonNull Realm realm,
                                                    @NonNull String sessionId,
                                                    @NonNull AppMessage message,
                                                    boolean requireContiguous) {
        SessionEntity session = realm.where(SessionEntity.class)
                .equalTo("sessionId", sessionId)
                .findFirst();
        if (session == null) {
            return;
        }
        // 仅服务端 msgId 可抬水位；禁止 resolve 到 clientMsgId/UUID（发送中消息常见）
        String candidateMsgId = message.getMsgId();
        if (!isValidServerSyncMsgId(candidateMsgId, sessionId)) {
            return;
        }
        // 已损坏的水位（如 UUID）先清掉，否则伪 seq 会挡住后续真实推进
        String currentMsgId = session.getSyncedMaxMsgId();
        long currentSeq = session.getSyncedMaxSequence();
        if (!TextUtils.isEmpty(currentMsgId) && !isValidAnchorMsgIdFormat(currentMsgId)) {
            ELogUtilKt.eLogD(ELogUtilKt.APP_IM_TAG,
                    "sessionId=" + sessionId + " 清理异常水位游标：" + currentMsgId);
            session.setSyncedMaxMsgId("");
            session.setSyncedMaxSequence(0L);
            currentSeq = 0L;
        }
        long candidateSeq = message.getSequenceId() > 0L
                ? message.getSequenceId()
                : ImMessageManager.extractTrailingSequence(candidateMsgId);
        if (candidateSeq == ImMessageManager.INVALID_SEQUENCE) {
            return;
        }
        if (candidateSeq <= currentSeq) {
            return;
        }
        // 发送成功：必须与当前水位严格 +1，否则中间缺口会被跳过
        if (requireContiguous && candidateSeq != currentSeq + 1) {
            return;
        }
        session.setSyncedMaxSequence(candidateSeq);
        session.setSyncedMaxMsgId(candidateMsgId);
    }

    /**
     * 仅纳入合法服务端 messageId 的消息，按会话保留 sequence 最大的一条。
     * 发送中 UUID / 空 msgId 直接跳过，不会参与水位竞选。
     */
    @NonNull
    private static Map<String, AppMessage> pickLatestValidServerMsgPerSession(
            @NonNull List<AppMessage> messages) {
        Map<String, AppMessage> map = new LinkedHashMap<>();
        for (AppMessage message : messages) {
            if (message == null) {
                continue;
            }
            String sessionId = resolveSessionId(message);
            if (TextUtils.isEmpty(sessionId)) {
                continue;
            }
            if (!isValidServerSyncMsgId(message.getMsgId(), sessionId)) {
                continue;
            }
            AppMessage existing = map.get(sessionId);
            if (existing == null || syncSequence(message) > syncSequence(existing)) {
                map.put(sessionId, message);
            }
        }
        return map;
    }

    /** 水位竞选用 seq：优先字段，否则从已校验的服务端 msgId 解析。 */
    private static long syncSequence(@NonNull AppMessage message) {
        if (message.getSequenceId() > 0L) {
            return message.getSequenceId();
        }
        return ImMessageManager.extractTrailingSequence(message.getMsgId());
    }

    /**
     * 解析会话 id：优先 conversationId；否则仅从服务端 msgId 拆锚点。
     * 禁止用 clientMsgId/UUID 回退，避免 extractConversationAnchor 得到假 sessionId。
     */
    @Nullable
    private static String resolveSessionId(@NonNull AppMessage message) {
        if (!TextUtils.isEmpty(message.getConversationId())) {
            return message.getConversationId();
        }
        String msgId = message.getMsgId();
        if (TextUtils.isEmpty(msgId)) {
            return null;
        }
        return ImMessageManager.extractConversationAnchor(msgId);
    }
}
