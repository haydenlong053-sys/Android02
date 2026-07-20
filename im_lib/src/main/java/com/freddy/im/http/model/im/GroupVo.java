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
    /** 本群会议缺席自动移出开关："0" 关 / "1" 开（群详情接口返回） */
    private String openAbsentKick;
    /** 全局连续未参会次数阈值，如 5（群详情接口返回） */
    private String absentKickLimit;
    /**
     * 群规模档位（群详情返回，对齐业务 {@code gsize}）。
     * {@code "1"}：未升到最高（上限约 1000）；其它：已升级最高档（上限约 10000）。
     */
    private String gsize;
    /**
     * 商业功能开关能力标记（群详情返回）。
     * null/空：不展示入口；"0"：关；"1"：开。对齐野火 {@code CommonResult.isshop}。
     */
    private String isshop;
    /** 会话内商城入口是否展示，通常与 {@link #isshop} 同步。 */
    private String showShop;

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

    public String getOpenAbsentKick() {
        return openAbsentKick;
    }

    public void setOpenAbsentKick(String openAbsentKick) {
        this.openAbsentKick = openAbsentKick;
    }

    public String getAbsentKickLimit() {
        return absentKickLimit;
    }

    public void setAbsentKickLimit(String absentKickLimit) {
        this.absentKickLimit = absentKickLimit;
    }

    public String getGsize() {
        return gsize;
    }

    public void setGsize(String gsize) {
        this.gsize = gsize;
    }

    public String getIsshop() {
        return isshop;
    }

    public void setIsshop(String isshop) {
        this.isshop = isshop;
    }

    public String getShowShop() {
        return showShop;
    }

    public void setShowShop(String showShop) {
        this.showShop = showShop;
    }

}
