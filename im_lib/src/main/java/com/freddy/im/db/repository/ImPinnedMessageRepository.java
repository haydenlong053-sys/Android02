package com.freddy.im.db.repository;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.db.ImDbExecutor;
import com.freddy.im.db.ImRealm;
import com.freddy.im.db.entity.PinnedMessageEntity;
import com.freddy.im.db.mapper.PinnedMessageRealmMapper;
import com.freddy.im.http.model.im.PinnedMessageItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * 群置顶消息快照仓储：与消息表独立，删除消息不级联。
 */
public final class ImPinnedMessageRepository {

    private static final ImPinnedMessageRepository INSTANCE = new ImPinnedMessageRepository();

    public interface OnPinnedListCallback {
        void onLoaded(@NonNull List<PinnedMessageItem> list);
    }

    public interface OnPinnedReplaceCallback {
        void onReplaced(@NonNull String groupId, @NonNull List<PinnedMessageItem> list);
    }

    private ImPinnedMessageRepository() {
    }

    @NonNull
    public static ImPinnedMessageRepository getInstance() {
        return INSTANCE;
    }

    /** 全量替换某群置顶列表（按 pinTime 倒序入库）。 */
    public void replaceGroupPinned(@NonNull String groupId,
                                   @Nullable List<PinnedMessageItem> items,
                                   @Nullable OnPinnedReplaceCallback onDone) {
        if (TextUtils.isEmpty(groupId) || !ImRealm.isUserBound()) {
            if (onDone != null) {
                onDone.onReplaced(groupId, Collections.emptyList());
            }
            return;
        }
        final String gid = groupId;
        final List<PinnedMessageItem> source = items != null ? items : Collections.emptyList();
        ImDbExecutor.runWrite(() -> {
            Realm realm = ImRealm.getRealm();
            List<PinnedMessageItem> saved = new ArrayList<>();
            try {
                realm.executeTransaction(r -> {
                    RealmResults<PinnedMessageEntity> old = r.where(PinnedMessageEntity.class)
                            .equalTo("groupId", gid)
                            .findAll();
                    old.deleteAllFromRealm();
                    for (PinnedMessageItem item : source) {
                        if (item == null || TextUtils.isEmpty(item.getMessageId())) {
                            continue;
                        }
                        item.setGroupId(gid);
                        PinnedMessageEntity entity = r.createObject(
                                PinnedMessageEntity.class,
                                PinnedMessageEntity.buildKey(gid, item.getMessageId()));
                        PinnedMessageRealmMapper.fillEntity(entity, item);
                        saved.add(PinnedMessageRealmMapper.toItem(entity));
                    }
                });
            } finally {
                realm.close();
            }
            if (onDone != null) {
                onDone.onReplaced(gid, saved);
            }
        });
    }

    public void loadByGroupId(@Nullable String groupId, @NonNull OnPinnedListCallback callback) {
        if (TextUtils.isEmpty(groupId) || !ImRealm.isUserBound()) {
            callback.onLoaded(Collections.emptyList());
            return;
        }
        final String gid = groupId;
        ImDbExecutor.runRead(() -> {
            Realm realm = ImRealm.getRealm();
            List<PinnedMessageItem> list = new ArrayList<>();
            try {
                RealmResults<PinnedMessageEntity> results = realm.where(PinnedMessageEntity.class)
                        .equalTo("groupId", gid)
                        .sort("pinTime", Sort.DESCENDING)
                        .findAll();
                for (PinnedMessageEntity entity : results) {
                    list.add(PinnedMessageRealmMapper.toItem(realm.copyFromRealm(entity)));
                }
            } finally {
                realm.close();
            }
            callback.onLoaded(list);
        });
    }

    @Nullable
    public PinnedMessageItem queryLatest(@Nullable String groupId) {
        if (TextUtils.isEmpty(groupId) || !ImRealm.isUserBound()) {
            return null;
        }
        Realm realm = ImRealm.getRealm();
        try {
            PinnedMessageEntity entity = realm.where(PinnedMessageEntity.class)
                    .equalTo("groupId", groupId)
                    .sort("pinTime", Sort.DESCENDING)
                    .findFirst();
            if (entity == null) {
                return null;
            }
            return PinnedMessageRealmMapper.toItem(realm.copyFromRealm(entity));
        } finally {
            realm.close();
        }
    }

    public boolean isPinned(@Nullable String groupId, @Nullable String messageId) {
        if (TextUtils.isEmpty(groupId) || TextUtils.isEmpty(messageId) || !ImRealm.isUserBound()) {
            return false;
        }
        Realm realm = ImRealm.getRealm();
        try {
            return realm.where(PinnedMessageEntity.class)
                    .equalTo("pinKey", PinnedMessageEntity.buildKey(groupId, messageId))
                    .findFirst() != null;
        } finally {
            realm.close();
        }
    }
}
