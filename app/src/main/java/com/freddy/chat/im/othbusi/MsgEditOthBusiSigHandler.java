package com.freddy.chat.im.othbusi;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.cboda.baselibrary.utils.ELogUtilKt;
import com.freddy.chat.im.MessageProcessor;
import com.freddy.im.db.ImLocalStore;
import com.freddy.im.signal.OthBusiSigCodes;
import com.freddy.im.signal.othbusi.OthBusiSigHandler;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 消息编辑推送：code=6001，
 * data={"kind":"msg_edit","messageId":"...","content":"{...}","customInfo":"{...}","editedAt":0,"status":5}。
 * 旧客户端不认识应忽略，勿当撤回。
 */
public final class MsgEditOthBusiSigHandler implements OthBusiSigHandler {

    private static final String TAG = "OthBusiSig.MsgEdit";

    @Override
    public int code() {
        return OthBusiSigCodes.MSG_EDIT;
    }

    @Override
    public boolean handle(@Nullable String data) {
        if (TextUtils.isEmpty(data)) {
            return false;
        }
        try {
            JsonObject obj = JsonParser.parseString(data).getAsJsonObject();
            String kind = readString(obj, "kind");
            if (!TextUtils.isEmpty(kind) && !"msg_edit".equals(kind)) {
                ELogUtilKt.eLogD(TAG, "ignore kind=" + kind);
                return false;
            }
            String messageId = readString(obj, "messageId");
            String content = readString(obj, "content");
            if (TextUtils.isEmpty(messageId) || TextUtils.isEmpty(content)) {
                return false;
            }
            String customInfo = readString(obj, "customInfo");
            long editedAt = readLong(obj, "editedAt");
            ELogUtilKt.eLogD(TAG, "messageId=" + messageId + " editedAt=" + editedAt);
            ImLocalStore.applyMessageEdit(messageId, content, customInfo, editedAt, updated -> {
                if (updated != null) {
                    MessageProcessor.getInstance().notifyMessageUpdated(updated);
                }
            });
            return true;
        } catch (Exception e) {
            ELogUtilKt.eLogD(TAG, "parse fail: " + e.getMessage());
            return false;
        }
    }

    @Nullable
    private static String readString(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) {
            return null;
        }
        try {
            JsonElement el = obj.get(key);
            if (el.isJsonPrimitive()) {
                String value = el.getAsString();
                return TextUtils.isEmpty(value) ? null : value;
            }
            // content / customInfo 偶发被解析成对象时再转回字符串
            return el.toString();
        } catch (Exception ignored) {
            return null;
        }
    }

    private static long readLong(JsonObject obj, String key) {
        if (obj == null || !obj.has(key) || obj.get(key).isJsonNull()) {
            return 0L;
        }
        try {
            JsonElement el = obj.get(key);
            if (el.isJsonPrimitive() && el.getAsJsonPrimitive().isNumber()) {
                return el.getAsLong();
            }
            String raw = el.getAsString();
            if (TextUtils.isEmpty(raw)) {
                return 0L;
            }
            return Long.parseLong(raw.trim());
        } catch (Exception ignored) {
            return 0L;
        }
    }
}
