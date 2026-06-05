package com.freddy.im.http.model.im;

/** POST {@code api/im/group/searchGroupAsst} 列表项。 */
public class GroupApplyInfoRes {

    private String applyId;
    private String groupId;
    private String applicant;
    private String status;
    private long applyTime;

    public String getApplyId() {
        return applyId;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getApplicant() {
        return applicant;
    }

    public String getStatus() {
        return status;
    }

    public long getApplyTime() {
        return applyTime;
    }
}
