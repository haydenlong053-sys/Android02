package com.lxkj.aiim.bean

import com.cboda.baselibrary.bean.BaseResponse
import com.google.gson.annotations.SerializedName

 class UpdateRoomBean(
    @SerializedName("data")
    val `data`: UpdateSuccess? = UpdateSuccess(),

): BaseResponse()
data class UpdateSuccess(
    @SerializedName("success")
    val success: Boolean? = false,

)




