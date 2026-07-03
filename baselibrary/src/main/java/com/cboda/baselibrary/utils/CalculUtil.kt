package com.cboda.baselibrary.utils

import java.math.BigDecimal

private const val tAG = "CalculUtilClass"
fun mul(v1: Any?, v2: Any?): Float {
    return try {
        val getV1 = v1?.let {
            when (it) {
                is String -> if (it.trim() == "" || it.trim() == "null") "1" else it
                else -> it.toString()
            }
        } ?: "1"
        val getV2 = v2?.let {
            when (it) {
                is String -> if (it.trim() == "" || it.trim() == "null") "1" else it
                else -> it.toString()
            }
        } ?: "1"
        val b1 = BigDecimal(getV1)
        val b2 = BigDecimal(getV2)
        b1.multiply(b2).toFloat()
    } catch (e: Exception) {
        e.toLog(tAG)
        0f
    }
}