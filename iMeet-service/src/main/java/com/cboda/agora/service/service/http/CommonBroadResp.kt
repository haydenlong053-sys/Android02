package com.cboda.agora.service.service.http

data class CommonBroadResp<Data>(
    val code: Int = 0,
    val message: String?,
    val data: Data?
)

data class PayloadResp<Payload>(
    val roomId: String,
    val createTime: Long,
    val updateTime: Long,
    val payload: Payload?
)