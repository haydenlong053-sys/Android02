package com.cboda.baselibrary.bean

import com.google.gson.annotations.SerializedName

class LoginBean {
    var secondVerifyNeed = -1

    var secondVerifyUnsetHint = ""

    @SerializedName("mbname")
    var securityQuestion = ""
    var existMbName = false
    var existPayPassword = false
    var twoFactorCode = false

    //    var type = ""
    var linkId = ""
    var haveOldDeviceCode = false
    var oldDevice = false

    var loginAccount = ""

    var loginPwd = ""
    var secondVerifyType: Int = 0
}


class LoginReturnBean {
    var loginBean: LoginBean? = null
    var msg: String? = null
    var errorCode: Int? = null
}

class LoginIdBean {
    var securityQuestion = ""
    var payPwd = ""
    var oldDevCode = ""
    var googleCode = ""
    var linkId = ""
}