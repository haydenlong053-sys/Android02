package com.cboda.intelligent.repository

import com.cboda.baselibrary.network.repository.NetworkBroadRepository
import com.cboda.baselibrary.network.result.NetworkBroadResult
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.cboda.chatui.quickmeet.UrlMeetConstants
import com.cboda.intelligent.utils.UrlBroadBroadConstants
import com.cboda.intelligent.model.CreateMeetingRequest
import com.cboda.intelligent.model.CreateMeetingResponse
import com.cboda.intelligent.model.MeetingConfig
import com.cboda.intelligent.model.PaymentPasswordStatusResponse
import com.cboda.intelligent.model.PaymentPasswordVerifyRequest
import com.cboda.intelligent.model.PaymentPasswordVerifyResponse
import com.cboda.intelligent.model.PriceQueryResponse
import com.cboda.intelligent.model.UserInfoBroadModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * 会议相关的Repository
 */
class QuickMeetingRepository {
    
    /**
     * 获取会议配置信息
     */
    suspend fun getMeetingConfig(): NetworkBroadResult<MeetingConfig> = withContext(Dispatchers.IO) {
          NetworkBroadRepository.postEncrypted2(UrlMeetConstants.QUICK_LIVE_CONFIG, MeetingConfig::class.java,hashMapOf<String, Any>())
    }

    
    /**
     * 检查用户是否设置了支付密码
     */
    suspend fun checkPaymentPasswordStatus(): Result<PaymentPasswordStatusResponse> {
        val params = hashMapOf("code" to "")

        return withContext(Dispatchers.IO) {
            try {
                var result: Result<PaymentPasswordStatusResponse>? = null

                NetworkBroadRepository.postEncrypted<UserInfoBroadModel>(
                    UrlBroadBroadConstants.GET_USER_INFO,
                    UserInfoBroadModel::class.java,
                    params
                ).onSuccess { userInfo ->
                    result = if (userInfo == null || userInfo.hasPaypwd == "0") {
                        Result.success(
                            PaymentPasswordStatusResponse(
                                hasPassword = false,
                                message = "未设置支付密码"
                            )
                        )
                    } else {
                        Result.success(
                            PaymentPasswordStatusResponse(
                                hasPassword = true,
                                message = "已设置支付密码"
                            )
                        )
                    }
                }.onFailure { _, _ ->
                    result = Result.success(
                        PaymentPasswordStatusResponse(
                            hasPassword = false,
                            message = "未设置支付密码"
                        )
                    )
                }.onError {
                    result = Result.success(
                        PaymentPasswordStatusResponse(
                            hasPassword = false,
                            message = "未设置支付密码"
                        )
                    )
                }

                result ?: Result.failure(Exception("未知错误"))
            } catch (e: Exception) {
                Result.failure(e)
            }
        }
    }


    /**
     * 验证支付密码
     */
    suspend fun verifyPaymentPassword(request: PaymentPasswordVerifyRequest): Result<PaymentPasswordVerifyResponse> {
        return  withContext(Dispatchers.IO){
           try {
               var result: Result<PaymentPasswordVerifyResponse>? = null
               NetworkBroadRepository.postEncrypted(UrlBroadBroadConstants.CHECK_PAY_PASSWORD, Any::class.java, request)
                   .onSuccess {
                       result=Result.success(PaymentPasswordVerifyResponse(true,"验证成功"))
                   }
                   .onFailure { code, message, data ->
                       result=Result.success(PaymentPasswordVerifyResponse(false,message))
                   }
                   .onError {
                       result=Result.success(PaymentPasswordVerifyResponse(false,"未知错误"))
                   }
               result?:Result.failure(Exception("未知错误"))
           }catch (e:Exception){
               Result.failure(e)
           }
        }
//        return try {
//            delay(800) // 模拟网络延迟
//            // 模拟验证逻辑：密码为"123456"时验证成功
//            val isCorrect = request.pwd == "123456"
//            Result.success(
//                PaymentPasswordVerifyResponse(
//                    success = isCorrect,
//                    message = if (isCorrect) "密码验证成功" else "支付密码错误，请重新输入"
//                )
//            )
//        } catch (e: Exception) {
//            Result.failure(e)
//        }
    }

    /**
     * 查询会议价格
     */
    suspend fun queryMeetingPrice(durationConfigId: Int,userTierConfigId: Int): NetworkBroadResult<PriceQueryResponse> = withContext(Dispatchers.IO) {
        val params = hashMapOf("durationConfigId" to durationConfigId,"userTierConfigId" to userTierConfigId)
        NetworkBroadRepository.postEncrypted2(UrlMeetConstants.QUICK_LIVE_CALCULATE, PriceQueryResponse::class.java, params)

    }

    /**
     * 创建会议
     */
    suspend fun createMeeting(request: CreateMeetingRequest): NetworkBroadResult<CreateMeetingResponse> {
        return NetworkBroadRepository.post(UrlBroadBroadConstants.PRE_CREATE_MEETING, CreateMeetingResponse::class.java, request)
    }

}
