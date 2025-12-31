package com.lxkj.aiim.bean

import com.cboda.baselibrary.bean.BaseResponse
import com.google.gson.annotations.SerializedName

 class CreateRoomBean(
    @SerializedName("data")
    val `data`: RoomData? = RoomData(),

): BaseResponse()

data class RoomData(
    @SerializedName("appId")
    val appId: String? = "",
    @SerializedName("chatRoomConfig")
    val chatRoomConfig: ChatRoomConfig? = ChatRoomConfig(),
    @SerializedName("createTime")
    val createTime: Long? = 0,
    @SerializedName("groupId")
    val groupId: String? = "",
    @SerializedName("ownerId")
    val ownerId: String? = "",
    @SerializedName("payload")
    val payload: Payload? = Payload(),
    @SerializedName("roomId")
    val roomId: String? = "",
    @SerializedName("sceneId")
    val sceneId: String? = "",
    @SerializedName("status")
    val status: String? = "",
    @SerializedName("updateTime")
    val updateTime: Long? = 0
)

data class ChatRoomConfig(
    @SerializedName("maxUsers")
    val maxUsers: Int? = 0,
    @SerializedName("name")
    val name: String? = ""
)

data class Payload(
    @SerializedName("allMic")
    val allMic: Boolean? = true,
    @SerializedName("allMute")
    val allMute: Boolean? = false,
    @SerializedName("chatRoomConfig")
    val chatRoomConfig: ChatRoomConfig? = ChatRoomConfig(),
    @SerializedName("description")
    val description: String? = "",
    @SerializedName("maxUsers")
    val maxUsers: Int? = 0,
    @SerializedName("roomName")
    val roomName: String? = "",
    @SerializedName("type")
    val type: Int? = 0,
    @SerializedName("user")
    val user: User? = User()
)

data class User(
    @SerializedName("username")
    val username: String? = ""
)


