package com.cboda.intelligent.bean

import com.cboda.baselibrary.bean.BaseBroadResponse
import com.google.gson.annotations.SerializedName

 class UpdateRoomBroadBean(
    @SerializedName("data")
    val `data`: UpdateSuccess? = UpdateSuccess(),

): BaseBroadResponse()
data class UpdateSuccess(
    @SerializedName("success")
    val success: Boolean? = false,

)




