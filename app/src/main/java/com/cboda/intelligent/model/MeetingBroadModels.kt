package com.cboda.intelligent.model

import com.cboda.intelligent.bean.MeetingData

/**
 * 用户人数选项
 */
data class UserTierBroadOption(
    val name: String,
    val value: Int
)

/**
 * 时间选项
 */
data class TimeOption(
    val id:Int,
    val name: String,
    val value: Int // 分钟数
)

/**
 * 会议配置数据
 */
data class MeetingConfig(
    val allMic: Boolean,                    // 是否全禁麦
    val allMute: Boolean,                   // 是否全禁言
//    val stepConsumptionToken: Int,          // 每30分钟消耗的token
    val timeZone: String,                   // 时区
    val userTierOptions: List<UserTierBroadOption>, // 用户人数选项
    val timeOline: List<TimeOption>,        // 时间选项
    val isNonPayPwd: Boolean,               // 是否支持续时免密支付
    val renewalRules: String                // 续费规则说明文本
)

/**
 * 支付密码状态检查响应
 */
data class PaymentPasswordStatusResponse(
    val hasPassword: Boolean,               // 是否已设置支付密码
    val message: String? = null             // 提示信息
)

/**
 * 支付密码验证请求
 */
data class PaymentPasswordVerifyRequest(
    val pwd: String                    // 6位数字密码
)

/**
 * 支付密码验证响应
 */
data class PaymentPasswordVerifyResponse(
    val success: Boolean,                   // 验证是否成功
    val message: String? = null             // 错误信息或成功信息
)

/**
 * 价格查询请求
 */
data class PriceQueryRequest(
    val ownerId: String,                    // 用户ID
    val meetingTime: String,                // 会议时长（分钟）
    val meetingMaxUsers: String             // 最大用户数
)

/**
 * 价格查询响应
 */
data class PriceQueryResponse(
    val discountMeetingTokens: String,      // 折扣后价格
    val meetingTokens: String,              // 原价
    val availableTokens: String             // 可用代币
)

/**
 * 会议创建请求数据
 */
data class CreateMeetingRequest(
    val roomName: String,                   // 会议室名称
    val startTime: String,                  // 开始时间
    val ownerId: String,                  // 房主id
    val groupId: String,                  // 群id
    val meetingTime: Int,                      // 会议时长（分钟）
    val meetingMaxUsers: Int,               // 最大参与人数
    val allMic: Boolean,                    // 是否全禁麦
    val allMute: Boolean,                   // 是否全禁言
    val isNonPayPwd: Boolean                // 是否续时免密支付
)

data class CreateMeetingResponse(
    val createout:Int?=-1,
    val meeting:MeetingData?=null,
)

/**
 * UI状态数据
 */
data class MeetingCreateUiState(
    val isLoading: Boolean = false,
    val config: MeetingConfig? = null,
    val roomName: String = "",
    val groupId: String = "",
    val selectedDate: String = "",
    val selectedTime: String = "",
    val selectedDuration: TimeOption? = null,
    val selectedUserTier: UserTierBroadOption? = null,
    val allMic: Boolean = true,
    val allMute: Boolean = false,
    val isNonPayPwd: Boolean = false,
    val error: String? = null,
    // 支付密码相关状态
    val showPaymentPasswordDialog: Boolean = false,
    val showSetPasswordDialog: Boolean = false,
    val paymentPasswordLoading: Boolean = false,
    val paymentPasswordError: String? = null,
    val hasPaymentPassword: Boolean? = null,
    // 创建会议错误弹窗状态
    val showInsufficientTokenDialog: Boolean = false,
    val showCreateFailedDialog: Boolean = false,
    val createFailedMessage: String? = null,
    // 验证失败弹窗相关
    val showValidationFailedDialog: Boolean = false,
    val validationFailedMessage: String? = null,
    
    // 价格相关
    val currentPrice: Double = 0.0,         // 当前价格
    val originalPrice: Double = 0.0,        // 原价
    val availableTokens: Double = 0.0,      // 可用代币
    val isPriceLoading: Boolean = false     // 价格加载状态
) {
    // 计算总价格 - 使用实时获取的价格
    fun calculateTotalPrice(): Int {
        return currentPrice.toInt()
    }
    
    // 计算原价（如果有折扣的话）
    fun calculateOriginalPrice(): Int? {
        return if (originalPrice > currentPrice && originalPrice > 0) {
            originalPrice.toInt()
        } else {
            null
        }
    }
    
    // 检查是否可以创建会议
    fun canCreateMeeting(): Boolean {
        return roomName.isNotBlank() && 
               selectedDate.isNotBlank() && 
               selectedTime.isNotBlank() && 
               selectedDuration != null && 
               selectedUserTier != null &&
               !isLoading
    }
}