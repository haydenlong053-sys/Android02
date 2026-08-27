package com.freddy.chat.utils;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.List;

/**
 * 红包 / 领取通知 content JSON 解析（contentType=1000 / 1001）。
 * 字段对齐野火 {@code HongBaoMessageContent} / {@code LingQuMessageContent}。
 */
public final class HongBaoMessageContentUtil {

    public static final String KEY_HONGBAO_ID = "hongbaoId";
    public static final String KEY_PRICE = "price";
    public static final String KEY_HONGBAO_DESC = "info";
    public static final String KEY_LINGQU_STATUS = "lingquStatus";
    public static final String KEY_HONGBAO_LEIXING = "hongbaoLeixing";
    public static final String KEY_ZHUANSHU_USER_ID = "zhuanshuUserId";
    public static final String KEY_ZHUANSHU_USER_NAME = "zhuanshuUserName";
    public static final String KEY_ZHUANSHU_USER_AVATAR = "zhuanshuUserAvatar";
    public static final String KEY_LQ_USERS = "lqUsers";

    public static final String KEY_TIP_TYPE = "tipType";
    public static final String KEY_SEND_USER_ID = "sendUserId";
    public static final String KEY_SEND_USER_NAME = "sendUserName";
    public static final String KEY_LINGQU_USER_ID = "lingquUserId";
    public static final String KEY_LINGQU_USER_NAME = "lingquUserName";

    private HongBaoMessageContentUtil() {
    }

    @NonNull
    public static String resolveHongbaoId(@Nullable String contentJson) {
        return readString(contentJson, KEY_HONGBAO_ID);
    }

    @NonNull
    public static String resolvePrice(@Nullable String contentJson) {
        return readString(contentJson, KEY_PRICE);
    }

    @NonNull
    public static String resolveHongbaoDesc(@Nullable String contentJson) {
        return readString(contentJson, KEY_HONGBAO_DESC);
    }

    @NonNull
    public static String resolveLingquStatus(@Nullable String contentJson) {
        return readString(contentJson, KEY_LINGQU_STATUS);
    }

    @NonNull
    public static String resolveHongbaoLeixing(@Nullable String contentJson) {
        return readString(contentJson, KEY_HONGBAO_LEIXING);
    }

    @NonNull
    public static String resolveZhuanshuUserId(@Nullable String contentJson) {
        return readString(contentJson, KEY_ZHUANSHU_USER_ID);
    }

    @NonNull
    public static String resolveZhuanshuUserName(@Nullable String contentJson) {
        return readString(contentJson, KEY_ZHUANSHU_USER_NAME);
    }

    @NonNull
    public static String resolveZhuanshuUserAvatar(@Nullable String contentJson) {
        return readString(contentJson, KEY_ZHUANSHU_USER_AVATAR);
    }

    @NonNull
    public static List<String> resolveLqUsers(@Nullable String contentJson) {
        JsonObject obj = parseObject(contentJson);
        if (obj == null || !obj.has(KEY_LQ_USERS) || !obj.get(KEY_LQ_USERS).isJsonArray()) {
            return new ArrayList<>();
        }
        JsonArray arr = obj.getAsJsonArray(KEY_LQ_USERS);
        List<String> users = new ArrayList<>(arr.size());
        for (JsonElement el : arr) {
            if (el == null || !el.isJsonPrimitive()) {
                continue;
            }
            String id = el.getAsString();
            if (!TextUtils.isEmpty(id)) {
                users.add(id.trim());
            }
        }
        return users;
    }

    @NonNull
    public static String resolveTipType(@Nullable String contentJson) {
        return readString(contentJson, KEY_TIP_TYPE);
    }

    @NonNull
    public static String resolveSendUserId(@Nullable String contentJson) {
        return readString(contentJson, KEY_SEND_USER_ID);
    }

    @NonNull
    public static String resolveSendUserName(@Nullable String contentJson) {
        return readString(contentJson, KEY_SEND_USER_NAME);
    }

    @NonNull
    public static String resolveLingquUserId(@Nullable String contentJson) {
        return readString(contentJson, KEY_LINGQU_USER_ID);
    }

    @NonNull
    public static String resolveLingquUserName(@Nullable String contentJson) {
        return readString(contentJson, KEY_LINGQU_USER_NAME);
    }

    /**
     * 将 content JSON 中的 {@code lingquStatus}（及可选 {@code lqUsers}）写回新 JSON。
     */
    @Nullable
    public static String patchLingquStatus(@Nullable String contentJson,
                                           @Nullable String lingquStatus,
                                           @Nullable List<String> lqUsers) {
        JsonObject obj = parseObject(contentJson);
        if (obj == null) {
            obj = new JsonObject();
        }
        if (lingquStatus != null) {
            obj.addProperty(KEY_LINGQU_STATUS, lingquStatus);
        }
        if (lqUsers != null) {
            JsonArray arr = new JsonArray();
            for (String id : lqUsers) {
                if (!TextUtils.isEmpty(id)) {
                    arr.add(id);
                }
            }
            obj.add(KEY_LQ_USERS, arr);
        }
        return obj.toString();
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
