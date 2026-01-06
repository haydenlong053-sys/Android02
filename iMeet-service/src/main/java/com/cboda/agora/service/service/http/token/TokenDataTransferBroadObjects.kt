package com.cboda.agora.service.service.http.token

data class TokenGenerateBroadReq(
    val appId: String,
    val appCert: String,
    val channelName: String,
    val userId: String
)
data class TokenGenerateResp(
    val appId: String,
    val rtcToken: String,
    val rtmToken: String
)