package com.freddy.im.http.model.im;

/** GET {@code api/im/group/getGroupNotice/{groupId}} 响应 content。 */
public class GroupNoticeRes {

    private String groupId;
    private String content;
    private String publisher;
    private long publishTime;

    public String getGroupId() {
        return groupId;
    }

    public String getContent() {
        return content;
    }

    public String getPublisher() {
        return publisher;
    }

    public long getPublishTime() {
        return publishTime;
    }
}
