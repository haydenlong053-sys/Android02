package com.freddy.chat.utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cboda.baselibrary.utils.RealmUtil;
import com.freddy.im.MessageType;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.db.ImLocalStore;
import com.freddy.im.db.repository.ImMessageRepository;
import com.freddy.im.http.model.im.GroupMemberItem;
import com.freddy.im.http.model.im.SessionItem;
import com.freddy.im.http.model.im.UserInfoItem;
import com.freddy.im.message.MessageContentFormatter;
import com.freddy.im.message.MessageRepealHelper;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * 微信风格 UI 辅助：头像占位、时间戳、消息摘要等。
 */
public final class WeChatUiHelper {

    private static final long TIME_GAP_SHOW_DIVIDER_MS = 5 * 60 * 1000L;

    private WeChatUiHelper() {
    }

    @NonNull
    public static String avatarLabel(@Nullable String primary, @Nullable String fallback) {
        String source = !TextUtils.isEmpty(primary) ? primary.trim() : null;
        if (TextUtils.isEmpty(source)) {
            source = !TextUtils.isEmpty(fallback) ? fallback.trim() : null;
        }
        if (TextUtils.isEmpty(source)) {
            return "?";
        }
        return source.substring(0, 1);
    }

    @Nullable
    public static String resolveAvatarUrl(@Nullable UserInfoItem user) {
        if (user == null) {
            return null;
        }
        if (!TextUtils.isEmpty(user.getIcon())) {
            return user.getIcon().trim();
        }
        if (!TextUtils.isEmpty(user.getAvatar())) {
            return user.getAvatar().trim();
        }
        if (!TextUtils.isEmpty(user.getHeadImg())) {
            return user.getHeadImg().trim();
        }
        if (!TextUtils.isEmpty(user.getHeadUrl())) {
            return user.getHeadUrl().trim();
        }
        return null;
    }

    @Nullable
    public static String resolveAvatarUrl(@Nullable SessionItem session) {
        if (session == null || TextUtils.isEmpty(session.getIcon())) {
            return null;
        }
        return session.getIcon().trim();
    }

    /**
     * 单聊对方展示名：本地好友备注 &gt; 昵称 &gt; sessionName &gt; username &gt; oppositeUserId。
     * 群聊请勿依赖此方法（应使用群名 / 成员 displayName）。
     */
    @NonNull
    public static String resolvePeerDisplayName(@Nullable SessionItem session) {
        return resolvePeerDisplayName(session, null);
    }

    /**
     * @param currentUserId 用于从 {@code sessionId}（a!b）拆对端；可为 null
     */
    @NonNull
    public static String resolvePeerDisplayName(@Nullable SessionItem session,
                                                @Nullable String currentUserId) {
        if (session == null) {
            return "";
        }
        if (MessageType.isSingleChat(session.getConversation())) {
            String peerId = SessionDisplayEnricher.INSTANCE.resolvePeerUserId(session, currentUserId);
            String friendName = RealmUtil.INSTANCE.findFriendDisplayName(peerId);
            if (!TextUtils.isEmpty(friendName)) {
                return friendName.trim();
            }
        }
        if (!TextUtils.isEmpty(session.getSessionName())) {
            return session.getSessionName().trim();
        }
        if (!TextUtils.isEmpty(session.getUsername())) {
            return session.getUsername().trim();
        }
        if (!TextUtils.isEmpty(session.getOppositeUserId())) {
            return session.getOppositeUserId();
        }
        return "";
    }

    /**
     * 会话列表标题：单聊走好友备注优先；群聊仍用 sessionName（群名）。
     */
    @NonNull
    public static String resolveSessionListDisplayName(@Nullable SessionItem session,
                                                       @Nullable String currentUserId) {
        if (session == null) {
            return "";
        }
        if (MessageType.isSingleChat(session.getConversation())) {
            return resolvePeerDisplayName(session, currentUserId);
        }
        if (!TextUtils.isEmpty(session.getSessionName())) {
            return session.getSessionName().trim();
        }
        return "";
    }

    public static long normalizeToMillis(long timestamp) {
        if (timestamp <= 0L) {
            return 0L;
        }
        return timestamp < 1_000_000_000_000L ? timestamp * 1000L : timestamp;
    }

    public static boolean shouldShowTimeDivider(long prevMillis, long currentMillis) {
        if (currentMillis <= 0L) {
            return false;
        }
        if (prevMillis <= 0L) {
            return true;
        }
        return currentMillis - prevMillis >= TIME_GAP_SHOW_DIVIDER_MS;
    }

    @NonNull
    public static String formatMessageTime(long timestamp) {
        long millis = normalizeToMillis(timestamp);
        if (millis <= 0L) {
            return "";
        }
        Calendar now = Calendar.getInstance();
        Calendar target = Calendar.getInstance();
        target.setTimeInMillis(millis);

        if (now.get(Calendar.YEAR) == target.get(Calendar.YEAR)
                && now.get(Calendar.DAY_OF_YEAR) == target.get(Calendar.DAY_OF_YEAR)) {
            return new SimpleDateFormat("HH:mm", Locale.getDefault()).format(new Date(millis));
        }
        if (now.get(Calendar.YEAR) == target.get(Calendar.YEAR)) {
            return new SimpleDateFormat("M月d日 HH:mm", Locale.getDefault()).format(new Date(millis));
        }
        return new SimpleDateFormat("yyyy年M月d日 HH:mm", Locale.getDefault()).format(new Date(millis));
    }

    public static boolean isSystemMessage(@Nullable AppMessage message) {
        return MessageContentFormatter.isSystemMessage(message);
    }

    public static boolean isRecalledMessage(@Nullable AppMessage message) {
        return MessageRepealHelper.isRecalled(message);
    }

    /**
     * 撤回消息展示文案（含 ${uid} 占位，由 nameMap / currentUserId 替换）。
     */
    @NonNull
    public static String formatRecallDisplay(@NonNull AppMessage message,
                                             @Nullable String currentUserId,
                                             @Nullable Map<String, String> userIdToDisplayName) {
        String raw = MessageRepealHelper.buildRecallContent(message.getFromId());
        return MessageContentFormatter.formatSessionPreview(
                raw,
                null,
                raw,
                null,
                currentUserId,
                userIdToDisplayName,
                0,
                MessageRepealHelper.STATUS_RECALLED, false);
    }

    public static boolean isVoiceMessage(int msgContentType) {
        return MessageContentFormatter.isVoiceMessage(msgContentType);
    }

    public static boolean isImageMessage(int msgContentType) {
        return MessageContentFormatter.isImageMessage(msgContentType);
    }

    public static boolean isVideoMessage(int msgContentType) {
        return MessageContentFormatter.isVideoMessage(msgContentType);
    }

    public static boolean isFileMessage(int msgContentType) {
        return MessageContentFormatter.isFileMessage(msgContentType);
    }

    public static boolean isMapMessage(int msgContentType) {
        return MessageContentFormatter.isMapMessage(msgContentType);
    }

    public static boolean isCardMessage(int msgContentType) {
        return MessageContentFormatter.isCardMessage(msgContentType);
    }

    public static boolean isNoticeMessage(int msgContentType) {
        return MessageContentFormatter.isNoticeMessage(msgContentType);
    }

    public static boolean isMeetStartMessage(int msgContentType) {
        return MessageContentFormatter.isMeetStartMessage(msgContentType);
    }

    public static boolean isMeetEndMessage(int msgContentType) {
        return MessageContentFormatter.isMeetEndMessage(msgContentType);
    }

    public static boolean isMeetStatusMessage(int msgContentType) {
        return MessageContentFormatter.isMeetStatusMessage(msgContentType);
    }

    public static boolean isMergeTranspondMessage(int msgContentType) {
        return MessageContentFormatter.isMergeTranspondMessage(msgContentType);
    }

    public static boolean isCallMessage(@Nullable AppMessage message) {
        return message != null && MessageContentFormatter.isCallMessageContent(message.getContent());
    }

    public static boolean isVoiceSessionPreview(@Nullable SessionItem session) {
        return session != null && matchesContentType(session.getLastContentType(),
                MessageType.MessageContentType.VOICE.getMsgContentType(), "voice");
    }

    public static boolean isImageSessionPreview(@Nullable SessionItem session) {
        return session != null && matchesContentType(session.getLastContentType(),
                MessageType.MessageContentType.IMAGE.getMsgContentType(), "image");
    }

    private static boolean matchesContentType(@Nullable String contentType, int expected,
                                              @Nullable String nameFallback) {
        if (contentType == null || contentType.isEmpty()) {
            return false;
        }
        try {
            return Integer.parseInt(contentType.trim()) == expected;
        } catch (NumberFormatException ignored) {
            return nameFallback != null && nameFallback.equalsIgnoreCase(contentType.trim());
        }
    }

    @NonNull
    public static String formatMessageBody(@NonNull AppMessage message) {
        return formatMessageBody(message, null, null);
    }

    @NonNull
    public static String formatMessageBody(@NonNull AppMessage message,
                                           @Nullable String currentUserId,
                                           @Nullable Map<String, String> userIdToDisplayName) {
        return MessageContentFormatter.formatDisplayText(message, currentUserId, userIdToDisplayName);
    }


    /**
     * 会话列表预览：传入当前用户与群成员昵称，避免撤回等系统文案显示裸 userId。
     */
    @NonNull
    public static String formatSessionPreview(@NonNull SessionItem session,
                                              @Nullable String currentUserId) {
        boolean groupChat = MessageType.isGroupChat(session.getConversation());
        String lastFromUserId = resolveSessionLastFromUserId(session);
        Map<String, String> nameMap = buildSessionPreviewNameMap(session, lastFromUserId);
        return MessageContentFormatter.formatSessionPreview(
                session.getLastMessageContent(),
                session.getLastContentType(),
                session.getLastMessageContent(),
                session.getLastCustomInfo(),
                currentUserId,
                nameMap,
                session.getLastMsgType(),
                session.getLastMessageStatus(),
                groupChat,
                lastFromUserId);
    }

    @Nullable
    private static String resolveSessionLastFromUserId(@NonNull SessionItem session) {
        String lastMessageId = session.getLastMessageId();
        if (!TextUtils.isEmpty(lastMessageId)) {
            AppMessage last = ImMessageRepository.getInstance().queryByMsgId(lastMessageId);
            if (last != null && !TextUtils.isEmpty(last.getFromId())) {
                return last.getFromId();
            }
        }
        return MessageContentFormatter.extractCallCallerUserId(session.getLastMessageContent());
    }

    @NonNull
    private static Map<String, String> buildSessionPreviewNameMap(@NonNull SessionItem session,
                                                                  @Nullable String lastFromUserId) {
        Map<String, String> map = new HashMap<>();
        List<String> uids = MessageContentFormatter.extractPlaceholderUserIds(
                session.getLastMessageContent());
        boolean group = MessageType.isGroupChat(session.getConversation());
        if (group && !TextUtils.isEmpty(lastFromUserId) && !uids.contains(lastFromUserId)) {
            uids = new ArrayList<>(uids);
            uids.add(lastFromUserId);
        }
        if (uids.isEmpty()) {
            return map;
        }
        for (String uid : uids) {
            if (TextUtils.isEmpty(uid)) {
                continue;
            }
            if (group) {
                GroupMemberItem member = ImLocalStore.queryGroupMember(session.getSessionId(), uid);
                if (member != null && !TextUtils.isEmpty(member.getDisplayName())) {
                    map.put(uid, member.getDisplayName());
                }
            } else {
                String peerId = SessionDisplayEnricher.INSTANCE.resolvePeerUserId(session, null);
                if (uid.equals(peerId) || uid.equals(session.getOppositeUserId())) {
                    String peerName = resolvePeerDisplayName(session, null);
                    if (!TextUtils.isEmpty(peerName)) {
                        map.put(uid, peerName);
                    }
                }
            }
        }
        return map;
    }
}
