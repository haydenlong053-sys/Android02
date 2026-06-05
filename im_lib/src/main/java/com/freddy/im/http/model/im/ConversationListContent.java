package com.freddy.im.http.model.im;

import java.util.List;

@SuppressWarnings("unused")
public class ConversationListContent {

    private int anchor;
    private List<SessionItem> sessionList;
    private int total;
//    private List<UnreadCountItem> unreadCounts;
    /** 成员列表，结构由业务决定；空数组时常为 null 或空列表 */
    private List<Object> members;

    public int getAnchor() {
        return anchor;
    }

    public List<SessionItem> getSessionList() {
        return sessionList;
    }

    public int getTotal() {
        return total;
    }

//    public List<UnreadCountItem> getUnreadCounts() {
//        return unreadCounts;
//    }

    public List<Object> getMembers() {
        return members;
    }
}
