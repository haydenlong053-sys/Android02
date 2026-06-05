package com.freddy.im.message;

import android.text.TextUtils;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.db.ImLocalStore;
import com.freddy.im.db.ImRealm;
import com.freddy.im.db.mapper.GroupUserVoMapper;
import com.freddy.im.http.ImMessageHttpExample;
import com.freddy.im.http.model.im.GroupMemberItem;
import com.freddy.im.http.model.im.GroupMemberListContent;
import com.freddy.im.http.model.im.GroupUserVo;
import com.freddy.im.listener.OnHttpImListener;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * 群成员变更信令（0x0102 {@code MemberChangeSig}）。
 * <p>
 * HTTP 行为对齐 {@code ~/Downloads/api/GroupController-API.md}：
 * §3 {@code memberList}、§12 {@code getSpecifyMembers}。
 */
public final class ImGroupMemberChangeManager {

    private static final String TAG = "ImGroupMemberChange";

    private static final ImGroupMemberChangeManager INSTANCE = new ImGroupMemberChangeManager();

    private ImGroupMemberChangeManager() {
    }

    public static ImGroupMemberChangeManager getInstance() {
        return INSTANCE;
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
            ImLocalStore.softDeleteGroupMembersThen(gid, removed, null);
        }
        List<String> enterIds = dedupe(enters);
        if (enterIds.isEmpty()) {
            return;
        }
        String currentUserId = ImRealm.getCurrentUserId();
        if (containsUserId(enterIds, currentUserId)) {
            fetchAllMembersPaginated(gid);
        } else {
            fetchSpecifyMembers(gid, enterIds);
        }
    }

    /**
     * 分页拉取群全量成员并入库（§3 {@code memberList}）；首页不传 anchor。
     */
    public void fetchAllMembersPaginated(@NonNull String groupId) {
        if (TextUtils.isEmpty(groupId)) {
            return;
        }
        fetchMemberListPage(groupId.trim(), 0, GroupMemberListContent.DEFAULT_PAGE_SIZE);
    }

    private void fetchMemberListPage(@NonNull String groupId,
                                      int anchor,
                                     int pageSize) {
        ImMessageHttpExample.requestGetGroupMemberList(groupId, anchor, pageSize,
                new OnHttpImListener<GroupMemberListContent>() {
                    @Override
                    public void onHttpImSuccess(@NonNull GroupMemberListContent content) {
                        List<GroupUserVo> page = content.getMembers();
                        if (page == null || page.isEmpty()) {
                            return;
                        }
                        List<GroupMemberItem> items = GroupUserVoMapper.toItems(groupId, page);
                        ImLocalStore.saveGroupMembersThen(items, groupId, null);
                        if (content.hasNextPage(pageSize)) {
                            fetchMemberListPage(groupId, content.getAnchor(), pageSize);
                        }
                    }
                }, () -> Log.w(TAG, "memberList 失败 groupId=" + groupId
                        + " anchor=" + anchor));
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
                        ImLocalStore.saveGroupMembersThen(items, groupId, null);
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
