package com.cboda.intelligent.utils

import android.content.ClipboardManager
import android.content.Context

object ClipboardPasteBroadUtils {

    /**
     * 从剪贴板获取文本内容
     */
    fun getTextFromClipboard(context: Context): String? {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            if (!clipboard.hasPrimaryClip()) return null

            val clipData = clipboard.primaryClip ?: return null
            if (clipData.itemCount == 0) return null

            val item = clipData.getItemAt(0)
            item.text?.toString()
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    /**
     * 检查剪贴板是否有文本内容
     */
    fun hasTextContent(context: Context): Boolean {
        return try {
            val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            if (!clipboard.hasPrimaryClip()) return false

            val clipData = clipboard.primaryClip ?: return false
            if (clipData.itemCount == 0) return false

            val item = clipData.getItemAt(0)
            item.text != null
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }
}