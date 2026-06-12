package com.freddy.chat.collect;

import android.content.Context;
import android.content.Intent;

import androidx.annotation.NonNull;

/**
 * 自研收藏夹页 Intent 参数（对齐野火 {@code MyCollectBroadActivity} 的 uid / qunid）。
 */
public final class CollectIntent {

    public static final String EXTRA_UID = "uid";
    public static final String EXTRA_QUN_ID = "qunid";
    public static final String EXTRA_SESSION_ID = "sessionId";

    private CollectIntent() {
    }

    @NonNull
    public static Intent build(@NonNull Context context, @NonNull String uid, @NonNull String qunid, @NonNull String sessionId) {
        Intent intent = new Intent(context, MyCollectActivity.class);
        intent.putExtra(EXTRA_UID, uid);
        intent.putExtra(EXTRA_QUN_ID, qunid);
        intent.putExtra(EXTRA_SESSION_ID, sessionId);
        return intent;
    }
}
