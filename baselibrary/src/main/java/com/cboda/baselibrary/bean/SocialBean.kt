package com.cboda.baselibrary.bean

import com.cboda.baselibrary.bean.CommonResultBroadBean.RegisterDayBean
import com.google.gson.annotations.SerializedName

data class SocialBroadCircle(
    val id: Int,
    val characterTags: String,
    val characterExplanation: String,
    val createTime: String,
    val updateTime: String
)

data class SocialContact(
    val id: String = "",
    val userId: String = "",
    val nickName: String? = "",
    /** 昵称（im/friend/friendList 返回 name） */
    val name: String? = "",
    val remarkName: String? = null,
    /** 备注（im/friend/friendList 返回 bei） */
    val bei: String? = null,
    val icon: String? = null,
    val city: String? = null,
    val profession: String? = null,
    val sex: String? = null,
    val grade: String? = null,
    val days: Int? = null,
    val characterTags: String? = "",
    val characterTagsId: Int? = null,
    val registerDay: String? = null,
    val isvip: String? = null,
    val lianghao: String? = null,
    val first: String? = null, // 服务器返回的首字母备用
    val zimu: String? = null, // 服务器返回的首字母
    val idno: String? = null,
    val sign: String? = null,
    val isZy: String? = null,
) {
    /** 展示名：备注 > 昵称 > userId */
    fun displayName(): String {
        return remarkName?.takeIf { it.isNotBlank() }
            ?: bei?.takeIf { it.isNotBlank() }
            ?: nickName?.takeIf { it.isNotBlank() }
            ?: name?.takeIf { it.isNotBlank() }
            ?: userId
    }
}

data class SocialUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
//    val circles: List<SocialBroadCircle> = emptyList(),
    val contactsAll: List<SocialContact> = emptyList(),
    val selectedCircleTag: String? = null,
    val filteredContacts: List<SocialContact> = emptyList(),
    val indexLetters: List<Char> = emptyList(),
    val groupedByLetter: Map<Char, List<SocialContact>> = emptyMap(),
    val overlayLetter: Char? = null,
    val showOverlay: Boolean = false,
    // 角标数量：新增好友 & 新群邀请
    val newFriendCount: Int = 0,
    val newGroupInviteCount: Int = 0
)

data class SocialFriendRequest(
    val count: Int? = 0
)

data class UserInfoBroadModel(
    @SerializedName("gradeStatus")
    val gradeStatus: Int? = null,
    @SerializedName("gradePrice")
    val gradePrice: String? = "",
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
    @SerializedName("lianghao")
    val lianghao: String? = "",
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
    val grade: Double = 0.0,
    @SerializedName("mId")
    val mId: String? = "",
    @SerializedName("profession")
    val vocational: String? = "",
    @SerializedName("3dDoll")
    var registerDayBean: RegisterDayBean? = null,
    @SerializedName("memberInterestList")
    var memberInterestList: Array<String>? = null,
)
