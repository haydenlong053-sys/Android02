package com.freddy.chat.mention;

import android.text.Editable;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cboda.chatui.conversation.mention.MentionBroadSpan;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

/**
 * 从输入框 {@link MentionBroadSpan} 构造自研 IM {@code customInfo} JSON。
 */
public final class AtMentionCustomInfoBuilder {

    private AtMentionCustomInfoBuilder() {
    }

    public static boolean hasMentions(@Nullable Editable editable) {
        if (editable == null) {
            return false;
        }
        MentionBroadSpan[] spans = editable.getSpans(0, editable.length(), MentionBroadSpan.class);
        return spans != null && spans.length > 0;
    }

    /**
     * 去掉 @ 段落后的正文（展示时由 {@code customInfo} 的 at 条目补全 @ 昵称）。
     */
    @NonNull
    public static String extractBodyText(@NonNull Editable editable) {
        MentionBroadSpan[] spans = editable.getSpans(0, editable.length(), MentionBroadSpan.class);
        if (spans == null || spans.length == 0) {
            return editable.toString().trim();
        }
        StringBuilder sb = new StringBuilder(editable.toString());
        Arrays.sort(spans, (a, b) -> Integer.compare(editable.getSpanStart(b), editable.getSpanStart(a)));
        for (MentionBroadSpan span : spans) {
            int start = editable.getSpanStart(span);
            int end = editable.getSpanEnd(span);
            if (start >= 0 && end > start && end <= sb.length()) {
                sb.delete(start, end);
            }
        }
        return sb.toString().trim();
    }

    @Nullable
    public static String buildCustomInfoFromSpans(@NonNull Editable editable) {
        MentionBroadSpan[] spans = editable.getSpans(0, editable.length(), MentionBroadSpan.class);
        if (spans == null || spans.length == 0) {
            return null;
        }
        JsonArray info = new JsonArray();
        Set<String> addedAtUserIds = new HashSet<>();
        Arrays.sort(spans, (a, b) -> Integer.compare(editable.getSpanStart(a), editable.getSpanStart(b)));
        for (MentionBroadSpan span : spans) {
            if (span.isMentionAll()) {
                JsonObject atAll = new JsonObject();
                atAll.addProperty("type", "atAll");
                info.add(atAll);
                continue;
            }
            String uid = span.getUid();
            if (TextUtils.isEmpty(uid)) {
                continue;
            }
            int start = editable.getSpanStart(span);
            int end = editable.getSpanEnd(span);
            String label = editable.subSequence(start, end).toString().trim();
            if (label.startsWith("@")) {
                label = label.substring(1).trim();
            }
//            JsonObject sysAt = new JsonObject();
//            sysAt.addProperty("type", "sysAt");
//            sysAt.addProperty("key", uid);
//            sysAt.addProperty("value", label);
//            info.add(sysAt);
            if (!addedAtUserIds.contains(uid)) {
                JsonObject at = new JsonObject();
                at.addProperty("type", "at");
                at.addProperty("userId", uid);
                at.addProperty("name", label);
                info.add(at);
                addedAtUserIds.add(uid);
            }
        }
        if (info.size() == 0) {
            return null;
        }
        JsonObject root = new JsonObject();
        root.add("info", info);
        return root.toString();
    }
}
