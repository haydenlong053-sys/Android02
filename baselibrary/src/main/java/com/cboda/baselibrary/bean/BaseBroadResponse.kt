package com.cboda.baselibrary.bean

/**
 * 简述： 接口返回base bean对象
 * Created by ysh on 2023/3/1.
 */
open class BaseBroadResponse {
    /** 须为 var，否则 Gson 无法写入 IM 信封展平后的 code/success */
    var code: String? = null
    var msg: String? = null
    var errorMsg: String? = null
    var errorCode: Int = -1
    var totalCount: String? = null
    var totalPage: Int = 0
    var success = false

    fun tipMessage(): String {
        return msg?.takeIf { it.isNotEmpty() } ?: errorMsg?.takeIf { it.isNotEmpty() }.orEmpty()
    }
}
