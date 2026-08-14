package com.cboda.baselibrary.bean

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class UserInfoBean : Serializable {
    @SerializedName("acount")
    var loginAccount = ""

    @SerializedName("eqno")
    var deviceId = ""

    @SerializedName("idno")
    var showId = ""

    @SerializedName("mId")
    var realityId = ""//真实ID

    /** IM getUser 原始 userId */
    @SerializedName("userId")
    var userId: String? = null

    var vocational = ""//职业

    var id = ""

    @SerializedName("hasPaypwd")
    var hasPayPwd = ""//是否有支付密码

    var gradePrice: String? = null
    var gradeStatus: String? = null

    var creditScoreNewUserDays = ""//支持发送链接注册最短时间（天数）
    var creditScoreNewInGroupDays = ""//入群变化样式所需的最短天数（配置新入群天数）
    var canSendLink = "false" //能否发送链接

    @SerializedName("nickname")
    var name = ""//名称

    @SerializedName("icon")
    var userImg = ""

    @SerializedName("qrcode2")
    var loginQrcode = ""

    @SerializedName("tQrcode2")
    var loginNewQrCode = ""//扫码登录二维码

    @SerializedName("tQrcode")
    var businessQrCode = ""//加好友二维码

    @SerializedName(value = "qrcode", alternate = ["cardQrUrl"])
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

    /**
     * 兼容旧接口字符串与 IM 数字。仅用 [isVipAny] 接 JSON，避免 String setter 遇数字抛异常。
     */
    @SerializedName(value = "isVip", alternate = ["isvip"])
    @JvmField
    var isVipAny: Any? = null

    /** 对外统一成 "0"/"1"，只读，防止 Gson 走 String setter */
    val isVip: String
        get() = when (val v = isVipAny) {
            null -> ""
            is Number -> if (v.toInt() != 0) "1" else "0"
            is Boolean -> if (v) "1" else "0"
            else -> {
                val s = v.toString()
                when {
                    s == "1" || s.equals("true", ignoreCase = true) -> "1"
                    s == "0" || s.equals("false", ignoreCase = true) -> "0"
                    else -> s
                }
            }
        }

    var sex: String? = ""

    var isLiang = ""//是否为靓号

    @SerializedName("memberInterestList")
    var memberInterestList: Array<String>? = null //标签

    @SerializedName("3dDoll")
    var registerDayBean: RegisterDayBeanNew? = null

    @SerializedName("kfid")
    var aiRobotId = "robot001" //机器人ID

    var chatImg = ""

    fun normalizeImFields() {
        val uid = userId?.takeIf { it.isNotEmpty() }
        if (!uid.isNullOrEmpty()) {
            if (id.isEmpty()) id = uid
            if (realityId.isEmpty()) realityId = uid
        }
        if (userImg == null) {
            // Gson 可能把 JSON null 写进非空 String
            userImg = ""
        }
        if (sex == null) sex = ""
    }

    class RegisterDayBeanNew {
        @SerializedName("registDay")
        var registrationDays = ""//注册天数

        @SerializedName("newUserDays")
        var registrationDayScore = ""//信用分底色变化所需的最短天数
    }
}
