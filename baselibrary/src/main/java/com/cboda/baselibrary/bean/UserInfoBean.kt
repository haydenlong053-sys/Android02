package com.cboda.baselibrary.bean

import com.google.gson.annotations.SerializedName

class UserInfoBean {
    @SerializedName("acount")
    var loginAccount = ""

    @SerializedName("eqno")
    var deviceId = ""

    @SerializedName("idno")
    var showId = ""


    @SerializedName("mId")
    var realityId = ""//真实ID

    var vocational = ""//职业

    var id = ""

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

    var grade = ""//信用分

    @SerializedName("lianghao")
    var niceNumberId = "" //靓号Id

    @SerializedName("isvip")
    var isVip = ""

    var sex = "1"//性别
//    var isLiang = ""//是否为靓号


//    @SerializedName("memberInterestList")
//    var memberInterestList: Array<String>? = null //标签

    @SerializedName("3dDoll")
    var registerDayBean: RegisterDayBeanNew? = null


    class RegisterDayBeanNew {
        @SerializedName("registDay")
        var registrationDays = ""
    }
}