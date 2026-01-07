package com.cboda.intelligent.utils

import java.math.BigDecimal

/**
 * 简述： <功能描述>
 * Created by ysh on 2023/4/6.
 */
class BigDecimalBroadUtils {
    companion object {
        //scale 保留小数位数
        fun add(v1: String, v2: String, scale: Int): String {
            val b1 = BigDecimal(v1)
            val b2 = BigDecimal(v2)
            return b1.add(b2).setScale(scale, BigDecimal.ROUND_HALF_UP).toString()
        }

        //scale 保留小数位数
        fun subtract(v1: String, v2: String, scale: Int): String {
            val b1 = BigDecimal(v1)
            val b2 = BigDecimal(v2)
            return b1.subtract(b2).setScale(scale, BigDecimal.ROUND_HALF_UP).toString()
        }

        fun multiply(v1: String, v2: String, scale: Int): String {
            val b1 = BigDecimal(v1)
            val b2 = BigDecimal(v2)
            return b1.multiply(b2).setScale(scale, BigDecimal.ROUND_HALF_UP).toString()
        }

        fun divide(v1: String, v2: String, scale: Int): String {
            val b1 = BigDecimal(v1)
            val b2 = BigDecimal(v2)
            return b1.divide(b2).setScale(scale, BigDecimal.ROUND_HALF_UP).toString()
        }

        fun payPrice(price: String, num: String): String {
            try {
                val totalPrice = (price.toDouble()) * (num.toInt())
                val b1 = BigDecimal(totalPrice)
                return b1.setScale(0, BigDecimal.ROUND_HALF_UP).toString()
            } catch (e: Exception) {
                AppBroadUtils.debug("价格计算异常", e.toString())
            }
            return ""
        }
    }
}