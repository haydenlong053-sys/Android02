package com.cboda.intelligent.utils

import org.json.JSONObject

object QuickJsonBroadUtils {
    /**
     * 从 JSON 字符串解析某个 key 的值
     */
    fun getValue(jsonStr: String, key: String): String? {
        return try {
            val obj = JSONObject(jsonStr)
            obj.optString(key, null)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 把 JSON 字符串解析成 JSONObject
     */
    fun parse(jsonStr: String): JSONObject? {
        return try {
            JSONObject(jsonStr)
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 构建一个 JSONObject 并快速添加 key-value
     */
    fun build(vararg pairs: Pair<String, Any?>): JSONObject {
        val obj = JSONObject()
        for ((key, value) in pairs) {
            obj.put(key, value)
        }
        return obj
    }

    /**
     * 把 JSONObject 转换为 JSON 字符串
     */
    fun toJson(obj: JSONObject?): String? {
        return obj?.toString()
    }


}