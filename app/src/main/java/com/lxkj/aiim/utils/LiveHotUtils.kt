package com.lxkj.aiim.utils

import java.math.RoundingMode
import java.text.DecimalFormat


/**
 * 简述： <功能描述>
 * Created by ysh on 2023/9/11.
 */
class LiveHotUtils {
    companion object {
        var dfOne = DecimalFormat("0.0")

        fun formatLiveHot(hotNum:String?):String{
            try {
                dfOne.roundingMode = RoundingMode.HALF_EVEN
                val count = hotNum?.toDouble()?:0.0
//                if (count > 99999000) {
//                    return "9999.9W+"
//                } else if (count >= 10000) {
//                    return dfOne.format((count / 10000f).toDouble()) + "W"
//                }
                if (count >= 10000) {
                    return dfOne.format((count / 10000f).toDouble()) + "W"
                }
            } catch (e: Exception) {
                AppUtil.debug("formatLiveHot", "excepiton:$e")
            }
            return hotNum?:"0"
        }
    }
}