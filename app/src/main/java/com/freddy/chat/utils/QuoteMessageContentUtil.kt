package com.freddy.chat.utils

import android.text.TextUtils
import com.freddy.im.bean.AppMessage
import com.freddy.im.message.MessageContentFormatter
import com.google.gson.JsonObject
import com.google.gson.JsonParser

/**
 * 文本消息引用（仿微信 / 对齐野火 `QuoteInfo`）。
 * 
 * 
 * 写入消息 `customInfo`（可与 @ 并存）：
 * `{"info":[...],"quote":{"msgId":"...","clientMsgId":"...","userId":"...","displayName":"...","digest":"...","contentType":100,"thumbUrl":"...","mediaUrl":"...","fileName":"..."}}`
 * 
 * 
 * content 仅保留正文：`{"msg":"..."}`。
 */
object QuoteMessageContentUtil {
    const val KEY_QUOTE: String = "quote"
    private const val KEY_MSG_ID = "msgId"
    private const val KEY_CLIENT_MSG_ID = "clientMsgId"
    private const val KEY_USER_ID = "userId"
    private const val KEY_DISPLAY_NAME = "displayName"
    private const val KEY_DIGEST = "digest"
    private const val KEY_CONTENT_TYPE = "contentType"
    private const val KEY_THUMB_URL = "thumbUrl"
    private const val KEY_MEDIA_URL = "mediaUrl"
    private const val KEY_FILE_NAME = "fileName"
    private const val KEY_EXTENDED = "extended"
    private const val DIGEST_MAX_LEN = 48

    /**
     * 优先从 `customInfo.quote` 解析；兼容旧数据从 `content.quote` 解析。
     */
    @JvmStatic
    fun parseFromMessage(message: AppMessage?): QuoteInfo? {
        if (message == null) {
            return null
        }
        val fromCustom = parse(message.customInfo)
        if (fromCustom != null) {
            return fromCustom
        }
        return parse(message.content)
    }

    /**
     * 从 JSON（customInfo 或旧版 content）中解析 `quote` 对象。
     */
    fun parse(json: String?): QuoteInfo? {
        val root = parseObject(json)
        if (root == null || !root.has(KEY_QUOTE) || !root.get(KEY_QUOTE).isJsonObject) {
            return null
        }
        return parseQuoteObject(root.getAsJsonObject(KEY_QUOTE))
    }

    /**
     * 将引用写入 customInfo：保留已有 `info`（@ 等），覆盖/写入 `quote`。
     * 
     * @return 合并后的 customInfo JSON；无引用且无原 customInfo 时返回 null
     */
    @JvmStatic
    fun mergeIntoCustomInfo(
        existingCustomInfo: String?,
        quoteInfo: QuoteInfo?
    ): String? {
        if (quoteInfo == null) {
            return if (TextUtils.isEmpty(existingCustomInfo)) null else existingCustomInfo
        }
        var root = parseObject(existingCustomInfo)
        if (root == null) {
            root = JsonObject()
        }
        root.add(KEY_QUOTE, toJsonObject(quoteInfo))
        return root.toString()
    }

    private fun parseQuoteObject(q: JsonObject): QuoteInfo? {
        val info = QuoteInfo()
        info.msgId = readString(q, KEY_MSG_ID)
        info.clientMsgId = readString(q, KEY_CLIENT_MSG_ID)
        info.userId = readString(q, KEY_USER_ID)
        info.displayName = readString(q, KEY_DISPLAY_NAME)
        info.digest = readString(q, KEY_DIGEST)
        info.thumbUrl = readString(q, KEY_THUMB_URL)
        info.mediaUrl = readString(q, KEY_MEDIA_URL)
        info.fileName = readString(q, KEY_FILE_NAME)
        info.extended = readString(q, KEY_EXTENDED)
        if (q.has(KEY_CONTENT_TYPE) && q.get(KEY_CONTENT_TYPE).isJsonPrimitive()) {
            try {
                info.contentType = q.get(KEY_CONTENT_TYPE).getAsInt()
            } catch (ignored: Exception) {
                info.contentType = 0
            }
        }
        if (TextUtils.isEmpty(info.resolveAnchorId())
            && TextUtils.isEmpty(info.digest)
            && TextUtils.isEmpty(info.thumbUrl)
            && TextUtils.isEmpty(info.mediaUrl)
        ) {
            return null
        }
        return info
    }

    @JvmStatic
    fun fromMessage(
        message: AppMessage,
        currentUserId: String?,
        nameMap: MutableMap<String?, String?>?
    ): QuoteInfo {
        val info = QuoteInfo()
        info.msgId = message.msgId
        info.clientMsgId = message.clientMsgId
        info.userId = message.fromId
        info.displayName = WeChatUiHelper.resolveUserDisplayName(info.userId, currentUserId, nameMap)
        var digest = MessageContentFormatter.formatDisplayText(message, currentUserId, nameMap)
        if (digest.length > DIGEST_MAX_LEN) {
            digest = digest.substring(0, DIGEST_MAX_LEN)
        }
        info.digest = digest
        info.contentType = message.msgContentType
        info.extended = message.extend
        fillMediaFields(info, message, currentUserId)
        return info
    }

    private fun fillMediaFields(
        info: QuoteInfo,
        message: AppMessage,
        currentUserId: String?
    ) {
        val content = message.getContent()
        val sentByMe = message.isSender(currentUserId)
        if (info.isImageQuote) {
            val remote = MediaMessageContentUtil.resolveRemoteUrl(content)
            val display = MediaMessageContentUtil.resolveDisplayUrl(content, sentByMe)
            val thumb = MediaMessageContentUtil.resolveDisplayThumbUrl(content, sentByMe)
            info.mediaUrl = firstNonEmpty(remote, display)
            info.thumbUrl = firstNonEmpty(thumb, info.mediaUrl)
            return
        }
        if (info.isVideoQuote) {
            val remote = MediaMessageContentUtil.resolveRemoteUrl(content)
            val play = MediaMessageContentUtil.resolveVideoPlayUrl(content, sentByMe)
            info.mediaUrl = firstNonEmpty(remote, play)
            val cover = MediaMessageContentUtil.resolveCoverDisplayUrl(content, sentByMe)
            info.thumbUrl = cover
            return
        }
        if (info.isFileQuote) {
            val remote = MediaMessageContentUtil.resolveRemoteUrl(content)
            val display = MediaMessageContentUtil.resolveDisplayUrl(content, sentByMe)
            info.mediaUrl = firstNonEmpty(remote, display)
            info.fileName = MediaMessageContentUtil.resolveFileName(content)
        }
        if (info.isVoiceQuote()) {
            val remote = MediaMessageContentUtil.resolveRemoteUrl(content)
            val display = MediaMessageContentUtil.resolveDisplayUrl(content, sentByMe)
            info.mediaUrl = firstNonEmpty(remote, display)
            info.fileName = MediaMessageContentUtil.resolveVoiceDurationSec(content).toString()
        }
    }

    private fun firstNonEmpty(a: String?, b: String?): String {
        if (!TextUtils.isEmpty(a)) {
            return a!!.trim { it <= ' ' }
        }
        if (!TextUtils.isEmpty(b)) {
            return b!!.trim { it <= ' ' }
        }
        return ""
    }

    /**
     * 展示时按「当前用户视角」解析名称：本地备注/群昵称优先，不使用对方写入的 displayName。
     */
    @JvmStatic
    fun resolveDisplayNameForPreview(
        quote: QuoteInfo?,
        currentUserId: String?,
        nameMap: MutableMap<String?, String?>?
    ): String {
        if (quote == null) {
            return ""
        }
        val local = WeChatUiHelper.resolveUserDisplayName(quote.userId, currentUserId, nameMap)
        if (!TextUtils.isEmpty(local)
            && (TextUtils.isEmpty(quote.userId) || !TextUtils.equals(local, quote.userId))
        ) {
            return local
        }
        // 本地解析失败时再退回消息里携带的名称
        if (!TextUtils.isEmpty(quote.displayName)
            && !TextUtils.equals(quote.displayName, quote.userId)
        ) {
            return quote.displayName!!.trim { it <= ' ' }
        }
        return if (TextUtils.isEmpty(local)) "" else local
    }

    fun formatPreviewLine(
        quote: QuoteInfo?,
        currentUserId: String?,
        nameMap: MutableMap<String?, String?>?
    ): String {
        if (quote == null) {
            return ""
        }
        val name = resolveDisplayNameForPreview(quote, currentUserId, nameMap)
        val dig: String = (if (quote.digest == null) "" else quote.digest)!!
        if (TextUtils.isEmpty(name)) {
            return dig
        }
        return "$name: $dig"
    }

    fun toJsonObject(info: QuoteInfo): JsonObject {
        val q = JsonObject()
        if (!TextUtils.isEmpty(info.msgId)) {
            q.addProperty(KEY_MSG_ID, info.msgId)
        }
        if (!TextUtils.isEmpty(info.clientMsgId)) {
            q.addProperty(KEY_CLIENT_MSG_ID, info.clientMsgId)
        }
        if (!TextUtils.isEmpty(info.userId)) {
            q.addProperty(KEY_USER_ID, info.userId)
        }
        if (!TextUtils.isEmpty(info.displayName)) {
            q.addProperty(KEY_DISPLAY_NAME, info.displayName)
        }
        if (!TextUtils.isEmpty(info.digest)) {
            q.addProperty(KEY_DIGEST, info.digest)
        }
        if (info.contentType > 0) {
            q.addProperty(KEY_CONTENT_TYPE, info.contentType)
        }
        if (!TextUtils.isEmpty(info.thumbUrl)) {
            q.addProperty(KEY_THUMB_URL, info.thumbUrl)
        }
        if (!TextUtils.isEmpty(info.mediaUrl)) {
            q.addProperty(KEY_MEDIA_URL, info.mediaUrl)
        }
        if (!TextUtils.isEmpty(info.fileName)) {
            q.addProperty(KEY_FILE_NAME, info.fileName)
        }
        if (!TextUtils.isEmpty(info.extended)) {
            q.addProperty(KEY_EXTENDED, info.extended)
        }
        return q
    }

    private fun parseObject(raw: String?): JsonObject? {
        if (TextUtils.isEmpty(raw)) {
            return null
        }
        try {
            return JsonParser.parseString(raw!!.trim { it <= ' ' }).getAsJsonObject()
        } catch (ignored: Exception) {
            return null
        }
    }

    private fun readString(obj: JsonObject, key: String): String {
        if (!obj.has(key) || !obj.get(key).isJsonPrimitive) {
            return ""
        }
        val v = obj.get(key).asString
        return v?.trim { it <= ' ' } ?: ""
    }

    class QuoteInfo {
        var msgId: String? = null //被引用消息的服务端消息 ID，用于定位原消息

        @JvmField
        var clientMsgId: String? = null //被引用消息的客户端消息 ID，服务端 ID 缺失时的定位兜底

        @JvmField
        var userId: String? = null //被引用消息发送者的用户 ID

        @JvmField
        var displayName: String? = null //被引用消息发送者展示名（发送时写入，展示时优先用本地备注/群昵称覆盖）。

        @JvmField
        var digest: String? = null //被引用内容摘要文案（文本截断后内容，或 [图片]/[视频] 等占位描述）
        var contentType: Int = 0 //被引用消息的内容类型（见 {@link com.freddy.im.MessageType.MessageContentType}）

        @JvmField
        var thumbUrl: String? = null // 引用预览缩略图地址：图片用 thumb，视频用封面；文件为空时改用 fileName 解析图标

        @JvmField
        var mediaUrl: String? = null //可直接打开的媒体地址：图片原图 / 视频播放地址 / 文件下载地址（优先远端 URL）

        @JvmField
        var fileName: String? = null //被引用文件的原始文件名，用于展示与匹配文件类型图标
        var extended: String? = null //拓展字段，暂未使用

        fun formatPreviewLine(): String {
            val name = if (TextUtils.isEmpty(displayName)) (if (userId == null) "" else userId) else displayName
            val dig: String = (if (digest == null) "" else digest)!!
            if (TextUtils.isEmpty(name)) {
                return dig
            }
            return "$name: $dig"
        }

        /** 优先服务端 msgId，其次 clientMsgId，用于列表定位。  */
        fun resolveAnchorId(): String? {
            if (!TextUtils.isEmpty(msgId)) {
                return msgId
            }
            if (!TextUtils.isEmpty(clientMsgId)) {
                return clientMsgId
            }
            return null
        }

        val isImageQuote: Boolean
            get() = MessageContentFormatter.isImageMessage(contentType)

        val isVideoQuote: Boolean
            get() = MessageContentFormatter.isVideoMessage(contentType)

        val isFileQuote: Boolean
            get() = MessageContentFormatter.isFileMessage(contentType)

        fun isVoiceQuote(): Boolean {
            return MessageContentFormatter.isVoiceMessage(contentType)
        }

        val isMediaQuote: Boolean
            get() = this.isImageQuote || this.isVideoQuote || this.isFileQuote

        fun hasThumbPreview(): Boolean {
            return this.isMediaQuote
                    && (!TextUtils.isEmpty(thumbUrl) || !TextUtils.isEmpty(mediaUrl) || this.isFileQuote)
        }
    }
}
