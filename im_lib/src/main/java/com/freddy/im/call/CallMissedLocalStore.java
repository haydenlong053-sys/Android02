package com.freddy.im.call;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.cboda.baselibrary.utils.MMKVBroadUtils;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 本机记录来电 / 振铃超时，纠正通话记录文案：
 * <ul>
 *   <li>自己拨打超时 → 对方未接听</li>
 *   <li>对方拨打、自己未接 → 未接听</li>
 *   <li>被叫收到主叫主动取消 → 对方已取消</li>
 * </ul>
 */
public final class CallMissedLocalStore {

    private static final String MMKV_KEY = "im_call_role_marks_v1";
    private static final long TTL_MS = 24L * 60L * 60L * 1000L;
    private static final int MAX_ENTRIES = 80;

    public static final int ROLE_INCOMING = 1;
    public static final int ROLE_TIMEOUT_CALLEE = 2;
    public static final int ROLE_TIMEOUT_CALLER = 3;

    private CallMissedLocalStore() {
    }

    /** 收到来电 INVITE：标记本机为该通被叫。 */
    public static void markIncoming(@Nullable String callId, @Nullable String peerUserId) {
        long now = System.currentTimeMillis();
        putMark(callId, ROLE_INCOMING, now);
        putMark(peerKey(peerUserId), ROLE_INCOMING, now);
    }

    /** 主叫振铃超时。 */
    public static void markTimeoutAsCaller(@Nullable String callId, @Nullable String peerUserId) {
        long now = System.currentTimeMillis();
        putMark(callId, ROLE_TIMEOUT_CALLER, now);
        putMark(peerKey(peerUserId), ROLE_TIMEOUT_CALLER, now);
        // 兼容旧接口
        putMark(roomKey(callId), ROLE_TIMEOUT_CALLER, now);
    }

    /** 被叫侧：主叫因超时取消（或振铃过久后的 CANCEL）。 */
    public static void markTimeoutAsCallee(@Nullable String callId, @Nullable String peerUserId) {
        long now = System.currentTimeMillis();
        putMark(callId, ROLE_TIMEOUT_CALLEE, now);
        putMark(peerKey(peerUserId), ROLE_TIMEOUT_CALLEE, now);
        putMark(roomKey(callId), ROLE_TIMEOUT_CALLEE, now);
    }

    /** @deprecated 使用 {@link #markTimeoutAsCaller} / {@link #markTimeoutAsCallee} */
    @Deprecated
    public static void markTimeoutMissed(@Nullable String callId) {
        markTimeoutAsCallee(callId, null);
    }

    public static boolean isTimeoutMissed(@Nullable String callId) {
        int role = getRole(callId);
        return role == ROLE_TIMEOUT_CALLEE || role == ROLE_TIMEOUT_CALLER;
    }

    /** 来电标记是否已持续超过指定时长（用于无 timeout payload 时判断振铃超时）。 */
    public static boolean isIncomingOlderThan(@Nullable String callId,
                                              @Nullable String peerUserId,
                                              long minAgeMs) {
        long at = Math.max(getMarkAt(callId), getMarkAt(peerKey(peerUserId)));
        if (at <= 0L) {
            return false;
        }
        return System.currentTimeMillis() - at >= minAgeMs;
    }

    public static boolean hasRecentTimeoutMissed() {
        return hasRoleRecently(ROLE_TIMEOUT_CALLEE) || hasRoleRecently(ROLE_TIMEOUT_CALLER);
    }

    public static boolean hasRecentTimeoutCaller() {
        return hasRoleRecently(ROLE_TIMEOUT_CALLER);
    }

    public static boolean hasRecentTimeoutCallee() {
        return hasRoleRecently(ROLE_TIMEOUT_CALLEE);
    }

    /**
     * 是否为本机「该通」被叫。
     * <p>
     * 仅按 callId / room 键判断，不用 peer 键：peer 标记会污染同一对端后续主叫取消文案
     * （自己拨打取消被误判成「对方已取消」）。
     */
    public static boolean wasIncoming(@Nullable String callId, @Nullable String peerUserId) {
        return wasIncomingCall(callId);
    }

    /** 仅按 callId / room:callId 判断本机是否曾作为该通被叫。 */
    public static boolean wasIncomingCall(@Nullable String callId) {
        if (TextUtils.isEmpty(callId)) {
            return false;
        }
        int r1 = getRole(callId);
        int r2 = getRole(roomKey(callId));
        return isIncomingRole(r1) || isIncomingRole(r2);
    }

    public static boolean isTimeoutCallee(@Nullable String callId, @Nullable String peerUserId) {
        return getRole(callId) == ROLE_TIMEOUT_CALLEE
                || getRole(peerKey(peerUserId)) == ROLE_TIMEOUT_CALLEE
                || getRole(roomKey(callId)) == ROLE_TIMEOUT_CALLEE;
    }

    public static boolean isTimeoutCaller(@Nullable String callId, @Nullable String peerUserId) {
        return getRole(callId) == ROLE_TIMEOUT_CALLER
                || getRole(peerKey(peerUserId)) == ROLE_TIMEOUT_CALLER
                || getRole(roomKey(callId)) == ROLE_TIMEOUT_CALLER;
    }

    private static boolean isIncomingRole(int role) {
        return role == ROLE_INCOMING
                || role == ROLE_TIMEOUT_CALLEE;
    }

    @Nullable
    private static String peerKey(@Nullable String peerUserId) {
        if (TextUtils.isEmpty(peerUserId)) {
            return null;
        }
        return "peer:" + peerUserId.trim();
    }

    @Nullable
    private static String roomKey(@Nullable String callId) {
        if (TextUtils.isEmpty(callId)) {
            return null;
        }
        return "room:" + callId.trim();
    }

    private static void putMark(@Nullable String key, int role, long atMs) {
        if (TextUtils.isEmpty(key)) {
            return;
        }
        try {
            JSONObject root = loadRoot();
            JSONArray arr = root.optJSONArray("items");
            if (arr == null) {
                arr = new JSONArray();
            }
            JSONArray next = new JSONArray();
            String id = key.trim();
            long expireBefore = atMs - TTL_MS;
            for (int i = 0; i < arr.length(); i++) {
                JSONObject item = arr.optJSONObject(i);
                if (item == null) {
                    continue;
                }
                String itemId = item.optString("id", "");
                long t = item.optLong("at", 0L);
                if (TextUtils.isEmpty(itemId) || t < expireBefore) {
                    continue;
                }
                if (id.equals(itemId)) {
                    continue;
                }
                next.put(item);
            }
            JSONObject neu = new JSONObject();
            neu.put("id", id);
            neu.put("role", role);
            neu.put("at", atMs);
            next.put(neu);
            while (next.length() > MAX_ENTRIES) {
                JSONArray trimmed = new JSONArray();
                for (int i = 1; i < next.length(); i++) {
                    trimmed.put(next.get(i));
                }
                next = trimmed;
            }
            root.put("items", next);
            MMKVBroadUtils.Companion.saveSqlStrData(MMKV_KEY, root.toString());
        } catch (Exception ignored) {
            // ignore persist errors
        }
    }

    private static int getRole(@Nullable String key) {
        JSONObject item = findItem(key);
        return item == null ? 0 : item.optInt("role", 0);
    }

    private static long getMarkAt(@Nullable String key) {
        JSONObject item = findItem(key);
        if (item == null) {
            return 0L;
        }
        long at = item.optLong("at", 0L);
        if (at < System.currentTimeMillis() - TTL_MS) {
            return 0L;
        }
        return at;
    }

    @Nullable
    private static JSONObject findItem(@Nullable String key) {
        if (TextUtils.isEmpty(key)) {
            return null;
        }
        try {
            JSONObject root = loadRoot();
            JSONArray arr = root.optJSONArray("items");
            if (arr == null) {
                return null;
            }
            String id = key.trim();
            long expireBefore = System.currentTimeMillis() - TTL_MS;
            for (int i = arr.length() - 1; i >= 0; i--) {
                JSONObject item = arr.optJSONObject(i);
                if (item == null) {
                    continue;
                }
                if (!id.equals(item.optString("id", ""))) {
                    continue;
                }
                if (item.optLong("at", 0L) < expireBefore) {
                    return null;
                }
                return item;
            }
        } catch (Exception ignored) {
            // ignore
        }
        return null;
    }

    private static boolean hasRoleRecently(int role) {
        try {
            JSONObject root = loadRoot();
            JSONArray arr = root.optJSONArray("items");
            if (arr == null) {
                return false;
            }
            long expireBefore = System.currentTimeMillis() - 3L * 60L * 1000L;
            for (int i = arr.length() - 1; i >= 0; i--) {
                JSONObject item = arr.optJSONObject(i);
                if (item == null) {
                    continue;
                }
                if (item.optInt("role", 0) == role && item.optLong("at", 0L) >= expireBefore) {
                    return true;
                }
            }
        } catch (Exception ignored) {
            // ignore
        }
        return false;
    }

    @Nullable
    private static JSONObject loadRoot() {
        try {
            String raw = MMKVBroadUtils.Companion.getSqlStrData(MMKV_KEY, "");
            if (TextUtils.isEmpty(raw)) {
                return new JSONObject();
            }
            return new JSONObject(raw);
        } catch (Exception e) {
            return new JSONObject();
        }
    }
}
