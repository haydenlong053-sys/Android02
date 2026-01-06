package com.cboda.agora.service.service.http.user

import com.cboda.agora.service.service.http.CommonBroadResp
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface UserBroadInterface {

    @POST("users/kickOut")
    fun kickOut(@Body req: UserKickOutBroadReq): Call<CommonBroadResp<UserKickOutResp>>

}