package com.freddy.im.http.model.im;

/** GET {@code api/im/group/getGroupWelcome/{groupId}} 响应 content。 */
public class GroupWelcomeRes {

    private String groupId;
    private boolean enabled;
    private String content;

    public String getGroupId() {
        return groupId;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public String getContent() {
        return content;
    }
}
