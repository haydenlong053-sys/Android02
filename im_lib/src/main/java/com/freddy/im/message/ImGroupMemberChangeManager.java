package com.freddy.im.message;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.db.ImLocalStore;
import com.freddy.im.db.ImRealm;
import com.freddy.im.db.mapper.GroupUserVoMapper;
import com.freddy.im.http.ImGroupHttpExample;
import com.freddy.im.http.ImMessageHttpExample;
import com.freddy.im.http.model.im.GroupMemberChangeListContent;
import com.freddy.im.http.model.im.GroupMemberItem;
import com.freddy.im.http.model.im.GroupMemberListContent;
import com.freddy.im.http.model.im.GroupUserVo;
import com.freddy.im.listener.OnHttpImListener;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 群成员变更信令（0x0102 {@code MemberChangeSig}）与成员全量/增量拉取。
 * <p>
 * HTTP：{@code memberList}（全量分页）、{@code changeMemberList}（增量）、{@code getSpecifyMembers}。
 */
public final class ImGroupMemberChangeManager {

    public static final String TAG = "ImGroupMemberChange";

    private static final ImGroupMemberChangeManager INSTANCE = new ImGroupMemberChangeManager();

    /** 同群全量 memberList 只保留一条分页链，避免反复进群并行从 anchor=0 重拉。 */
    private final ConcurrentHashMap<String, FullSyncState> fullSyncStates = new ConcurrentHashMap<>();

    /** 信令同步成员后是否对齐 {@code memChgTime}，默认关闭（由进群同步统一写水位）。 */
    private boolean alignMemChgTimeAfterSignal;

    private ImGroupMemberChangeManager() {
    }

    public static ImGroupMemberChangeManager getInstance() {
        return INSTANCE;
    }

    public boolean isAlignMemChgTimeAfterSignal() {
        return alignMemChgTimeAfterSignal;
    }

    public void setAlignMemChgTimeAfterSignal(boolean alignMemChgTimeAfterSignal) {
        this.alignMemChgTimeAfterSignal = alignMemChgTimeAfterSignal;
    }

    /** 该群是否正在全量 memberList 分页拉取中。 */
    public boolean isFullSyncInFlight(@Nullable String groupId) {
        if (TextUtils.isEmpty(groupId)) {
            return false;
        }
        FullSyncState state = fullSyncStates.get(groupId.trim());
        return state != null && state.inFlight;
    }

    private void maybeAlignMemChgTimeAfterSignal(@NonNull String groupId,
                                                 @Nullable Runnable onComplete) {
        if (alignMemChgTimeAfterSignal) {
            GroupMemberSyncHelper.refreshMemChgTimeFromDetail(groupId, onComplete);
        } else if (onComplete != null) {
            onComplete.run();
        }
    }

    public void handleMemberChange(@Nullable String groupId,
                                   @Nullable List<String> enters,
                                   @Nullable List<String> leaves,
                                   @Nullable List<String> kickouts) {
        if (TextUtils.isEmpty(groupId)) {
            return;
        }
        final String gid = groupId.trim();
        List<String> removed = mergeUserIds(leaves, kickouts);
        if (!removed.isEmpty()) {
            ImLocalStore.softDeleteGroupMembersThen(gid, removed, () ->
                    maybeAlignMemChgTimeAfterSignal(gid, null));
        }
        List<String> enterIds = dedupe(enters);
        if (enterIds.isEmpty()) {
            return;
        }
        String currentUserId = ImRealm.getCurrentUserId();
        // 自己出现在 enters，且本地尚无该群活跃成员记录 → 无底库，走全量；
        // 若本地已有自己，说明底库在，增量拉 enters 即可，避免重复全量。
        boolean selfEntered = containsUserId(enterIds, currentUserId);
        boolean selfMissingLocally = TextUtils.isEmpty(currentUserId)
                || ImLocalStore.queryGroupMember(gid, currentUserId) == null;
        if (selfEntered && selfMissingLocally) {
            fetchAllMembersPaginated(gid,
                    () -> maybeAlignMemChgTimeAfterSignal(gid, null),
                    null);
        } else {
            fetchSpecifyMembers(gid, enterIds);
        }
    }

    /**
     * 分页拉取群全量成员并入库；仅全部页成功后回调 {@code onFullSyncSuccess}。
     * 失败/取消不得抬 {@code memChgTime}（杀进程半截下次可重拉）。
     */
    public void fetchAllMembersPaginated(@NonNull String groupId) {
        fetchAllMembersPaginated(groupId, null, null);
    }

    public void fetchAllMembersPaginated(@NonNull String groupId,
                                         @Nullable Runnable onFullSyncSuccess) {
        fetchAllMembersPaginated(groupId, onFullSyncSuccess, null);
    }

    public void fetchAllMembersPaginated(@NonNull String groupId,
                                         @Nullable Runnable onFullSyncSuccess,
                                         @Nullable Runnable onFullSyncFailed) {
        if (TextUtils.isEmpty(groupId)) {
            if (onFullSyncFailed != null) {
                onFullSyncFailed.run();
            }
            return;
        }
        final String gid = groupId.trim();
        FullSyncState state = fullSyncStates.computeIfAbsent(gid, k -> new FullSyncState());
        synchronized (state) {
            state.mergeCallbacks(onFullSyncSuccess, onFullSyncFailed);
            if (state.inFlight) {
                Log.d(TAG, "memberList 已在飞，合并回调 groupId=" + gid);
                return;
            }
            state.inFlight = true;
        }
        fetchMemberListPage(gid, 0, GroupMemberListContent.DEFAULT_PAGE_SIZE,
                () -> finishFullSyncChain(gid, true),
                () -> finishFullSyncChain(gid, false));
    }

    private void fetchMemberListPage(@NonNull String groupId,
                                     int anchor,
                                     int pageSize,
                                     @NonNull Runnable onChainSuccess,
                                     @NonNull Runnable onChainFailed) {
        ImMessageHttpExample.requestGetGroupMemberList(groupId, anchor, pageSize,
                new OnHttpImListener<GroupMemberListContent>() {
                    @Override
                    public void onHttpImSuccess(@NonNull GroupMemberListContent content) {
                        List<GroupUserVo> page = content.getMembers();
                        if (page == null || page.isEmpty()) {
                            onChainSuccess.run();
                            return;
                        }
                        List<GroupMemberItem> items = GroupUserVoMapper.toItems(groupId, page);
                        ImLocalStore.saveGroupMembersThen(items, groupId, () -> {
                            if (content.hasNextPage(pageSize)) {
                                fetchMemberListPage(groupId, content.getAnchor(), pageSize,
                                        onChainSuccess, onChainFailed);
                            } else {
                                onChainSuccess.run();
                            }
                        });
                    }
                }, () -> {
                    Log.w(TAG, "memberList 失败 groupId=" + groupId + " anchor=" + anchor
                            + "（不更新 memChgTime，下次可重拉全量）");
                    onChainFailed.run();
                });
    }

    private void finishFullSyncChain(@NonNull String groupId, boolean success) {
        FullSyncState state = fullSyncStates.get(groupId);
        if (state == null) {
            return;
        }
        Runnable callback;
        synchronized (state) {
            if (!state.inFlight) {
                return;
            }
            callback = success ? state.onSuccess : state.onFailed;
            state.inFlight = false;
            state.onSuccess = null;
            state.onFailed = null;
        }
        fullSyncStates.remove(groupId, state);
        if (callback != null) {
            callback.run();
        }
    }

    /**
     * 按 {@code since}（本地 memChgTime）拉增量并入库（含 delFlag 软删）。
     * 仅成功回调 {@code onSuccess}；失败不抬水位。
     */
    public void fetchChangedMembers(@NonNull String groupId,
                                    long since,
                                    @Nullable Runnable onSuccess,
                                    @Nullable Runnable onFailed) {
        if (TextUtils.isEmpty(groupId)) {
            if (onFailed != null) {
                onFailed.run();
            }
            return;
        }
        final String gid = groupId.trim();
        ImGroupHttpExample.requestChangeGroupMemberList(gid, since,
                new OnHttpImListener<GroupMemberChangeListContent>() {
                    @Override
                    public void onHttpImSuccess(@NonNull GroupMemberChangeListContent content) {
                        List<GroupMemberItem> items = GroupUserVoMapper.toItems(gid, content.getMembers());
                        if (items.isEmpty()) {
                            if (onSuccess != null) {
                                onSuccess.run();
                            }
                            return;
                        }
                        ImLocalStore.saveGroupMembersThen(items, gid, onSuccess);
                    }
                }, () -> {
                    Log.w(TAG, "changeMemberList 失败 groupId=" + gid + " since=" + since);
                    if (onFailed != null) {
                        onFailed.run();
                    }
                });
    }

    private void fetchSpecifyMembers(@NonNull String groupId, @NonNull List<String> userIds) {
        ImMessageHttpExample.requestGetSpecifyMembers(groupId, userIds,
                new OnHttpImListener<List<GroupUserVo>>() {
                    @Override
                    public void onHttpImSuccess(@NonNull List<GroupUserVo> members) {
                        if (members.isEmpty()) {
                            return;
                        }
                        List<GroupMemberItem> items = GroupUserVoMapper.toItems(groupId, members);
                        ImLocalStore.saveGroupMembersThen(items, groupId, () ->
                                maybeAlignMemChgTimeAfterSignal(groupId, null));
                    }
                }, () -> Log.w(TAG, "getSpecifyMembers 失败 groupId=" + groupId));
    }

    private static boolean containsUserId(@NonNull List<String> userIds, @Nullable String target) {
        if (TextUtils.isEmpty(target)) {
            return false;
        }
        String id = target.trim();
        for (String uid : userIds) {
            if (id.equals(uid)) {
                return true;
            }
        }
        return false;
    }

    @NonNull
    private static List<String> mergeUserIds(@Nullable List<String> a, @Nullable List<String> b) {
        Set<String> set = new LinkedHashSet<>();
        addAll(set, a);
        addAll(set, b);
        return new ArrayList<>(set);
    }

    @NonNull
    private static List<String> dedupe(@Nullable List<String> ids) {
        return mergeUserIds(ids, null);
    }

    private static void addAll(@NonNull Set<String> set, @Nullable List<String> ids) {
        if (ids == null) {
            return;
        }
        for (String id : ids) {
            if (!TextUtils.isEmpty(id)) {
                set.add(id.trim());
            }
        }
    }

    /** 同群全量 memberList 在飞状态：合并成功/失败回调。 */
    private static final class FullSyncState {
        boolean inFlight;
        @Nullable
        Runnable onSuccess;
        @Nullable
        Runnable onFailed;

        void mergeCallbacks(@Nullable Runnable success, @Nullable Runnable failed) {
            onSuccess = chain(onSuccess, success);
            onFailed = chain(onFailed, failed);
        }

        @Nullable
        private static Runnable chain(@Nullable Runnable existing, @Nullable Runnable more) {
            if (more == null) {
                return existing;
            }
            if (existing == null) {
                return more;
            }
            Runnable prev = existing;
            return () -> {
                prev.run();
                more.run();
            };
        }
    }
}
