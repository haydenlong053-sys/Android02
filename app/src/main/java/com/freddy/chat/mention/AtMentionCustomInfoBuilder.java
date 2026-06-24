package com.freddy.chat.mention;

import android.text.Editable;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cboda.chatui.conversation.mention.MentionBroadSpan;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

import java.util.Arrays;

/**
 * 从输入框 {@link MentionBroadSpan} 构造自研 IM @ 报文。
 * <p>
 * content：{@code {"msg":"@${userId} 正文"}}<br>
 * customInfo：{@code {"info":[{"type":"at","key":"userId","value":"群昵称"}]}}
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
     * 构造文本消息 {@code content.msg}：@ 段替换为 {@code @${userId}}，@所有人 为 {@code @所有人}。
     */
    @NonNull
    public static String buildMessageText(@NonNull Editable editable) {
        MentionBroadSpan[] spans = editable.getSpans(0, editable.length(), MentionBroadSpan.class);
        if (spans == null || spans.length == 0) {
            return editable.toString().trim();
        }
        Arrays.sort(spans, (a, b) -> Integer.compare(editable.getSpanStart(a), editable.getSpanStart(b)));
        StringBuilder result = new StringBuilder();
        int lastEnd = 0;
        for (MentionBroadSpan span : spans) {
            int start = editable.getSpanStart(span);
            int end = editable.getSpanEnd(span);
            if (end <= lastEnd) {
                continue;
            }
            if (start > lastEnd) {
                result.append(editable.subSequence(lastEnd, start));
            }
            if (span.isMentionAll()) {
                result.append("@所有人 ");
            } else {
                String uid = span.getUid();
                if (!TextUtils.isEmpty(uid)) {
                    result.append("@${").append(uid).append("} ");
                }
            }
            lastEnd = end;
        }
        if (lastEnd < editable.length()) {
            result.append(editable.subSequence(lastEnd, editable.length()));
        }
        return result.toString().trim();
    }

    @Nullable
    public static String buildCustomInfoFromSpans(@NonNull Editable editable) {
        MentionBroadSpan[] spans = editable.getSpans(0, editable.length(), MentionBroadSpan.class);
        if (spans == null || spans.length == 0) {
            return null;
        }
        JsonArray info = new JsonArray();
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
            JsonObject at = new JsonObject();
            at.addProperty("type", "at");
            at.addProperty("key", uid);
            at.addProperty("value", label);
            info.add(at);
        }
        if (info.size() == 0) {
            return null;
        }
        JsonObject root = new JsonObject();
        root.add("info", info);
        return root.toString();
    }
}
