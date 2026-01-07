package com.cboda.intelligent.model

data class SocialBroadCircle(
    val id: Int,
    val characterTags: String,
    val characterExplanation: String,
    val createTime: String,
    val updateTime: String
)

data class SocialContact(
    val id: String,
    val userId: String,
    val nickName: String? = "",
    val remarkName: String?,
    val icon: String?,
    val city: String?,
    val profession: String?,
    val sex: String?,
    val grade: Int?,
    val days: Int?,
    val characterTags: String? = "",
    val characterTagsId: Int?,
    val registerDay: Int?,
    val first: String? // 服务器返回的首字母备用
)

data class SocialUiState(
    val isLoading: Boolean = false,
    val error: String? = null,
    val circles: List<SocialBroadCircle> = emptyList(),
    val contactsAll: List<SocialContact> = emptyList(),
    val selectedCircleTag: String? = null,
    val filteredContacts: List<SocialContact> = emptyList(),
    val indexLetters: List<Char> = emptyList(),
    val groupedByLetter: Map<Char, List<SocialContact>> = emptyMap(),
    val overlayLetter: Char? = null,
    val showOverlay: Boolean = false,
    // 角标数量：新增好友 & 新群邀请
    val newFriendCount: Int = 0,
    val newGroupInviteCount: Int = 0
)

data class SocialFriendRequest(
    val count: Int? = 0
)