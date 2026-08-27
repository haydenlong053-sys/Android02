package com.freddy.chat.draft;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.cboda.baselibrary.utils.MMKVBroadUtils;
import com.freddy.chat.event.CEventCenter;
import com.freddy.chat.event.Events;

import org.json.JSONArray;

import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 自研 IM 草稿：经 {@link MMKVBroadUtils} 按 userId+sessionId 键值存储，
 * 内存缓存供会话列表副标题快速读取。
 */
public final class ChatDraftStore {

    private static final String KEY_PREFIX = "im_draft_";
    private static final String INDEX_PREFIX = "im_draft_idx_";
    private static final int MAX_CONTENT_CHARS = 2000;

    /** sessionId -> 草稿纯文本摘要（列表用）；空串表示已确认无草稿 */
    private static final Map<String, String> previewCache = new ConcurrentHashMap<>();

    private ChatDraftStore() {
    }

    public static void save(@Nullable String sessionId, @Nullable String draftJson) {
        if (TextUtils.isEmpty(sessionId)) {
            return;
        }
        String userId = currentUserId();
        if (TextUtils.isEmpty(userId)) {
            return;
        }
        String dataKey = dataKey(userId, sessionId);
        if (TextUtils.isEmpty(draftJson)) {
            clearInternal(userId, sessionId, dataKey);
            return;
        }
        String truncated = truncateJsonIfNeeded(draftJson);
        MMKVBroadUtils.Companion.saveSqlStrData(dataKey, truncated);
        addToIndex(userId, sessionId);
        String preview = ChatDraftHelper.previewText(truncated);
        if (TextUtils.isEmpty(preview)) {
            clearInternal(userId, sessionId, dataKey);
            return;
        }
        previewCache.put(sessionId, preview);
        CEventCenter.dispatchEvent(Events.CHAT_DRAFT_UPDATED, 0, 0, sessionId);
    }

    public static void clear(@Nullable String sessionId) {
        save(sessionId, null);
    }

    @Nullable
    public static String loadJson(@Nullable String sessionId) {
        if (TextUtils.isEmpty(sessionId)) {
            return null;
        }
        String userId = currentUserId();
        if (TextUtils.isEmpty(userId)) {
            return null;
        }
        String json = MMKVBroadUtils.Companion.getSqlStrData(dataKey(userId, sessionId), "");
        if (TextUtils.isEmpty(json)) {
            previewCache.remove(sessionId);
            return null;
        }
        String preview = ChatDraftHelper.previewText(json);
        if (!TextUtils.isEmpty(preview)) {
            previewCache.put(sessionId, preview);
        }
        return json;
    }

    public static boolean hasDraft(@Nullable String sessionId) {
        if (TextUtils.isEmpty(sessionId)) {
            return false;
        }
        if (previewCache.containsKey(sessionId)) {
            String cached = previewCache.get(sessionId);
            return !TextUtils.isEmpty(cached);
        }
        return !TextUtils.isEmpty(peekPreview(sessionId));
    }

    /**
     * 会话列表副标题用：优先内存缓存，未命中再读 MMKV（并回填缓存）。
     */
    @Nullable
    public static String peekPreview(@Nullable String sessionId) {
        if (TextUtils.isEmpty(sessionId)) {
            return null;
        }
        if (previewCache.containsKey(sessionId)) {
            String cached = previewCache.get(sessionId);
            return TextUtils.isEmpty(cached) ? null : cached;
        }
        String json = loadJson(sessionId);
        if (TextUtils.isEmpty(json)) {
            previewCache.put(sessionId, "");
            return null;
        }
        return previewCache.get(sessionId);
    }

    /** 列表加载后可批量预热，避免滑动时首次 decode。 */
    public static void warmUp(@Nullable Iterable<String> sessionIds) {
        if (sessionIds == null) {
            return;
        }
        for (String id : sessionIds) {
            if (!TextUtils.isEmpty(id) && !previewCache.containsKey(id)) {
                peekPreview(id);
            }
        }
    }

    @NonNull
    public static Set<String> draftSessionIds() {
        String userId = currentUserId();
        if (TextUtils.isEmpty(userId)) {
            return Collections.emptySet();
        }
        return loadIndex(userId);
    }

    private static void clearInternal(@NonNull String userId,
                                      @NonNull String sessionId,
                                      @NonNull String dataKey) {
        MMKVBroadUtils.Companion.removeSqlData(dataKey);
        removeFromIndex(userId, sessionId);
        previewCache.put(sessionId, "");
        CEventCenter.dispatchEvent(Events.CHAT_DRAFT_UPDATED, 0, 0, sessionId);
    }

    private static void addToIndex(@NonNull String userId, @NonNull String sessionId) {
        Set<String> set = loadIndex(userId);
        if (set.add(sessionId)) {
            saveIndex(userId, set);
        }
    }

    private static void removeFromIndex(@NonNull String userId, @NonNull String sessionId) {
        Set<String> set = loadIndex(userId);
        if (set.isEmpty()) {
            return;
        }
        if (set.remove(sessionId)) {
            saveIndex(userId, set);
        }
    }

    @NonNull
    private static Set<String> loadIndex(@NonNull String userId) {
        String raw = MMKVBroadUtils.Companion.getSqlStrData(indexKey(userId), "");
        Set<String> set = new HashSet<>();
        if (TextUtils.isEmpty(raw)) {
            return set;
        }
        try {
            JSONArray arr = new JSONArray(raw);
            for (int i = 0; i < arr.length(); i++) {
                String id = arr.optString(i, null);
                if (!TextUtils.isEmpty(id)) {
                    set.add(id);
                }
            }
        } catch (Exception ignored) {
            // ignore corrupt index
        }
        return set;
    }

    private static void saveIndex(@NonNull String userId, @NonNull Set<String> set) {
        if (set.isEmpty()) {
            MMKVBroadUtils.Companion.removeSqlData(indexKey(userId));
            return;
        }
        JSONArray arr = new JSONArray();
        for (String id : set) {
            if (!TextUtils.isEmpty(id)) {
                arr.put(id);
            }
        }
        MMKVBroadUtils.Companion.saveSqlStrData(indexKey(userId), arr.toString());
    }

    @NonNull
    private static String truncateJsonIfNeeded(@NonNull String draftJson) {
        String content = ChatDraftHelper.previewText(draftJson);
        if (content == null || content.length() <= MAX_CONTENT_CHARS) {
            return draftJson;
        }
        // 超长时退化为纯文本草稿，避免异常粘贴撑爆
        return ChatDraftHelper.toPlainJson(content.substring(0, MAX_CONTENT_CHARS));
    }

    @NonNull
    private static String dataKey(@NonNull String userId, @NonNull String sessionId) {
        return KEY_PREFIX + userId + "_" + sessionId;
    }

    @NonNull
    private static String indexKey(@NonNull String userId) {
        return INDEX_PREFIX + userId;
    }

    @NonNull
    private static String currentUserId() {
        String id = MMKVBroadUtils.Companion.getRealUserId();
        return id != null ? id : "";
    }
}
