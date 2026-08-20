package com.freddy.im.http;

import android.text.TextUtils;

import com.cboda.baselibrary.utils.MMKVBroadUtils;

/**
 * 会话列表增量水位：下次 {@code getList} 的 {@code startTime}。
 * key 带 userId，换号互不影响；成功后只写入服务端 {@code syncTime}。
 */
public final class ConversationListSyncStore {

    private static final String KEY_PREFIX = "im_conv_list_start_time_";

    private ConversationListSyncStore() {
    }

    public static long resolveStartTime() {
        String userId = MMKVBroadUtils.Companion.getRealUserId();
        if (TextUtils.isEmpty(userId)) {
            return 0L;
        }
        return MMKVBroadUtils.Companion.getSqlLongData(key(userId), 0L);
    }

    /**
     * 仅在 getList 成功后调用。{@code syncTime <= 0} 不写。
     * 全量以服务端时间为准覆盖；增量只升不降。
     */
    public static void saveSyncTime(long syncTime, boolean fullSync) {
        if (syncTime <= 0L) {
            return;
        }
        String userId = MMKVBroadUtils.Companion.getRealUserId();
        if (TextUtils.isEmpty(userId)) {
            return;
        }
        String storeKey = key(userId);
        if (!fullSync) {
            long old = MMKVBroadUtils.Companion.getSqlLongData(storeKey, 0L);
            if (syncTime <= old) {
                return;
            }
        }
        MMKVBroadUtils.Companion.saveSqlLongData(storeKey, syncTime);
    }

    private static String key(String userId) {
        return KEY_PREFIX + userId;
    }
}
