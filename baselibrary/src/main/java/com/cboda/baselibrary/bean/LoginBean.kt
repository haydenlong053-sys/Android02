package com.cboda.baselibrary.bean

import com.google.gson.annotations.SerializedName

class LoginBean {
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


class LoginReturnBean: java.io.Serializable {
    @SerializedName("apptoken")
    var appToken = ""

    @SerializedName("imtoken")
    var imToken = ""

    var userId = ""
    var code: String? = null
    var success = false
    var msg: String? = null
}

class LoginIdBean {
    var securityQuestion = ""
    var payPwd = ""
    var oldDevCode = ""
    var googleCode = ""
    var linkId = ""
}