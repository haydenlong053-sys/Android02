package com.cboda.intelligent.uistate

import com.cboda.baselibrary.bean.UserInfoBroadModel

/**
 * 镜像页面的ui状态管理
 */
data class MirrorPageUiBroadState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val userInfo: UserInfoBroadModel?=null,
    val isSign: Boolean =false

    )
