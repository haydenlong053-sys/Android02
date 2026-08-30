package com.freddy.chat.utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 群邀请卡片 content（复用 contentType=700，{@code msg=GroupInvite}）。
 * <p>
 * {@code {"msg":"GroupInvite","groupId":"...","groupName":"...","groupIcon":"...","from":"...","fromName":"..."}}
 */
public final class GroupInviteMessageContentUtil {

    public static final String MSG_GROUP_INVITE = "GroupInvite";

    private static final String KEY_MSG = "msg";
    private static final String KEY_GROUP_ID = "groupId";
    private static final String KEY_GROUP_NAME = "groupName";
    private static final String KEY_GROUP_ICON = "groupIcon";
    private static final String KEY_FROM = "from";
    private static final String KEY_FROM_NAME = "fromName";

    private GroupInviteMessageContentUtil() {
    }

    public static boolean isGroupInviteContent(@Nullable String contentJson) {
        return MSG_GROUP_INVITE.equals(readString(contentJson, KEY_MSG));
    }

    @NonNull
    public static String resolveGroupId(@Nullable String contentJson) {
        return readString(contentJson, KEY_GROUP_ID);
    }

    @NonNull
    public static String resolveGroupName(@Nullable String contentJson) {
        return readString(contentJson, KEY_GROUP_NAME);
    }

    @NonNull
    public static String resolveGroupIcon(@Nullable String contentJson) {
        return readString(contentJson, KEY_GROUP_ICON);
    }

    @NonNull
    public static String resolveFrom(@Nullable String contentJson) {
        return readString(contentJson, KEY_FROM);
    }

    @NonNull
    public static String resolveFromName(@Nullable String contentJson) {
        return readString(contentJson, KEY_FROM_NAME);
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
