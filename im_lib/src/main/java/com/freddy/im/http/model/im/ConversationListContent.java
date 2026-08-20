package com.freddy.im.http.model.im;

import java.util.List;

@SuppressWarnings("unused")
public class ConversationListContent {

    private int anchor;
    private List<SessionItem> sessionList;
    private int total;
    /** 服务端同步水位，成功后作为下次请求的 {@code startTime} */
    private long syncTime;
    /** 本次请求是否增量（非 JSON，由请求层按 startTime 写入） */
    private transient boolean incremental;
//    private List<UnreadCountItem> unreadCounts;
    /** 成员列表，结构由业务决定；空数组时常为 null 或空列表 */
    private List<Object> members;

    public int getAnchor() {
        return anchor;
    }

    public List<SessionItem> getSessionList() {
        return sessionList;
    }

    public void setSessionList(List<SessionItem> sessionList) {
        this.sessionList = sessionList;
    }

    public int getTotal() {
        return total;
    }

    public long getSyncTime() {
        return syncTime;
    }

    public void setSyncTime(long syncTime) {
        this.syncTime = syncTime;
    }

    public boolean isIncremental() {
        return incremental;
    }

    public void setIncremental(boolean incremental) {
        this.incremental = incremental;
    }

//    public List<UnreadCountItem> getUnreadCounts() {
//        return unreadCounts;
//    }

    public List<Object> getMembers() {
        return members;
    }
}
