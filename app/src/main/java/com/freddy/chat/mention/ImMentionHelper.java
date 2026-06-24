package com.freddy.chat.mention;

import android.content.Context;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextUtils;

import androidx.annotation.NonNull;

import com.cboda.chatui.conversation.mention.MentionBroadSpan;

/**
 * 构造带 {@link MentionBroadSpan} 的 @ 输入片段。
 */
public final class ImMentionHelper {

    private ImMentionHelper() {
    }

    @NonNull
    public static SpannableString buildMentionSpannable(@NonNull String userId,
                                                        @NonNull String displayName) {
        String text = "@" + displayName.trim() + " ";
        SpannableString spannable = new SpannableString(text);
        spannable.setSpan(new MentionBroadSpan(userId), 0, text.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    @NonNull
    public static SpannableString buildMentionAllSpannable(@NonNull Context context) {
        String label = context.getString(com.cboda.chatui.R.string.msg_mention_all).trim();
        String text = "@" + label+" ";
        SpannableString spannable = new SpannableString(text);
        spannable.setSpan(new MentionBroadSpan(true), 0, text.length(), Spanned.SPAN_INCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    @NonNull
    public static String resolveDisplayName(@NonNull String userId, @NonNull String displayName) {
        return TextUtils.isEmpty(displayName) ? userId : displayName.trim();
    }
}
