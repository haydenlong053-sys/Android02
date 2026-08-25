package com.freddy.im.http.model.im;

/**
 * 群业务扩展字段（群详情 {@code GroupVo.busiExt}）。
 * <p>
 * 对齐业务侧群管理开关 / 商城 / 缺席踢人等配置，与 IM 核心群字段分离。
 */
@SuppressWarnings("unused")
public class GroupBusiExt implements java.io.Serializable {
    /**
     * 手续费率，如 0.01 表示 1%
     */
    private String joinPayFeeRate;
    /**
     * 群内禁止加好友："0" / "1"
     */
    private String openQysl;
    /**
     * 本群会议缺席自动移出："0" / "1"
     */
    private String openAbsentKick;
    /**
     * 置顶（业务侧）
     */
    private String isZd;
    /**
     * 自动翻译相关时间
     */
    private String fantime;
    /**
     * 可否发红包
     */
    private String openQzhb;
    /**
     * 群规模档位：{@code "1"} 未升到最高（约 1000 人）；其它已最高档（约 10000 人）。
     */
    private String gsize;
    /**
     * 敏感词相关
     */
    private String openMgc;
    /**
     * 连续缺席次数阈值（全局/平台配置）
     */
    private Integer absentKickLimit;
    /**
     * 群号展示 id
     */
    private String idno;
    /**
     * 等级/价格相关
     */
    private Long gradePrice;
    /**
     * 商城链接
     */
    private String shopLink;
    /**
     * 商业功能开关："0" / "1"；null/空表示不展示入口
     */
    private String isshop;
    /**
     * 群员邀请好友进群："0" / "1"
     */
    private String openQyyq;
    /**
     * 仅群主及管理员可推送名片："0" / "1"
     */
    private String openQzts;
    /**
     * 群员进群邀请校验："0" / "1"
     */
    private String openQyyz;
    /**
     * 群员入群邀请管理验证："0" / "1"
     */
    private String openQygl;
    /**
     * 商城相关时间
     */
    private String shoptime;
    /**
     * 普通成员仅可见群主及管理员："0" / "1"
     */
    private String openQykj;
    /**
     * 发言间隔（秒）
     */
    private int jiange;
    /**
     * 会话内商城入口是否展示，通常与 {@link #isshop} 同步
     */
    private String showShop;

    private String openJoinPay;


    public String getOpenJoinPay() {
        return openJoinPay;
    }

    public void setOpenJoinPay(String openJoinPay) {
        this.openJoinPay = openJoinPay;
    }

    public String getJoinPayFeeRate() {
        return joinPayFeeRate;
    }

    public void setJoinPayFeeRate(String joinPayFeeRate) {
        this.joinPayFeeRate = joinPayFeeRate;
    }

    public String getOpenQysl() {
        return openQysl;
    }

    public void setOpenQysl(String openQysl) {
        this.openQysl = openQysl;
    }

    public String getOpenAbsentKick() {
        return openAbsentKick;
    }

    public void setOpenAbsentKick(String openAbsentKick) {
        this.openAbsentKick = openAbsentKick;
    }

    public String getIsZd() {
        return isZd;
    }

    public void setIsZd(String isZd) {
        this.isZd = isZd;
    }

    public String getFantime() {
        return fantime;
    }

    public void setFantime(String fantime) {
        this.fantime = fantime;
    }

    public String getOpenQzhb() {
        return openQzhb;
    }

    public void setOpenQzhb(String openQzhb) {
        this.openQzhb = openQzhb;
    }

    public String getGsize() {
        return gsize;
    }

    public void setGsize(String gsize) {
        this.gsize = gsize;
    }

    public String getOpenMgc() {
        return openMgc;
    }

    public void setOpenMgc(String openMgc) {
        this.openMgc = openMgc;
    }

    public Integer getAbsentKickLimit() {
        return absentKickLimit;
    }

    public void setAbsentKickLimit(Integer absentKickLimit) {
        this.absentKickLimit = absentKickLimit;
    }

    public String getIdno() {
        return idno;
    }

    public void setIdno(String idno) {
        this.idno = idno;
    }

    public Long getGradePrice() {
        return gradePrice;
    }

    public void setGradePrice(Long gradePrice) {
        this.gradePrice = gradePrice;
    }

    public String getShopLink() {
        return shopLink;
    }

    public void setShopLink(String shopLink) {
        this.shopLink = shopLink;
    }

    public String getIsshop() {
        return isshop;
    }

    public void setIsshop(String isshop) {
        this.isshop = isshop;
    }

    public String getOpenQyyq() {
        return openQyyq;
    }

    public void setOpenQyyq(String openQyyq) {
        this.openQyyq = openQyyq;
    }

    public String getOpenQzts() {
        return openQzts;
    }

    public void setOpenQzts(String openQzts) {
        this.openQzts = openQzts;
    }

    public String getOpenQyyz() {
        return openQyyz;
    }

    public void setOpenQyyz(String openQyyz) {
        this.openQyyz = openQyyz;
    }

    public String getOpenQygl() {
        return openQygl;
    }

    public void setOpenQygl(String openQygl) {
        this.openQygl = openQygl;
    }

    public String getShoptime() {
        return shoptime;
    }

    public void setShoptime(String shoptime) {
        this.shoptime = shoptime;
    }

    public String getOpenQykj() {
        return openQykj;
    }

    public void setOpenQykj(String openQykj) {
        this.openQykj = openQykj;
    }

    public int getJiange() {
        return jiange;
    }

    public void setJiange(int jiange) {
        this.jiange = jiange;
    }

    public String getShowShop() {
        return showShop;
    }

    public void setShowShop(String showShop) {
        this.showShop = showShop;
    }
}
