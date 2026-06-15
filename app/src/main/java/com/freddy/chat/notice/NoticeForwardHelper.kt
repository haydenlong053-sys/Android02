package com.freddy.chat.notice

import android.content.Context
import com.cboda.intelligent.model.NoticeInfoBroadBean
import com.freddy.chat.MessageForwardActivity
import com.freddy.chat.im.MessageBuilder

/**
 * 系统公告分享/转发（自研 [MessageForwardActivity]）。
 */
object NoticeForwardHelper {

    @JvmStatic
    fun startForward(context: Context, notice: NoticeInfoBroadBean) {
        val message = MessageBuilder.buildForwardableNoticeMessage(
            notice.id,
            notice.contentUrl,
            notice.createTime,
            notice.noticeTitle,
            notice.previewContent,
            if (notice.status) 1 else 0,
        )
        context.startActivity(MessageForwardActivity.buildInlineForwardIntent(context, message))
    }
}
