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
 * {@code {"msg":"...","quote":{"msgId":"...","clientMsgId":"...","userId":"...","displayName":"...","digest":"...","contentType":100,"thumbUrl":"...","mediaUrl":"...","fileName":"...","duration":3}}}
 */
public final class QuoteMessageContentUtil {

    public static final String KEY_QUOTE = "quote";
    private static final String KEY_MSG_ID = "msgId";
    private static final String KEY_CLIENT_MSG_ID = "clientMsgId";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_DISPLAY_NAME = "displayName";
    private static final String KEY_DIGEST = "digest";
    private static final String KEY_CONTENT_TYPE = "contentType";
    private static final String KEY_THUMB_URL = "thumbUrl";
    private static final String KEY_MEDIA_URL = "mediaUrl";
    private static final String KEY_FILE_NAME = "fileName";
//    private static final String KEY_DURATION = "duration";
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
        /**
         * 图片缩略图 / 视频封面；文件为空时用 fileName 解析图标。
         */
        @Nullable
        public String thumbUrl;
        /**
         * 可打开的图片 / 视频 / 文件 / 语音地址（优先远端）。
         */
        @Nullable
        public String mediaUrl;
        @Nullable
        public String fileName;


        @NonNull
        public String formatPreviewLine() {
            String name = TextUtils.isEmpty(displayName) ? (userId == null ? "" : userId) : displayName;
            String dig = digest == null ? "" : digest;
            if (TextUtils.isEmpty(name)) {
                return dig;
            }
            return name + ": " + dig;
        }

        /**
         * 优先服务端 msgId，其次 clientMsgId，用于列表定位。
         */
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

        public boolean isImageQuote() {
            return MessageContentFormatter.isImageMessage(contentType);
        }

        public boolean isVideoQuote() {
            return MessageContentFormatter.isVideoMessage(contentType);
        }

        public boolean isFileQuote() {
            return MessageContentFormatter.isFileMessage(contentType);
        }

        public boolean isVoiceQuote() {
            return MessageContentFormatter.isVoiceMessage(contentType);
        }

        public boolean isMediaQuote() {
            return isImageQuote() || isVideoQuote() || isFileQuote();
        }

        public boolean hasThumbPreview() {
            return isMediaQuote()
                    && (!TextUtils.isEmpty(thumbUrl)
                    || !TextUtils.isEmpty(mediaUrl)
                    || isFileQuote());
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
        info.thumbUrl = readString(q, KEY_THUMB_URL);
        info.mediaUrl = readString(q, KEY_MEDIA_URL);
        info.fileName = readString(q, KEY_FILE_NAME);
        if (q.has(KEY_CONTENT_TYPE) && q.get(KEY_CONTENT_TYPE).isJsonPrimitive()) {
            try {
                info.contentType = q.get(KEY_CONTENT_TYPE).getAsInt();
            } catch (Exception ignored) {
                info.contentType = 0;
            }
        }
        if (TextUtils.isEmpty(info.resolveAnchorId())
                && TextUtils.isEmpty(info.digest)
                && TextUtils.isEmpty(info.thumbUrl)
                && TextUtils.isEmpty(info.mediaUrl)) {
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
        fillMediaFields(info, message, currentUserId);
        return info;
    }

    private static void fillMediaFields(@NonNull QuoteInfo info,
                                        @NonNull AppMessage message,
                                        @Nullable String currentUserId) {
        String content = message.getContent();
        boolean sentByMe = message.isSender(currentUserId);
        if (info.isImageQuote()) {
            String remote = MediaMessageContentUtil.resolveRemoteUrl(content);
            String display = MediaMessageContentUtil.resolveDisplayUrl(content, sentByMe);
            String thumb = MediaMessageContentUtil.resolveDisplayThumbUrl(content, sentByMe);
            info.mediaUrl = firstNonEmpty(remote, display);
            info.thumbUrl = firstNonEmpty(thumb, info.mediaUrl);
            return;
        }
        if (info.isVideoQuote()) {
            String remote = MediaMessageContentUtil.resolveRemoteUrl(content);
            String play = MediaMessageContentUtil.resolveVideoPlayUrl(content, sentByMe);
            info.mediaUrl = firstNonEmpty(remote, play);
            String cover = MediaMessageContentUtil.resolveCoverDisplayUrl(content, sentByMe);
            info.thumbUrl = cover;
            return;
        }
        if (info.isFileQuote()) {
            String remote = MediaMessageContentUtil.resolveRemoteUrl(content);
            String display = MediaMessageContentUtil.resolveDisplayUrl(content, sentByMe);
            info.mediaUrl = firstNonEmpty(remote, display);
            info.fileName = MediaMessageContentUtil.resolveFileName(content);
            return;
        }
        if (info.isVoiceQuote()) {
            String remote = MediaMessageContentUtil.resolveRemoteUrl(content);
            String display = MediaMessageContentUtil.resolveDisplayUrl(content, sentByMe);
            info.mediaUrl = firstNonEmpty(remote, display);
            info.fileName = String.valueOf(MediaMessageContentUtil.resolveVoiceDurationSec(content));
        }
    }

    @NonNull
    private static String firstNonEmpty(@Nullable String a, @Nullable String b) {
        if (!TextUtils.isEmpty(a)) {
            return a.trim();
        }
        if (!TextUtils.isEmpty(b)) {
            return b.trim();
        }
        return "";
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
        if (!TextUtils.isEmpty(info.thumbUrl)) {
            q.addProperty(KEY_THUMB_URL, info.thumbUrl);
        }
        if (!TextUtils.isEmpty(info.mediaUrl)) {
            q.addProperty(KEY_MEDIA_URL, info.mediaUrl);
        }
        if (!TextUtils.isEmpty(info.fileName)) {
            q.addProperty(KEY_FILE_NAME, info.fileName);
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
