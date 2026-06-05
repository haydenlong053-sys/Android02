package com.freddy.im.http.model.im;

/** GET {@code api/im/group/getGroupQrCode/{groupId}} 响应 content。 */
public class GroupQrCodeRes {

    private String groupId;
    private String codeUrl;
    private String companyId;
    private int qrCodeType;
    private long createTime;

    public String getGroupId() {
        return groupId;
    }

    public String getCodeUrl() {
        return codeUrl;
    }
}
