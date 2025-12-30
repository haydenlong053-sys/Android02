package com.lxkj.aiim.uistate

import com.lxkj.aiim.model.UserInfoModel

/**
 * 镜像页面的ui状态管理
 */
data class MirrorPageUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val userInfo: UserInfoModel?=null,
    val isSign: Boolean =false

    )
