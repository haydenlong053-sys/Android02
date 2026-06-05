package com.freddy.chat;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.http.model.im.SessionItem;

/**
 * 通过 Intent 以 {@link java.io.Serializable} 传递 {@link SessionItem}。
 */
public final class SessionItemIntent {

    public static final String EXTRA_SESSION_ITEM = "extra_session_item";

    private SessionItemIntent() {
    }

    @NonNull
    public static Bundle newArguments(@NonNull SessionItem item) {
        Bundle bundle = new Bundle();
        bundle.putSerializable(EXTRA_SESSION_ITEM, item);
        return bundle;
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
        if (intent == null || item == null) {
            return;
        }
        intent.putExtra(EXTRA_SESSION_ITEM, item);
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
