package com.cboda.baselibrary.bean

import android.graphics.Bitmap
import android.graphics.Color

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
    //    var id = ""
    var width = 0f
    var height = 0f
    var y = 0f
    var targetY = 0f

    // 记录起始 Y，用于按时间统一计算位置，避免逐帧累积误差导致抖动
    var startY = 0f
    var alpha = 0.8f
    var isVisible = true
    var textColor = Color.argb(1, 255, 217, 87)
    var backgroundColor = Color.argb(0.85f, 0f, 0f, 0f)
    var avatarBitmap: Bitmap? = null  // 预加载的头像
    var textSize: Float = 14f

    // 基础速度调低一点，避免单帧位移过大带来的抖动感
    var speed: Float = 600f  // 像素/秒
    // priority 已不再使用，统一改用 isMeSend + 队列规则控制顺序
    var createdAt = 0L


    var content: String = ""
    var avatarUrl: String? = null      // 头像 URL
    var iconResId: Int? = null         // 本地图标（如礼物）

    /** 弹幕文字后面的图片：Bitmap（远程或已解码的本地图），由调用方加载后传入 */
    var trailingImageBitmap: Bitmap? = null

    /** 弹幕文字后面的图片：本地 drawable 资源 id，View 内会解码为 Bitmap */
//    var trailingImageResId: Int? = null

    /** 尾图宽度（dp），0 表示使用默认（约 24dp） */
    var trailingImageWidthDp: Float = 24f

    /** 尾图高度（dp），0 表示使用默认（约 24dp） */
    var trailingImageHeightDp: Float = 24f

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