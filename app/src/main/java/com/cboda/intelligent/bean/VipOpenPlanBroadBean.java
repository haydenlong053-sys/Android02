package com.cboda.intelligent.bean;

import java.io.Serializable;

/**
 * 会员开通套餐 api/vip/open/plans 返回项
 */
public class VipOpenPlanBroadBean implements Serializable {
    public int planId;
    public String planName;
    public int durationDays;
    public String priceUsdt;
    public String priceOdic;
    public boolean odicPriceAvailable;
}
