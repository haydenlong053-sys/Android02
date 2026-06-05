package com.freddy.im.http.api;

import androidx.annotation.NonNull;

import com.hjq.http.config.IRequestApi;

/**
 * POST {@code im/user/getUserList}：获取用户列表（5.2）。
 */
public final class GetUserListApi implements IRequestApi {

    private String keyword;
    private Integer pageNo;
    private Integer pageSize;

    @NonNull
    @Override
    public String getApi() {
        return "api/im/user/getUserList";
    }

    public GetUserListApi setKeyword(String keyword) {
        this.keyword = keyword;
        return this;
    }

    public GetUserListApi setPageNo(int pageNo) {
        this.pageNo = pageNo;
        return this;
    }

    public GetUserListApi setPageSize(int pageSize) {
        this.pageSize = pageSize;
        return this;
    }
}
