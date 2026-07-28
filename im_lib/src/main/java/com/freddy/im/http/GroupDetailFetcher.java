package com.freddy.im.http;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.db.repository.ImSessionRepository;
import com.freddy.im.http.model.im.GroupVo;
import com.freddy.im.http.model.im.SessionItem;
import com.freddy.im.listener.OnHttpImListener;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * 批量拉取群详情并转为 {@link SessionItem}（key 为 sessionId / groupId）。
 */
public final class GroupDetailFetcher {

    private static final String TAG = "GroupDetailFetcher";

    public interface OnGroupSessionsFetchedListener {
        void onFetched(@NonNull Map<String, SessionItem> sessionsById);
    }

    private GroupDetailFetcher() {
    }

    public static void fetchSessionItems(@Nullable List<String> groupIds,
                                         @NonNull OnGroupSessionsFetchedListener listener) {
        List<String> ids = dedupe(groupIds);
        if (ids.isEmpty()) {
            listener.onFetched(new LinkedHashMap<>());
            return;
        }
        fetchSequential(ids, 0, new LinkedHashMap<>(), listener);
    }

    @NonNull
    private static List<String> dedupe(@Nullable List<String> groupIds) {
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

    private static void fetchSequential(@NonNull List<String> groupIds,
                                        int index,
                                        @NonNull Map<String, SessionItem> accumulated,
                                        @NonNull OnGroupSessionsFetchedListener listener) {
        if (index >= groupIds.size()) {
            listener.onFetched(accumulated);
            return;
        }
        String groupId = groupIds.get(index);
        ImMessageHttpExample.requestGetGroupDetail(groupId, new OnHttpImListener<GroupVo>() {
            @Override
            public void onHttpImSuccess(@NonNull GroupVo group) {
                String sessionId = !TextUtils.isEmpty(group.getId()) ? group.getId() : groupId;
                SessionItem existing = ImSessionRepository.getInstance().queryBySessionId(sessionId);
                // 仅刷新会话展示字段；不抬 memChgTime（水位仅在成员全量/增量成功后写入）
                SessionItem item = GroupVoSessionMapper.toSessionItem(group, existing, groupId, false);
                accumulated.put(sessionId, item);
                fetchSequential(groupIds, index + 1, accumulated, listener);
            }
        }, () -> {
            Log.w(TAG, "getGroupDetail 失败 groupId=" + groupId);
            fetchSequential(groupIds, index + 1, accumulated, listener);
        });
    }
}
