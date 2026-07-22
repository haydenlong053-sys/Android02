package com.freddy.chat.im.othbusi;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.cboda.baselibrary.utils.ELogUtilKt;
import com.freddy.im.db.ImLocalStore;
import com.freddy.im.signal.OthBusiSigCodes;
import com.freddy.im.signal.othbusi.OthBusiSigHandler;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 信用分推送：code=3001，
 * data=[{"u":"userId","s":"100.58"}, ...]（u=userId，s=信用分）。
 * 收到后更新本地群成员表 grade。
 */
public final class CreditScoreOthBusiSigHandler implements OthBusiSigHandler {

    private static final String TAG = "OthBusiSig.CreditScore";

    @Override
    public int code() {
        return OthBusiSigCodes.CREDIT_SCORE;
    }

    @Override
    public boolean handle(@Nullable String data) {
        if (TextUtils.isEmpty(data)) {
            return false;
        }
        try {
            JsonElement root = JsonParser.parseString(data);
            if (root == null || !root.isJsonArray()) {
                ELogUtilKt.eLogD(TAG, "data 非数组: " + data);
                return false;
            }
            JsonArray arr = root.getAsJsonArray();
            if (arr.isEmpty()) {
                return false;
            }
            boolean handled = false;
            for (int i = 0; i < arr.size(); i++) {
                JsonElement el = arr.get(i);
                if (el == null || !el.isJsonObject()) {
                    continue;
                }
                JsonObject obj = el.getAsJsonObject();
                String userId = readField(obj, "u");
                String score = readField(obj, "s");
                if (TextUtils.isEmpty(userId) || score == null) {
                    continue;
                }
                ELogUtilKt.eLogD(TAG, "userId=" + userId + " grade=" + score);
                ImLocalStore.updateGroupMemberGradeByUserIdThen(userId, score, null);
                handled = true;
            }
            return handled;
        } catch (Exception e) {
            ELogUtilKt.eLogD(TAG, "parse fail: " + e.getMessage());
            return false;
        }
    }

    @Nullable
    private static String readField(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) {
            return null;
        }
        try {
            JsonElement el = obj.get(key);
            if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) {
                return el.getAsString();
            }
            String value = el.getAsString();
            return TextUtils.isEmpty(value) ? null : value.trim();
        } catch (Exception ignored) {
            return null;
        }
    }
}
