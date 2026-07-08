package com.freddy.im.http.model.im;

/**
 * POST {@code im/group/create} 响应 {@code content}（GroupVO）。
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

}
