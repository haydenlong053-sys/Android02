package com.cboda.baselibrary.bean

/**
 * 简述： 接口返回base bean对象
 * Created by ysh on 2023/3/1.
 */
open class BaseResponse {
    val code: String? = null
    val msg: String? = null
    val totalCount: String? = null
    val totalPage: Int = 0
    val success = false
}