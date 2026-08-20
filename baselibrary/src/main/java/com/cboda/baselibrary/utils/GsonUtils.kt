package com.cboda.baselibrary.utils

import com.blankj.utilcode.util.GsonUtils
import com.google.gson.GsonBuilder
import com.google.gson.JsonParser
import com.google.gson.reflect.TypeToken

fun toJson2(any: Any?): String {
    return try {
        GsonUtils.getGson().toJson(any)
    } catch (_: Exception) {
        ""
    }
}

fun <T> fromJson2(key: String?, classOfT: Class<T>): T {
    return try {
        var jsonValue = key ?: ""
        if (jsonValue.isEmpty()) {
            jsonValue = "{}"
        }
        if (jsonValue.contains("{") && jsonValue.contains("}")) {
        } else {
            jsonValue = "{}"
        }
        GsonUtils.getGson().fromJson(jsonValue, classOfT)
    } catch (_: Exception) {
        GsonUtils.getGson().fromJson("{}", classOfT)
    }
}

fun <T> fromJsonTypeToken(key: String?, type: TypeToken<T>): T {
    return try {
        var jsonValue = key ?: ""
        if (jsonValue.isEmpty()) {
            jsonValue = "{}"
        }
        if (jsonValue.contains("{") && jsonValue.contains("}")) {
        } else {
            jsonValue = "{}"
        }
        GsonUtils.getGson().fromJson(jsonValue, type.type)
    } catch (e: Exception) {
        GsonUtils.getGson().fromJson("{}", type.type)
    }
}

fun <T> fromMapJson(key: String?): HashMap<String, T> {
    return try {
        val jsonValue = key ?: ""
        GsonUtils.getGson().fromJson(jsonValue, object : TypeToken<HashMap<String, T>>() {}.type)
    } catch (e: Exception) {
        HashMap()
    }
}

fun <T> fromListJson(key: String?): List<T> {
    return try {
        var jsonValue = key ?: ""
        if (jsonValue.isEmpty()) {
            jsonValue = "{}"
        }
        if (jsonValue.contains("{") && jsonValue.contains("}")) {
        } else {
            jsonValue = "{}"
        }
        GsonUtils.getGson().fromJson(jsonValue, object : TypeToken<List<T>>() {}.type)
    } catch (e: Exception) {
        GsonUtils.getGson().fromJson("", object : TypeToken<List<T>>() {}.type)
    }
}


fun <T> fromListJson2(key: String?, classOfT: Class<T>): ArrayList<T> {
    val parser = JsonParser()
    val jArray = parser.parse(key).asJsonArray
    val list = ArrayList<T>()
    for (obj in jArray) {
        val cse = GsonUtils.getGson().fromJson(obj, classOfT)
        list.add(cse)
    }
    return list
}


fun gsonFormat(json: String): String {
    return try {
        // 先解析再 pretty 输出，避免一行堆在一起
        val jsonElement = JsonParser().parse(json)
        if (getIsDebug()) {
            GsonBuilder().setPrettyPrinting().create().toJson(jsonElement)
        } else {
            toJson2(jsonElement)
        }
    } catch (_: Exception) {
        json // 格式错误时返回原字符串
    }
}