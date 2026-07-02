package com.cboda.intelligent.bean

/** {@code api/absentKickRules} 响应 data。 */
class AbsentKickRulesBean {
    var platformEnabled: Int? = null
    var consecutiveMissLimit: Int? = null
    var ruleTitle: String? = null
    var descriptions: List<String>? = null
}
