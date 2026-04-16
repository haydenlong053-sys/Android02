package com.cboda.baselibrary.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class MsgBroadExtra(
    val grade: Double = 0.0,
    val joinGroupTime: String = "",
    val isMeetingImage: String = ""
) : Parcelable