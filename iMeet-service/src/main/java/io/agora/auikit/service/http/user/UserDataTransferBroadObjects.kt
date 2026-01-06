package io.agora.auikit.service.http.user

data class UserKickOutBroadReq(
    val appId: String,
    val basicAuth: String = "",
    val roomId: String,
    val uid: Long
)

data class UserKickOutResp(
    val uid: Long
)