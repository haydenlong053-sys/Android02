package com.freddy.chat.utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.MessageType;
import com.freddy.im.bean.AppMessage;
import com.freddy.im.http.model.im.SessionItem;
import com.freddy.im.http.model.im.UserInfoItem;
import com.freddy.im.message.MessageContentFormatter;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
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

    @NonNull
    public static String formatSessionPreview(@NonNull SessionItem session) {
        return MessageContentFormatter.formatSessionPreview(
                session.getLastMessageContent(),
                session.getLastContentType(),
                session.getLastMessageContent(),
                session.getLastCustomInfo(),
                null,
                session.getLastMsgType());
    }
}
