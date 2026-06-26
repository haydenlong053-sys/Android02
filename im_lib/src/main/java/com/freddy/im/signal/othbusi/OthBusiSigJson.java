package com.freddy.im.signal.othbusi;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

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
}
