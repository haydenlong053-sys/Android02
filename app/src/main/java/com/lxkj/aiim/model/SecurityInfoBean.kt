package com.lxkj.aiim.model

import java.io.Serializable

class SecurityInfoBean : Serializable{
    @JvmField
    var securityQuestions : String? = ""
    @JvmField
    var securityAnswer : String? = ""

}