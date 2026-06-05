package com.freddy.im.message;

import android.text.TextUtils;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.db.ImLocalStore;
import com.freddy.im.http.GroupDetailFetcher;
import com.freddy.im.http.model.im.SessionItem;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 群变更信令（0x0101 {@code GroupChangeSig}）：拉群详情 → 转 {@link SessionItem} → 入库 → 回调 UI。
 */
public final class ImGroupChangeManager {

    private static final ImGroupChangeManager INSTANCE = new ImGroupChangeManager();

    private ImGroupChangeManager() {
    }

    public static ImGroupChangeManager getInstance() {
        return INSTANCE;
    }

    /**
     * @param groupIds 信令 body 中的 groupList
     * @param listener 入库后的会话列表（仅包含本次成功同步的群会话）
     */
    public void handleGroupChange(@Nullable List<String> groupIds,
                                  @NonNull OnGroupSessionsSyncedListener listener) {
        List<String> ids = dedupeGroupIds(groupIds);
        if (ids.isEmpty()) {
            listener.onGroupSessionsSynced(new ArrayList<>());
            return;
        }
        GroupDetailFetcher.fetchSessionItems(ids, map -> {
            List<SessionItem> sessions = new ArrayList<>(map.values());
            finishSync(sessions, listener);
        });
    }

    @NonNull
    private static List<String> dedupeGroupIds(@Nullable List<String> groupIds) {
        Set<String> set = new LinkedHashSet<>();
        if (groupIds != null) {
            for (String id : groupIds) {
                if (!TextUtils.isEmpty(id)) {
                    set.add(id.trim());
                }
            }
        }
        return new ArrayList<>(set);
    }

    private void finishSync(@NonNull List<SessionItem> sessions,
                            @NonNull OnGroupSessionsSyncedListener listener) {
        if (sessions.isEmpty()) {
            listener.onGroupSessionsSynced(sessions);
            return;
        }
        ImLocalStore.saveSessionsThen(sessions, () -> listener.onGroupSessionsSynced(sessions));
    }

    public interface OnGroupSessionsSyncedListener {
        void onGroupSessionsSynced(@NonNull List<SessionItem> sessions);
    }
}
