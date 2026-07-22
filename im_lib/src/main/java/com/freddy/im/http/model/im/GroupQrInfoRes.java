package com.freddy.im.http.model.im;

/**
 * GET {@code api/im/group/getGroupByCodeId/{codeId}} 响应 content。
 * 继承 {@link GroupVo}，额外含入群方式与公司名称。
 */
@SuppressWarnings("unused")
public class GroupQrInfoRes extends GroupVo {

    /** 入群方式 */
    private int addType;
    /** 公司名称 */
    private String companyName;

    public int getAddType() {
        return addType;
    }

    public void setAddType(int addType) {
        this.addType = addType;
    }

    public String getCompanyName() {
        return companyName;
    }

    public void setCompanyName(String companyName) {
        this.companyName = companyName;
    }
}
