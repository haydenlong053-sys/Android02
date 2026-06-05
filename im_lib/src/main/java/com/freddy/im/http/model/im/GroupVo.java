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
    /** 接口字段名为 {@code owerId}（群主） */
    private String owerId;

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

    public String getOwerId() {
        return owerId;
    }

    public void setOwerId(String owerId) {
        this.owerId = owerId;
    }
}
