package com.freddy.im.call;

import android.text.TextUtils;

import androidx.annotation.Nullable;

import com.cboda.baselibrary.utils.MMKVBroadUtils;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * 本机记录 1v1 已接通通话时长，用于服务端会话消息未带 duration 时
 * 仍展示「通话时长 mm:ss」。
 */
public final class CallDurationLocalStore {

    private static final String MMKV_KEY = "im_call_duration_marks_v1";
    private static final long TTL_MS = 24L * 60L * 60L * 1000L;
    private static final int MAX_ENTRIES = 80;

    private CallDurationLocalStore() {
    }

    public static void mark(@Nullable String callId, int durationSec) {
        if (TextUtils.isEmpty(callId) || durationSec <= 0) {
            return;
        }
        long now = System.currentTimeMillis();
        put(callId.trim(), durationSec, now);
        put("room:" + callId.trim(), durationSec, now);
    }

    public static int get(@Nullable String callId) {
        if (TextUtils.isEmpty(callId)) {
            return 0;
        }
        int d1 = read(callId.trim());
        if (d1 > 0) {
            return d1;
        }
        return read("room:" + callId.trim());
    }

    private static void put(String id, int durationSec, long atMs) {
        try {
            JSONObject root = loadRoot();
            JSONArray arr = root.optJSONArray("items");
            if (arr == null) {
                arr = new JSONArray();
            }
            JSONArray next = new JSONArray();
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
            neu.put("duration", durationSec);
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
            // ignore
        }
    }

    private static int read(String id) {
        try {
            JSONObject root = loadRoot();
            JSONArray arr = root.optJSONArray("items");
            if (arr == null) {
                return 0;
            }
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
                    return 0;
                }
                return Math.max(0, item.optInt("duration", 0));
            }
        } catch (Exception ignored) {
            // ignore
        }
        return 0;
    }

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
