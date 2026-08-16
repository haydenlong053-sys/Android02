package com.cboda.intelligent.utils

import com.blankj.utilcode.util.GsonUtils
import com.google.gson.reflect.TypeToken

object GsonBroadUtil {
    private val gson = GsonUtils.getGson()

    /**
     * 将JSON字符串转换为List
     * @param json JSON字符串
     * @param clazz List中元素的类型
     * @return List对象
     */
    fun <T> stringToList(json: String?, clazz: Class<T?>): MutableList<T?>? {
        val type = TypeToken.getParameterized(MutableList::class.java, clazz).getType()
        return gson.fromJson<MutableList<T?>?>(json, type)
    }

    /**
     * 将List转换为JSON字符串
     * @param list List对象
     * @return JSON字符串
     */
    fun <T> listToString(list: MutableList<T?>?): String? {
        return gson.toJson(list)
    }
}
