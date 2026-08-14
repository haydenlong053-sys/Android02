package com.cboda.baselibrary.network.model

/**
 * 网络请求基础响应。
 * 兼容旧格式 code/msg/success/data，以及 IM 格式 errorCode/errorMsg/content。
 *
 * 使用无参构造 + 可写字段，避免 Kotlin 主构造导致 Gson 反序列化丢字段。
 */
open class NetworkBroadResponse<T> {

    var code: Int = 0
    var msg: String? = null
    var success: Boolean = false
    var data: T? = null

    /** IM：0 成功；缺省 -1 表示非 IM 信封 */
    var errorCode: Int = -1
    var errorMsg: String? = null
    var content: T? = null

    constructor()

    constructor(code: Int, msg: String?, success: Boolean, data: T?) {
        this.code = code
        this.msg = msg
        this.success = success
        this.data = data
    }

    fun getPayload(): T? = data ?: content

    fun isRequestSuccess(): Boolean {
        if (code == 200 && success) {
            return true
        }
        return errorCode == 0
    }

    fun getFailureCode(): Int {
        if (errorCode != -1 && errorCode != 0) {
            return errorCode
        }
        if (code != 0 && code != 200) {
            return code
        }
        return -1
    }

    fun getErrorMessage(): String? {
        return if (isRequestSuccess()) {
            null
        } else {
            msg?.takeIf { it.isNotEmpty() } ?: errorMsg ?: "请求失败"
        }
    }

    fun hasData(): Boolean = getPayload() != null

    override fun toString(): String {
        return "NetworkBroadResponse(code=$code, msg='$msg', success=$success, data=$data, errorCode=$errorCode, errorMsg='$errorMsg', content=$content)"
    }
}
