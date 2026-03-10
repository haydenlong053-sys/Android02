package com.cboda.baselibrary.bean

//data class DanmakuItem(
//    val text: String,
//    val avatarUrl: String? = null,      // 头像 URL
//    val iconResId: Int? = null,         // 本地图标（如礼物）
////    val textColor: Int = android.graphics.Color.WHITE,
////    val showAvatar: Boolean = false,    // 是否显示头像
//    val isMeSend: Boolean = false,//是否自己发送的
//    val onClick: (() -> Unit)? = null   // 点击回调
//
//)
class DanmakuItem {
    var content: String = ""
    var avatarUrl: String? = null      // 头像 URL
    var iconResId: Int? = null         // 本地图标（如礼物）

    //    val textColor: Int = android.graphics.Color.WHITE,
//    val showAvatar: Boolean = false,    // 是否显示头像
    var isMeSend: Boolean = false//是否自己发送的
//    val onClick: (() -> Unit)? = null   // 点击回调
}

class BarrageBean {
    var sender = ""
    var conversationType = -1
    var conversationId = ""
    var giftType = 0
}