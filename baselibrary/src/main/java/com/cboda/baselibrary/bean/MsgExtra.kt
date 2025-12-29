package com.cboda.baselibrary.bean

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class MsgExtra(
    val grade: Int = 0,
    val joinGroupTime: String = ""
) : Parcelable