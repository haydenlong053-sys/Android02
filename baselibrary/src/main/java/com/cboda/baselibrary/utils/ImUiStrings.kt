package com.cboda.baselibrary.utils

import android.content.Context
import androidx.annotation.StringRes
import com.cboda.select.picture.utils.LanguageUtil

/**
 * IM / 通话相关 UI 文案：按当前 App 语言从 baselibrary strings 取值。
 * 会话列表摘要、消息气泡、语音通话页统一走此入口。
 */
object ImUiStrings {

    @JvmStatic
    fun get(@StringRes resId: Int): String {
        return try {
            requireContext().getString(resId)
        } catch (_: Exception) {
            ""
        }
    }

    @JvmStatic
    fun get(@StringRes resId: Int, vararg args: Any?): String {
        return try {
            requireContext().getString(resId, *args)
        } catch (_: Exception) {
            ""
        }
    }

    private fun requireContext(): Context {
        val app = getAppContext(null)
        return LanguageUtil.getLanguageContext(app, false) ?: app
            ?: throw IllegalStateException("App context not ready")
    }
}
