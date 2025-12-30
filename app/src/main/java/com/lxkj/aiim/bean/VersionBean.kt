package com.lxkj.aiim.bean

import com.base.commonlibrary.bean.BaseResponse

/**
 * 简述： <功能描述>
 * Created by ysh on 2023/4/7.
 */
class VersionBean : BaseResponse() {
    var result: Version? = null

    class Version {
        var number: String? = null
        var version: String? = null
        var type: String? = null //类型 1强制更新
        var content: String? = null
        var url: String? = null
    }
}