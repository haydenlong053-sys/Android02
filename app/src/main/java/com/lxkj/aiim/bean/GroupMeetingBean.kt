package com.lxkj.aiim.bean
import com.cboda.baselibrary.bean.BaseResponse
import com.google.gson.annotations.SerializedName
 class GroupMeetingBean(

    @SerializedName("data")
    val `data`: GroupMeetingData? = GroupMeetingData(),
): BaseResponse()

data class GroupMeetingData(
    @SerializedName("activeMeetingCount")
    val activeMeetingCount: Int? = 0,
    @SerializedName("activeMeetings")
    val activeMeetings: List<RoomData?>? = listOf(),
    @SerializedName("groupId")
    val groupId: String? = "",
    @SerializedName("hasActiveMeeting")
    val hasActiveMeeting: Boolean? = false
)





