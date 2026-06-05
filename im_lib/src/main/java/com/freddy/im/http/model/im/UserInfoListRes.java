package com.freddy.im.http.model.im;

import java.util.List;

/**
 * POST {@code im/user/getUserList} 响应 {@code content}（UserInfoListRes）。
 */
@SuppressWarnings("unused")
public class UserInfoListRes {

    private List<UserInfoItem> userList;
    private Integer total;
    private Integer pageNo;
    private Integer pageSize;

    public List<UserInfoItem> getUserList() {
        return userList;
    }


    public Integer getTotal() {
        return total;
    }

    public Integer getPageNo() {
        return pageNo;
    }

    public Integer getPageSize() {
        return pageSize;
    }
}
