package com.freddy.chat.utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 系统公告消息 content JSON 解析（type=710）。
 * <p>
 * {@code {"msg":"announcement","id":"...","contentUrl":"...","createTime":"...","noticeTitle":"...","previewContent":"...","status":0}}
 */
public final class NoticeMessageContentUtil {

    public static final String MSG_ANNOUNCEMENT = "announcement";
    private static final String KEY_MSG = "msg";
    private static final String KEY_ID = "id";
    private static final String KEY_CONTENT_URL = "contentUrl";
    private static final String KEY_CREATE_TIME = "createTime";
    private static final String KEY_NOTICE_TITLE = "noticeTitle";
    private static final String KEY_PREVIEW_CONTENT = "previewContent";
    private static final String KEY_STATUS = "status";

    private NoticeMessageContentUtil() {
    }

    @NonNull
    public static String resolveId(@Nullable String contentJson) {
        return readString(contentJson, KEY_ID);
    }

    @NonNull
    public static String resolveContentUrl(@Nullable String contentJson) {
        return readString(contentJson, KEY_CONTENT_URL);
    }

    @NonNull
    public static String resolveCreateTime(@Nullable String contentJson) {
        return readString(contentJson, KEY_CREATE_TIME);
    }

    @NonNull
    public static String resolveNoticeTitle(@Nullable String contentJson) {
        return readString(contentJson, KEY_NOTICE_TITLE);
    }

    @NonNull
    public static String resolvePreviewContent(@Nullable String contentJson) {
        return readString(contentJson, KEY_PREVIEW_CONTENT);
    }

    public static int resolveStatus(@Nullable String contentJson) {
        JsonObject obj = parseObject(contentJson);
        if (obj == null || !obj.has(KEY_STATUS)) {
            return 0;
        }
        try {
            return obj.get(KEY_STATUS).getAsInt();
        } catch (Exception ignored) {
            return 0;
        }
    }

    @NonNull
    private static String readString(@Nullable String contentJson, @NonNull String key) {
        JsonObject obj = parseObject(contentJson);
        if (obj == null || !obj.has(key) || !obj.get(key).isJsonPrimitive()) {
            return "";
        }
        String value = obj.get(key).getAsString();
        return TextUtils.isEmpty(value) ? "" : value.trim();
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
}
