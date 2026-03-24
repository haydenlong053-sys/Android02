package com.cboda.baselibrary.bean

import com.google.gson.annotations.SerializedName

class LoginBean {
    @SerializedName("mbname")
    var securityQuestion = ""
    var existMbName = false
    var existPayPassword = false
    var twoFactorCode = false
    var type = ""
    var linkId = ""
    var haveOldDeviceCode = false
    var oldDevice = false
}