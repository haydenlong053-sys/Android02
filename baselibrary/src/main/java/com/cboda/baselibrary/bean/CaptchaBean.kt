package com.cboda.baselibrary.bean

/**
 * 图形验证码（{@code im/user/captcha}）响应。
 * [imageBase64] 可能带 `data:image/png;base64,` 前缀。
 */
data class CaptchaBean(
    var success: Boolean = false,
    var captchaId: String? = null,
    var imageBase64: String? = null,
)
