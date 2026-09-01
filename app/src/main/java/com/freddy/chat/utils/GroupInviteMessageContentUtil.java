package com.freddy.chat.utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 邀请进群消息封装与解析
 * 邀请进群消息 content JSON 解析（contentType=730）。
 * <p>
 * {@code {"msg":"GroupInvite","groupId":"...","groupName":"...","groupIcon":"...","idno":"...",
 * "from":"...","fromName":"...","to":"...","toName":"..."}}
 */
public final class GroupInviteMessageContentUtil {

    public static final String MSG_GROUP_INVITE = "GroupInvite";

    public static final String KEY_MSG = "msg";
    public static final String KEY_GROUP_ID = "gId";
    public static final String KEY_GROUP_NAME = "gName";
    public static final String KEY_GROUP_ICON = "gIcon";
    public static final String KEY_ID_NO = "gNum";
    public static final String KEY_FROM = "from";
    public static final String KEY_FROM_NAME = "fName";
    public static final String KEY_TO = "to";
    public static final String KEY_TO_NAME = "tName";
    public static final String KEY_GROUP_MEMBER_COUNT = "pCount";


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

    /**
     * 显示用群号（{@code busiExt.idno}）。
     */
    @NonNull
    public static String resolveGroupIdno(@Nullable String contentJson) {
        return readString(contentJson, KEY_ID_NO);
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
    public static String resolveGroupMemberCount(@Nullable String contentJson) {
        return readString(contentJson, KEY_GROUP_MEMBER_COUNT);
    }


    @NonNull
    public static String resolveTo(@Nullable String contentJson) {
        return readString(contentJson, KEY_TO);
    }

    @NonNull
    public static String resolveToName(@Nullable String contentJson) {
        return readString(contentJson, KEY_TO_NAME);
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
