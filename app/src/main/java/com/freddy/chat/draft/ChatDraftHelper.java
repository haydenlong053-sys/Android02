package com.freddy.chat.draft;

import android.text.Editable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cboda.chatui.conversation.mention.MentionBroadSpan;
import com.freddy.chat.utils.AppGson;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 草稿 JSON 编解码（content + mentions），对齐野火 Draft 语义、字段自研化。
 */
public final class ChatDraftHelper {

    private ChatDraftHelper() {
    }

    @Nullable
    public static String toJson(@Nullable Editable editable) {
        if (editable == null) {
            return null;
        }
        String content = editable.toString();
        if (TextUtils.isEmpty(content.trim()) && !hasMentionSpans(editable)) {
            return null;
        }
        DraftBody body = new DraftBody();
        body.content = content;
        body.mentions = extractMentions(editable);
        return AppGson.get().toJson(body);
    }

    @NonNull
    public static String toPlainJson(@NonNull String content) {
        DraftBody body = new DraftBody();
        body.content = content;
        return AppGson.get().toJson(body);
    }

    @Nullable
    public static CharSequence toEditable(@Nullable String draftJson) {
        DraftBody body = fromJson(draftJson);
        if (body == null || (TextUtils.isEmpty(body.content) && (body.mentions == null || body.mentions.isEmpty()))) {
            return null;
        }
        String content = body.content != null ? body.content : "";
        SpannableStringBuilder builder = new SpannableStringBuilder(content);
        if (body.mentions != null) {
            for (MentionRef mention : body.mentions) {
                if (mention == null || mention.start < 0 || mention.end > content.length()
                        || mention.start >= mention.end) {
                    continue;
                }
                MentionBroadSpan span;
                if (mention.mentionAll) {
                    span = new MentionBroadSpan(true);
                } else if (!TextUtils.isEmpty(mention.uid)) {
                    span = new MentionBroadSpan(mention.uid);
                } else {
                    continue;
                }
                builder.setSpan(span, mention.start, mention.end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
            }
        }
        return builder;
    }

    @Nullable
    public static String previewText(@Nullable String draftJson) {
        DraftBody body = fromJson(draftJson);
        if (body == null || TextUtils.isEmpty(body.content)) {
            return null;
        }
        String trimmed = body.content.trim();
        return TextUtils.isEmpty(trimmed) ? null : trimmed;
    }

    @Nullable
    private static DraftBody fromJson(@Nullable String draftJson) {
        if (TextUtils.isEmpty(draftJson)) {
            return null;
        }
        try {
            DraftBody body = AppGson.get().fromJson(draftJson, DraftBody.class);
            if (body != null) {
                return body;
            }
        } catch (Exception ignored) {
        }
        DraftBody fallback = new DraftBody();
        fallback.content = draftJson;
        return fallback;
    }

    private static boolean hasMentionSpans(@NonNull Editable editable) {
        MentionBroadSpan[] spans = editable.getSpans(0, editable.length(), MentionBroadSpan.class);
        return spans != null && spans.length > 0;
    }

    @Nullable
    private static List<MentionRef> extractMentions(@NonNull Editable editable) {
        MentionBroadSpan[] spans = editable.getSpans(0, editable.length(), MentionBroadSpan.class);
        if (spans == null || spans.length == 0) {
            return null;
        }
        Arrays.sort(spans, (a, b) -> Integer.compare(editable.getSpanStart(a), editable.getSpanStart(b)));
        List<MentionRef> list = new ArrayList<>(spans.length);
        for (MentionBroadSpan span : spans) {
            MentionRef ref = new MentionRef();
            ref.start = editable.getSpanStart(span);
            ref.end = editable.getSpanEnd(span);
            ref.mentionAll = span.isMentionAll();
            ref.uid = span.getUid();
            list.add(ref);
        }
        return list;
    }

    private static final class DraftBody {
        @SerializedName("content")
        String content;
        @SerializedName("mentions")
        List<MentionRef> mentions;
    }

    private static final class MentionRef {
        @SerializedName("start")
        int start;
        @SerializedName("end")
        int end;
        @SerializedName("uid")
        String uid;
        @SerializedName("mentionAll")
        boolean mentionAll;
    }
}
