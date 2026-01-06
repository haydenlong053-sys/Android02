package io.agora.auikit.service.http.room

import io.agora.auikit.model.AUIRoomBroadInfo
import io.agora.auikit.service.http.PayloadResp

data class CreateRoomBroadReq(
    val appId: String,
    val sceneId: String,
    val roomId: String,
    val payload: AUIRoomBroadInfo
)
data class CreateRoomResp(
    val roomId: String,
    val payload: AUIRoomBroadInfo,
    val createTime: Long,
    val updateTime: Long
)
data class RoomUserReq(
    val appId: String,
    val sceneId: String,
    val roomId: String
)
data class DestroyRoomResp(
    val roomId: String
)
data class RoomListReq(
    val appId: String,
    val sceneId: String,
    val pageSize: Int,
    val lastCreateTime: Long?
)
data class QueryRoomResp(
    val appId: String,
    val sceneId: String,
    val roomId: String,
    val payload: AUIRoomBroadInfo,
    val createTime: Long,
    val updateTime: Long
)
data class UpdateRoomReq(
    val appId: String,
    val sceneId: String,
    val roomId: String,
    val payload: AUIRoomBroadInfo
)
data class QueryRoomReq(
    val appId: String,
    val sceneId: String,
    val roomId: String
)
data class RoomListResp(
    val pageSize: Int,
    val count: Int,
    val list: List<PayloadResp<AUIRoomBroadInfo>>
){
    fun getRoomList(): List<AUIRoomBroadInfo>{
        val list = mutableListOf<AUIRoomBroadInfo>()
        this.list.forEach {
            list.add(AUIRoomBroadInfo().apply {
                roomId = it.roomId
                roomName = it.payload?.roomName ?: ""
                memberCount = it.payload?.memberCount ?: 0
                owner = it.payload?.owner
                thumbnail = it.payload?.thumbnail ?: ""
                createTime = it.createTime
            })
        }
        return list
    }
}
