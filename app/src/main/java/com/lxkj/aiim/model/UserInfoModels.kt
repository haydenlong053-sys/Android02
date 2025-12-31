package com.lxkj.aiim.model

import com.cboda.baselibrary.bean.CommonResultBean.RegisterDayBean
import com.google.gson.annotations.SerializedName

/**
 * 原本老代码是所有接口在一个类
 * 现在分开来
 */
data class UserInfoModel(
    @SerializedName("accSerch")
    val accSerch: String? = "",
    @SerializedName("acount")
    val acount: String? = "",
    @SerializedName("autofanyi")
    val autofanyi: String? = "",
    @SerializedName("balalce")
    val balalce: Double? = 0.0,
    @SerializedName("biandabianyi")
    val biandabianyi: String? = "",
    @SerializedName("chatImg")
    val chatImg: String? = "",
    @SerializedName("fris")
    val fris: String? = "",
    @SerializedName("groAdd")
    val groAdd: String? = "",
    @SerializedName("gros")
    val gros: String? = "",
    @SerializedName("hasPaypwd")
    val hasPaypwd: String? = "",
    @SerializedName("hdIcon")
    val hdIcon: String? = "",
    @SerializedName("hudong")
    val hudong: String? = "",
    @SerializedName("icon")
    val icon: String? = "",
    @SerializedName("id")
    val id: String? = "",
    @SerializedName("idno")
    val idno: String? = "",
    @SerializedName("idnoSerch")
    val idnoSerch: String? = "",
    @SerializedName("img")
    val img: String? = "",
    @SerializedName("sex")
    val sex: String? = "0",
    @SerializedName("isAddYz")
    val isAddYz: String? = "",
    @SerializedName("isLiang")
    val isLiang: String? = "",
    @SerializedName("isMsgtx")
    val isMsgtx: String? = "",
    @SerializedName("isQuantx")
    val isQuantx: String? = "",
    @SerializedName("isvip")
    val isvip: String? = "",
    @SerializedName("kfid")
    val kfid: String? = "",
    @SerializedName("mb")
    val mb: String? = "",
    @SerializedName("mbid")
    val mbid: String? = "",
    @SerializedName("mpAdd")
    val mpAdd: String? = "",
    @SerializedName("name")
    val name: String? = "",
    @SerializedName("noadd")
    val noadd: String? = "",
    @SerializedName("pwd")
    val pwd: String? = "",
    @SerializedName("qrAdd")
    val qrAdd: String? = "",
    @SerializedName("qrcode")
    val qrcode: String? = "",
    @SerializedName("qrcode2")
    val qrcode2: String? = "",
    @SerializedName("quanCount")
    val quanCount: String? = "",
    @SerializedName("regType")
    val regType: String? = "",
    @SerializedName("showQuan")
    val showQuan: String? = "",
    @SerializedName("vipTime")
    val vipTime: String? = "",
    @SerializedName("ziti")
    val ziti: String? = "",
    @SerializedName("zxState")
    val zxState: String? = "",
    @SerializedName("grade")
    val grade: Int? = 100,
    @SerializedName("mId")
    val mId: String? = "",
    @SerializedName("profession")
    val vocational: String? = "",
    @SerializedName("3dDoll")
    var registerDayBean: RegisterDayBean? = null,
    @SerializedName("memberInterestList")
    var memberInterestList: Array<String>? = null,
)


