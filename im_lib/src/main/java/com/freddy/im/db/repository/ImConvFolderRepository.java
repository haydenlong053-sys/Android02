package com.freddy.im.db.repository;

import android.text.TextUtils;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.db.ImDbExecutor;
import com.freddy.im.db.ImRealm;
import com.freddy.im.db.entity.ConvFolderEntity;
import com.freddy.im.db.entity.ConvFolderMetaEntity;
import com.freddy.im.db.mapper.ConvFolderRealmMapper;
import com.freddy.im.http.model.im.ConvFolderItem;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

/**
 * 会话分组仓储：整份替换 + 按 sortIndex 读取；保留本地 iconType。
 */
public final class ImConvFolderRepository {

    private static final ImConvFolderRepository INSTANCE = new ImConvFolderRepository();

    public interface OnFoldersCallback {
        void onLoaded(long version, @NonNull List<ConvFolderItem> folders);
    }

    private ImConvFolderRepository() {
    }

    @NonNull
    public static ImConvFolderRepository getInstance() {
        return INSTANCE;
    }

    public void replaceAll(long version,
                           @Nullable List<ConvFolderItem> folders,
                           @Nullable OnFoldersCallback onDone) {
        if (!ImRealm.isUserBound()) {
            if (onDone != null) {
                onDone.onLoaded(0L, Collections.emptyList());
            }
            return;
        }
        final List<ConvFolderItem> source = folders != null ? folders : Collections.emptyList();
        ImDbExecutor.runWrite(() -> {
            Realm realm = ImRealm.getRealm();
            List<ConvFolderItem> saved = new ArrayList<>();
            try {
                realm.executeTransaction(r -> {
                    Map<String, Integer> localIcons = new HashMap<>();
                    RealmResults<ConvFolderEntity> old = r.where(ConvFolderEntity.class).findAll();
                    for (ConvFolderEntity e : old) {
                        if (e != null && !TextUtils.isEmpty(e.getFolderId())) {
                            localIcons.put(e.getFolderId(), e.getIconType());
                        }
                    }
                    old.deleteAllFromRealm();

                    int index = 0;
                    for (ConvFolderItem item : source) {
                        if (item == null || TextUtils.isEmpty(item.getId())) {
                            continue;
                        }
                        Integer keepIcon = localIcons.get(item.getId());
                        if (keepIcon != null && item.getIconType() == 0) {
                            item.setIconType(keepIcon);
                        }
                        ConvFolderEntity entity = r.createObject(ConvFolderEntity.class, item.getId());
                        ConvFolderRealmMapper.fillEntity(entity, item, index);
                        saved.add(ConvFolderRealmMapper.toItem(entity));
                        index++;
                    }

                    ConvFolderMetaEntity meta = r.where(ConvFolderMetaEntity.class)
                            .equalTo("key", ConvFolderMetaEntity.KEY_DEFAULT)
                            .findFirst();
                    if (meta == null) {
                        meta = r.createObject(ConvFolderMetaEntity.class, ConvFolderMetaEntity.KEY_DEFAULT);
                    }
                    meta.setVersion(version);
                });
            } finally {
                realm.close();
            }
            if (onDone != null) {
                onDone.onLoaded(version, saved);
            }
        });
    }

    public void loadAll(@NonNull OnFoldersCallback callback) {
        if (!ImRealm.isUserBound()) {
            callback.onLoaded(0L, Collections.emptyList());
            return;
        }
        ImDbExecutor.runRead(() -> {
            Realm realm = ImRealm.getRealm();
            long version = 0L;
            List<ConvFolderItem> list = new ArrayList<>();
            try {
                ConvFolderMetaEntity meta = realm.where(ConvFolderMetaEntity.class)
                        .equalTo("key", ConvFolderMetaEntity.KEY_DEFAULT)
                        .findFirst();
                if (meta != null) {
                    version = meta.getVersion();
                }
                RealmResults<ConvFolderEntity> results = realm.where(ConvFolderEntity.class)
                        .sort("sortIndex", Sort.ASCENDING)
                        .findAll();
                for (ConvFolderEntity e : results) {
                    list.add(ConvFolderRealmMapper.toItem(e));
                }
            } finally {
                realm.close();
            }
            callback.onLoaded(version, list);
        });
    }

    public long loadVersionSync() {
        if (!ImRealm.isUserBound()) {
            return 0L;
        }
        Realm realm = ImRealm.getRealm();
        try {
            ConvFolderMetaEntity meta = realm.where(ConvFolderMetaEntity.class)
                    .equalTo("key", ConvFolderMetaEntity.KEY_DEFAULT)
                    .findFirst();
            return meta != null ? meta.getVersion() : 0L;
        } finally {
            realm.close();
        }
    }
}
