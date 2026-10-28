package com.freddy.im.db.entity;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

/**
 * 会话分组本地表（与服务端 ConvFolder 对齐；{@code iconType} 仅本地）。
 */
public class ConvFolderEntity extends RealmObject {

    @PrimaryKey
    private String folderId;

    private String name;
    /** 本地图标索引 0–6；他端新建默认 0。 */
    private int iconType;
    private int sortIndex;
    /** sessionIds JSON 数组字符串，如 {@code ["id1","id2"]}。 */
    private String sessionIdsJson;

    public String getFolderId() {
        return folderId;
    }

    public void setFolderId(String folderId) {
        this.folderId = folderId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIconType() {
        return iconType;
    }

    public void setIconType(int iconType) {
        this.iconType = iconType;
    }

    public int getSortIndex() {
        return sortIndex;
    }

    public void setSortIndex(int sortIndex) {
        this.sortIndex = sortIndex;
    }

    public String getSessionIdsJson() {
        return sessionIdsJson;
    }

    public void setSessionIdsJson(String sessionIdsJson) {
        this.sessionIdsJson = sessionIdsJson;
    }
}
