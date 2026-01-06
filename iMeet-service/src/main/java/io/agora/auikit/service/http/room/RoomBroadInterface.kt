package io.agora.auikit.service.http.room

import io.agora.auikit.service.http.CommonBroadResp
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface RoomBroadInterface {

    @POST("room/create")
    fun createRoom(@Body req: CreateRoomBroadReq): Call<CommonBroadResp<CreateRoomResp>>

    @POST("room/destroy")
    fun destroyRoom(@Body req: RoomUserReq): Call<CommonBroadResp<DestroyRoomResp>>

    @POST("room/list")
    fun fetchRoomList(@Body req: RoomListReq): Call<CommonBroadResp<RoomListResp>>

    @POST("room/query")
    fun queryRoomInfo(@Body req: QueryRoomReq): Call<CommonBroadResp<QueryRoomResp>>

    @POST("room/update")
    fun updateRoomInfo(@Body req: UpdateRoomReq): Call<CommonBroadResp<String>>

}