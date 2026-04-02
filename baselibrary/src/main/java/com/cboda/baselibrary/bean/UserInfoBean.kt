package com.cboda.baselibrary.bean

import com.google.gson.annotations.SerializedName

class UserInfoBean {
    @SerializedName("acount")
    var loginAccount = ""

    @SerializedName("idno")
    var showId = ""


    @SerializedName("mId")
    var realityId = ""

    var name = ""

    @SerializedName("icon")
    var userImg = ""

    @SerializedName("qrcode2")
    var loginQrcode = ""

    var qrcode = ""

    @SerializedName("paypwd")
    var payPwd = "" //支付密码


    @SerializedName("twoFactorCode")
    var googleCode = "" //谷歌验证码

    @SerializedName("mbName")
    var securityQuestion = "" //密保问题

    @SerializedName("mbAnswer")
    var securityQuestionAnswer = "" //密保问题答案

}