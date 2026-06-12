package com.freddy.chat.collect;

import android.content.Context;

import androidx.annotation.NonNull;

import com.freddy.chat.MessageForwardActivity;
import com.freddy.im.bean.AppMessage;

/**
 * 收藏项转发到其它会话（自研 {@link MessageForwardActivity}）。
 */
public final class CollectForwardHelper {

    private CollectForwardHelper() {
    }

    public static void startForward(@NonNull Context context, @NonNull AppMessage message) {
        context.startActivity(MessageForwardActivity.buildInlineForwardIntent(context, message));
    }
}
