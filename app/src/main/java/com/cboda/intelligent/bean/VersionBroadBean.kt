package com.cboda.intelligent.bean

import java.io.Serializable

/**
 * POST {@code /api/versionUpdate} 扁平响应业务字段。
 *
 * @property number 升级编号，与本地 versionCode 比较
 * @property type 0 可选 / 1 强制
 * @property platform android / ios/all
 */
class VersionUpdateBean : Serializable {
    var number: Int = 0
    var version: String? = null
    var url: String? = null
    var content: String? = null
    var type: String? = null
    var platform: String? = null

    fun isForceUpdate(): Boolean = type == "1"

    fun isAndroidPlatform(): Boolean {
        val p = platform?.trim()?.lowercase().orEmpty()
        return p.isEmpty() || p == "android" || p == "all"
    }
}
