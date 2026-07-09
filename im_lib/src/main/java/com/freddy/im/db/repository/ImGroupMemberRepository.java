package com.freddy.im.db.repository;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.db.ImDbExecutor;
import com.freddy.im.db.ImRealm;
import com.freddy.im.db.entity.GroupMemberEntity;
import com.freddy.im.db.mapper.GroupMemberRealmMapper;
import com.freddy.im.http.model.im.GroupMemberItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import io.realm.Realm;

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
