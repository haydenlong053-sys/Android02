package com.cboda.intelligent.bean

import java.io.Serializable


/**
 * 检测结果条目数据模型
 * @param title 标题（团队背景、商业逻辑、合规性、资金流向）
 * @param description 描述内容
 * @param isNormal 是否异常（true=正常，false=异常）
 */
data class CheckResultBroadItem(
    val title: String?,
    val description: String?,
    val isNormal: Boolean,
    val stateDes: String?,
): Serializable

class CheckResultInfo : Serializable{
    var riskLevel: String? = null
    var riskValue: Double = 0.0
    var mList  =mutableListOf<CheckResultBroadItem>()
}

data class AiResultListBean(
    var aiGenerated: Boolean = false,
    var createTime: String? = null,
    var fileNames: List<String>? = null,
    var id: Int = 0,
    var resourceType: String? = null,
    var updateTime: String? = null,
    var urls: List<String>? = null,
    var userId: String? = null
)

