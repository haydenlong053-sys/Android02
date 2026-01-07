package com.cboda.intelligent.bean

import com.cboda.baselibrary.bean.BaseBroadResponse

/**
 * 简述： <功能描述>
 * Created by ysh on 2023/4/7.
 */
class VersionBroadBean : BaseBroadResponse() {
    var result: Version? = null

    class Version {
        var number: String? = null
        var version: String? = null
        var type: String? = null //类型 1强制更新
        var content: String? = null
        var url: String? = null
    }
}