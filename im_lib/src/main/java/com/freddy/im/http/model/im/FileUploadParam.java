package com.freddy.im.http.model.im;

/**
 * {@code POST /filehub/upload} 表单字段 {@code param}（JSON）。
 */
public final class FileUploadParam {

    private String groupId;
    private String userId;
    private String fileHash;

    public String getGroupId() {
        return groupId;
    }

    public FileUploadParam setGroupId(String groupId) {
        this.groupId = groupId;
        return this;
    }

    public String getUserId() {
        return userId;
    }

    public FileUploadParam setUserId(String userId) {
        this.userId = userId;
        return this;
    }

    public String getFileHash() {
        return fileHash;
    }

    public FileUploadParam setFileHash(String fileHash) {
        this.fileHash = fileHash;
        return this;
    }
}
