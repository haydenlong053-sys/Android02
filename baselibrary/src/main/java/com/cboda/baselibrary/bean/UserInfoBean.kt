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

}