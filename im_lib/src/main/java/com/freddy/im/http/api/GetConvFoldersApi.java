package com.freddy.im.http.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/** GET {@code im/conversation/folders}（文档 § convFolders）。 */
public final class GetConvFoldersApi implements IRequestApi {

    @NonNull
    @Override
    public String getApi() {
        return "im/conversation/folders";
    }
}
