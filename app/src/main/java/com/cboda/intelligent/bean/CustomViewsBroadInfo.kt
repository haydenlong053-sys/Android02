package com.cboda.intelligent.bean

import com.stx.xhb.xbanner.entity.SimpleBannerInfo

/**
 * 简述:xbanner 使用freso
 */
class CustomViewsBroadInfo(private var info: String) : SimpleBannerInfo() {
    override fun getXBannerUrl(): String {
        return info
    }

    override fun getXBannerTitle(): String {
        return ""
    }
}