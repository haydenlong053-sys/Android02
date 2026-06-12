package com.freddy.chat.utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 个人名片消息 content JSON 解析（type=700）。
 * <p>
 * {@code {"msg":"Card","idno":"...","displayName":"...","userId":"...","portrait":"...","from":"..."}}
 */
public final class CardMessageContentUtil {

    public static final String MSG_CARD = "Card";
    private static final String KEY_MSG = "msg";
    private static final String KEY_IDNO = "idno";
    private static final String KEY_DISPLAY_NAME = "displayName";
    private static final String KEY_USER_ID = "userId";
    private static final String KEY_PORTRAIT = "portrait";
    private static final String KEY_FROM = "from";

    private CardMessageContentUtil() {
    }

    @NonNull
    public static String resolveUserId(@Nullable String contentJson) {
        return readString(contentJson, KEY_USER_ID);
    }

    @NonNull
    public static String resolveDisplayName(@Nullable String contentJson) {
        return readString(contentJson, KEY_DISPLAY_NAME);
    }

    @NonNull
    public static String resolvePortrait(@Nullable String contentJson) {
        return readString(contentJson, KEY_PORTRAIT);
    }

    @NonNull
    public static String resolveIdno(@Nullable String contentJson) {
        return readString(contentJson, KEY_IDNO);
    }

    @NonNull
    public static String resolveFrom(@Nullable String contentJson) {
        return readString(contentJson, KEY_FROM);
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
