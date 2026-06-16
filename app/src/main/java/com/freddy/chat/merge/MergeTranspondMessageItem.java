package com.freddy.chat.merge;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * 合并转发 content 内 {@code messageList} 单条记录。
 */
public final class MergeTranspondMessageItem {

    @Nullable
    private String fromMsgId;
    @Nullable
    private String fromUserId;
    @Nullable
    private String customInfo;
    private int contentType;
    @Nullable
    private String content;

    @Nullable
    public String getFromMsgId() {
        return fromMsgId;
    }

    public void setFromMsgId(@Nullable String fromMsgId) {
        this.fromMsgId = fromMsgId;
    }

    @Nullable
    public String getFromUserId() {
        return fromUserId;
    }

    public void setFromUserId(@Nullable String fromUserId) {
        this.fromUserId = fromUserId;
    }

    @Nullable
    public String getCustomInfo() {
        return customInfo;
    }

    public void setCustomInfo(@Nullable String customInfo) {
        this.customInfo = customInfo;
    }

    public int getContentType() {
        return contentType;
    }

    public void setContentType(int contentType) {
        this.contentType = contentType;
    }

    @Nullable
    public String getContent() {
        return content;
    }

    public void setContent(@Nullable String content) {
        this.content = content;
    }

    @NonNull
    public String requireFromUserId() {
        return fromUserId != null ? fromUserId : "";
    }
}
