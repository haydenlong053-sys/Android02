package cn.wildfirechat.uistate

data class MessagePageUiState(
    val showBottomUI: Boolean=false,
    val isAllSelect: Boolean =false,
    val isAllRead: Boolean=true,
    val enableDelete: Boolean=false
)
data class MessageBottomClickState(
    val clickAllSelect: Boolean=false,
    val clickAllRead: Boolean=false,
    val clickDelete: Boolean=false
)
