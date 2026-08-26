package com.freddy.chat.utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.bean.AppMessage;
import com.freddy.im.message.MessageContentFormatter;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Map;

/**
 * 文本消息引用（仿微信 / 对齐野火 {@code QuoteInfo}）。
 * <p>
 * 写入文本 content JSON：
 * {@code {"msg":"...","quote":{"msgId":"...","clientMsgId":"...","userId":"...","displayName":"...","digest":"...","contentType":100}}}
 */
public final class QuoteMessageContentUtil {

    public static final String KEY_QUOTE = "quote";
    private static final String KEY_MSG_ID = "msgId";
    private static final String KEY_CLIENT_MSG_ID = "clientMsgId";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_DISPLAY_NAME = "displayName";
    private static final String KEY_DIGEST = "digest";
    private static final String KEY_CONTENT_TYPE = "contentType";
    private static final int DIGEST_MAX_LEN = 48;

    private QuoteMessageContentUtil() {
    }

    public static final class QuoteInfo {
        @Nullable
        public String msgId;
        @Nullable
        public String clientMsgId;
        @Nullable
        public String userId;
        @Nullable
        public String displayName;
        @Nullable
        public String digest;
        public int contentType;

        @NonNull
        public String formatPreviewLine() {
            String name = TextUtils.isEmpty(displayName) ? (userId == null ? "" : userId) : displayName;
            String dig = digest == null ? "" : digest;
            if (TextUtils.isEmpty(name)) {
                return dig;
            }
            return name + ": " + dig;
        }

        /** 优先服务端 msgId，其次 clientMsgId，用于列表定位。 */
        @Nullable
        public String resolveAnchorId() {
            if (!TextUtils.isEmpty(msgId)) {
                return msgId;
            }
            if (!TextUtils.isEmpty(clientMsgId)) {
                return clientMsgId;
            }
            return null;
        }
    }

    @Nullable
    public static QuoteInfo parse(@Nullable String contentJson) {
        JsonObject root = parseObject(contentJson);
        if (root == null || !root.has(KEY_QUOTE) || !root.get(KEY_QUOTE).isJsonObject()) {
            return null;
        }
        JsonObject q = root.getAsJsonObject(KEY_QUOTE);
        QuoteInfo info = new QuoteInfo();
        info.msgId = readString(q, KEY_MSG_ID);
        info.clientMsgId = readString(q, KEY_CLIENT_MSG_ID);
        info.userId = readString(q, KEY_USER_ID);
        info.displayName = readString(q, KEY_DISPLAY_NAME);
        info.digest = readString(q, KEY_DIGEST);
        if (q.has(KEY_CONTENT_TYPE) && q.get(KEY_CONTENT_TYPE).isJsonPrimitive()) {
            try {
                info.contentType = q.get(KEY_CONTENT_TYPE).getAsInt();
            } catch (Exception ignored) {
                info.contentType = 0;
            }
        }
        if (TextUtils.isEmpty(info.resolveAnchorId()) && TextUtils.isEmpty(info.digest)) {
            return null;
        }
        return info;
    }

    @NonNull
    public static QuoteInfo fromMessage(@NonNull AppMessage message,
                                         @Nullable String currentUserId,
                                         @Nullable Map<String, String> nameMap) {
        QuoteInfo info = new QuoteInfo();
        info.msgId = message.getMsgId();
        info.clientMsgId = message.getClientMsgId();
        info.userId = message.getFromId();
        info.displayName = WeChatUiHelper.resolveUserDisplayName(info.userId, currentUserId, nameMap);
        String digest = MessageContentFormatter.formatDisplayText(message, currentUserId, nameMap);
        if (digest.length() > DIGEST_MAX_LEN) {
            digest = digest.substring(0, DIGEST_MAX_LEN);
        }
        info.digest = digest;
        info.contentType = message.getMsgContentType();
        return info;
    }

    /**
     * 展示时按「当前用户视角」解析名称：本地备注/群昵称优先，不使用对方写入的 displayName。
     */
    @NonNull
    public static String resolveDisplayNameForPreview(@Nullable QuoteInfo quote,
                                                      @Nullable String currentUserId,
                                                      @Nullable Map<String, String> nameMap) {
        if (quote == null) {
            return "";
        }
        String local = WeChatUiHelper.resolveUserDisplayName(quote.userId, currentUserId, nameMap);
        if (!TextUtils.isEmpty(local)
                && (TextUtils.isEmpty(quote.userId) || !TextUtils.equals(local, quote.userId))) {
            return local;
        }
        // 本地解析失败时再退回消息里携带的名称
        if (!TextUtils.isEmpty(quote.displayName)
                && !TextUtils.equals(quote.displayName, quote.userId)) {
            return quote.displayName.trim();
        }
        return TextUtils.isEmpty(local) ? "" : local;
    }

    @NonNull
    public static String formatPreviewLine(@Nullable QuoteInfo quote,
                                           @Nullable String currentUserId,
                                           @Nullable Map<String, String> nameMap) {
        if (quote == null) {
            return "";
        }
        String name = resolveDisplayNameForPreview(quote, currentUserId, nameMap);
        String dig = quote.digest == null ? "" : quote.digest;
        if (TextUtils.isEmpty(name)) {
            return dig;
        }
        return name + ": " + dig;
    }

    @NonNull
    public static JsonObject toJsonObject(@NonNull QuoteInfo info) {
        JsonObject q = new JsonObject();
        if (!TextUtils.isEmpty(info.msgId)) {
            q.addProperty(KEY_MSG_ID, info.msgId);
        }
        if (!TextUtils.isEmpty(info.clientMsgId)) {
            q.addProperty(KEY_CLIENT_MSG_ID, info.clientMsgId);
        }
        if (!TextUtils.isEmpty(info.userId)) {
            q.addProperty(KEY_USER_ID, info.userId);
        }
        if (!TextUtils.isEmpty(info.displayName)) {
            q.addProperty(KEY_DISPLAY_NAME, info.displayName);
        }
        if (!TextUtils.isEmpty(info.digest)) {
            q.addProperty(KEY_DIGEST, info.digest);
        }
        if (info.contentType > 0) {
            q.addProperty(KEY_CONTENT_TYPE, info.contentType);
        }
        return q;
    }

    @Nullable
    private static JsonObject parseObject(@Nullable String raw) {
        if (TextUtils.isEmpty(raw)) {
            return null;
        }
        try {
            return JsonParser.parseString(raw.trim()).getAsJsonObject();
        } catch (Exception ignored) {
            return null;
        }
    }

    @NonNull
    private static String readString(@NonNull JsonObject obj, @NonNull String key) {
        if (!obj.has(key) || !obj.get(key).isJsonPrimitive()) {
            return "";
        }
        String v = obj.get(key).getAsString();
        return v == null ? "" : v.trim();
    }
}
