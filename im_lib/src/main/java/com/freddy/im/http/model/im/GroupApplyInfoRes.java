package com.freddy.im.http.model.im;

/**
 * POST {@code api/im/group/searchGroupAsst} 列表项。
 * <p>
 * {@code type}：{@code approve} 待我审批；{@code apply} 我发起的入群申请。
 * {@code status}：{@code 0} 待审 / {@code 1} 已通过 / {@code 2} 已拒绝。
 */
@SuppressWarnings("unused")
public class GroupApplyInfoRes {

    private String applyId;
    private String applicantName;
    private String groupId;
    private String applicant;
    private String companyId;
    private String approvedBy;
    private String status;
    private long applyTime;
    private Long updateTime;
    private String groupName;
    private String icon;
    /**
     * approve：待我审批；apply：我发起的申请
     */
    private String type;
    private String desc;

    public String getApplicantName() {
        return applicantName;
    }

    public void setApplicantName(String applicantName) {
        this.applicantName = applicantName;
    }

    public String getApplyId() {
        return applyId;
    }

    public String getGroupId() {
        return groupId;
    }

    public String getApplicant() {
        return applicant;
    }

    public String getCompanyId() {
        return companyId;
    }

    public String getApprovedBy() {
        return approvedBy;
    }

    public String getStatus() {
        return status;
    }

    public long getApplyTime() {
        return applyTime;
    }

    public Long getUpdateTime() {
        return updateTime;
    }

    public String getGroupName() {
        return groupName;
    }

    public String getType() {
        return type;
    }

    public String getDesc() {
        return desc;
    }

    public String getIcon() {
        return icon;
    }
}
