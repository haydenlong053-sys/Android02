package com.cboda.intelligent.model

import java.io.Serializable

class SecurityInfoBroadBean : Serializable{
    @JvmField
    var securityQuestions : String? = ""
    @JvmField
    var securityAnswer : String? = ""

}