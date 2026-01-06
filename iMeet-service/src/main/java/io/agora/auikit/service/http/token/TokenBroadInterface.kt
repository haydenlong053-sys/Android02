package io.agora.auikit.service.http.token

import io.agora.auikit.service.http.CommonBroadResp
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

interface TokenBroadInterface {

    @POST("token006/generate")
    fun tokenGenerate006(@Body req: TokenGenerateBroadReq): Call<CommonBroadResp<TokenGenerateResp>>

    @POST("token/generate")
    fun tokenGenerate(@Body req: TokenGenerateBroadReq): Call<CommonBroadResp<TokenGenerateResp>>

}