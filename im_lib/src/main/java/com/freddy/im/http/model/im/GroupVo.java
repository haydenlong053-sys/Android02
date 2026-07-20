package com.freddy.im.http.model.im;

import java.util.List;

/**
 * 群详情 / 建群响应 {@code content}（GroupVO）。
 * <p>
 * 业务开关与扩展配置见嵌套对象 {@link #busiExt}，不再平铺在根上。
 */
@SuppressWarnings("unused")
public class GroupVo implements java.io.Serializable {

    private String id;
    private String name;
    private int conversation;
    private String creator;
    private String icon;
    private int memCount;
    /** 群成员变更时间（毫秒）；用于与本地比对后决定是否全量同步成员 */
    private long memChgTime;
    /** 接口字段名为 {@code owerId}（群主） */
    private String owerId;
    /** 全员禁言：0 未开启，1 开启（群详情接口返回） */
    private int allMute;
    private List<String> managerIds;
    private String allowMemberInvite;
    private String joinNeedVerify;
    /** 业务扩展（缺席踢人、商城、群规模 gsize 等） */
    private GroupBusiExt busiExt;

    /** @return 是否全员禁言 */
    public boolean isAllMute() {
        return allMute == 1;
    }

    public int getAllMute() {
        return allMute;
    }

    public void setAllMute(int allMute) {
        this.allMute = allMute;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getConversation() {
        return conversation;
    }

    public void setConversation(int conversation) {
        this.conversation = conversation;
    }

    public String getCreator() {
        return creator;
    }

    public void setCreator(String creator) {
        this.creator = creator;
    }

    public String getIcon() {
        return icon;
    }

    public void setIcon(String icon) {
        this.icon = icon;
    }

    public int getMemCount() {
        return memCount;
    }

    public void setMemCount(int memCount) {
        this.memCount = memCount;
    }

    public long getMemChgTime() {
        return memChgTime;
    }

    public void setMemChgTime(long memChgTime) {
        this.memChgTime = memChgTime;
    }

    public String getOwerId() {
        return owerId;
    }

    public void setOwerId(String owerId) {
        this.owerId = owerId;
    }

    public List<String> getManagerIds() {
        return managerIds;
    }

    public void setManagerIds(List<String> managerIds) {
        this.managerIds = managerIds;
    }

    public String getAllowMemberInvite() {
        return allowMemberInvite;
    }

    public void setAllowMemberInvite(String allowMemberInvite) {
        this.allowMemberInvite = allowMemberInvite;
    }

    public String getJoinNeedVerify() {
        return joinNeedVerify;
    }

    public void setJoinNeedVerify(String joinNeedVerify) {
        this.joinNeedVerify = joinNeedVerify;
    }

    public GroupBusiExt getBusiExt() {
        return busiExt;
    }

    public void setBusiExt(GroupBusiExt busiExt) {
        this.busiExt = busiExt;
    }

    /** 懒创建 {@link #busiExt}，便于本地回写开关态。 */
    public GroupBusiExt ensureBusiExt() {
        if (busiExt == null) {
            busiExt = new GroupBusiExt();
        }
        return busiExt;
    }
}
