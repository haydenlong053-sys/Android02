package com.freddy.im.http.model.im;

import java.util.List;

/** POST {@code api/im/group/getList} 响应 content。 */
public class GroupListResp {

    private int anchor;
    private int total;
    private List<GroupVo> groupList;

    public int getAnchor() {
        return anchor;
    }

    public int getTotal() {
        return total;
    }

    public List<GroupVo> getGroupList() {
        return groupList;
    }
}
