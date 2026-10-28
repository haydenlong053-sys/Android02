package com.freddy.im.http.model.im;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Collections;
import java.util.List;

/**
 * GET/POST {@code /im/conversation/folders|saveFolders} 的 content。
 */
public class ConvFoldersContent {

    private long version;
    private List<ConvFolderItem> folders;

    public long getVersion() {
        return version;
    }

    public void setVersion(long version) {
        this.version = version;
    }

    @NonNull
    public List<ConvFolderItem> getFolders() {
        return folders != null ? folders : Collections.emptyList();
    }

    public void setFolders(@Nullable List<ConvFolderItem> folders) {
        this.folders = folders;
    }
}
