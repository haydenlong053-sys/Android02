package com.freddy.im.db.repository;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.db.ImDbExecutor;
import com.freddy.im.db.ImRealm;
import com.freddy.im.db.entity.GroupMemberEntity;
import com.freddy.im.db.mapper.GroupMemberRealmMapper;
import com.freddy.im.group.GroupRole;
import com.freddy.im.http.model.im.GroupMemberItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

import io.realm.Realm;
import io.realm.Sort;

/**
 * 群成员表读写（含软删除）。
 */
public final class ImGroupMemberRepository {

    private static final ImGroupMemberRepository INSTANCE = new ImGroupMemberRepository();

    private ImGroupMemberRepository() {
    }

    public static ImGroupMemberRepository getInstance() {
        return INSTANCE;
    }

    public void upsertAllThen(@Nullable List<GroupMemberItem> members, @Nullable Runnable afterCommit) {
        if (members == null || members.isEmpty() || !ImRealm.isUserBound()) {
            if (afterCommit != null) {
                afterCommit.run();
            }
            return;
        }
        final List<GroupMemberItem> snapshot = new ArrayList<>(members);
        ImDbExecutor.runWrite(() -> {
            Realm realm = ImRealm.getRealm();
            try {
                realm.executeTransaction(r -> {
                    for (GroupMemberItem item : snapshot) {
                        if (item == null || TextUtils.isEmpty(item.getGroupId())
                                || TextUtils.isEmpty(item.getUserId())) {
                            continue;
                        }
                        item.setDeleted(false);
                        r.copyToRealmOrUpdate(GroupMemberRealmMapper.toEntity(item));
                    }
                });
                if (afterCommit != null) {
                    afterCommit.run();
                }
            } finally {
                realm.close();
            }
        });
    }

    public void softDeleteThen(@NonNull String groupId,
                               @Nullable List<String> userIds,
                               @Nullable Runnable afterCommit) {
        List<String> ids = dedupeUserIds(userIds);
        if (ids.isEmpty() || !ImRealm.isUserBound()) {
            if (afterCommit != null) {
                afterCommit.run();
            }
            return;
        }
        final String gid = groupId;
        ImDbExecutor.runWrite(() -> {
            Realm realm = ImRealm.getRealm();
            try {
                realm.executeTransaction(r -> {
                    for (String userId : ids) {
                        String key = GroupMemberRealmMapper.memberKey(gid, userId);
                        GroupMemberEntity entity = r.where(GroupMemberEntity.class)
                                .equalTo("memberKey", key)
                                .findFirst();
                        if (entity != null) {
                            entity.setDeleted(true);
                        } else {
                            GroupMemberItem stub = new GroupMemberItem();
                            stub.setGroupId(gid);
                            stub.setUserId(userId);
                            stub.setDeleted(true);
                            r.copyToRealmOrUpdate(GroupMemberRealmMapper.toEntity(stub));
                        }
                    }
                });
                if (afterCommit != null) {
                    afterCommit.run();
                }
            } finally {
                realm.close();
            }
        });
    }

    public void updateRolesThen(@NonNull String groupId,
                                @Nullable List<String> userIds,
                                int role,
                                @Nullable Runnable afterCommit) {
        List<String> ids = dedupeUserIds(userIds);
        if (ids.isEmpty() || !ImRealm.isUserBound()) {
            if (afterCommit != null) {
                afterCommit.run();
            }
            return;
        }
        final String gid = groupId;
        ImDbExecutor.runWrite(() -> {
            Realm realm = ImRealm.getRealm();
            try {
                realm.executeTransaction(r -> {
                    for (String userId : ids) {
                        GroupMemberEntity entity = r.where(GroupMemberEntity.class)
                                .equalTo("memberKey", GroupMemberRealmMapper.memberKey(gid, userId))
                                .equalTo("deleted", false)
                                .findFirst();
                        if (entity != null) {
                            entity.setRole(role);
                        }
                    }
                });
                if (afterCommit != null) {
                    afterCommit.run();
                }
            } finally {
                realm.close();
            }
        });
    }

    /**
     * 按 userId 更新所有未删除群成员记录的信用分（grade）；
     * 完成后回调受影响的 groupId 列表（已去重）。
     */
    public void updateGradeByUserIdThen(@NonNull String userId,
                                        @Nullable String grade,
                                        @Nullable Consumer<List<String>> afterCommit) {
        if (TextUtils.isEmpty(userId) || !ImRealm.isUserBound()) {
            if (afterCommit != null) {
                afterCommit.accept(Collections.emptyList());
            }
            return;
        }
        final String uid = userId.trim();
        final String score = grade != null ? grade : "";
        ImDbExecutor.runWrite(() -> {
            Realm realm = ImRealm.getRealm();
            List<String> affectedGroupIds = new ArrayList<>();
            try {
                realm.executeTransaction(r -> {
                    List<GroupMemberEntity> results = r.where(GroupMemberEntity.class)
                            .equalTo("userId", uid)
                            .equalTo("deleted", false)
                            .findAll();
                    Set<String> groupIdSet = new HashSet<>();
                    for (GroupMemberEntity entity : results) {
                        if (entity == null) {
                            continue;
                        }
                        entity.setGrade(score);
                        if (!TextUtils.isEmpty(entity.getGroupId())) {
                            groupIdSet.add(entity.getGroupId());
                        }
                    }
                    affectedGroupIds.addAll(groupIdSet);
                });
                if (afterCommit != null) {
                    afterCommit.accept(affectedGroupIds);
                }
            } finally {
                realm.close();
            }
        });
    }

    @Nullable
    public GroupMemberItem queryActiveMember(@NonNull String groupId, @NonNull String userId) {
        if (!ImRealm.isUserBound()) {
            return null;
        }
        Realm realm = ImRealm.getRealm();
        try {
            GroupMemberEntity entity = realm.where(GroupMemberEntity.class)
                    .equalTo("memberKey", GroupMemberRealmMapper.memberKey(groupId, userId))
                    .equalTo("deleted", false)
                    .findFirst();
            return GroupMemberRealmMapper.toItem(entity);
        } finally {
            realm.close();
        }
    }

    @NonNull
    public List<GroupMemberItem> queryActiveByGroupId(@NonNull String groupId) {
        if (!ImRealm.isUserBound()) {
            return Collections.emptyList();
        }
        Realm realm = ImRealm.getRealm();
        try {
            List<GroupMemberEntity> results = realm.where(GroupMemberEntity.class)
                    .equalTo("groupId", groupId)
                    .equalTo("deleted", false)
                    .findAll();
            List<GroupMemberItem> list = new ArrayList<>(results.size());
            for (GroupMemberEntity entity : results) {
                list.add(GroupMemberRealmMapper.toItem(entity));
            }
            return list;
        } finally {
            realm.close();
        }
    }

    /**
     * 分页查询未删除成员，按身份升序（群主0 &gt; 管理员1 &gt; 普通成员2），同身份再按 {@code joinTime}。
     *
     * @param ownerOrAdminOnly true 时仅返回群主 / 管理员（role=0/1）
     */
    @NonNull
    public List<GroupMemberItem> queryActivePage(@NonNull String groupId,
                                                 int offset,
                                                 int limit,
                                                 boolean ownerOrAdminOnly) {
        if (!ImRealm.isUserBound() || TextUtils.isEmpty(groupId) || limit <= 0) {
            return Collections.emptyList();
        }
        int safeOffset = Math.max(0, offset);
        Realm realm = ImRealm.getRealm();
        try {
            io.realm.RealmQuery<GroupMemberEntity> query = realm.where(GroupMemberEntity.class)
                    .equalTo("groupId", groupId)
                    .equalTo("deleted", false);
            if (ownerOrAdminOnly) {
                query = query.in("role", new Integer[]{GroupRole.OWNER, GroupRole.ADMIN});
            }
            List<GroupMemberEntity> results = query
                    .sort(new String[]{"role", "joinTime"}, new Sort[]{Sort.ASCENDING, Sort.ASCENDING})
                    .findAll();
            if (safeOffset >= results.size()) {
                return Collections.emptyList();
            }
            int end = Math.min(safeOffset + limit, results.size());
            List<GroupMemberItem> list = new ArrayList<>(end - safeOffset);
            for (int i = safeOffset; i < end; i++) {
                list.add(GroupMemberRealmMapper.toItem(results.get(i)));
            }
            return list;
        } finally {
            realm.close();
        }
    }

    public int countActive(@NonNull String groupId, boolean ownerOrAdminOnly) {
        if (!ImRealm.isUserBound() || TextUtils.isEmpty(groupId)) {
            return 0;
        }
        Realm realm = ImRealm.getRealm();
        try {
            io.realm.RealmQuery<GroupMemberEntity> query = realm.where(GroupMemberEntity.class)
                    .equalTo("groupId", groupId)
                    .equalTo("deleted", false);
            if (ownerOrAdminOnly) {
                query = query.in("role", new Integer[]{GroupRole.OWNER, GroupRole.ADMIN});
            }
            return (int) query.count();
        } finally {
            realm.close();
        }
    }

    @NonNull
    private static List<String> dedupeUserIds(@Nullable List<String> userIds) {
        Set<String> set = new HashSet<>();
        if (userIds != null) {
            for (String id : userIds) {
                if (!TextUtils.isEmpty(id)) {
                    set.add(id.trim());
                }
            }
        }
        return new ArrayList<>(set);
    }
}
