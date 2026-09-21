package com.freddy.im.http.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

import java.util.List;

/**
 * POST {@code im/message/deleteForAll}：双向删除（对所有人删除，文档 § msgDeleteForAll）。
 */
public final class DeleteForAllMessageApi implements IRequestApi {

    private List<String> messageIds;

    @NonNull
    @Override
    public String getApi() {
        return "im/message/deleteForAll";
    }

    public DeleteForAllMessageApi setMessageIds(@NonNull List<String> messageIds) {
        this.messageIds = messageIds;
        return this;
    }
}
