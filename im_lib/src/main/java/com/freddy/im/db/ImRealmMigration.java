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

        // → 3：群置顶消息快照表
        if (oldVersion < 3) {
            if (schema.get("PinnedMessageEntity") == null) {
                schema.create("PinnedMessageEntity")
                        .addField("pinKey", String.class, FieldAttribute.PRIMARY_KEY)
                        .addField("groupId", String.class, FieldAttribute.INDEXED)
                        .addField("messageId", String.class)
                        .addField("sender", String.class)
                        .addField("contentType", String.class)
                        .addField("content", String.class)
                        .addField("customInfo", String.class)
                        .addField("sendTime", long.class)
                        .addField("pinnedBy", String.class)
                        .addField("pinTime", long.class, FieldAttribute.INDEXED);
            }
            oldVersion = 3;
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
