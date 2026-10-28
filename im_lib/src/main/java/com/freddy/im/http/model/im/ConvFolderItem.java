package com.freddy.im.http.model.im;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * 会话分组（文档 ConvFolder）；{@link #iconType} 仅客户端本地。
 */
public class ConvFolderItem implements Serializable {

    private String id;
    private String name;
    private List<String> sessionIds;
    /** 本地图标；服务端不存。 */
    private int iconType;
    private int sortIndex;

    @Nullable
    public String getId() {
        return id;
    }

    public void setId(@Nullable String id) {
        this.id = id;
    }

    @Nullable
    public String getName() {
        return name;
    }

    public void setName(@Nullable String name) {
        this.name = name;
    }

    @NonNull
    public List<String> getSessionIds() {
        return sessionIds != null ? sessionIds : Collections.emptyList();
    }

    public void setSessionIds(@Nullable List<String> sessionIds) {
        this.sessionIds = sessionIds != null ? new ArrayList<>(sessionIds) : null;
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

    @NonNull
    public ConvFolderItem copy() {
        ConvFolderItem c = new ConvFolderItem();
        c.id = id;
        c.name = name;
        c.iconType = iconType;
        c.sortIndex = sortIndex;
        c.sessionIds = sessionIds != null ? new ArrayList<>(sessionIds) : new ArrayList<>();
        return c;
    }
}
