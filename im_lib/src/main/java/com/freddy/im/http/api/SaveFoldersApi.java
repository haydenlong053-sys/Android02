package com.freddy.im.http.api;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.freddy.im.http.model.im.ConvFolderItem;
import com.hjq.http.config.IRequestApi;

import java.util.List;

/**
 * POST {@code im/conversation/saveFolders}：整份覆盖保存会话分组（文档 § convFolders）。
 */
public final class SaveFoldersApi implements IRequestApi {

    private long version;
    private List<ConvFolderItem> folders;

    @NonNull
    @Override
    public String getApi() {
        return "im/conversation/saveFolders";
    }

    public SaveFoldersApi setVersion(long version) {
        this.version = version;
        return this;
    }

    public SaveFoldersApi setFolders(@Nullable List<ConvFolderItem> folders) {
        this.folders = folders;
        return this;
    }
}
