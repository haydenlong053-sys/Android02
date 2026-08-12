package com.freddy.im.message;

import android.os.Handler;
import android.os.Looper;
import android.os.MessageQueue;
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
 * <p>
 * 万人群全量：首屏页立刻拉取，余量进闲时续拉；打开成员页 / {@code @} 时可
 * {@link #promoteFullSyncPriority} 提速为立刻翻页。
 */
public final class ImGroupMemberChangeManager {

    public static final String TAG = "ImGroupMemberChange";

    /**
     * 预期/响应总人数达到此阈值时，首屏后余量走闲时；否则整链立刻翻页。
     */
    public static final int IMMEDIATE_FULL_SYNC_MAX_MEMBERS = 500;

    /** 主线程空闲未到时的兜底延时，避免永远不续拉。 */
    private static final long IDLE_PAGE_FALLBACK_DELAY_MS = 1500L;

    /** 闲时页与页之间最小间隔，减轻连续打满。 */
    private static final long IDLE_PAGE_MIN_GAP_MS = 400L;

    private static final ImGroupMemberChangeManager INSTANCE = new ImGroupMemberChangeManager();

    private final Handler mainHandler = new Handler(Looper.getMainLooper());

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

    /**
     * 用户打开群成员 / {@code @} 等需要完整名单的页面时调用：
     * 取消闲时等待，立刻续拉剩余页。
     */
    public void promoteFullSyncPriority(@Nullable String groupId) {
        if (TextUtils.isEmpty(groupId)) {
            return;
        }
        final String gid = groupId.trim();
        FullSyncState state = fullSyncStates.get(gid);
        if (state == null) {
            return;
        }
        synchronized (state) {
            if (!state.inFlight || !state.waitingIdle) {
                state.urgent = true;
                return;
            }
            state.urgent = true;
            state.waitingIdle = false;
            cancelIdleScheduleLocked(state);
            final int nextAnchor = state.nextAnchor;
            final int pageSize = state.pageSize;
            Log.d(TAG, "memberList 提速立刻续拉 groupId=" + gid + " anchor=" + nextAnchor);
            mainHandler.post(() -> fetchMemberListPage(gid, nextAnchor, pageSize,
                    () -> finishFullSyncChain(gid, true),
                    () -> finishFullSyncChain(gid, false)));
        }
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
            // 人数未知：首屏立刻，是否闲时续拉由首包 total 决定
            fetchAllMembersPaginated(gid,
                    () -> maybeAlignMemChgTimeAfterSignal(gid, null),
                    null,
                    0);
        } else {
            fetchSpecifyMembers(gid, enterIds);
        }
    }

    /**
     * 分页拉取群全量成员并入库；仅全部页成功后回调 {@code onFullSyncSuccess}。
     * 失败/取消不得抬 {@code memChgTime}（杀进程半截下次可重拉）。
     */
    public void fetchAllMembersPaginated(@NonNull String groupId) {
        fetchAllMembersPaginated(groupId, null, null, 0);
    }

    public void fetchAllMembersPaginated(@NonNull String groupId,
                                         @Nullable Runnable onFullSyncSuccess) {
        fetchAllMembersPaginated(groupId, onFullSyncSuccess, null, 0);
    }

    public void fetchAllMembersPaginated(@NonNull String groupId,
                                         @Nullable Runnable onFullSyncSuccess,
                                         @Nullable Runnable onFullSyncFailed) {
        fetchAllMembersPaginated(groupId, onFullSyncSuccess, onFullSyncFailed, 0);
    }

    /**
     * @param expectedMemCount 远端 memCount 提示；&gt;= {@link #IMMEDIATE_FULL_SYNC_MAX_MEMBERS}
     *                         时首屏后余量闲时续拉；0 表示未知，由首包 {@code total} 再判断
     */
    public void fetchAllMembersPaginated(@NonNull String groupId,
                                         @Nullable Runnable onFullSyncSuccess,
                                         @Nullable Runnable onFullSyncFailed,
                                         int expectedMemCount) {
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
                // 进行中再触发：若调用方已知大群，保持/打开闲时；提速由 promote 负责
                if (expectedMemCount >= IMMEDIATE_FULL_SYNC_MAX_MEMBERS) {
                    state.deferTail = true;
                }
                Log.d(TAG, "memberList 已在飞，合并回调 groupId=" + gid
                        + " deferTail=" + state.deferTail);
                return;
            }
            state.inFlight = true;
            state.urgent = false;
            state.waitingIdle = false;
            state.deferTail = expectedMemCount >= IMMEDIATE_FULL_SYNC_MAX_MEMBERS;
            state.expectedMemCount = Math.max(0, expectedMemCount);
            state.pageSize = GroupMemberListContent.DEFAULT_PAGE_SIZE;
            state.nextAnchor = 0;
            cancelIdleScheduleLocked(state);
        }
        Log.d(TAG, "memberList 开始 groupId=" + gid
                + " expectedMemCount=" + expectedMemCount
                + " deferTailHint=" + (expectedMemCount >= IMMEDIATE_FULL_SYNC_MAX_MEMBERS));
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
                            if (!content.hasNextPage(pageSize)) {
                                onChainSuccess.run();
                                return;
                            }
                            int nextAnchor = content.getAnchor();
                            scheduleOrContinueNextPage(groupId, nextAnchor, pageSize,
                                    content.getTotal(), onChainSuccess, onChainFailed);
                        });
                    }
                }, () -> {
                    Log.w(TAG, "memberList 失败 groupId=" + groupId + " anchor=" + anchor
                            + "（不更新 memChgTime，下次可重拉全量）");
                    onChainFailed.run();
                });
    }

    private void scheduleOrContinueNextPage(@NonNull String groupId,
                                            int nextAnchor,
                                            int pageSize,
                                            int responseTotal,
                                            @NonNull Runnable onChainSuccess,
                                            @NonNull Runnable onChainFailed) {
        FullSyncState state = fullSyncStates.get(groupId);
        if (state == null) {
            onChainFailed.run();
            return;
        }
        boolean defer;
        synchronized (state) {
            if (!state.inFlight) {
                return;
            }
            // 首包后用 total / expected 再判定是否大群
            if (responseTotal >= IMMEDIATE_FULL_SYNC_MAX_MEMBERS
                    || state.expectedMemCount >= IMMEDIATE_FULL_SYNC_MAX_MEMBERS) {
                state.deferTail = true;
            }
            state.nextAnchor = nextAnchor;
            state.pageSize = pageSize;
            defer = state.deferTail && !state.urgent;
            if (defer) {
                state.waitingIdle = true;
                cancelIdleScheduleLocked(state);
                final Runnable continuePage = () -> {
                    synchronized (state) {
                        if (!state.inFlight || !state.waitingIdle) {
                            return;
                        }
                        state.waitingIdle = false;
                        state.idleHandler = null;
                        state.fallbackRunnable = null;
                    }
                    fetchMemberListPage(groupId, nextAnchor, pageSize,
                            onChainSuccess, onChainFailed);
                };
                state.fallbackRunnable = continuePage;
                state.idleHandler = () -> {
                    mainHandler.removeCallbacks(continuePage);
                    mainHandler.postDelayed(continuePage, IDLE_PAGE_MIN_GAP_MS);
                    return false;
                };
                Log.d(TAG, "memberList 余量闲时等待 groupId=" + groupId
                        + " nextAnchor=" + nextAnchor + " total=" + responseTotal);
            }
        }
        if (defer) {
            mainHandler.post(() -> {
                FullSyncState s = fullSyncStates.get(groupId);
                if (s == null) {
                    return;
                }
                MessageQueue.IdleHandler handler;
                Runnable fallback;
                synchronized (s) {
                    if (!s.inFlight || !s.waitingIdle) {
                        return;
                    }
                    handler = s.idleHandler;
                    fallback = s.fallbackRunnable;
                }
                if (handler != null) {
                    Looper.myQueue().addIdleHandler(handler);
                }
                if (fallback != null) {
                    mainHandler.postDelayed(fallback, IDLE_PAGE_FALLBACK_DELAY_MS);
                }
            });
            return;
        }
        fetchMemberListPage(groupId, nextAnchor, pageSize, onChainSuccess, onChainFailed);
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
            cancelIdleScheduleLocked(state);
            callback = success ? state.onSuccess : state.onFailed;
            state.inFlight = false;
            state.waitingIdle = false;
            state.urgent = false;
            state.deferTail = false;
            state.onSuccess = null;
            state.onFailed = null;
            state.idleHandler = null;
            state.fallbackRunnable = null;
        }
        fullSyncStates.remove(groupId, state);
        if (callback != null) {
            callback.run();
        }
    }

    private void cancelIdleScheduleLocked(@NonNull FullSyncState state) {
        if (state.fallbackRunnable != null) {
            mainHandler.removeCallbacks(state.fallbackRunnable);
        }
        if (state.idleHandler != null) {
            // IdleHandler 只能在主线程 queue 上 remove；若当前不在主线程则 post
            final MessageQueue.IdleHandler handler = state.idleHandler;
            if (Looper.myLooper() == Looper.getMainLooper()) {
                Looper.myQueue().removeIdleHandler(handler);
            } else {
                mainHandler.post(() -> Looper.myQueue().removeIdleHandler(handler));
            }
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

    /** 同群全量 memberList 在飞状态：合并成功/失败回调 + 闲时续拉。 */
    private static final class FullSyncState {
        boolean inFlight;
        /** 大群：首屏后余量可闲时。 */
        boolean deferTail;
        /** 用户操作提速：剩余页立刻拉。 */
        boolean urgent;
        /** 已挂起下一页闲时任务。 */
        boolean waitingIdle;
        int expectedMemCount;
        int nextAnchor;
        int pageSize = GroupMemberListContent.DEFAULT_PAGE_SIZE;
        @Nullable
        MessageQueue.IdleHandler idleHandler;
        @Nullable
        Runnable fallbackRunnable;
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
