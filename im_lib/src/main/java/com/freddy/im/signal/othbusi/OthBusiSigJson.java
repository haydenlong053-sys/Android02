package com.freddy.im.signal.othbusi;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * OthBusiSig.data 常用 JSON 字段读取。
 */
public final class OthBusiSigJson {

    private OthBusiSigJson() {
    }

    @Nullable
    public static String readGroupId(@Nullable String data) {
        return readString(data, "groupId");
    }

    /** 会议室业务统一群 ID 字段；兼容误写为 {@code groupId} 的场景。 */
    @Nullable
    public static String readGroupIdNo(@Nullable String data) {
        String groupIdNo = readString(data, "groupIdNo");
        if (!TextUtils.isEmpty(groupIdNo)) {
            return groupIdNo;
        }
        return readGroupId(data);
    }

    @Nullable
    public static String readString(@Nullable String data, @NonNull String field) {
        if (TextUtils.isEmpty(data) || TextUtils.isEmpty(field)) {
            return null;
        }
        try {
            JsonObject obj = JsonParser.parseString(data).getAsJsonObject();
            if (obj == null || !obj.has(field) || obj.get(field).isJsonNull()) {
                return null;
            }
            String value = obj.get(field).getAsString();
            return TextUtils.isEmpty(value) ? null : value;
        } catch (Exception ignored) {
            return null;
        }
    }

    /**
     * 读取整型字段；不存在或解析失败时返回 {@code defaultValue}。
     */
    public static int readInt(@Nullable String data, @NonNull String field, int defaultValue) {
        if (TextUtils.isEmpty(data) || TextUtils.isEmpty(field)) {
            return defaultValue;
        }
        try {
            JsonObject obj = JsonParser.parseString(data).getAsJsonObject();
            if (obj == null || !obj.has(field) || obj.get(field).isJsonNull()) {
                return defaultValue;
            }
            return obj.get(field).getAsInt();
        } catch (Exception ignored) {
            return defaultValue;
        }
    }

    @NonNull
    public static List<String> readStringList(@Nullable String data, @NonNull String field) {
        if (TextUtils.isEmpty(data) || TextUtils.isEmpty(field)) {
            return Collections.emptyList();
        }
        try {
            JsonObject obj = JsonParser.parseString(data).getAsJsonObject();
            if (obj == null || !obj.has(field) || obj.get(field).isJsonNull()) {
                return Collections.emptyList();
            }
            JsonArray arr = obj.getAsJsonArray(field);
            if (arr == null || arr.isEmpty()) {
                return Collections.emptyList();
            }
            List<String> list = new ArrayList<>(arr.size());
            for (int i = 0; i < arr.size(); i++) {
                if (arr.get(i).isJsonNull()) {
                    continue;
                }
                String value = arr.get(i).getAsString();
                if (!TextUtils.isEmpty(value)) {
                    list.add(value.trim());
                }
            }
            return list;
        } catch (Exception ignored) {
            return Collections.emptyList();
        }
    }
}
