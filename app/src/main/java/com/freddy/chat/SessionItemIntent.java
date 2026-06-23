package com.freddy.chat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.http.model.im.SessionItem;

/**
 * 通过 Intent 以 {@link java.io.Serializable} 传递 {@link SessionItem}。
 */
public final class SessionItemIntent {

    public static final String EXTRA_SESSION_ITEM = "extra_session_item";
    public static final String EXTRA_FOCUS_MSG_ID = "extra_focus_msg_id";

    private SessionItemIntent() {
    }

    @NonNull
    public static Bundle newArguments(@NonNull SessionItem item) {
        return newArguments(item, null);
    }

    @NonNull
    public static Bundle newArguments(@NonNull SessionItem item, @Nullable String focusMsgId) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_SESSION_ITEM, item);
        if (!TextUtils.isEmpty(focusMsgId)) {
            bundle.putString(EXTRA_FOCUS_MSG_ID, focusMsgId);
        }
        return bundle;
    }

    @Nullable
    public static String getFocusMsgId(@Nullable Bundle arguments) {
        if (arguments == null) {
            return null;
        }
        return arguments.getString(EXTRA_FOCUS_MSG_ID);
    }

    @Nullable
    public static SessionItem getSessionItem(@Nullable Bundle arguments) {
        if (arguments == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return arguments.getSerializable(EXTRA_SESSION_ITEM, SessionItem.class);
        }
        Object extra = arguments.getSerializable(EXTRA_SESSION_ITEM);
        if (extra instanceof SessionItem) {
            return (SessionItem) extra;
        }
        return null;
    }

    public static void putSessionItem(@Nullable Intent intent, @Nullable SessionItem item) {
        putSessionItem(intent, item, null);
    }

    public static void putSessionItem(@Nullable Intent intent,
                                      @Nullable SessionItem item,
                                      @Nullable String focusMsgId) {
        if (intent == null || item == null) {
            return;
        }
        intent.putExtra(EXTRA_SESSION_ITEM, item);
        if (!TextUtils.isEmpty(focusMsgId)) {
            intent.putExtra(EXTRA_FOCUS_MSG_ID, focusMsgId);
        }
    }

    @Nullable
    public static String getFocusMsgId(@Nullable Intent intent) {
        if (intent == null) {
            return null;
        }
        return intent.getStringExtra(EXTRA_FOCUS_MSG_ID);
    }

    @Nullable
    public static SessionItem getSessionItem(@Nullable Intent intent) {
        if (intent == null) {
            return null;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            return intent.getSerializableExtra(EXTRA_SESSION_ITEM, SessionItem.class);
        }
        Object extra = intent.getSerializableExtra(EXTRA_SESSION_ITEM);
        if (extra instanceof SessionItem) {
            return (SessionItem) extra;
        }
        return null;
    }
}
