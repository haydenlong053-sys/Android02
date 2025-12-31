package com.lxkj.aiim.bean
import com.cboda.baselibrary.bean.BaseResponse
import com.google.gson.annotations.SerializedName
class MeetingDetailBean(
    @SerializedName("data")
    val `data`: MeetingData? = MeetingData(),
): BaseResponse()
data class MeetingData(
    @SerializedName("allMic")
    val allMic: Boolean? = false,
    @SerializedName("allMute")
    val allMute: Boolean? = false,
    @SerializedName("createTime")
    val createTime: Long? = 0,
    @SerializedName("currentSize")
    val currentSize: Int? = 0,
    @SerializedName("currentUsers")
    val currentUsers: Int? = 0,
    @SerializedName("endTime")
    val endTime: Long? = 0,
    @SerializedName("groupId")
    val groupId: String? = "",
    @SerializedName("id")
    val id: String? = "",
    @SerializedName("idNo")
    val idNo: String? = "",
    @SerializedName("isNonPayPwd")
    val isNonPayPwd: Boolean? = false,
    @SerializedName("meetingId")
    val meetingId: String? = "",
    @SerializedName("meetingMaxUsers")
    val meetingMaxUsers: Int? = 0,
    @SerializedName("meetingStatus")
    val meetingStatus: String? = "",
    @SerializedName("meetingTime")
    val meetingTime: Int? = 0,
    @SerializedName("ownerId")
    val ownerId: String? = "",
    @SerializedName("remainingTime")
    val remainingTime: Int? = 0,
    @SerializedName("roomId")
    val roomId: String? = "",
    @SerializedName("roomName")
    val roomName: String? = "",
    @SerializedName("startTime")
    val startTime: Long? = 0,
    @SerializedName("status")
    val status: String? = "",
    @SerializedName("timeZone")
    val timeZone: String? = "",
    @SerializedName("meetingTimeName")
    val meetingTimeName: String? = "",
    @SerializedName("minMeetingTimeName")
    val minMeetingTimeName: String? = "",
    @SerializedName("minMeetingTime")
    val minMeetingTime:  Int? = 0,

    @SerializedName("updateTime")
    val updateTime: Long? = 0
)






