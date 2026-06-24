package com.freddy.chat.mention;

import android.text.Spannable;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 文本消息气泡内 @ 昵称高亮。
 */
public final class AtMentionSpanUtil {

    private AtMentionSpanUtil() {
    }

    public static void applyMentionHighlight(@NonNull Spannable spannable,
                                             @Nullable String customInfoJson,
                                             int color) {
        if (TextUtils.isEmpty(customInfoJson)) {
            return;
        }
        JsonObject custom = parseJsonObject(customInfoJson);
        if (custom == null || !custom.has("info") || !custom.get("info").isJsonArray()) {
            return;
        }
        JsonArray info = custom.getAsJsonArray("info");
        for (JsonElement element : info) {
            if (!element.isJsonObject()) {
                continue;
            }
            JsonObject item = element.getAsJsonObject();
            String type = item.has("type") ? item.get("type").getAsString() : "";
            if ("atAll".equalsIgnoreCase(type)) {
                highlightAllTokens(spannable, color);
                continue;
            }
            if ("at".equalsIgnoreCase(type) && item.has("value")) {
                String value = item.get("value").getAsString();
                if (!TextUtils.isEmpty(value)) {
                    highlightToken(spannable, "@" + value, color);
                }
                continue;
            }
            if ("sysAt".equalsIgnoreCase(type) && item.has("value")) {
                String value = item.get("value").getAsString();
                if (!TextUtils.isEmpty(value)) {
                    highlightToken(spannable, "@" + value, color);
                }
            }
        }
    }

    private static void highlightAllTokens(@NonNull Spannable spannable, int color) {
        highlightToken(spannable, "@所有人", color);
        highlightToken(spannable, "@所有人 ", color);
    }

    private static void highlightToken(@NonNull Spannable spannable, @NonNull String token, int color) {
        String text = spannable.toString();
        int index = 0;
        while ((index = text.indexOf(token, index)) >= 0) {
            spannable.setSpan(
                    new ForegroundColorSpan(color),
                    index,
                    index + token.length(),
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            index += token.length();
        }
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
