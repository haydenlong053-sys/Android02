package com.cboda.agora.service.service.room

import com.cboda.agora.service.model.AUIRoomBroadContext
import com.cboda.agora.service.model.AUIRoomBroadInfo
import com.cboda.agora.service.service.callback.AUIBroadCallback
import com.cboda.agora.service.service.callback.AUIBroadException
import com.cboda.agora.service.service.callback.AUIRoomBroadCallback
import com.cboda.agora.service.service.callback.AUIRoomListBroadCallback
import com.cboda.agora.service.service.http.BroadUtils
import com.cboda.agora.service.service.http.CommonBroadResp
import com.cboda.agora.service.service.http.HttpBroadManager
import com.cboda.agora.service.service.http.room.CreateRoomBroadReq
import com.cboda.agora.service.service.http.room.CreateRoomResp
import com.cboda.agora.service.service.http.room.DestroyRoomResp
import com.cboda.agora.service.service.http.room.QueryRoomReq
import com.cboda.agora.service.service.http.room.QueryRoomResp
import com.cboda.agora.service.service.http.room.RoomBroadInterface
import com.cboda.agora.service.service.http.room.RoomListReq
import com.cboda.agora.service.service.http.room.RoomListResp
import com.cboda.agora.service.service.http.room.RoomUserReq
import com.cboda.agora.service.service.http.room.UpdateRoomReq
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class AUIRoomBroadManager(
    private val appId: String = AUIRoomBroadContext.shared().mCommonConfig?.appId ?: "",
    private val sceneId: String
) {

    private val roomInterface by lazy {
        HttpBroadManager.getService(RoomBroadInterface::class.java)
    }

    fun createRoom(
        roomInfo: AUIRoomBroadInfo,
        callback: AUIRoomBroadCallback?
    ) {
        val roomId = roomInfo.roomId
        roomInterface.createRoom(
            CreateRoomBroadReq(
                appId,
                sceneId,
                roomId,
                roomInfo
            )
        ).enqueue(object : Callback<CommonBroadResp<CreateRoomResp>> {
                override fun onResponse(
                    call: Call<CommonBroadResp<CreateRoomResp>>,
                    response: Response<CommonBroadResp<CreateRoomResp>>
                ) {
                    val rsp = response.body()?.data
                    if (response.body()?.code == 0 && rsp != null) {
                        AUIRoomBroadContext.shared().insertRoomInfo(rsp.payload)
                        // success
                        callback?.onResult(null, rsp.payload)
                    } else {
                        callback?.onResult(BroadUtils.errorFromResponse(response), null)
                    }
                }

                override fun onFailure(call: Call<CommonBroadResp<CreateRoomResp>>, t: Throwable) {
                    callback?.onResult(AUIBroadException(-1, t.message), null)
                }
            })
    }

    fun destroyRoom(
        roomId: String,
        callback: AUIBroadCallback?
    ) {
        roomInterface.destroyRoom(RoomUserReq(appId, sceneId, roomId))
            .enqueue(object : Callback<CommonBroadResp<DestroyRoomResp>> {
                override fun onResponse(
                    call: Call<CommonBroadResp<DestroyRoomResp>>,
                    response: Response<CommonBroadResp<DestroyRoomResp>>
                ) {
                    if (response.code() == 200) {
                        // success
                        callback?.onResult(null)
                    } else {
                        callback?.onResult(BroadUtils.errorFromResponse(response))
                    }
                }

                override fun onFailure(call: Call<CommonBroadResp<DestroyRoomResp>>, t: Throwable) {
                    callback?.onResult(
                        AUIBroadException(
                            -1,
                            t.message
                        )
                    )
                }
            })
    }

    fun getRoomInfoList(
        lastCreateTime: Long?,
        pageSize: Int,
        callback: AUIRoomListBroadCallback?
    ) {
        roomInterface.fetchRoomList(RoomListReq(appId, sceneId, pageSize, lastCreateTime))
            .enqueue(object : Callback<CommonBroadResp<RoomListResp>> {
                override fun onResponse(
                    call: Call<CommonBroadResp<RoomListResp>>,
                    response: Response<CommonBroadResp<RoomListResp>>
                ) {
                    val roomList = response.body()?.data?.getRoomList()
                    if (roomList != null) {
                        AUIRoomBroadContext.shared().resetRoomMap(roomList)
                        callback?.onResult(null, roomList)
                    } else {
                        callback?.onResult(BroadUtils.errorFromResponse(response), null)
                    }
                }

                override fun onFailure(call: Call<CommonBroadResp<RoomListResp>>, t: Throwable) {
                    callback?.onResult(
                        AUIBroadException(
                            -1,
                            t.message
                        ), null
                    )
                }
            })
    }

    fun getRoomInfo(
        roomId: String,
        callback: AUIRoomBroadCallback?
    ) {
        roomInterface.queryRoomInfo(QueryRoomReq(appId, sceneId, roomId))
            .enqueue(object : Callback<CommonBroadResp<QueryRoomResp>> {
                override fun onResponse(
                    call: Call<CommonBroadResp<QueryRoomResp>>,
                    response: Response<CommonBroadResp<QueryRoomResp>>
                ) {
                    val rsp = response.body()?.data
                    if (response.body()?.code == 0 && rsp != null) {
                        AUIRoomBroadContext.shared().insertRoomInfo(rsp.payload)
                        // success
                        callback?.onResult(null, rsp.payload)
                    } else {
                        callback?.onResult(BroadUtils.errorFromResponse(response), null)
                    }
                }

                override fun onFailure(call: Call<CommonBroadResp<QueryRoomResp>>, t: Throwable) {
                    callback?.onResult(AUIBroadException(-1, t.message), null)
                }
            })
    }

    fun updateRoomInfo(
        roomInfo: AUIRoomBroadInfo,
        callback: AUIRoomBroadCallback?
    ) {
        roomInterface.updateRoomInfo(UpdateRoomReq(appId, sceneId, roomInfo.roomId, roomInfo))
            .enqueue(object : Callback<CommonBroadResp<String>> {
                override fun onResponse(
                    call: Call<CommonBroadResp<String>>,
                    response: Response<CommonBroadResp<String>>
                ) {
                    val rsp = response.body()?.data
                    if (response.body()?.code == 0 && rsp != null) {
                        // success
                        callback?.onResult(null, roomInfo)
                    } else {
                        callback?.onResult(BroadUtils.errorFromResponse(response), null)
                    }
                }

                override fun onFailure(call: Call<CommonBroadResp<String>>, t: Throwable) {
                    callback?.onResult(AUIBroadException(-1, t.message), null)
                }
            })
    }
}