package com.freddy.im.http.model.im;

import java.util.List;

/**
 * POST {@code im/group/memberList} 响应 {@code content}（见 {@code ~/Downloads/api/GroupController-API.md} §3）。
 */
@SuppressWarnings("unused")
public class GroupMemberListContent {

    /** 与接口文档默认每页条数一致 */
    public static final int DEFAULT_PAGE_SIZE = 200;
    public static final int PAGE_SIZE_100 = 100;
    public static final int PAGE_SIZE_50 =50;

    private String groupId;
    private int anchor;
    private int total;
    private List<GroupUserVo> members;

    public String getGroupId() {
        return groupId;
    }

    public int getAnchor() {
        return anchor;
    }

    public int getTotal() {
        return total;
    }

    public List<GroupUserVo> getMembers() {
        return members;
    }

    /**
     * 是否还有下一页：本页条数 &gt;= 请求的 {@code pageSize}（文档：直到返回条数小于每页条数为止）。
     */
    public boolean hasNextPage(int pageSize) {
        List<GroupUserVo> list = members;
        return list != null && !list.isEmpty() && list.size() >= pageSize;
    }
}
