package com.freddy.chat.mention;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.freddy.im.MessageType;
import com.freddy.im.http.model.im.SessionItem;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 会话列表「有人@我」判断（基于最近一条消息的 {@code lastCustomInfo}）。
 */
public final class AtMentionChecker {

    private AtMentionChecker() {
    }

    public static boolean isMentioningMe(@Nullable SessionItem session,
                                         @Nullable String currentUserId) {
        if (session == null || TextUtils.isEmpty(currentUserId)) {
            return false;
        }
        if (session.getConversation() != MessageType.GROUP_CHAT.getMsgType()) {
            return false;
        }
        Integer unread = session.getUnreadCount();
        if (unread == null || unread <= 0) {
            return false;
        }
        return isMentioningUser(session.getLastCustomInfo(), currentUserId);
    }

    public static boolean isMentioningUser(@Nullable String customInfoJson,
                                           @Nullable String currentUserId) {
        if (TextUtils.isEmpty(customInfoJson) || TextUtils.isEmpty(currentUserId)) {
            return false;
        }
        JsonObject custom = parseJsonObject(customInfoJson);
        if (custom == null || !custom.has("info") || !custom.get("info").isJsonArray()) {
            return false;
        }
        JsonArray info = custom.getAsJsonArray("info");
        for (JsonElement element : info) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject item = element.getAsJsonObject();
            String type = item.has("type") ? item.get("type").getAsString() : "";
            if ("atAll".equalsIgnoreCase(type)) {
                return true;
            }
            if ("at".equalsIgnoreCase(type) && item.has("userId")) {
                String userId = item.get("userId").getAsString();
                if (currentUserId.equals(userId)) {
                    return true;
                }
            }
        }
        return false;
    }

    @Nullable
    private static JsonObject parseJsonObject(@Nullable String raw) {
        if (TextUtils.isEmpty(raw)) {
            return null;
        }
        String trimmed = raw.trim();
        if (!trimmed.startsWith("{")) {
            return null;
        }
        try {
            return JsonParser.parseString(trimmed).getAsJsonObject();
        } catch (Exception ignored) {
            return null;
        }
    }
}
