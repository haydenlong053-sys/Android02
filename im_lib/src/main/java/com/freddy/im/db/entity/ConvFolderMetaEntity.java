package com.freddy.im.db.entity;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * 会话分组同步元数据（单行）：服务端乐观锁 {@code version}。
 */
public class ConvFolderMetaEntity extends RealmObject {

    public static final String KEY_DEFAULT = "default";

    @PrimaryKey
    private String key = KEY_DEFAULT;

    private long version;

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }
}
