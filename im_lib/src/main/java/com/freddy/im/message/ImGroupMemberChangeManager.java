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

/**
 * 群成员变更信令（0x0102 {@code MemberChangeSig}）与成员全量/增量拉取。
 * <p>
 * HTTP：{@code memberList}（全量分页）、{@code changeMemberList}（增量）、{@code getSpecifyMembers}。
 */
public final class ImGroupMemberChangeManager {

    public static final String TAG = "ImGroupMemberChange";

    private static final ImGroupMemberChangeManager INSTANCE = new ImGroupMemberChangeManager();

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
        fetchMemberListPage(groupId.trim(), 0, GroupMemberListContent.DEFAULT_PAGE_SIZE,
                onFullSyncSuccess, onFullSyncFailed);
    }

    private void fetchMemberListPage(@NonNull String groupId,
                                     int anchor,
                                     int pageSize,
                                     @Nullable Runnable onFullSyncSuccess,
                                     @Nullable Runnable onFullSyncFailed) {
        ImMessageHttpExample.requestGetGroupMemberList(groupId, anchor, pageSize,
                new OnHttpImListener<GroupMemberListContent>() {
                    @Override
                    public void onHttpImSuccess(@NonNull GroupMemberListContent content) {
                        List<GroupUserVo> page = content.getMembers();
                        if (page == null || page.isEmpty()) {
                            if (onFullSyncSuccess != null) {
                                onFullSyncSuccess.run();
                            }
                            return;
                        }
                        List<GroupMemberItem> items = GroupUserVoMapper.toItems(groupId, page);
                        ImLocalStore.saveGroupMembersThen(items, groupId, () -> {
                            if (content.hasNextPage(pageSize)) {
                                fetchMemberListPage(groupId, content.getAnchor(), pageSize,
                                        onFullSyncSuccess, onFullSyncFailed);
                            } else if (onFullSyncSuccess != null) {
                                onFullSyncSuccess.run();
                            }
                        });
                    }
                }, () -> {
                    Log.w(TAG, "memberList 失败 groupId=" + groupId + " anchor=" + anchor
                            + "（不更新 memChgTime，下次可重拉全量）");
                    if (onFullSyncFailed != null) {
                        onFullSyncFailed.run();
                    }
                });
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
}
