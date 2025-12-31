package com.lxkj.aiim.bean

import com.cboda.baselibrary.bean.BaseResponse
import com.google.gson.annotations.SerializedName

data class UniqueRoomBean(
    @SerializedName("data")
    val `data`: UniqueRoomData? = UniqueRoomData(),
): BaseResponse()

data class UniqueRoomData(
    @SerializedName("roomId")
    val roomId: String? = "",

    @SerializedName("exists")
    var exists:Int?=1
)