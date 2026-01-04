package com.lxkj.aiim.bean

import com.cboda.baselibrary.bean.BaseBroadResponse
import com.google.gson.annotations.SerializedName

class KickListBean : BaseBroadResponse(){
    @SerializedName("data")
    val `data`: List<KickData>? =emptyList()
}

data class KickData(
    @SerializedName("kickTime")
    val kickTime: Long? = 0,
    @SerializedName("operator")
    val `operator`: String? = "",
    @SerializedName("reason")
    val reason: String? = "",
    @SerializedName("userId")
    val userId: String? = "",
    @SerializedName("userName")
    val userName: String? = ""
)




