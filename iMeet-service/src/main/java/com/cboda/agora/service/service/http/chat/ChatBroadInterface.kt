package com.cboda.agora.service.service.http.chat

import com.cboda.agora.service.service.http.CommonBroadResp
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface ChatBroadInterface {

    @POST("chatRoom/create")
    fun createChatRoom(@Body req: CreateChatRoomReq): Call<CommonBroadResp<CreateChatRoomResp>>

}