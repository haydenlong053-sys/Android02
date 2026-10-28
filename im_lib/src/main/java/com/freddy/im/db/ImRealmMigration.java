package com.freddy.im.db;

import androidx.annotation.NonNull;

import io.realm.DynamicRealm;
import io.realm.FieldAttribute;
import io.realm.RealmMigration;
import io.realm.RealmSchema;

/**
 * IM 用户库迁移：保留消息/会话等已有数据，仅增量加表/加字段。
 */
public final class ImRealmMigration implements RealmMigration {

    @Override
    public void migrate(@NonNull DynamicRealm realm, long oldVersion, long newVersion) {
        RealmSchema schema = realm.getSchema();

        // 3 → 4：会话分组（不清库）
        if (oldVersion < 4) {
            if (schema.get("ConvFolderEntity") == null) {
                schema.create("ConvFolderEntity")
                        .addField("folderId", String.class, FieldAttribute.PRIMARY_KEY)
                        .addField("name", String.class)
                        .addField("iconType", int.class)
                        .addField("sortIndex", int.class)
                        .addField("sessionIdsJson", String.class);
            }
            if (schema.get("ConvFolderMetaEntity") == null) {
                schema.create("ConvFolderMetaEntity")
                        .addField("key", String.class, FieldAttribute.PRIMARY_KEY)
                        .addField("version", long.class);
                realm.createObject("ConvFolderMetaEntity", "default").setLong("version", 0L);
            }
            oldVersion = 4;
        }
    }

    @Override
    public int hashCode() {
        return ImRealmMigration.class.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        return obj instanceof ImRealmMigration;
    }
}
