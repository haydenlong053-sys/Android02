package com.lxkj.aiim.repository

import com.cboda.baselibrary.network.NetworkUtils
import com.cboda.baselibrary.network.config.NetworkConfig
import com.cboda.baselibrary.network.repository.NetworkRepository
import com.cboda.baselibrary.network.result.NetworkResult
import com.cboda.baselibrary.network.result.onError
import com.cboda.baselibrary.network.result.onFailure
import com.cboda.baselibrary.network.result.onSuccess
import com.lxkj.aiim.lj.http.UrlConstants
import com.lxkj.aiim.model.CreateMeetingRequest
import com.lxkj.aiim.model.CreateMeetingResponse
import com.lxkj.aiim.model.MeetingConfig
import com.lxkj.aiim.model.PaymentPasswordStatusResponse
import com.lxkj.aiim.model.PaymentPasswordVerifyRequest
import com.lxkj.aiim.model.PaymentPasswordVerifyResponse
import com.lxkj.aiim.model.PriceQueryRequest
import com.lxkj.aiim.model.PriceQueryResponse
import com.lxkj.aiim.model.UserInfoModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext

/**
 * 会议相关的Repository
 */
class MeetingRepository {
    
    /**
     * 获取会议配置信息
     */
    suspend fun getMeetingConfig(): NetworkResult<MeetingConfig> = withContext(Dispatchers.IO) {
          NetworkRepository.get(UrlConstants.GET_MEETING_CONFIG, MeetingConfig::class.java)
    }
    
    /**
     * 检查用户是否设置了支付密码
     */
    suspend fun checkPaymentPasswordStatus(): Result<PaymentPasswordStatusResponse> {
        val params = hashMapOf("code" to "")

        return withContext(Dispatchers.IO) {
            try {
                var result: Result<PaymentPasswordStatusResponse>? = null

                NetworkRepository.postEncrypted<UserInfoModel>(
                    UrlConstants.GET_USER_INFO,
                    UserInfoModel::class.java,
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
               NetworkRepository.postEncrypted(UrlConstants.CHECK_PAY_PASSWORD, Any::class.java, request)
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
    suspend fun queryMeetingPrice(request: PriceQueryRequest): NetworkResult<PriceQueryResponse> = withContext(Dispatchers.IO) {

        NetworkRepository.post(UrlConstants.CALCULATE_MEETING_PRICE, PriceQueryResponse::class.java, request)

    }

    /**
     * 创建会议
     */
    suspend fun createMeeting(request: CreateMeetingRequest): NetworkResult<CreateMeetingResponse> {
        return NetworkRepository.post(UrlConstants.PRE_CREATE_MEETING, CreateMeetingResponse::class.java, request)
    }

}

/**
 * 会议API接口（示例，实际项目中应该使用Retrofit等网络库）
 */
interface MeetingApiService {
    
    /**
     * 获取会议配置
     */
    suspend fun getMeetingConfig(): MeetingConfig
    
    /**
     * 创建会议
     */
    suspend fun createMeeting(request: CreateMeetingRequest): String
}