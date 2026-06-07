package com.cboda.intelligent.bean

data class ReportConfigBean(
    val id: Int = 0,
    /** 举报类型名称，列表展示用 */
    val reportType: String = "",
    /** 说明文案 */
    val description: String = "",
) {
    fun displayTitle(): String = reportType.trim()
}
