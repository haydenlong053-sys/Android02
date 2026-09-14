package com.cboda.chatui.quickmeet

import com.cboda.baselibrary.utils.MMKVBroadUtils
import com.cboda.chatui.quickmeet.bean.QuickDetailMeetBean
import com.freddy.im.http.model.im.MeetingSessionVO

/** MeetingSessionVO ↔ QuickDetailMeetBean（保持 MeetPlugin / 创建页 UI 字段兼容）。 */
object LiveKitMeetMapper {

    @JvmStatic
    fun toQuickDetail(
        session: MeetingSessionVO,
        groupId: String?,
        fallbackTitle: String? = null,
    ): QuickDetailMeetBean {
        val bean = QuickDetailMeetBean()
        bean.sfuUrl = session.url
        bean.token = session.token
        bean.meetingId = session.meetingId
        bean.roomName = session.roomName
        bean.canPublish = session.isCanPublish
            || "host".equals(session.myRole, ignoreCase = true)
            || "speaker".equals(session.myRole, ignoreCase = true)
        bean.myRole = session.myRole
        bean.mode = session.mode
        bean.channelName = session.title ?: fallbackTitle
        bean.channelId = groupId
        bean.groupIdNo = groupId
        bean.userId = session.hostId ?: MMKVBroadUtils.getRealUserId()
        bean.idNo = session.hostId
        return bean
    }
}
