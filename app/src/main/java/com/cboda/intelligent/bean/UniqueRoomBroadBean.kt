package com.cboda.intelligent.bean

import com.cboda.baselibrary.bean.BaseBroadResponse
import com.google.gson.annotations.SerializedName

data class UniqueRoomBroadBean(
    @SerializedName("data")
    val `data`: UniqueRoomData? = UniqueRoomData(),
): BaseBroadResponse()

data class UniqueRoomData(
    @SerializedName("roomId")
    val roomId: String? = "",

    @SerializedName("exists")
    var exists:Int?=1
)