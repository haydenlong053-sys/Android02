package com.lxkj.aiim.bean

import com.cboda.baselibrary.bean.BaseResponse
import com.google.gson.annotations.SerializedName

class AddMeetingTimeBean : BaseResponse(){
    @SerializedName("data")
    val `data`: AddMeetingTimeData? =null
}

data class AddMeetingTimeData(
    @SerializedName("createout")
    val `createout`: Int? = -1,
    @SerializedName("meeting")
    val `meeting`: MeetingData? = null,
)




