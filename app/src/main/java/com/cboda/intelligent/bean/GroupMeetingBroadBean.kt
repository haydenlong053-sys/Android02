package com.cboda.intelligent.bean
import com.cboda.baselibrary.bean.BaseBroadResponse
import com.google.gson.annotations.SerializedName
 class GroupMeetingBroadBean(

    @SerializedName("data")
    val `data`: GroupMeetingData? = GroupMeetingData(),
): BaseBroadResponse()

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





