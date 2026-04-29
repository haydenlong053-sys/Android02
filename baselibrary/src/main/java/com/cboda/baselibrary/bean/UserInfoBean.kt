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

    @SerializedName("hasPaypwd")
    var hasPayPwd = ""//是否有支付密码

    var gradePrice: String? = null
    var gradeStatus: String? = null

    var creditScoreNewUserDays = ""//支持发送链接注册最短时间（天数）
    var creditScoreNewInGroupDays = ""//入群变化样式所需的最短天数（配置新入群天数）
    var canSendLink = "false" //能否发送链接
    var name = ""//名称

    @SerializedName("icon")
    var userImg = ""

    @SerializedName("qrcode2")
    var loginQrcode = ""

    @SerializedName("tQrcode2")
    var loginNewQrCode = ""//扫码登录二维码


    @SerializedName("tQrcode")
    var businessQrCode = ""//加好友二维码


    var qrcode = ""
    var fris = 0L
    var gros = 0L

    @SerializedName("autofanyi")
    var autoTranslation = ""

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

    var sex = ""//性别


    var isLiang = ""//是否为靓号


    @SerializedName("memberInterestList")
    var memberInterestList: Array<String>? = null //标签

    @SerializedName("3dDoll")
    var registerDayBean: RegisterDayBeanNew? = null


    @SerializedName("kfid")
    var aiRobotId = "robot001" //机器人ID


    var chatImg = ""

//    @JvmField
//    var profession = "" //职业
//
//
//    @JvmField
//    var city = "" //地区

    class RegisterDayBeanNew {
        @SerializedName("registDay")
        var registrationDays = ""//注册天数

        @SerializedName("newUserDays")
        var registrationDayScore = ""//信用分底色变化所需的最短天数
    }
}